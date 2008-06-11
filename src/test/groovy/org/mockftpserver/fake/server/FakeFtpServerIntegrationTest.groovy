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
import org.apache.commons.net.ftp.FTPFile
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
    static final ASCII_CONTENTS = "abcdef\tghijklmnopqr";
    static final BINARY_CONTENTS = new byte[256];
    static final HOME_DIR = "c:/home"
    static final SUBDIR_NAME = 'sub'
    static final SUBDIR = p(HOME_DIR, SUBDIR_NAME)
    static final FILENAME1 = "abc.txt";
    static final FILENAME2 = "SomeOtherFile.xml";
    static final FILE1 = p(HOME_DIR, FILENAME1)

    private FakeFtpServer ftpServer;
    private FTPClient ftpClient;
    private FileSystem fileSystem

    //-------------------------------------------------------------------------
    // Tests
    //-------------------------------------------------------------------------

    void testLogin() {
        ftpClientConnect()
        String userAndPassword = USERNAME + "/" + PASSWORD
        LOG.info("Logging in as " + userAndPassword)
        assert ftpClient.login(USERNAME, PASSWORD)
        verifyReplyCode("login with $userAndPassword", 230)
    }

    void testQuit() {
        ftpClientConnect()
        ftpClient.quit()
        verifyReplyCode("quit", 221)
    }

    void testPwd() {
        ftpClientConnectAndLogin()
        assert ftpClient.printWorkingDirectory() == HOME_DIR
        verifyReplyCode("printWorkingDirectory", 257)
    }

    void testCwd() {
        ftpClientConnectAndLogin()
        assert ftpClient.changeWorkingDirectory(SUBDIR_NAME);
        verifyReplyCode("changeWorkingDirectory", 250);
    }

    void testDele() {
        fileSystem.createFile(FILE1)

        ftpClientConnectAndLogin()
        assert ftpClient.deleteFile(FILENAME1);
        verifyReplyCode("deleteFile", 250);
    }

//    void testList() {
//        def DATA = "abc"
//        def LAST_MODIFIED = new Date()
//        def NAME2 = "archive"
//        fileSystem.addEntry(new FileEntry(path:p(SUBDIR,FILENAME1), lastModified:LAST_MODIFIED, contents:DATA))
//        fileSystem.addEntry(new DirectoryEntry(path:p(SUBDIR,NAME2), lastModified:LAST_MODIFIED))
//
//        ftpClientConnectAndLogin()
//
//        FTPFile[] files = ftpClient.listFiles()
//        assertEquals("number of files", 2, files.length)
//        verifyFTPFile(files[0], FTPFile.FILE_TYPE, FILENAME1, DATA.size())
//        verifyFTPFile(files[1], FTPFile.DIRECTORY_TYPE, NAME2, 0)
//        verifyReplyCode("list", 226)
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
        fileSystem.createParentDirectoriesAutomatically = true
        fileSystem.createDirectory(SUBDIR)
        ftpServer.fileSystem = fileSystem

        def userAccount = new UserAccount(username: USERNAME, password: PASSWORD,
                passwordRequiredForLogin: true, homeDirectory: HOME_DIR)
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

    private ftpClientConnectAndLogin() {
        ftpClientConnect()
        assert ftpClient.login(USERNAME, PASSWORD)
    }

    /**
     * Connect to the server from the FTPClient
     */
    private void ftpClientConnect() {
        def port = PortTestUtil.getFtpServerControlPort()
        LOG.info("Conecting to $SERVER on port $port")
        ftpClient.connect(SERVER, port);
        verifyReplyCode("connect", 220)
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

    private void verifyFTPFile(FTPFile ftpFile, int type, String name, long size) {
        LOG.info(ftpFile);
        assertEquals("type: " + ftpFile, type, ftpFile.getType());
        assertEquals("name: " + ftpFile, name, ftpFile.getName());
        assertEquals("size: " + ftpFile, size, ftpFile.getSize());
    }

}