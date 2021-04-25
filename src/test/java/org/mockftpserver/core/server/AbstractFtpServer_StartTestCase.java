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
package org.mockftpserver.core.server;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.test.*;
import org.mockftpserver.test.AbstractTestCase;

/**
 * Abstract superclass for tests of AbstractFtpServer subclasses that require the server thread to be started.
 *
 * @author Chris Mair
 */
public abstract class AbstractFtpServer_StartTestCase extends AbstractTestCase {

    private static final String SERVER = "localhost";

    private AbstractFtpServer ftpServer;

    @Test
    void testStartAndStop() throws Exception {
        ftpServer.setServerControlPort(PortTestUtil.getFtpServerControlPort());
        assertEquals("started - before", false, ftpServer.isStarted());

        ftpServer.start();
        Thread.sleep(200L);     // give it some time to get started
        assertEquals("started - after start()", true, ftpServer.isStarted());
        assertEquals("shutdown - after start()", false, ftpServer.isShutdown());

        ftpServer.stop();

        assertEquals("shutdown - after stop()", true, ftpServer.isShutdown());
    }

    @Test
    void testCustomServerControlPort() throws Exception {
        final int SERVER_CONTROL_PORT = 9187;

        ftpServer.setServerControlPort(SERVER_CONTROL_PORT);
        ftpServer.start();

        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(SERVER, SERVER_CONTROL_PORT);
        }
        finally {
            ftpServer.stop();
        }
    }

    //-------------------------------------------------------------------------
    // Test setup
    //-------------------------------------------------------------------------

    @BeforeEach
    void setUp() throws Exception {
        ftpServer = createFtpServer();
    }

    //-------------------------------------------------------------------------
    // Abstract method declarations
    //-------------------------------------------------------------------------

    protected abstract AbstractFtpServer createFtpServer();

}