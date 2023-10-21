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
package org.mockftpserver.fake

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockftpserver.core.command.CommandNames
import org.mockftpserver.core.command.StaticReplyCommandHandler

import org.mockftpserver.fake.filesystem.DirectoryEntry
import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.fake.filesystem.FileSystem
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem
import org.mockftpserver.stub.command.CwdCommandHandler
import org.mockftpserver.test.AbstractGroovyTestCase
import org.mockftpserver.test.PortTestUtil

/**
 * Integration tests for FakeFtpServer.
 *
 * @author Chris Mair
 */
class FakeFtpServerIntegrationTest extends AbstractGroovyTestCase {

    private static final String SERVER = "localhost"
    private static final String USERNAME = "user123"
    private static final String PASSWORD = "password"
    private static final String ACCOUNT = "account123"
    private static final String ASCII_DATA = "abcdef\tghijklmnopqr"
    private static final byte[] BINARY_DATA = new byte[256]
    private static final String ROOT_DIR = "c:/"
    private static final String HOME_DIR = p(ROOT_DIR, "home")
    private static final String SUBDIR_NAME = 'sub'
    private static final String SUBDIR_NAME2 = "archive"
    private static final String SUBDIR = p(HOME_DIR, SUBDIR_NAME)
    private static final String FILENAME1 = "abc.txt"
    private static final String FILENAME2 = "SomeOtherFile.xml"
    private static final String FILE1 = p(HOME_DIR, FILENAME1)
    private static final String SYSTEM_NAME = "WINDOWS"

    private FakeFtpServer ftpServer = new FakeFtpServer()
    private FTPClient ftpClient = new FTPClient()
    private FileSystem fileSystem = new WindowsFakeFileSystem()
    private UserAccount userAccount = new UserAccount(USERNAME, PASSWORD, HOME_DIR)

    //-------------------------------------------------------------------------
    // Tests
    //-------------------------------------------------------------------------

    @Test
    void testAbor() {
        ftpClientConnectAndLogin()
        assert ftpClient.abort()
        verifyReplyCode("ABOR", 226)
    }

    @Test
    void testAcct() {
        ftpClientConnectAndLogin()
        assert ftpClient.acct(ACCOUNT) == 230
    }

    @Test
    void testAllo() {
        ftpClientConnectAndLogin()
        assert ftpClient.allocate(99)
        verifyReplyCode("ALLO", 200)
    }

    @Test
    void testAppe() {
        def ORIGINAL_CONTENTS = '123 456 789'
        fileSystem.add(new FileEntry(path: FILE1, contents: ORIGINAL_CONTENTS))

        ftpClientConnectAndLogin()

        LOG.info("Put File for local path [$FILE1]")
        def inputStream = new ByteArrayInputStream(ASCII_DATA.getBytes())
        assert ftpClient.appendFile(FILE1, inputStream)
        def contents = fileSystem.getEntry(FILE1).createInputStream().text
        LOG.info("File contents=[" + contents + "]")
        assert contents == ORIGINAL_CONTENTS + ASCII_DATA
    }

    @Test
    void testCdup() {
        ftpClientConnectAndLogin()
        assert ftpClient.changeToParentDirectory()
        verifyReplyCode("changeToParentDirectory", 200)
    }

    @Test
    void testCwd() {
        ftpClientConnectAndLogin()
        assert ftpClient.changeWorkingDirectory(SUBDIR_NAME)
        verifyReplyCode("changeWorkingDirectory", 250)
    }

    /**
     * Test that a CWD to ".." properly resolves the current dir (without the "..") so that PWD returns the parent 
     */
    @Test
    void testCwd_DotDot_Pwd() {
        ftpClientConnectAndLogin()
        assert ftpClient.changeWorkingDirectory("..")
        verifyReplyCode("changeWorkingDirectory", 250)
        assert p(ftpClient.printWorkingDirectory()) == p(ROOT_DIR)
        assert ftpClient.changeWorkingDirectory("home")
        assert p(ftpClient.printWorkingDirectory()) == p(HOME_DIR)
    }

