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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockftpserver.core.MockFtpServerException;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.socket.StubServerSocket;
import org.mockftpserver.core.socket.StubServerSocketFactory;
import org.mockftpserver.core.socket.StubSocket;
import org.mockftpserver.core.socket.StubSocketFactory;
import org.mockftpserver.core.util.AssertFailedException;
import org.mockftpserver.test.AbstractTestCase;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the DefaultSession class
 * 
 * @author Chris Mair
 */
class DefaultSessionTest extends AbstractTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSessionTest.class);
    private static final String DATA = "sample data 123";
    private static final int PORT = 197;
    private static final String NAME1 = "name1";
    private static final String NAME2 = "name2";
    private static final Object VALUE = "value";
    private static final String COMMAND = "LIST";

    private DefaultSession session;
    private ByteArrayOutputStream outputStream;
    private Map commandHandlerMap;
    private StubSocket stubSocket;
    private InetAddress clientHost;

    @Test
    void testConstructor_NullControlSocket() {
        assertThrows(AssertFailedException.class, () -> new DefaultSession(null, commandHandlerMap));
    }

    @Test
    void testConstructor_NullCommandHandlerMap() {
        assertThrows(AssertFailedException.class, () -> new DefaultSession(stubSocket, null));
    }

    @Test
    void testSetClientDataPort() {
        StubSocket stubSocket = createTestSocket("");
        StubSocketFactory stubSocketFactory = new StubSocketFactory(stubSocket);
        session.socketFactory = stubSocketFactory;
        session.setClientDataPort(PORT);
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        assertEquals("data port", PORT, stubSocketFactory.requestedDataPort);
    }

    @Test
    void testSetClientDataPort_AfterPassiveConnectionMode() throws IOException {
        StubServerSocket stubServerSocket = new StubServerSocket(PORT);
        StubServerSocketFactory stubServerSocketFactory = new StubServerSocketFactory(stubServerSocket);
        session.serverSocketFactory = stubServerSocketFactory;

        session.switchToPassiveMode();
        assertFalse("server socket closed", stubServerSocket.isClosed());
        assertNotNull("passiveModeDataSocket", session.passiveModeDataSocket);
        session.setClientDataPort(PORT);

        // Make sure that any passive mode connection info is cleared out
        assertTrue("server socket closed", stubServerSocket.isClosed());
        assertNull("passiveModeDataSocket should be null", session.passiveModeDataSocket);
    }

    @Test
    void testSetClientHost() throws Exception {
        StubSocket stubSocket = createTestSocket("");
        StubSocketFactory stubSocketFactory = new StubSocketFactory(stubSocket);
        session.socketFactory = stubSocketFactory;
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        assertEquals("client host", clientHost, stubSocketFactory.requestedHost);
    }

    @Test
    void testOpenDataConnection() {
        StubSocket stubSocket = createTestSocket("");
        StubSocketFactory stubSocketFactory = new StubSocketFactory(stubSocket);
        session.socketFactory = stubSocketFactory;

        // Use default client data port
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        assertEquals("data port", DefaultSession.DEFAULT_CLIENT_DATA_PORT, stubSocketFactory.requestedDataPort);
        assertEquals("client host", clientHost, stubSocketFactory.requestedHost);

        // Set client data port explicitly
        session.setClientDataPort(PORT);
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        assertEquals("data port", PORT, stubSocketFactory.requestedDataPort);
        assertEquals("client host", clientHost, stubSocketFactory.requestedHost);
    }

    @Test
    void testOpenDataConnection_PassiveMode_NoConnection() throws IOException {
        StubServerSocket stubServerSocket = new StubServerSocket(PORT);
        StubServerSocketFactory stubServerSocketFactory = new StubServerSocketFactory(stubServerSocket);
        session.serverSocketFactory = stubServerSocketFactory;

        session.switchToPassiveMode();

        assertThrows(MockFtpServerException.class, () -> session.openDataConnection());
    }

    @Test
    void testOpenDataConnection_NullClientHost() {
        assertThrows(AssertFailedException.class, () -> session.openDataConnection());
    }

    @Test
    void testReadData() {
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);
        session.setClientDataHost(clientHost);

        session.openDataConnection();
        byte[] data = session.readData();
        LOG.info("data=[" + new String(data) + "]");
        assertEquals("data", DATA.getBytes(), data);
    }

    @Test
    void testReadData_PassiveMode() throws IOException {
        StubSocket stubSocket = createTestSocket(DATA);
        StubServerSocket stubServerSocket = new StubServerSocket(PORT, stubSocket);
        StubServerSocketFactory stubServerSocketFactory = new StubServerSocketFactory(stubServerSocket);
        session.serverSocketFactory = stubServerSocketFactory;

        session.switchToPassiveMode();
        session.openDataConnection();
        byte[] data = session.readData();
        LOG.info("data=[" + new String(data) + "]");
        assertEquals("data", DATA.getBytes(), data);
    }

    @Test
    void testReadData_NumBytes() {
        final int NUM_BYTES = 5;
        final String EXPECTED_DATA = DATA.substring(0, NUM_BYTES);
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);
        session.setClientDataHost(clientHost);

        session.openDataConnection();
        byte[] data = session.readData(NUM_BYTES);
        LOG.info("data=[" + new String(data) + "]");
        assertEquals("data", EXPECTED_DATA.getBytes(), data);
    }

    @Test
    void testReadData_NumBytes_AskForMoreBytesThanThereAre() {
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);
        session.setClientDataHost(clientHost);

        session.openDataConnection();
        byte[] data = session.readData(10000);
        LOG.info("data=[" + new String(data) + "]");
        assertEquals("data", DATA.getBytes(), data);
    }

    @Test
    void testCloseDataConnection() {
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);

        session.setClientDataHost(clientHost);
        session.openDataConnection();
        session.closeDataConnection();
        assertTrue("client data socket should be closed", stubSocket.isClosed());
    }

    @Test
    void testCloseDataConnection_PassiveMode() throws IOException {
        StubSocket stubSocket = createTestSocket(DATA);
        StubServerSocket stubServerSocket = new StubServerSocket(1, stubSocket);
        session.serverSocketFactory = new StubServerSocketFactory(stubServerSocket);

        session.switchToPassiveMode();
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        session.closeDataConnection();
        assertTrue("client data socket should be closed", stubSocket.isClosed());
        assertTrue("passive mode data socket should be closed", stubServerSocket.isClosed());
    }

    @Test
    void testSwitchToPassiveMode() throws IOException {
        StubServerSocket stubServerSocket = new StubServerSocket(PORT);
        StubServerSocketFactory stubServerSocketFactory = new StubServerSocketFactory(stubServerSocket);
        session.serverSocketFactory = stubServerSocketFactory;

        assertNull("passiveModeDataSocket starts out null", session.passiveModeDataSocket);
        int port = session.switchToPassiveMode();
        assertSame("passiveModeDataSocket", stubServerSocket, session.passiveModeDataSocket);
        assertEquals("port", PORT, port);
    }

    @Test
    void testGetServerHost() {
        assertEquals("host", DEFAULT_HOST, session.getServerHost());
    }

    @Test
    void testGetClientHost_NotRunning() {
        assertNull("null", session.getClientHost());
    }

    @Test
    void testReadCommand() {
        StringReader stringReader = new StringReader(COMMAND);
        session.controlConnectionReader = new BufferedReader(stringReader);
        assertEquals(new Command(COMMAND, EMPTY), session.readCommand());
    }

    @Test
    void testReadCommand_ReadLineReturnsNull_ReturnsNull() {
        session.controlConnectionReader = new BufferedReader(new StringReader(COMMAND)) {
            public boolean ready() { return true; }
            public String readLine() { return null; }
        };
        assertNull(session.readCommand());
    }

    @Test
    void testReadCommand_Closed_ReturnsNull() {
        session.close();
        assertNull(session.readCommand());
    }

    @Test
    void testParseCommand() {
        Command command = session.parseCommand("LIST");
        assertEquals("command name", "LIST", command.getName());
        assertEquals("command parameters", EMPTY, command.getParameters());

        command = session.parseCommand("USER user123");
        assertEquals("command name", "USER", command.getName());
        assertEquals("command parameters", array("user123"), command.getParameters());

        command = session.parseCommand("PORT 127,0,0,1,17,37");
        assertEquals("command name", "PORT", command.getName());
        assertEquals("command parameters", new String[] { "127", "0", "0", "1", "17", "37" }, command
                .getParameters());
    }

    @Test
    void testParseCommand_EmptyCommandString() {
        assertThrows(AssertFailedException.class, () -> session.parseCommand(""));
    }

    @Test
    void testSendData() {
        StubSocket stubSocket = createTestSocket("1234567890 abcdef");
        session.socketFactory = new StubSocketFactory(stubSocket);

        session.setClientDataHost(clientHost);
        session.openDataConnection();
        session.sendData(DATA.getBytes(), DATA.length());
        LOG.info("output=[" + outputStream.toString() + "]");
        assertEquals("output", DATA, outputStream.toString());
    }

    @Test
    void testSendData_Null() {
        assertThrows(AssertFailedException.class, () -> session.sendData(null, 1));
    }

    @Test
    void testSendReply_InvalidReplyCode() {
        assertThrows(AssertFailedException.class, () -> session.sendReply(-66, "text"));
    }

    @Test
    void testGetAndSetAttribute() {
        assertNull("name does not exist yet", session.getAttribute(NAME1));
        session.setAttribute(NAME1, VALUE);
        session.setAttribute(NAME2, null);
        assertEquals("NAME1", VALUE, session.getAttribute(NAME1));
        assertNull("NAME2", session.getAttribute(NAME2));
        assertNull("no such name", session.getAttribute("noSuchName"));
    }

    @Test
    void testGetAttribute_Null() {
        assertThrows(AssertFailedException.class, () -> session.getAttribute(null));
    }

    @Test
    void testSetAttribute_NullName() {
        assertThrows(AssertFailedException.class, () -> session.setAttribute(null, VALUE));
    }

    @Test
    void testRemoveAttribute() {
        session.removeAttribute("noSuchName");      // do nothing
        session.setAttribute(NAME1, VALUE);
        session.removeAttribute(NAME1);
        assertNull("NAME1", session.getAttribute(NAME1));
    }

    @Test
    void testRemoveAttribute_Null() {
        assertThrows(AssertFailedException.class, () -> session.removeAttribute(null));
    }

    @Test
    void testGetAttributeNames() {
        assertEquals("No names yet", Collections.EMPTY_SET, session.getAttributeNames());
        session.setAttribute(NAME1, VALUE);
        assertEquals("1", Collections.singleton(NAME1), session.getAttributeNames());
        session.setAttribute(NAME2, VALUE);
        assertEquals("2", set(NAME1, NAME2), session.getAttributeNames());
    }
    
    // -------------------------------------------------------------------------
    // Setup and Internal Helper Methods
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() throws Exception {
        commandHandlerMap = new HashMap();
        outputStream = new ByteArrayOutputStream();
        session = createDefaultSession("");
        clientHost = InetAddress.getLocalHost();
    }

    /**
     * Create and return a DefaultSession object that reads from an InputStream with the specified
     * contents and writes to the predefined outputStrean ByteArrayOutputStream. Also, save the
     * StubSocket being used in the stubSocket attribute.
     * 
     * @param inputStreamContents - the contents of the input stream
     * @return the DefaultSession
     */
    private DefaultSession createDefaultSession(String inputStreamContents) {
        stubSocket = createTestSocket(inputStreamContents);
        return new DefaultSession(stubSocket, commandHandlerMap);
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
        StubSocket stubSocket = new StubSocket(inputStream, outputStream);
        stubSocket._setLocalAddress(DEFAULT_HOST);
        return stubSocket;
    }

}
