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


import org.junit.jupiter.api.Test
import org.mockftpserver.core.command.Command
import org.mockftpserver.core.command.CommandHandler
import org.mockftpserver.core.command.ReplyTextBundleAware
import org.mockftpserver.core.server.AbstractFtpServer
import org.mockftpserver.core.server.AbstractFtpServerTestCase
import org.mockftpserver.core.session.Session
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem

/**
 * Tests for FakeFtpServer.
 *
 * @author Chris Mair
 */
class FakeFtpServerTest extends AbstractFtpServerTestCase {

    private CommandHandler commandHandler = new TestCommandHandler()
    private CommandHandler commandHandler_NotServerConfigurationAware = new TestCommandHandlerNotServerConfigurationAware()

    //-------------------------------------------------------------------------
    // Extra tests  (Standard tests defined in superclass)
    //-------------------------------------------------------------------------

    @Test
    void testSetCommandHandler_NotServerConfigurationAware() {
        ftpServer.setCommandHandler("ZZZ", commandHandler_NotServerConfigurationAware)
        assert ftpServer.getCommandHandler("ZZZ") == commandHandler_NotServerConfigurationAware
    }

    @Test
    void testSetCommandHandler_ServerConfigurationAware() {
        ftpServer.setCommandHandler("ZZZ", commandHandler)
        assert ftpServer.getCommandHandler("ZZZ") == commandHandler
        assert ftpServer == commandHandler.serverConfiguration
    }

    @Test
    void testSetCommandHandler_ReplyTextBundleAware() {
        def cmdHandler = new TestCommandHandlerReplyTextBundleAware()
        ftpServer.setCommandHandler("ZZZ", cmdHandler)
        assert ftpServer.getCommandHandler("ZZZ") == cmdHandler
        assert ftpServer.replyTextBundle == cmdHandler.replyTextBundle
    }

    @Test
    void testUserAccounts() {
        def userAccount = new UserAccount(username: 'abc')

        // addUserAccount()
        ftpServer.addUserAccount(userAccount)
        assert ftpServer.getUserAccount("abc") == userAccount

        // setUserAccounts
        def userAccounts = [userAccount]
        ftpServer.userAccounts = userAccounts
        assert ftpServer.getUserAccount("abc") == userAccount
    }

    @Test
    void testHelpText() {
        ftpServer.helpText = [a: 'aaaaa', b: 'bbbbb', '': 'default']
        assert ftpServer.getHelpText('a') == 'aaaaa'
        assert ftpServer.getHelpText('b') == 'bbbbb'
        assert ftpServer.getHelpText('') == 'default'
        assert ftpServer.getHelpText('unrecognized') == null
    }

    @Test
    void testSystemName() {
        ftpServer.setFileSystem(new UnixFakeFileSystem())
        assert ftpServer.systemName == "UNIX"

        ftpServer.setFileSystem(new WindowsFakeFileSystem())
        assert ftpServer.systemName == "WINDOWS"

        // Can be overridden; takes precedence over the FileSystem value
        ftpServer.systemName = "abc"
        assert ftpServer.systemName == "abc"
    }

    @Test
    void testSystemStatus() {
        assert ftpServer.systemStatus == "Connected"
        ftpServer.systemStatus = "abc"
        assert ftpServer.systemStatus == "abc"
    }

    @Test
    void testReplyText() {
        ftpServer.replyTextBaseName = "SampleReplyText"

        ResourceBundle resourceBundle = ftpServer.replyTextBundle
        assert resourceBundle.getString("110") == "Testing123"
    }

    //-------------------------------------------------------------------------
    // Abstract method implementations
    //-------------------------------------------------------------------------

    protected AbstractFtpServer createFtpServer() {
        return new FakeFtpServer();
    }

    protected CommandHandler createCommandHandler() {
        return new TestCommandHandler();
    }

    protected void verifyCommandHandlerInitialized(CommandHandler commandHandler) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
class TestCommandHandlerReplyTextBundleAware implements CommandHandler, ReplyTextBundleAware {
    ResourceBundle replyTextBundle

    public void handleCommand(Command command, Session session) {
    }

}