    /**
     * Test that a CWD to "." properly resolves the current dir (without the ".") so that PWD returns the parent
     */
    @Test
    void testCwd_Dot_Pwd() {
        ftpClientConnectAndLogin()
        assert ftpClient.changeWorkingDirectory(".")
        verifyReplyCode("changeWorkingDirectory", 250)
        assert p(ftpClient.printWorkingDirectory()) == p(HOME_DIR)
    }

    @Test
    void testCwd_UseStaticReplyCommandHandler() {
        final int REPLY_CODE = 500;
        StaticReplyCommandHandler cwdCommandHandler = new StaticReplyCommandHandler(REPLY_CODE);
        ftpServer.setCommandHandler(CommandNames.CWD, cwdCommandHandler);

        ftpClientConnectAndLogin()
        assert !ftpClient.changeWorkingDirectory(SUBDIR_NAME)
        verifyReplyCode("changeWorkingDirectory", REPLY_CODE)
    }

    @Test
    void testCwd_UseStubCommandHandler() {
        final int REPLY_CODE = 502;
        CwdCommandHandler cwdCommandHandler = new CwdCommandHandler();     // Stub command handler
        cwdCommandHandler.setReplyCode(REPLY_CODE);
        ftpServer.setCommandHandler(CommandNames.CWD, cwdCommandHandler);

        ftpClientConnectAndLogin()
        assert !ftpClient.changeWorkingDirectory(SUBDIR_NAME)
        verifyReplyCode("changeWorkingDirectory", REPLY_CODE)
        assert cwdCommandHandler.getInvocation(0)
    }

    @Test
    void testDele() {
        fileSystem.add(new FileEntry(FILE1))

        ftpClientConnectAndLogin()
        assert ftpClient.deleteFile(FILENAME1)
        verifyReplyCode("deleteFile", 250)
        assert !fileSystem.exists(FILENAME1)
    }

    @Test
    void testEprt() {
        log("Skipping...")
//        ftpClientConnectAndLogin()
//        assert ftpClient.sendCommand("EPRT", "|2|1080::8:800:200C:417A|5282|") == 200
    }

    @Test
    void testEpsv() {
        ftpClientConnectAndLogin()
        assert ftpClient.sendCommand("EPSV") == 229
    }

    @Test
    void testFeat_UseStaticReplyCommandHandler() {
        // The FEAT command is not supported out of the box
        StaticReplyCommandHandler featCommandHandler = new StaticReplyCommandHandler(211, "No Features");
        ftpServer.setCommandHandler("FEAT", featCommandHandler);

        ftpClientConnectAndLogin()
        assert ftpClient.sendCommand("FEAT") == 211
    }

    @Test
    void testHelp() {
        ftpServer.helpText = [a: 'aaa', '': 'default']
        ftpClientConnect()

        String help = ftpClient.listHelp()
        assert help.contains('default')
        verifyReplyCode("listHelp", 214)

        help = ftpClient.listHelp('a')
        assert help.contains('aaa')
        verifyReplyCode("listHelp", 214)

        help = ftpClient.listHelp('bad')
        assert help.contains('bad')
        verifyReplyCode("listHelp", 214)
    }

    @Test
    void testList() {
        def LAST_MODIFIED = new Date()
        fileSystem.add(new FileEntry(path: p(SUBDIR, FILENAME1), lastModified: LAST_MODIFIED, contents: ASCII_DATA))
        fileSystem.add(new DirectoryEntry(path: p(SUBDIR, SUBDIR_NAME2), lastModified: LAST_MODIFIED))

        ftpClientConnectAndLogin()

        FTPFile[] files = ftpClient.listFiles(SUBDIR)
        assert files.length == 2

        // Can't be sure of order
        FTPFile fileEntry = (files[0].getType() == FTPFile.FILE_TYPE) ? files[0] : files[1]
        FTPFile dirEntry = (files[0].getType() == FTPFile.FILE_TYPE) ? files[1] : files[0]
        verifyFTPFile(fileEntry, FTPFile.FILE_TYPE, FILENAME1, ASCII_DATA.size())
        verifyFTPFile(dirEntry, FTPFile.DIRECTORY_TYPE, SUBDIR_NAME2, 0)

        verifyReplyCode("list", 226)
    }

