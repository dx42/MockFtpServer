/*
 * Copyright 2021 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mockftpserver.core.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.core.util.AssertFailedException;
import org.mockftpserver.test.AbstractTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

/**
 * Tests for SimpleCompositeCommandHandler
 * 
 * @author Chris Mair
 */
class SimpleCompositeCommandHandlerTest extends AbstractTestCase {

    private SimpleCompositeCommandHandler simpleCompositeCommandHandler;
    private Session session;
    private Command command;
    private CommandHandler commandHandler1;
    private CommandHandler commandHandler2;
    private CommandHandler commandHandler3;
    
    @Test
    void testHandleCommand_OneHandler_OneInvocation() throws Exception {
        simpleCompositeCommandHandler.addCommandHandler(commandHandler1);
        
        simpleCompositeCommandHandler.handleCommand(command, session);

        verify(commandHandler1).handleCommand(command, session);
    }
    
    @Test
    void testHandleCommand_TwoHandlers() throws Exception {
        simpleCompositeCommandHandler.addCommandHandler(commandHandler1);
        simpleCompositeCommandHandler.addCommandHandler(commandHandler2);

        simpleCompositeCommandHandler.handleCommand(command, session);
        simpleCompositeCommandHandler.handleCommand(command, session);

        verify(commandHandler1).handleCommand(command, session);
        verify(commandHandler2).handleCommand(command, session);
    }
    
    @Test
    void testHandleCommand_ThreeHandlers() throws Exception {
        List list = new ArrayList();
        list.add(commandHandler1);
        list.add(commandHandler2);
        list.add(commandHandler3);
        simpleCompositeCommandHandler.setCommandHandlers(list);

        simpleCompositeCommandHandler.handleCommand(command, session);
        simpleCompositeCommandHandler.handleCommand(command, session);
        simpleCompositeCommandHandler.handleCommand(command, session);

        verify(commandHandler1).handleCommand(command, session);
        verify(commandHandler2).handleCommand(command, session);
        verify(commandHandler3).handleCommand(command, session);
    }
    
    @Test
    void testHandleCommand_OneHandler_TooManyInvocations() throws Exception {
        simpleCompositeCommandHandler.addCommandHandler(commandHandler1);

        simpleCompositeCommandHandler.handleCommand(command, session);

        verify(commandHandler1).handleCommand(command, session);

        // Second invocation throws an exception
        assertThrows(AssertFailedException.class, () -> simpleCompositeCommandHandler.handleCommand(command, session));
    }
    
    @Test
    void testHandleCommand_NoHandlersDefined() {
        assertThrows(AssertFailedException.class, () -> simpleCompositeCommandHandler.handleCommand(command, session));
    }
    
    @Test
    void testHandleCommand_NullCommand() {
        assertThrows(AssertFailedException.class, () -> simpleCompositeCommandHandler.handleCommand(null, session));
    }
    
    @Test
    void testHandleCommand_NullSession() {
        assertThrows(AssertFailedException.class, () -> simpleCompositeCommandHandler.handleCommand(command, null));
    }

    @Test
    void testAddCommandHandler_NullCommandHandler() {
        assertThrows(AssertFailedException.class, () -> simpleCompositeCommandHandler.addCommandHandler(null));
    }
    
    @Test
    void testSetCommandHandlers_Null() {
        assertThrows(AssertFailedException.class, () -> simpleCompositeCommandHandler.setCommandHandlers(null));
    }
    
    @Test
    void testGetCommandHandler_UndefinedIndex() {
        simpleCompositeCommandHandler.addCommandHandler(commandHandler1);
        assertThrows(AssertFailedException.class, () -> simpleCompositeCommandHandler.getCommandHandler(1));
    }

    @Test
    void testGetCommandHandler() {
        simpleCompositeCommandHandler.addCommandHandler(commandHandler1);
        simpleCompositeCommandHandler.addCommandHandler(commandHandler2);
        assertSame(commandHandler1, simpleCompositeCommandHandler.getCommandHandler(0));
        assertSame(commandHandler2, simpleCompositeCommandHandler.getCommandHandler(1));
    }
    
    @Test
    void testGetCommandHandler_NegativeIndex() {
        simpleCompositeCommandHandler.addCommandHandler(commandHandler1);
        assertThrows(AssertFailedException.class, () -> simpleCompositeCommandHandler.getCommandHandler(-1));
    }
    
    @Test
    void testGetReplyTextBundle() {
        assertNull(simpleCompositeCommandHandler.getReplyTextBundle());
    }
    
    @Test
    void testSetReplyTextBundle() {
        AbstractTrackingCommandHandler replyTextBundleAwareCommandHandler1 = new StaticReplyCommandHandler();
        AbstractTrackingCommandHandler replyTextBundleAwareCommandHandler2 = new StaticReplyCommandHandler();
        simpleCompositeCommandHandler.addCommandHandler(replyTextBundleAwareCommandHandler1);
        simpleCompositeCommandHandler.addCommandHandler(commandHandler1);
        simpleCompositeCommandHandler.addCommandHandler(replyTextBundleAwareCommandHandler2);
        
        ResourceBundle resourceBundle = new ListResourceBundle() {
            protected Object[][] getContents() {
                return null;
            }
        };
        
        simpleCompositeCommandHandler.setReplyTextBundle(resourceBundle);
        assertSame(resourceBundle, replyTextBundleAwareCommandHandler1.getReplyTextBundle());
        assertSame(resourceBundle, replyTextBundleAwareCommandHandler1.getReplyTextBundle());
    }
    
    //-------------------------------------------------------------------------
    // Test setup
    //-------------------------------------------------------------------------
    
    @BeforeEach
    void setUp() {
        simpleCompositeCommandHandler = new SimpleCompositeCommandHandler();
        session = mock(Session.class);
        command = new Command("cmd", EMPTY);
        commandHandler1 = mock(CommandHandler.class);
        commandHandler2 = mock(CommandHandler.class);
        commandHandler3 = mock(CommandHandler.class);
    }
    
}
