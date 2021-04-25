/*
 * Copyright 2011 the original author or authors.
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
package org.mockftpserver.core.server

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockftpserver.fake.FakeFtpServer
import org.mockftpserver.test.AbstractGroovyTestCase
import org.mockftpserver.test.PortTestUtil

/**
 * Test starting and stopping Abstract(Fake)FtpServer multiple times. 
 *
 * @author Chris Mair
 */
class AbstractFtpServer_MultipleStartAndStopTest extends AbstractGroovyTestCase {

    private FakeFtpServer ftpServer = new FakeFtpServer()

    // Takes ~ 500ms per start/stop

    @Test
    void testStartAndStop() {
        10.times {
            final def port = PortTestUtil.getFtpServerControlPort()
            ftpServer.setServerControlPort(port);

            ftpServer.start();
            assert ftpServer.getServerControlPort() == port
            Thread.sleep(100L);     // give it some time to get started
            assert ftpServer.isStarted()
            assert !ftpServer.isShutdown()

            ftpServer.stop();

            assert ftpServer.isShutdown()
        }
    }

    @Test
    void testStartAndStop_UseDynamicFreePort() {
        5.times {
            ftpServer.setServerControlPort(0);
            assert ftpServer.getServerControlPort() == 0

            ftpServer.start();
            log("Using port ${ftpServer.getServerControlPort()}")
            assert ftpServer.getServerControlPort() != 0

            ftpServer.stop();
        }
    }

    @AfterEach
    void tearDown() {
        ftpServer.stop();   // just to be sure
    }
}