    @Test
    void testList_NoReadPermission() {
        def subDir = fileSystem.getEntry(SUBDIR)
        subDir.setOwner("other")
        subDir.setPermissionsFromString("rwx------")

        ftpClientConnectAndLogin()

        ftpClient.listFiles(SUBDIR)
        verifyReplyCode("list", 550)
    }

    @Test
    void testList_Unix() {
        ftpServer.systemName = 'UNIX'
        userAccount.homeDirectory = '/'

        def unixFileSystem = new UnixFakeFileSystem()
        unixFileSystem.createParentDirectoriesAutomatically = true
        unixFileSystem.add(new DirectoryEntry('/'))
        ftpServer.fileSystem = unixFileSystem

        def LAST_MODIFIED = new Date()
        unixFileSystem.add(new FileEntry(path: p('/', FILENAME1), lastModified: LAST_MODIFIED, contents: ASCII_DATA))
        unixFileSystem.add(new DirectoryEntry(path: p('/', SUBDIR_NAME2), lastModified: LAST_MODIFIED))

        ftpClientConnectAndLogin()

        FTPFile[] files = ftpClient.listFiles('/')
        assert files.length == 2

        // Can't be sure of order
        FTPFile fileEntry = (files[0].getType() == FTPFile.FILE_TYPE) ? files[0] : files[1]
        FTPFile dirEntry = (files[0].getType() == FTPFile.FILE_TYPE) ? files[1] : files[0]

        verifyFTPFile(dirEntry, FTPFile.DIRECTORY_TYPE, SUBDIR_NAME2, 0)
        verifyFTPFile(fileEntry, FTPFile.FILE_TYPE, FILENAME1, ASCII_DATA.size())
        verifyReplyCode("list", 226)
    }

    @Test
    void testLogin() {
        ftpClientConnect()
        LOG.info("Logging in as $USERNAME/$PASSWORD")
        assert ftpClient.login(USERNAME, PASSWORD)
        verifyReplyCode("login with $USERNAME/$PASSWORD", 230)

        assert ftpServer.isStarted()
        assert !ftpServer.isShutdown()
    }

    @Test
    void testLogin_WithAccount() {
        userAccount.accountRequiredForLogin = true
        ftpClientConnect()
        LOG.info("Logging in as $USERNAME/$PASSWORD with $ACCOUNT")
        assert ftpClient.login(USERNAME, PASSWORD, ACCOUNT)
        verifyReplyCode("login with $USERNAME/$PASSWORD with $ACCOUNT", 230)
    }

    @Test
    void testMkd() {
        ftpClientConnectAndLogin()

        def DIR = p(HOME_DIR, 'NewDir')
        assert ftpClient.makeDirectory(DIR)
        verifyReplyCode("makeDirectory", 257)
        assert fileSystem.isDirectory(DIR)
    }

    @Test
    void testMode() {
        ftpClientConnectAndLogin()
        assert ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        verifyReplyCode("MODE", 200)
    }

    @Test
    void testNlst() {
        fileSystem.add(new FileEntry(path: p(SUBDIR, FILENAME1)))
        fileSystem.add(new DirectoryEntry(path: p(SUBDIR, SUBDIR_NAME2)))

        ftpClientConnectAndLogin()

        String[] filenames = ftpClient.listNames(SUBDIR)
        assert filenames as Set == [FILENAME1, SUBDIR_NAME2] as Set
        verifyReplyCode("listNames", 226)
    }

