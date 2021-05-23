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

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.command.*;
import org.mockftpserver.stub.command.*;
import org.mockftpserver.test.AbstractTestCase;
import org.mockftpserver.test.IntegrationTest;
import org.mockftpserver.test.PortTestUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tests for StubFtpServer using the Apache Jakarta Commons Net FTP client.
 *
 * @author Chris Mair
 */
class StubFtpServerIntegrationTest extends AbstractTestCase implements IntegrationTest {

    private static final String SERVER = "localhost";
    private static final String USERNAME = "user123";
    private static final String PASSWORD = "password";
    private static final String FILENAME = "abc.txt";
    private static final String ASCII_CONTENTS = "abcdef\tghijklmnopqr";
    private static final byte[] BINARY_CONTENTS = new byte[256];

    private StubFtpServer stubFtpServer;
    private FTPClient ftpClient;
    private RetrCommandHandler retrCommandHandler;
    private StorCommandHandler storCommandHandler;

    //-------------------------------------------------------------------------
    // Tests
    //-------------------------------------------------------------------------

    @Test
    void testLogin() throws Exception {
        // Connect
        log("Conecting to " + SERVER);
        ftpClientConnect();
        verifyReplyCode("connect", 220);

        // Login
        String userAndPassword = USERNAME + "/" + PASSWORD;
        log("Logging in as " + userAndPassword);
        boolean success = ftpClient.login(USERNAME, PASSWORD);
        assertTrue(success);
        verifyReplyCode("login with " + userAndPassword, 230);

        assertTrue(stubFtpServer.isStarted());
        assertFalse(stubFtpServer.isShutdown());

        // Quit
        log("Quit");
        ftpClient.quit();
        verifyReplyCode("quit", 221);
    }

    @Test
    void testAcct() throws Exception {
        ftpClientConnect();

        // ACCT
        int replyCode = ftpClient.acct("123456");
        assertEquals(230, replyCode);
    }

    @Test
    void testStop_NoSessionEverStarted() {
        log("Testing a stop() when no session has ever been started");
    }

    @Test
    void testHelp() throws Exception {
        // Modify HELP CommandHandler to return a predefined help message
        final String HELP = "help message";
        HelpCommandHandler helpCommandHandler = (HelpCommandHandler) stubFtpServer.getCommandHandler(CommandNames.HELP);
        helpCommandHandler.setHelpMessage(HELP);

        ftpClientConnect();

        // HELP
        String help = ftpClient.listHelp();
        assertTrue(help.indexOf(HELP) != -1);
        verifyReplyCode("listHelp", 214);
    }

    @Test
    void testList() throws Exception {
        ftpClientConnect();

        // Set directory listing
        ListCommandHandler listCommandHandler = (ListCommandHandler) stubFtpServer.getCommandHandler(CommandNames.LIST);
        listCommandHandler.setDirectoryListing("11-09-01 12:30PM  406348 File2350.log\n"
                + "11-01-01 1:30PM <DIR>  archive");

        // LIST
        FTPFile[] files = ftpClient.listFiles();
        assertEquals(2, files.length);
        verifyFTPFile(files[0], FTPFile.FILE_TYPE, "File2350.log", 406348L);
        verifyFTPFile(files[1], FTPFile.DIRECTORY_TYPE, "archive", 0L);
        verifyReplyCode("list", 226);
    }

    @Test
    void testList_PassiveMode() throws Exception {
        ftpClientConnect();

        ftpClient.enterLocalPassiveMode();

        // Set directory listing
        ListCommandHandler listCommandHandler = (ListCommandHandler) stubFtpServer.getCommandHandler(CommandNames.LIST);
        listCommandHandler.setDirectoryListing("11-09-01 12:30PM  406348 File2350.log");

        // LIST
        FTPFile[] files = ftpClient.listFiles();
        assertEquals(1, files.length);
        verifyReplyCode("list", 226);
    }

    @Test
    void testNlst() throws Exception {
        ftpClientConnect();

        // Set directory listing
        NlstCommandHandler nlstCommandHandler = (NlstCommandHandler) stubFtpServer.getCommandHandler(CommandNames.NLST);
        nlstCommandHandler.setDirectoryListing("File1.txt\nfile2.data");

        // NLST
        String[] filenames = ftpClient.listNames();
        assertEquals(2, filenames.length);
        assertEquals(filenames[0], "File1.txt", "filenames[0]");
        assertEquals(filenames[1], "file2.data", "filenames[1]");
        verifyReplyCode("listNames", 226);
    }

