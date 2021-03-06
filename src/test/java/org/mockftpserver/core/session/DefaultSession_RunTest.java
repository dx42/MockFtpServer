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
package org.mockftpserver.core.session;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.command.*;
import org.mockftpserver.core.socket.StubSocket;
import org.mockftpserver.stub.command.AbstractStubCommandHandler;
import org.mockftpserver.test.AbstractTestCase;

import java.io.*;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Tests for the DefaultSession class that require the session (thread) to be running/active.
 *
 * @author Chris Mair
 */
class DefaultSession_RunTest extends AbstractTestCase {

    private static final Command COMMAND = new Command("USER", EMPTY);
    private static final int REPLY_CODE = 100;
    private static final String REPLY_TEXT = "sample text description";

    private DefaultSession session;
    private ByteArrayOutputStream outputStream;
    private Map commandHandlerMap;
    private StubSocket stubSocket;
    private boolean commandHandled = false;
    private String commandToRegister = COMMAND.getName();

    @BeforeEach
    void setUp() throws Exception {
        commandHandlerMap = new HashMap();
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    void testInvocationOfCommandHandler() throws Exception {
        AbstractStubCommandHandler commandHandler = new AbstractStubCommandHandler() {
            public void handleCommand(Command command, Session cmdSession, InvocationRecord invocationRecord) {
                assertEquals(COMMAND, command);
                assertSame(session, cmdSession);
                assertEquals(COMMAND, invocationRecord.getCommand());
                assertEquals(DEFAULT_HOST, invocationRecord.getClientHost());
                commandHandled = true;
            }
        };
        runCommandAndVerifyOutput(commandHandler, "");
    }

    @Test
    void testClose() throws Exception {
        CommandHandler commandHandler = new AbstractStubCommandHandler() {
            public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
                session.close();
                commandHandled = true;
            }
        };
        runCommandAndVerifyOutput(commandHandler, "");
        assertFalse(stubSocket.isClosed());
        assertTrue(session.isClosed());
    }

    @Test
    void testClose_WithoutCommand() throws Exception {
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(pipedOutputStream);
        stubSocket = new StubSocket(DEFAULT_HOST, inputStream, outputStream);
        session = new DefaultSession(stubSocket, commandHandlerMap);

        initializeConnectCommandHandler();

        Thread thread = new Thread(session);
        thread.start();
        Thread.sleep(1000L);

        session.close();
        thread.join();
        assertTrue(session.isClosed());
    }

