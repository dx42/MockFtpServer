/*
 * Copyright 2009 the original author or authors.
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
package org.mockftpserver.stub

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockftpserver.test.AbstractGroovyTestCase
import org.apache.commons.net.ftp.FTPClient
import org.mockftpserver.test.PortTestUtil

/**
 * Integration tests for restart of an StubFtpServer.
 *
 * @author Chris Mair
 */
class StubFtpServer_RestartTest extends AbstractGroovyTestCase {

    private static final String SERVER = "localhost"

    private StubFtpServer stubFtpServer = new StubFtpServer()
    private FTPClient ftpClient = new FTPClient()

    @Test
    void testRestart() {
        stubFtpServer.start()
        ftpClient.connect(SERVER, PortTestUtil.getFtpServerControlPort())
        assert ftpClient.changeWorkingDirectory("dir1")

        stubFtpServer.stop()
        LOG.info("Restarting...")

        stubFtpServer.start()
        ftpClient.connect(SERVER, PortTestUtil.getFtpServerControlPort())
        assert ftpClient.changeWorkingDirectory("dir1")
    }

    @BeforeEach
    void setUp() {
        stubFtpServer.setServerControlPort(PortTestUtil.getFtpServerControlPort())
    }

    @AfterEach
    void tearDown() {
        stubFtpServer.stop()
    }
}