package org.mockftpserver.fake

import org.apache.commons.net.ftp.FTPClient
import org.mockftpserver.test.AbstractGroovyTestCase
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

/*
* Copyright 2014 the original author or authors.
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

class FakeFtpServer_LoadTest extends AbstractGroovyTestCase {

    static final SERVER = "localhost"
    static final PORT = 9981
    static final USERNAME = 'joe'           // Must match Spring config
    static final PASSWORD = 'password'      // Must match Spring config 

    private FakeFtpServer fakeFtpServer
    private long clientIndex = 0L

    //--------------------------------------------------------------------------
    // Tests
    //--------------------------------------------------------------------------

    void testLotsOfClientConnections() {
        startFtpServer('fakeftpserver-beans.xml')

        final long NUM_CLIENTS = 50L
        //final long NUM_CLIENTS = 500000L
        NUM_CLIENTS.times {
            startClientSession()
        }
    }

    void testLotsOfClientConnections_LocalPassiveMode() {
        startFtpServer('fakeftpserver-beans.xml')

        final long NUM_CLIENTS = 50L
        //final long NUM_CLIENTS = 500000L
        NUM_CLIENTS.times {
            startClientSession_LocalPassiveMode()
        }
    }

    void testLotsOfClientConnections_NoQuit() {
        startFtpServer('fakeftpserver-beans.xml')

        final long NUM_CLIENTS = 50L
        //final long NUM_CLIENTS = 500000L
        NUM_CLIENTS.times {
            startClientSession_NoQuit()
        }
    }

    //--------------------------------------------------------------------------
    // Setup and tear-down and helper methods
    //--------------------------------------------------------------------------

    void setUp() {
        super.setUp()
    }

    void tearDown() {
        super.tearDown()
        fakeFtpServer?.stop()
    }

    private void startFtpServer(String springConfigFile) {
        ApplicationContext context = new ClassPathXmlApplicationContext(springConfigFile)
        fakeFtpServer = (FakeFtpServer) context.getBean("fakeFtpServer")
        fakeFtpServer.start()
    }

    private void startClientSession() {
        doClientSession { ftpClient ->
            String dir = ftpClient.printWorkingDirectory()
            assert dir == '/'
            ftpClient.quit()
        }
        assert fakeFtpServer.numberOfSessions() < 5
    }

    private void startClientSession_LocalPassiveMode() {
        doClientSession { ftpClient ->
            ftpClient.enterLocalPassiveMode()
            ftpClient.listFiles();
            ftpClient.quit();
            ftpClient.disconnect();
        }
        assert fakeFtpServer.numberOfSessions() < 5
    }

    private void startClientSession_NoQuit() {
        doClientSession { ftpClient ->
            ftpClient.setSoTimeout(100)
            ftpClient.enterLocalPassiveMode()
            ftpClient.printWorkingDirectory()
            //ftpClient.quit()
            ftpClient.disconnect();
        }
    }

    private void doClientSession(Closure closure) {
        long index = clientIndex++
        log("Starting client #$index")

        FTPClient ftpClient = new FTPClient()
        ftpClient.connect(SERVER, PORT)
        assert ftpClient.login(USERNAME, PASSWORD)

        closure(ftpClient)

        log("Finished client #$index")
    }

}