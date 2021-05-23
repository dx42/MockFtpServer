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
package org.mockftpserver.stub;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandNames;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.AbstractStubCommandHandler;
import org.mockftpserver.test.AbstractTestCase;
import org.mockftpserver.test.IntegrationTest;
import org.mockftpserver.test.PortTestUtil;

/**
 * StubFtpServer tests for multiple FTP clients using the Apache Jakarta Commons Net FTP client.
 * 
 * @author Chris Mair
 */
class StubFtpServer_MultipleClientsIntegrationTest extends AbstractTestCase implements IntegrationTest {

    private static final String SERVER = "localhost";

    // Custom CommandHandler for PWD so that we can verify unique session-specific responses.
    // Send back the hashCode for the Session as the reply text.
    private static class CustomPwdCommandHandler extends AbstractStubCommandHandler {
        protected void handleCommand(Command command, Session session, InvocationRecord invocationRecord) throws Exception {
            String replyText = quotes(Integer.toString(session.hashCode()));
            sendReply(session, 257, null, replyText, null);
        }
    }
    
    private StubFtpServer stubFtpServer;
    private FTPClient ftpClient1;
    private FTPClient ftpClient2;
    private FTPClient ftpClient3;

    @Test
    void testMultipleClients() throws Exception {

        // Connect from client 1
        log("connect() to ftpClient1");
        ftpClient1.connect(SERVER, PortTestUtil.getFtpServerControlPort());
        String sessionId1 = ftpClient1.printWorkingDirectory();
        log("PWD(1) reply =[" + sessionId1 + "]");

        // Connect from client 2
        log("connect() to ftpClient2");
        ftpClient2.connect(SERVER, PortTestUtil.getFtpServerControlPort());
        String sessionId2 = ftpClient2.printWorkingDirectory();
        log("PWD(2) reply =[" + sessionId2 + "]");

        // Connect from client 3
        log("connect() to ftpClient3");
        ftpClient3.connect(SERVER, PortTestUtil.getFtpServerControlPort());
        String sessionId3 = ftpClient3.printWorkingDirectory();
        log("PWD(3) reply =[" + sessionId3 + "]");
        
        // Make sure all session ids are unique
        assertNotSame("sessionId1 vs sessionId2", sessionId1, sessionId2);
        assertNotSame("sessionId2 vs sessionId3", sessionId2, sessionId3);
        assertNotSame("sessionId1 vs sessionId3", sessionId1, sessionId3);

        // Now make sure that the replies from the existing sessions remain consistent
        assertEquals(sessionId1, ftpClient1.printWorkingDirectory());
        assertEquals(sessionId2, ftpClient2.printWorkingDirectory());
        assertEquals(sessionId3, ftpClient3.printWorkingDirectory());
    }
    
    // -------------------------------------------------------------------------
    // Test setup and tear-down
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        stubFtpServer = new StubFtpServer();
        stubFtpServer.setServerControlPort(PortTestUtil.getFtpServerControlPort());
        stubFtpServer.setCommandHandler(CommandNames.PWD, new CustomPwdCommandHandler());
        stubFtpServer.start();
        
        ftpClient1 = new FTPClient();
        ftpClient2 = new FTPClient();
        ftpClient3 = new FTPClient();
        
        ftpClient1.setDefaultTimeout(1000);
        ftpClient2.setDefaultTimeout(1000);
        ftpClient3.setDefaultTimeout(1000);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        log("Cleaning up...");
        if (ftpClient1.isConnected()) {
            ftpClient1.disconnect();
        }
        if (ftpClient2.isConnected()) {
            ftpClient2.disconnect();
        }
        if (ftpClient3.isConnected()) {
            ftpClient3.disconnect();
        }

        stubFtpServer.stop();
    }

}
