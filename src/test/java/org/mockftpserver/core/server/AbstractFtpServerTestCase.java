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

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    protected Logger LOG = LoggerFactory.getLogger(getClass());

    protected AbstractFtpServer ftpServer;
    private CommandHandler commandHandler;
    private CommandHandler commandHandler2;

    @Test
    void testSetCommandHandlers() {
        Map mapping = new HashMap();
        mapping.put("AAA", commandHandler);
        mapping.put("BBB", commandHandler2);

        ftpServer.setCommandHandlers(mapping);
        assertSame("commandHandler1", commandHandler, ftpServer.getCommandHandler("AAA"));
        assertSame("commandHandler2", commandHandler2, ftpServer.getCommandHandler("BBB"));

        verifyCommandHandlerInitialized(commandHandler);
        verifyCommandHandlerInitialized(commandHandler2);

        // Make sure default CommandHandlers are still set
        assertTrue("ConnectCommandHandler", ftpServer.getCommandHandler(CommandNames.CONNECT) != null);
    }

    @Test
    void testSetCommandHandlers_Null() {
        try {
            ftpServer.setCommandHandlers(null);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @Test
    void testSetCommandHandler() {
        ftpServer.setCommandHandler("ZZZ", commandHandler2);
        assertSame("commandHandler", commandHandler2, ftpServer.getCommandHandler("ZZZ"));
        verifyCommandHandlerInitialized(commandHandler2);
    }

    @Test
    void testSetCommandHandler_NullCommandName() {
        CommandHandler commandHandler = mock(CommandHandler.class);
        try {
            ftpServer.setCommandHandler(null, commandHandler);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @Test
    void testSetCommandHandler_NullCommandHandler() {
        try {
            ftpServer.setCommandHandler("ZZZ", null);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @Test
    void testSetServerControlPort() {
        assertEquals("default", 21, ftpServer.getServerControlPort());
        ftpServer.setServerControlPort(99);
        assertEquals("99", 99, ftpServer.getServerControlPort());
    }

    @Test
    void testLowerCaseOrMixedCaseCommandNames() {
        ftpServer.setCommandHandler("XXX", commandHandler);
        assertSame("ZZZ", commandHandler, ftpServer.getCommandHandler("XXX"));
        assertSame("Zzz", commandHandler, ftpServer.getCommandHandler("Xxx"));
        assertSame("zzz", commandHandler, ftpServer.getCommandHandler("xxx"));

        ftpServer.setCommandHandler("YyY", commandHandler);
        assertSame("ZZZ", commandHandler, ftpServer.getCommandHandler("YYY"));
        assertSame("Zzz", commandHandler, ftpServer.getCommandHandler("Yyy"));
        assertSame("zzz", commandHandler, ftpServer.getCommandHandler("yyy"));

        ftpServer.setCommandHandler("zzz", commandHandler);
        assertSame("ZZZ", commandHandler, ftpServer.getCommandHandler("ZZZ"));
        assertSame("Zzz", commandHandler, ftpServer.getCommandHandler("zzZ"));
        assertSame("zzz", commandHandler, ftpServer.getCommandHandler("zzz"));
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
    void setUp_AbstractFtpServerTestCase() throws Exception {
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
