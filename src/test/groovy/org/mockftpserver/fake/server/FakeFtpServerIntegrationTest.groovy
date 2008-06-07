/*
 * Copyright 2008 the original author or authors.
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
package org.mockftpserver.fake.server

import org.apache.commons.net.ftp.FTPClient
import org.mockftpserver.fake.filesystem.FakeWindowsFileSystem
import org.mockftpserver.fake.filesystem.FileSystem
import org.mockftpserver.fake.user.UserAccount
import org.mockftpserver.test.AbstractGroovyTest
import org.mockftpserver.test.PortTestUtil

/**
 * Integration tests for FakeFtpServer.
 *
 * @version $Revision: 54 $ - $Date: 2008-05-13 21:54:53 -0400 (Tue, 13 May 2008) $
 *
 * @author Chris Mair
 */
class FakeFtpServerIntegrationTest extends AbstractGroovyTest {

    static final SERVER = "localhost";
    static final USERNAME = "user123";
    static final PASSWORD = "password";
    static final FILENAME = "abc.txt";
    static final ASCII_CONTENTS = "abcdef\tghijklmnopqr";
    static final BINARY_CONTENTS = new byte[256];
    static final HOME_DIRECTORY = "/"

    private FakeFtpServer ftpServer;
    private FTPClient ftpClient;
    private FileSystem fileSystem

    //-------------------------------------------------------------------------
    // Tests
    //-------------------------------------------------------------------------

    void testLogin() {
        // Connect
        LOG.info("Conecting to " + SERVER)
        ftpClientConnect()
        verifyReplyCode("connect", 220)

        // Login
        String userAndPassword = USERNAME + "/" + PASSWORD
        LOG.info("Logging in as " + userAndPassword)
        boolean success = ftpClient.login(USERNAME, PASSWORD)
        assertTrue("Unable to login with " + userAndPassword, success)
        verifyReplyCode("login with " + userAndPassword, 230)

        // Quit
        ftpClient.quit()
        verifyReplyCode("quit", 221)
    }

//    void testPwd() {
//        ftpClientConnect()
//
//        String dir = ftpClient.printWorkingDirectory()
//        assert dir == DIR
//        verifyReplyCode("printWorkingDirectory", 257)
//    }

    // -------------------------------------------------------------------------
    // Test setup and tear-down
    // -------------------------------------------------------------------------

    /**
     * Perform initialization before each test
     * @see org.mockftpserver.test.AbstractTest#setUp()
     */
    void setUp() {
        super.setUp();

        for (int i = 0; i < BINARY_CONTENTS.length; i++) {
            BINARY_CONTENTS[i] = (byte) i;
        }

        ftpServer = new FakeFtpServer();
        ftpServer.setServerControlPort(PortTestUtil.getFtpServerControlPort());

        fileSystem = new FakeWindowsFileSystem()
        ftpServer.fileSystem = fileSystem
        fileSystem.createDirectory(HOME_DIRECTORY)

        def userAccount = new UserAccount(username: USERNAME, password: PASSWORD,
                passwordRequiredForLogin: true, homeDirectory: HOME_DIRECTORY)
        ftpServer.userAccounts[USERNAME] = userAccount

        ftpServer.start();
        ftpClient = new FTPClient();
    }

    /**
     * Perform cleanup after each test
     * @see org.mockftpserver.test.AbstractTest#tearDown()
     */
    void tearDown() {
        super.tearDown();
        ftpServer.stop();
    }

    // -------------------------------------------------------------------------
    // Internal Helper Methods
    // -------------------------------------------------------------------------

    /**
     * Connect to the server from the FTPClient
     */
    private void ftpClientConnect() throws IOException {
        ftpClient.connect(SERVER, PortTestUtil.getFtpServerControlPort());
    }

    /**
     * Assert that the FtpClient reply code is equal to the expected value
     *
     * @param operation - the description of the operation performed; used in the error message
     * @param expectedReplyCode - the expected FtpClient reply code
     */
    private void verifyReplyCode(String operation, int expectedReplyCode) {
        int replyCode = ftpClient.getReplyCode();
        LOG.info("Reply: operation=\"" + operation + "\" replyCode=" + replyCode);
        assertEquals("Unexpected replyCode for " + operation, expectedReplyCode, replyCode);
    }

}