    @Test
    void testPwd() throws Exception {
        // Modify PWD CommandHandler to return a predefined directory
        final String DIR = "some/dir";
        PwdCommandHandler pwdCommandHandler = (PwdCommandHandler) stubFtpServer.getCommandHandler(CommandNames.PWD);
        pwdCommandHandler.setDirectory(DIR);

        ftpClientConnect();

        // PWD
        String dir = ftpClient.printWorkingDirectory();
        assertEquals(DIR, dir);
        verifyReplyCode("printWorkingDirectory", 257);
    }

    @Test
    void testStat() throws Exception {
        // Modify Stat CommandHandler to return predefined text
        final String STATUS = "some information 123";
        StatCommandHandler statCommandHandler = (StatCommandHandler) stubFtpServer.getCommandHandler(CommandNames.STAT);
        statCommandHandler.setStatus(STATUS);

        ftpClientConnect();

        // STAT
        String status = ftpClient.getStatus();
        assertEquals("211 " + STATUS + ".", status.trim());
        verifyReplyCode("getStatus", 211);
    }

    @Test
    void testStat_MultilineReplyText() throws Exception {
        // Modify Stat CommandHandler to return predefined text
        final String STATUS = "System name: abc.def\r\nVersion 3.5.7\r\nNumber of failed logins: 2";
        final String FORMATTED_REPLY_STATUS = "211-System name: abc.def\r\nVersion 3.5.7\r\n211 Number of failed logins: 2.";
        StatCommandHandler statCommandHandler = (StatCommandHandler) stubFtpServer.getCommandHandler(CommandNames.STAT);
        statCommandHandler.setStatus(STATUS);

        ftpClientConnect();

        // STAT
        String status = ftpClient.getStatus();
        assertEquals(FORMATTED_REPLY_STATUS, status.trim());
        verifyReplyCode("getStatus", 211);
    }

    @Test
    void testSyst() throws Exception {
        ftpClientConnect();

        // SYST
        assertEquals("\"WINDOWS\" system type.", ftpClient.getSystemName());
        verifyReplyCode("syst", 215);
    }

    @Test
    void testCwd() throws Exception {
        // Connect
        log("Conecting to " + SERVER);
        ftpClientConnect();
        verifyReplyCode("connect", 220);

        // CWD
        boolean success = ftpClient.changeWorkingDirectory("dir1/dir2");
        assertTrue(success);
        verifyReplyCode("changeWorkingDirectory", 250);
    }

    @Test
    void testCwd_Error() throws Exception {
        // Override CWD CommandHandler to return error reply code
        final int REPLY_CODE = 500;
        StaticReplyCommandHandler cwdCommandHandler = new StaticReplyCommandHandler(REPLY_CODE);
        stubFtpServer.setCommandHandler("CWD", cwdCommandHandler);

        ftpClientConnect();

        // CWD
        boolean success = ftpClient.changeWorkingDirectory("dir1/dir2");
        assertFalse(success);
        verifyReplyCode("changeWorkingDirectory", REPLY_CODE);
    }

    @Test
    void testCdup() throws Exception {
        ftpClientConnect();

        // CDUP
        boolean success = ftpClient.changeToParentDirectory();
        assertTrue(success);
        verifyReplyCode("changeToParentDirectory", 200);
    }

    @Test
    void testDele() throws Exception {
        ftpClientConnect();

        // DELE
        boolean success = ftpClient.deleteFile(FILENAME);
        assertTrue(success);
        verifyReplyCode("deleteFile", 250);
    }

    @Test
    void testEprt() {
        log("Skipping...");
//        ftpClientConnect();
//        ftpClient.sendCommand("EPRT", "|2|1080::8:800:200C:417A|5282|");
//        verifyReplyCode("EPRT", 200);
    }

    @Test
    void testEpsv() throws Exception {
        ftpClientConnect();
        ftpClient.sendCommand("EPSV");
        verifyReplyCode("EPSV", 229);
    }

    @Test
    void testFeat_UseStaticReplyCommandHandler() throws IOException {
        // The FEAT command is not supported out of the box
        final String FEAT_TEXT = "Extensions supported:\r\n" +
                "MLST size*;create;modify*;perm;media-type\r\n" +
                "SIZE\r\n" +
                "COMPRESSION\r\n" +
                "END";
        StaticReplyCommandHandler featCommandHandler = new StaticReplyCommandHandler(211, FEAT_TEXT);
        stubFtpServer.setCommandHandler("FEAT", featCommandHandler);

        ftpClientConnect();
        assertEquals(ftpClient.sendCommand("FEAT"), 211);
        log(ftpClient.getReplyString());
    }

