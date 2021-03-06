/*
 * Copyright 2007 the original author or authors.
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
package org.mockftpserver.core.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.command.CommandHandler;
import org.mockftpserver.core.command.CommandNames;
import org.mockftpserver.core.session.DefaultSession;
import org.mockftpserver.core.util.AssertFailedException;
import org.mockftpserver.test.AbstractTestCase;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract superclass for tests of AbstractFtpServer subclasses.
 *
 * @author Chris Mair
 */
public abstract class AbstractFtpServerTestCase extends AbstractTestCase {

    protected AbstractFtpServer ftpServer;
    private CommandHandler commandHandler;
    private CommandHandler commandHandler2;

    @Test
    void testSetCommandHandlers() {
        Map mapping = new HashMap();
        mapping.put("AAA", commandHandler);
        mapping.put("BBB", commandHandler2);

        ftpServer.setCommandHandlers(mapping);
        assertSame(commandHandler, ftpServer.getCommandHandler("AAA"));
        assertSame(commandHandler2, ftpServer.getCommandHandler("BBB"));

        verifyCommandHandlerInitialized(commandHandler);
        verifyCommandHandlerInitialized(commandHandler2);

        // Make sure default CommandHandlers are still set
        assertTrue(ftpServer.getCommandHandler(CommandNames.CONNECT) != null);
    }

    @Test
    void testSetCommandHandlers_Null() {
        assertThrows(AssertFailedException.class, () -> ftpServer.setCommandHandlers(null));
    }

    @Test
    void testSetCommandHandler() {
        ftpServer.setCommandHandler("ZZZ", commandHandler2);
        assertSame(commandHandler2, ftpServer.getCommandHandler("ZZZ"));
        verifyCommandHandlerInitialized(commandHandler2);
    }

    @Test
    void testSetCommandHandler_NullCommandName() {
        CommandHandler commandHandler = mock(CommandHandler.class);
        assertThrows(AssertFailedException.class, () -> ftpServer.setCommandHandler(null, commandHandler));
    }

    @Test
    void testSetCommandHandler_NullCommandHandler() {
        assertThrows(AssertFailedException.class, () -> ftpServer.setCommandHandler("ZZZ", null));
    }

    @Test
    void testSetServerControlPort() {
        assertEquals(21, ftpServer.getServerControlPort());
        ftpServer.setServerControlPort(99);
        assertEquals(99, ftpServer.getServerControlPort());
    }

    @Test
    void testLowerCaseOrMixedCaseCommandNames() {
        ftpServer.setCommandHandler("XXX", commandHandler);
        assertSame(commandHandler, ftpServer.getCommandHandler("XXX"));
        assertSame(commandHandler, ftpServer.getCommandHandler("Xxx"));
        assertSame(commandHandler, ftpServer.getCommandHandler("xxx"));

        ftpServer.setCommandHandler("YyY", commandHandler);
        assertSame(commandHandler, ftpServer.getCommandHandler("YYY"));
        assertSame(commandHandler, ftpServer.getCommandHandler("Yyy"));
        assertSame(commandHandler, ftpServer.getCommandHandler("yyy"));

        ftpServer.setCommandHandler("zzz", commandHandler);
        assertSame(commandHandler, ftpServer.getCommandHandler("ZZZ"));
        assertSame(commandHandler, ftpServer.getCommandHandler("zzZ"));
        assertSame(commandHandler, ftpServer.getCommandHandler("zzz"));
    }

    @Test
    void testStopWithoutStart() {
        ftpServer.stop();
    }

    @Test
    void testCreateSession() {
        assertEquals(ftpServer.createSession(new Socket()).getClass(), DefaultSession.class);
    }

    //-------------------------------------------------------------------------
    // Test setup
    //-------------------------------------------------------------------------

    @BeforeEach
    void setUp_AbstractFtpServerTestCase() {
        ftpServer = createFtpServer();
        commandHandler = createCommandHandler();
        commandHandler2 = createCommandHandler();
    }

    //-------------------------------------------------------------------------
    // Abstract method declarations
    //-------------------------------------------------------------------------

    protected abstract AbstractFtpServer createFtpServer();

    protected abstract CommandHandler createCommandHandler();

    protected abstract void verifyCommandHandlerInitialized(CommandHandler commandHandler);

}