    @Test
    void testNlst_NoReadPermission() {
        def subDir = fileSystem.getEntry(SUBDIR)
        subDir.setOwner("other")
        subDir.setPermissionsFromString("rwx------")

        ftpClientConnectAndLogin()

        ftpClient.listNames(SUBDIR)
        verifyReplyCode("list", 550)
    }


    @Test
    void testNoop() {
        ftpClientConnectAndLogin()
        assert ftpClient.sendNoOp()
        verifyReplyCode("NOOP", 200)
    }

    @Test
    void testPasv_Nlst() {
        fileSystem.add(new FileEntry(path: p(SUBDIR, FILENAME1)))
        fileSystem.add(new FileEntry(path: p(SUBDIR, FILENAME2)))

        ftpClientConnectAndLogin()
        ftpClient.enterLocalPassiveMode();

        String[] filenames = ftpClient.listNames(SUBDIR)
        assert filenames as Set == [FILENAME1, FILENAME2] as Set
        verifyReplyCode("listNames", 226)
    }

    @Test
    void testPwd() {
        ftpClientConnectAndLogin()
        assert ftpClient.printWorkingDirectory() == HOME_DIR
        verifyReplyCode("printWorkingDirectory", 257)
    }

    @Test
    void testQuit() {
        ftpClientConnect()
        ftpClient.quit()
        verifyReplyCode("quit", 221)
    }

    @Test
    void testRein() {
        ftpClientConnectAndLogin()
        assert ftpClient.rein() == 220
        assert ftpClient.cdup() == 530      // now logged out
    }

    @Test
    void testRest() {
        ftpClientConnectAndLogin()
        assert ftpClient.rest("marker") == 350
    }

    @Test
    void testRetr() {
        fileSystem.add(new FileEntry(path: FILE1, contents: ASCII_DATA))

        ftpClientConnectAndLogin()

        LOG.info("Get File for remotePath [$FILE1]")
        def outputStream = new ByteArrayOutputStream()
        assert ftpClient.retrieveFile(FILE1, outputStream)
        LOG.info("File contents=[${outputStream.toString()}]")
        assert outputStream.toString() == ASCII_DATA
    }

    @Test
    void testRetr_Using_retrieveFileStream() {
        fileSystem.add(new FileEntry(path: FILE1, contents: ASCII_DATA))

        ftpClientConnectAndLogin()

        InputStream inputStream = ftpClient.retrieveFileStream(FILE1)
        String text = inputStream.text
        assert text == ASCII_DATA

        inputStream.close()
        ftpClient.completePendingCommand();

        // An extra call to completePendingCommand() or getReply() will hang
        // ftpClient.completePendingCommand();
        // ftpClient.getReply();
    }

    @Test
    void testRmd() {
        ftpClientConnectAndLogin()

        assert ftpClient.removeDirectory(SUBDIR)
        verifyReplyCode("removeDirectory", 250)
        assert !fileSystem.exists(SUBDIR)
    }

    @Test
    void testRename() {                 // RNFR and RNTO
        fileSystem.add(new FileEntry(FILE1))

        ftpClientConnectAndLogin()

        assert ftpClient.rename(FILE1, FILE1 + "NEW")
        verifyReplyCode("rename", 250)
        assert !fileSystem.exists(FILE1)
        assert fileSystem.exists(FILE1 + "NEW")
    }

    @Test
    void testSite() {
        ftpClientConnectAndLogin()
        assert ftpClient.site("parameters,1,2,3") == 200
    }

    @Test
    void testSmnt() {
        ftpClientConnectAndLogin()
        assert ftpClient.smnt("dir") == 250
    }

    @Test
    void testStat() {
        ftpClientConnectAndLogin()
        def status = ftpClient.getStatus()
        assert status.contains('Connected')
        verifyReplyCode("stat", 211)
    }