    @Test
    void testMkd() throws Exception {
        ftpClientConnect();

        // MKD
        boolean success = ftpClient.makeDirectory("dir1/dir2");
        assertTrue(success);
        verifyReplyCode("makeDirectory", 257);
    }

    @Test
    void testNoop() throws Exception {
        ftpClientConnect();

        // NOOP
        boolean success = ftpClient.sendNoOp();
        assertTrue(success);
        verifyReplyCode("NOOP", 200);
    }

    @Test
    void testRest() throws Exception {
        ftpClientConnect();

        // REST
        int replyCode = ftpClient.rest("marker");
        assertEquals(350, replyCode);
    }

    @Test
    void testRmd() throws Exception {
        ftpClientConnect();

        // RMD
        boolean success = ftpClient.removeDirectory("dir1/dir2");
        assertTrue(success);
        verifyReplyCode("removeDirectory", 250);
    }

    @Test
    void testRename() throws Exception {
        ftpClientConnect();

        // Rename (RNFR, RNTO)
        boolean success = ftpClient.rename(FILENAME, "new_" + FILENAME);
        assertTrue(success);
        verifyReplyCode("rename", 250);
    }

    @Test
    void testAllo() throws Exception {
        ftpClientConnect();

        // ALLO
        assertTrue(ftpClient.allocate(1024));
        assertTrue(ftpClient.allocate(1024, 64));
    }

