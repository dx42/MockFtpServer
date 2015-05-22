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
 * @version $Revision$ - $Date$
 * 
 * @author Chris Mair
 */
public final class DefaultSessionTest extends AbstractTestCase {

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

    public void testConstructor_NullControlSocket() {
        try {
            new DefaultSession(null, commandHandlerMap);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    public void testConstructor_NullCommandHandlerMap() {
        try {
            new DefaultSession(stubSocket, null);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    public void testSetClientDataPort() {
        StubSocket stubSocket = createTestSocket("");
        StubSocketFactory stubSocketFactory = new StubSocketFactory(stubSocket);
        session.socketFactory = stubSocketFactory;
        session.setClientDataPort(PORT);
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        assertEquals("data port", PORT, stubSocketFactory.requestedDataPort);
    }

    public void testSetClientDataPort_AfterPassiveConnectionMode() throws IOException {
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

    public void testSetClientHost() throws Exception {
        StubSocket stubSocket = createTestSocket("");
        StubSocketFactory stubSocketFactory = new StubSocketFactory(stubSocket);
        session.socketFactory = stubSocketFactory;
        session.setClientDataHost(clientHost);
        session.openDataConnection();
        assertEquals("client host", clientHost, stubSocketFactory.requestedHost);
    }

    public void testOpenDataConnection() {
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

    public void testOpenDataConnection_PassiveMode_NoConnection() throws IOException {
        StubServerSocket stubServerSocket = new StubServerSocket(PORT);
        StubServerSocketFactory stubServerSocketFactory = new StubServerSocketFactory(stubServerSocket);
        session.serverSocketFactory = stubServerSocketFactory;

        session.switchToPassiveMode();

        try {
            session.openDataConnection();
            fail("Expected MockFtpServerException");
        }
        catch (MockFtpServerException expected) {
            LOG.info("Expected: " + expected);
            assertSame("cause", SocketTimeoutException.class, expected.getCause().getClass());
        }
    }

    public void testOpenDataConnection_NullClientHost() {
        try {
            session.openDataConnection();
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    public void testReadData() {
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);
        session.setClientDataHost(clientHost);

        session.openDataConnection();
        byte[] data = session.readData();
        LOG.info("data=[" + new String(data) + "]");
        assertEquals("data", DATA.getBytes(), data);
    }

    public void testReadData_PassiveMode() throws IOException {
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

    public void testReadData_NumBytes() {
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

    public void testReadData_NumBytes_AskForMoreBytesThanThereAre() {
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);
        session.setClientDataHost(clientHost);

        session.openDataConnection();
        byte[] data = session.readData(10000);
        LOG.info("data=[" + new String(data) + "]");
        assertEquals("data", DATA.getBytes(), data);
    }

    public void testCloseDataConnection() {
        StubSocket stubSocket = createTestSocket(DATA);
        session.socketFactory = new StubSocketFactory(stubSocket);

        session.setClientDataHost(clientHost);
        session.openDataConnection();
        session.closeDataConnection();
        assertTrue("client data socket should be closed", stubSocket.isClosed());
    }

    public void testCloseDataConnection_PassiveMode() throws IOException {
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

    public void testSwitchToPassiveMode() throws IOException {
        StubServerSocket stubServerSocket = new StubServerSocket(PORT);
        StubServerSocketFactory stubServerSocketFactory = new StubServerSocketFactory(stubServerSocket);
        session.serverSocketFactory = stubServerSocketFactory;

        assertNull("passiveModeDataSocket starts out null", session.passiveModeDataSocket);
        int port = session.switchToPassiveMode();
        assertSame("passiveModeDataSocket", stubServerSocket, session.passiveModeDataSocket);
        assertEquals("port", PORT, port);
    }

    public void testGetServerHost() {
        assertEquals("host", DEFAULT_HOST, session.getServerHost());
    }

    public void testGetClientHost_NotRunning() {
        assertNull("null", session.getClientHost());
    }

    public void testReadCommand() {
        StringReader stringReader = new StringReader(COMMAND);
        session.controlConnectionReader = new BufferedReader(stringReader);
        assertEquals(new Command(COMMAND, EMPTY), session.readCommand());
    }

    public void testReadCommand_ReadLineReturnsNull_ReturnsNull() {
        session.controlConnectionReader = new BufferedReader(new StringReader(COMMAND)) {
            public boolean ready() { return true; }
            public String readLine() { return null; }
        };
        assertNull(session.readCommand());

//        try {
//            session.readCommand();
//            fail("Expected AssertFailedException");
//        }
//        catch(AssertFailedException expected) {
//            // do nothing; expected
//        }
    }

    public void testReadCommand_Closed_ReturnsNull() {
        session.close();
        assertNull(session.readCommand());
    }

    public void testParseCommand() {
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

    public void testParseCommand_EmptyCommandString() {
        try {
            session.parseCommand("");
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }
    
    public void testSendData() {
        StubSocket stubSocket = createTestSocket("1234567890 abcdef");
        session.socketFactory = new StubSocketFactory(stubSocket);

        session.setClientDataHost(clientHost);
        session.openDataConnection();
        session.sendData(DATA.getBytes(), DATA.length());
        LOG.info("output=[" + outputStream.toString() + "]");
        assertEquals("output", DATA, outputStream.toString());
    }

    public void testSendData_Null() {
        try {
            session.sendData(null, 1);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    public void testSendReply_InvalidReplyCode() {
        try {
            session.sendReply(-66, "text");
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    public void testGetAndSetAttribute() {
        assertNull("name does not exist yet", session.getAttribute(NAME1));
        session.setAttribute(NAME1, VALUE);
        session.setAttribute(NAME2, null);
        assertEquals("NAME1", VALUE, session.getAttribute(NAME1));
        assertNull("NAME2", session.getAttribute(NAME2));
        assertNull("no such name", session.getAttribute("noSuchName"));
    }
    
    public void testGetAttribute_Null() {
        try {
            session.getAttribute(null);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }
    
    public void testSetAttribute_NullName() {
        try {
            session.setAttribute(null, VALUE);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }
    
    public void testRemoveAttribute() {
        session.removeAttribute("noSuchName");      // do nothing
        session.setAttribute(NAME1, VALUE);
        session.removeAttribute(NAME1);
        assertNull("NAME1", session.getAttribute(NAME1));
    }
    
    public void testRemoveAttribute_Null() {
        try {
            session.removeAttribute(null);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }
    
    public void testGetAttributeNames() {
        assertEquals("No names yet", Collections.EMPTY_SET, session.getAttributeNames());
        session.setAttribute(NAME1, VALUE);
        assertEquals("1", Collections.singleton(NAME1), session.getAttributeNames());
        session.setAttribute(NAME2, VALUE);
        assertEquals("2", set(NAME1, NAME2), session.getAttributeNames());
    }
    
    // -------------------------------------------------------------------------
    // Setup and Internal Helper Methods
    // -------------------------------------------------------------------------

    protected void setUp() throws Exception {
        super.setUp();

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
