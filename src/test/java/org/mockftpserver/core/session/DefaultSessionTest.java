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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the DefaultSession class
 * 
 * @author Chris Mair
 */
class DefaultSessionTest extends AbstractTestCase {

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
        assertEquals(PORT, stubSocketFactory.requestedDataPort);
    }

    @Test
    void testSetClientDataPort_AfterPassiveConnectionMode() throws IOException {
        StubServerSocket stubServerSocket = new StubServerSocket(PORT);
        StubServerSocketFactory stubServerSocketFactory = new StubServerSocketFactory(stubServerSocket);
        session.serverSocketFactory = stubServerSocketFactory;

        session.switchToPassiveMode();
        assertFalse(stubServerSocket.isClosed());
        assertNotNull(session.passiveModeDataSocket);
        session.setClientDataPort(PORT);

        // Make sure that any passive mode connection info is cleared out
        assertTrue(stubServerSocket.isClosed());
        assertNull(session.passiveModeDataSocket);
    }

    @Test
    void testSetClientHost() throws Exception {
        StubSocket stubSocket = createTestSocket("");
        StubSocketFactory stubSocketFactory = new StubSocketFactory(stubSocket);
        session.socketFactory = stubSocketFactory;
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        assertEquals(clientHost, stubSocketFactory.requestedHost);
    }

    @Test
    void testOpenDataConnection() {
        StubSocket stubSocket = createTestSocket("");
        StubSocketFactory stubSocketFactory = new StubSocketFactory(stubSocket);
        session.socketFactory = stubSocketFactory;

        // Use default client data port
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        assertEquals(DefaultSession.DEFAULT_CLIENT_DATA_PORT, stubSocketFactory.requestedDataPort);
        assertEquals(clientHost, stubSocketFactory.requestedHost);

        // Set client data port explicitly
        session.setClientDataPort(PORT);
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        assertEquals(PORT, stubSocketFactory.requestedDataPort);
        assertEquals(clientHost, stubSocketFactory.requestedHost);
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
        log("data=[" + new String(data) + "]");
        assertArrayEquals(DATA.getBytes(), data);
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
        log("data=[" + new String(data) + "]");
        assertArrayEquals(DATA.getBytes(), data);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 15})
    void testReadData_NumBytes(int numBytes) {
        final String EXPECTED_DATA = DATA.substring(0, numBytes);
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);
        session.setClientDataHost(clientHost);

        session.openDataConnection();
        byte[] data = session.readData(numBytes);
        log("data=[" + new String(data) + "]");
        assertArrayEquals(EXPECTED_DATA.getBytes(), data);
    }

    @Test
    void testReadData_LargeData() {
        final int NUM_BYTES = 50_000_000;
        byte[] bytes = new byte[NUM_BYTES + 3];

        StubSocket stubSocket = createTestSocket(bytes);
        session.socketFactory = new StubSocketFactory(stubSocket);
        session.setClientDataHost(clientHost);
        session.openDataConnection();

        Instant startTime = Instant.now();
        byte[] data = session.readData(NUM_BYTES);
        Instant endTime = Instant.now();

        Duration duration = Duration.between(startTime, endTime);
        log("Read " + data.length + " bytes in " + duration.toMillis() + " milliseconds");
        assertEquals(NUM_BYTES, data.length);
    }

    @Test
    void testReadData_NumBytes_AskForMoreBytesThanThereAre() {
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);
        session.setClientDataHost(clientHost);

        session.openDataConnection();
        byte[] data = session.readData(10000);
        log("data=[" + new String(data) + "]");
        assertArrayEquals(DATA.getBytes(), data);
    }

    @Test
    void testCloseDataConnection() {
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);

        session.setClientDataHost(clientHost);
        session.openDataConnection();
        session.closeDataConnection();
        assertTrue(stubSocket.isClosed());
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
        assertTrue(stubSocket.isClosed());
        assertTrue(stubServerSocket.isClosed());
    }

    @Test
    void testSwitchToPassiveMode() throws IOException {
        StubServerSocket stubServerSocket = new StubServerSocket(PORT);
        StubServerSocketFactory stubServerSocketFactory = new StubServerSocketFactory(stubServerSocket);
        session.serverSocketFactory = stubServerSocketFactory;

        assertNull(session.passiveModeDataSocket);
        int port = session.switchToPassiveMode();
        assertSame(stubServerSocket, session.passiveModeDataSocket);
        assertEquals(PORT, port);
    }

    @Test
    void testGetServerHost() {
        assertEquals(DEFAULT_HOST, session.getServerHost());
    }

    @Test
    void testGetClientHost_NotRunning() {
        assertNull(session.getClientHost());
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
        assertEquals("LIST", command.getName());
        assertArrayEquals(EMPTY, command.getParameters());

        command = session.parseCommand("USER user123");
        assertEquals("USER", command.getName());
        assertArrayEquals(array("user123"), command.getParameters());

        command = session.parseCommand("PORT 127,0,0,1,17,37");
        assertEquals("PORT", command.getName());
        assertArrayEquals(new String[] { "127", "0", "0", "1", "17", "37" }, command.getParameters());
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
        log("output=[" + outputStream.toString() + "]");
        assertEquals(DATA, outputStream.toString());
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
        assertNull(session.getAttribute(NAME1));
        session.setAttribute(NAME1, VALUE);
        session.setAttribute(NAME2, null);
        assertEquals(VALUE, session.getAttribute(NAME1));
        assertNull(session.getAttribute(NAME2));
        assertNull(session.getAttribute("noSuchName"));
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
        assertNull(session.getAttribute(NAME1));
    }

    @Test
    void testRemoveAttribute_Null() {
        assertThrows(AssertFailedException.class, () -> session.removeAttribute(null));
    }

    @Test
    void testGetAttributeNames() {
        assertEquals(Collections.EMPTY_SET, session.getAttributeNames());
        session.setAttribute(NAME1, VALUE);
        assertEquals(Collections.singleton(NAME1), session.getAttributeNames());
        session.setAttribute(NAME2, VALUE);
        assertEquals(set(NAME1, NAME2), session.getAttributeNames());
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
        return createTestSocket(inputStreamContents.getBytes());
    }

    private StubSocket createTestSocket(byte[] inputStreamContents) {
        InputStream inputStream = new ByteArrayInputStream(inputStreamContents);
        StubSocket stubSocket = new StubSocket(inputStream, outputStream);
        stubSocket._setLocalAddress(DEFAULT_HOST);
        return stubSocket;
    }

}