    @Test
    void testTransferAsciiFile() throws Exception {
        retrCommandHandler.setFileContents(ASCII_CONTENTS);

        ftpClientConnect();

        // Get File
        log("Get File for remotePath [" + FILENAME + "]");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        assertTrue(ftpClient.retrieveFile(FILENAME, outputStream));
        log("File contents=[" + outputStream.toString());
        assertEquals(ASCII_CONTENTS, outputStream.toString());

        // Put File
        log("Put File for local path [" + FILENAME + "]");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ASCII_CONTENTS.getBytes());
        assertTrue(ftpClient.storeFile(FILENAME, inputStream));
        InvocationRecord invocationRecord = storCommandHandler.getInvocation(0);
        byte[] contents = (byte[]) invocationRecord.getObject(StorCommandHandler.FILE_CONTENTS_KEY);
        log("File contents=[" + contents + "]");
        assertArrayEquals(ASCII_CONTENTS.getBytes(), contents);
    }

    @Test
    void testTransferBinaryFiles() throws Exception {
        retrCommandHandler.setFileContents(BINARY_CONTENTS);

        ftpClientConnect();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        // Get File
        log("Get File for remotePath [" + FILENAME + "]");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        assertTrue(ftpClient.retrieveFile(FILENAME, outputStream));
        log("GET File length=" + outputStream.size());
        assertArrayEquals(BINARY_CONTENTS, outputStream.toByteArray());

        // Put File
        log("Put File for local path [" + FILENAME + "]");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(BINARY_CONTENTS);
        assertTrue(ftpClient.storeFile(FILENAME, inputStream));
        InvocationRecord invocationRecord = storCommandHandler.getInvocation(0);
        byte[] contents = (byte[]) invocationRecord.getObject(StorCommandHandler.FILE_CONTENTS_KEY);
        log("PUT File length=" + contents.length);
        assertArrayEquals(BINARY_CONTENTS, contents);
    }

    @Test
    void testStou() throws Exception {
        StouCommandHandler stouCommandHandler = (StouCommandHandler) stubFtpServer.getCommandHandler(CommandNames.STOU);
        stouCommandHandler.setFilename(FILENAME);

        ftpClientConnect();

        // Stor a File (STOU)
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ASCII_CONTENTS.getBytes());
        assertTrue(ftpClient.storeUniqueFile(FILENAME, inputStream));
        InvocationRecord invocationRecord = stouCommandHandler.getInvocation(0);
        byte[] contents = (byte[]) invocationRecord.getObject(StorCommandHandler.FILE_CONTENTS_KEY);
        log("File contents=[" + contents + "]");
        assertArrayEquals(ASCII_CONTENTS.getBytes(), contents);
    }

    @Test
    void testAppe() throws Exception {
        AppeCommandHandler appeCommandHandler = (AppeCommandHandler) stubFtpServer.getCommandHandler(CommandNames.APPE);

        ftpClientConnect();

        // Append a File (APPE)
        ByteArrayInputStream inputStream = new ByteArrayInputStream(ASCII_CONTENTS.getBytes());
        assertTrue(ftpClient.appendFile(FILENAME, inputStream));
        InvocationRecord invocationRecord = appeCommandHandler.getInvocation(0);
        byte[] contents = (byte[]) invocationRecord.getObject(AppeCommandHandler.FILE_CONTENTS_KEY);
        log("File contents=[" + contents + "]");
        assertArrayEquals(ASCII_CONTENTS.getBytes(), contents);
    }

    @Test
    void testAbor() throws Exception {
        ftpClientConnect();

        // ABOR
        assertTrue(ftpClient.abort());
    }

    @Test
    void testPasv() throws Exception {
        ftpClientConnect();

        // PASV
        ftpClient.enterLocalPassiveMode();
        // no reply code; the PASV command is sent only when the data connection is opened 
    }

    @Test
    void testMode() throws Exception {
        ftpClientConnect();

        // MODE
        boolean success = ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        assertTrue(success);
        verifyReplyCode("setFileTransferMode", 200);
    }

    @Test
    void testStru() throws Exception {
        ftpClientConnect();

        // STRU
        boolean success = ftpClient.setFileStructure(FTP.FILE_STRUCTURE);
        assertTrue(success);
        verifyReplyCode("setFileStructure", 200);
    }

    @Test
    void testSimpleCompositeCommandHandler() throws Exception {
        // Replace CWD CommandHandler with a SimpleCompositeCommandHandler
        CommandHandler commandHandler1 = new StaticReplyCommandHandler(500);
        CommandHandler commandHandler2 = new CwdCommandHandler();
        SimpleCompositeCommandHandler simpleCompositeCommandHandler = new SimpleCompositeCommandHandler();
        simpleCompositeCommandHandler.addCommandHandler(commandHandler1);
        simpleCompositeCommandHandler.addCommandHandler(commandHandler2);
        stubFtpServer.setCommandHandler("CWD", simpleCompositeCommandHandler);

        // Connect
        ftpClientConnect();

        // CWD
        assertFalse(ftpClient.changeWorkingDirectory("dir1/dir2"));
        assertTrue(ftpClient.changeWorkingDirectory("dir1/dir2"));
    }

    @Test
    void testSite() throws Exception {
        ftpClientConnect();

        // SITE
        int replyCode = ftpClient.site("parameters,1,2,3");
        assertEquals(200, replyCode);
    }

    @Test
    void testSmnt() throws Exception {
        ftpClientConnect();

        // SMNT
        assertTrue(ftpClient.structureMount("dir1/dir2"));
        verifyReplyCode("structureMount", 250);
    }

    @Test
    void testRein() throws Exception {
        ftpClientConnect();

        // REIN
        assertEquals(220, ftpClient.rein());
    }

    @Test
    void testCommandNamesInLowerOrMixedCase() throws Exception {
        ftpClientConnect();

        assertEquals(220, ftpClient.sendCommand("rein"));
        assertEquals(220, ftpClient.sendCommand("rEIn"));
        assertEquals(220, ftpClient.sendCommand("reiN"));
        assertEquals(220, ftpClient.sendCommand("Rein"));
    }

    @Test
    void testUnrecognizedCommand() throws Exception {
        ftpClientConnect();

        assertEquals(502, ftpClient.sendCommand("XXXX"));
    }

    // -------------------------------------------------------------------------
    // Test setup and tear-down
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        for (int i = 0; i < BINARY_CONTENTS.length; i++) {
            BINARY_CONTENTS[i] = (byte) i;
        }

        stubFtpServer = new StubFtpServer();
        stubFtpServer.setServerControlPort(PortTestUtil.getFtpServerControlPort());
        stubFtpServer.start();
        ftpClient = new FTPClient();
        retrCommandHandler = (RetrCommandHandler) stubFtpServer.getCommandHandler(CommandNames.RETR);
        storCommandHandler = (StorCommandHandler) stubFtpServer.getCommandHandler(CommandNames.STOR);
    }

    @AfterEach
    void tearDown() {
        stubFtpServer.stop();
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
     * @param operation         - the description of the operation performed; used in the error message
     * @param expectedReplyCode - the expected FtpClient reply code
     */
    private void verifyReplyCode(String operation, int expectedReplyCode) {
        int replyCode = ftpClient.getReplyCode();
        log("Reply: operation=\"" + operation + "\" replyCode=" + replyCode);
        assertEquals(expectedReplyCode, replyCode);
    }

    /**
     * Verify that the FTPFile has the specified properties
     *
     * @param ftpFile - the FTPFile to verify
     * @param type    - the expected file type
     * @param name    - the expected file name
     * @param size    - the expected file size (will be zero for a directory)
     */
    private void verifyFTPFile(FTPFile ftpFile, int type, String name, long size) {
        log(ftpFile.toString());
        assertEquals(type, ftpFile.getType());
        assertEquals(name, ftpFile.getName());
        assertEquals(size, ftpFile.getSize());
    }

}
