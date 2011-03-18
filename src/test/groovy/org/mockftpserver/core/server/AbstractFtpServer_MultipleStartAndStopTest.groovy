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

import org.mockftpserver.fake.FakeFtpServer
import org.mockftpserver.test.AbstractGroovyTestCase
import org.mockftpserver.test.PortTestUtil

/**
 * Test starting and stopping Abstract(Fake)FtpServer multiple times. 
 *
 * @version $Revision: 242 $ - $Date: 2010-03-21 07:41:01 -0400 (Sun, 21 Mar 2010) $
 *
 * @author Chris Mair
 */
class AbstractFtpServer_MultipleStartAndStopTest extends AbstractGroovyTestCase {

    private FakeFtpServer ftpServer = new FakeFtpServer()

    // Takes ~ 500ms per start/stop

    void testStartAndStop() {
        10.times {
            ftpServer.setServerControlPort(PortTestUtil.getFtpServerControlPort());

            ftpServer.start();
            Thread.sleep(100L);     // give it some time to get started
            assertEquals("started - after start()", true, ftpServer.isStarted());
            assertEquals("shutdown - after start()", false, ftpServer.isShutdown());

            ftpServer.stop();

            assertEquals("shutdown - after stop()", true, ftpServer.isShutdown());
        }
    }

    void tearDown() {
        super.tearDown()
        ftpServer.stop();   // just to be sure
    }
}
