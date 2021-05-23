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
package org.mockftpserver.stub.example;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.stub.StubFtpServer;
import org.mockftpserver.test.AbstractTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.ByteArrayOutputStream;

/**
 * Example test for StubFtpServer, using the Spring Framework ({@link http://www.springframework.org/}) 
 * for configuration.
 */
class SpringConfigurationTest extends AbstractTestCase {

    private static final String SERVER = "localhost";
    private static final int PORT = 9981;

    private StubFtpServer stubFtpServer;
    private FTPClient ftpClient;
    
    /**
     * Test starting the StubFtpServer configured within the example Spring configuration file 
     */
    @Test
    void testStubFtpServer() throws Exception {
        stubFtpServer.start();
        
        ftpClient.connect(SERVER, PORT);

        // PWD
        String dir = ftpClient.printWorkingDirectory();
        assertEquals("foo/bar", dir);
        
        // LIST
        FTPFile[] files = ftpClient.listFiles();
        log("FTPFile[0]=" + files[0]);
        log("FTPFile[1]=" + files[1]);
        assertEquals(2, files.length);
        
        // DELE
        assertFalse(ftpClient.deleteFile("AnyFile.txt"));
        
        // RETR
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        assertTrue(ftpClient.retrieveFile("SomeFile.txt", outputStream));
        log("File contents=[" + outputStream.toString() + "]");
    }

    @BeforeEach
    void setUp() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("stubftpserver-beans.xml");
        stubFtpServer = (StubFtpServer) context.getBean("stubFtpServer");

        ftpClient = new FTPClient();
    }

    @AfterEach
    void tearDown() throws Exception {
        stubFtpServer.stop();
    }

}