    @Test
    void testStor() {
        ftpClientConnectAndLogin()

        LOG.info("Put File for local path [$FILE1]")
        def inputStream = new ByteArrayInputStream(ASCII_DATA.getBytes())
        assert ftpClient.storeFile(FILENAME1, inputStream)      // relative to homeDirectory
        def contents = fileSystem.getEntry(FILE1).createInputStream().text
        LOG.info("File contents=[" + contents + "]")
        assert contents == ASCII_DATA
    }

    @Test
    void testStor_Using_storeFileStream() {
        ftpClientConnectAndLogin()

        OutputStream outputStream = ftpClient.storeFileStream(FILENAME1)      // relative to homeDirectory
        outputStream << ASCII_DATA
        outputStream.close()
        ftpClient.completePendingCommand();

        // An extra call to completePendingCommand() or getReply() will hang
        // ftpClient.completePendingCommand();
        // ftpClient.getReply();

        def contents = fileSystem.getEntry(FILE1).createInputStream().text
        assert contents == ASCII_DATA
    }

    @Test
    void testStou() {
        ftpClientConnectAndLogin()

        def inputStream = new ByteArrayInputStream(ASCII_DATA.getBytes())
        assert ftpClient.storeUniqueFile(FILENAME1, inputStream)

        def names = fileSystem.listNames(HOME_DIR)
        def filename = names.find {name -> name.startsWith(FILENAME1) }
        assert filename

        def contents = fileSystem.getEntry(p(HOME_DIR, filename)).createInputStream().text
        LOG.info("File contents=[" + contents + "]")
        assert contents == ASCII_DATA
    }

    @Test
    void testStru() {
        ftpClientConnectAndLogin()
        assert ftpClient.setFileStructure(FTP.FILE_STRUCTURE);
        verifyReplyCode("STRU", 200)
    }

    @Test
    void testSyst() {
        ftpClientConnectAndLogin()

        def systemName = ftpClient.getSystemName()
        LOG.info("system name = [$systemName]")
        assert systemName.contains('"' + SYSTEM_NAME + '"')
        verifyReplyCode("getSystemName", 215)
    }

    @Test
    void testType() {
        ftpClientConnectAndLogin()
        assert ftpClient.type(FTP.ASCII_FILE_TYPE)
        verifyReplyCode("TYPE", 200)
    }

    @Test
    void testUnrecognizedCommand() {
        ftpClientConnectAndLogin()
        assert ftpClient.sendCommand("XXX") == 502
        verifyReplyCode("XXX", 502)
    }

    // -------------------------------------------------------------------------
    // Test setup and tear-down
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        for (int i = 0; i < BINARY_DATA.length; i++) {
            BINARY_DATA[i] = (byte) i
        }

        ftpServer.serverControlPort = PortTestUtil.getFtpServerControlPort()
        ftpServer.systemName = SYSTEM_NAME

        fileSystem.createParentDirectoriesAutomatically = true
        fileSystem.add(new DirectoryEntry(SUBDIR))
        ftpServer.fileSystem = fileSystem

        userAccount = new UserAccount(USERNAME, PASSWORD, HOME_DIR)
        ftpServer.addUserAccount(userAccount)

        ftpServer.start()
    }

    @AfterEach
    void tearDown() {
        ftpServer.stop()
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
        ftpClient.connect(SERVER, port)
        verifyReplyCode("connect", 220)
    }

    /**
     * Assert that the FtpClient reply code is equal to the expected value
     *
     * @param operation - the description of the operation performed used in the error message
     * @param expectedReplyCode - the expected FtpClient reply code
     */
    private void verifyReplyCode(String operation, int expectedReplyCode) {
        int replyCode = ftpClient.getReplyCode()
        LOG.info("Reply: operation=\"" + operation + "\" replyCode=" + replyCode)
        assert replyCode == expectedReplyCode, "Unexpected replyCode for " + operation
    }

    private void verifyFTPFile(FTPFile ftpFile, int type, String name, long size) {
        LOG.info(ftpFile.toString())
        assert ftpFile.getType() == type
        assert ftpFile.getName() == name
        assert ftpFile.getSize() == size
    }

}