    @Test
    void testGetClientHost() throws Exception {
        CommandHandler commandHandler = new AbstractStubCommandHandler() {
            public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
                commandHandled = true;
            }
        };
        runCommandAndVerifyOutput(commandHandler, "");
        log("clientHost=" + session.getClientHost());
        assertEquals(DEFAULT_HOST, session.getClientHost());
    }

    @Test
    void testSendReply_NullReplyText() throws Exception {
        CommandHandler commandHandler = new AbstractStubCommandHandler() {
            public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
                session.sendReply(REPLY_CODE, null);
                commandHandled = true;
            }
        };
        runCommandAndVerifyOutput(commandHandler, Integer.toString(REPLY_CODE));
    }

    @Test
    void testSendReply_TrimReplyText() throws Exception {
        CommandHandler commandHandler = new AbstractStubCommandHandler() {
            public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
                session.sendReply(REPLY_CODE, " " + REPLY_TEXT + " ");
                commandHandled = true;
            }
        };
        runCommandAndVerifyOutput(commandHandler, REPLY_CODE + " " + REPLY_TEXT);
    }

    @Test
    void testSendReply_MultiLineText() throws Exception {
        final String MULTILINE_REPLY_TEXT = "abc\ndef\nghi\njkl";
        final String FORMATTED_MULTILINE_REPLY_TEXT = "123-abc\ndef\nghi\n123 jkl";

        CommandHandler commandHandler = new AbstractStubCommandHandler() {
            public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
                session.sendReply(123, MULTILINE_REPLY_TEXT);
                commandHandled = true;
            }
        };
        runCommandAndVerifyOutput(commandHandler, FORMATTED_MULTILINE_REPLY_TEXT);
    }

    @Test
    void testSendReply_ReplyText() throws Exception {
        CommandHandler commandHandler = new AbstractStubCommandHandler() {
            public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
                session.sendReply(REPLY_CODE, REPLY_TEXT);
                commandHandled = true;
            }
        };
        runCommandAndVerifyOutput(commandHandler, REPLY_CODE + " " + REPLY_TEXT);
    }

    @Test
    void testUnrecognizedCommand() throws Exception {
        // Register a handler for unsupported commands
        CommandHandler commandHandler = new AbstractStubCommandHandler() {
            public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
                session.sendReply(502, "Unsupported");
                commandHandled = true;
            }
        };
        // Register the UNSUPPORTED command handler instead of the command that will be sent. So when we
        // send the regular command, it will trigger the handling for unsupported/unrecognized commands.
        commandToRegister = CommandNames.UNSUPPORTED;
        runCommandAndVerifyOutput(commandHandler, "502 Unsupported");
    }

    // -------------------------------------------------------------------------
    // Internal Helper Methods
    // -------------------------------------------------------------------------

    /**
     * Create and return a DefaultSession and define the specified CommandHandler. Also, save the
     * StubSocket being used in the stubSocket attribute.
     *
     * @param commandHandler - define this CommandHandler within the commandHandlerMap
     * @return the DefaultSession
     */
    private DefaultSession createDefaultSession(CommandHandler commandHandler) {
        stubSocket = createTestSocket(COMMAND.getName());
        commandHandlerMap.put(commandToRegister, commandHandler);
        initializeConnectCommandHandler();
        return new DefaultSession(stubSocket, commandHandlerMap);
    }

    private void initializeConnectCommandHandler() {
        ConnectCommandHandler connectCommandHandler = new ConnectCommandHandler();

        ResourceBundle replyTextBundle = new ListResourceBundle() {
            protected Object[][] getContents() {
                return new Object[][]{
                        {"220", "Reply for 220"},
                };
            }
        };
        connectCommandHandler.setReplyTextBundle(replyTextBundle);
        commandHandlerMap.put(CommandNames.CONNECT, connectCommandHandler);
    }

    /**
     * Create and return a StubSocket that reads from an InputStream with the specified contents and
     * writes to the predefined outputStrean ByteArrayOutputStream.
     *
     * @param inputStreamContents - the contents of the input stream
     * @return the StubSocket
     */
    private StubSocket createTestSocket(String inputStreamContents) {
        InputStream inputStream = new ByteArrayInputStream(inputStreamContents.getBytes());
        return new StubSocket(DEFAULT_HOST, inputStream, outputStream);
    }

    /**
     * Run the command represented by the CommandHandler and verify that the session output from the
     * control socket contains the expected output text.
     *
     * @param commandHandler - the CommandHandler to invoke
     * @param expectedOutput - the text expected within the session output
     * @throws InterruptedException - if the thread sleep is interrupted
     */
    private void runCommandAndVerifyOutput(CommandHandler commandHandler, String expectedOutput)
            throws InterruptedException {
        session = createDefaultSession(commandHandler);

        Thread thread = new Thread(session);
        thread.start();

        for (int i = 0; !commandHandled && i < 10; i++) {
            Thread.sleep(50L);
        }

        session.close();
        thread.join();

        assertEquals(true, commandHandled);

        String output = outputStream.toString();
        log("output=[" + output.trim() + "]");
        assertTrue(output.charAt(output.length() - 2) == '\r' && output.charAt(output.length() - 1) == '\n');
        assertTrue(output.indexOf(expectedOutput) != -1);
    }

}
