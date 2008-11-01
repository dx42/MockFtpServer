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

import org.mockftpserver.core.command.CommandHandler
import org.mockftpserver.core.command.CommandNames
import org.mockftpserver.core.server.AbstractFtpServer
import org.mockftpserver.fake.command.*
import org.mockftpserver.fake.filesystem.FileSystem
import org.mockftpserver.fake.server.ServerConfiguration
import org.mockftpserver.fake.server.ServerConfigurationAware
import org.mockftpserver.fake.user.UserAccount

/**
 * "Fake" implementation of an FTP server.
 *
 *      T B D
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class FakeFtpServer extends AbstractFtpServer implements ServerConfiguration {

    FileSystem fileSystem
    String systemName = "WINDOWS"
    Map helpText = [:]

    private Map userAccounts = [:]

    FakeFtpServer() {
        setCommandHandler(CommandNames.ACCT, new AcctCommandHandler())
        setCommandHandler(CommandNames.ABOR, new AborCommandHandler())
        setCommandHandler(CommandNames.ALLO, new AlloCommandHandler())
        setCommandHandler(CommandNames.APPE, new AppeCommandHandler())
        setCommandHandler(CommandNames.CONNECT, new ConnectCommandHandler())
        setCommandHandler(CommandNames.CWD, new CwdCommandHandler())
        setCommandHandler(CommandNames.CDUP, new CdupCommandHandler())
        setCommandHandler(CommandNames.DELE, new DeleCommandHandler())
        setCommandHandler(CommandNames.HELP, new HelpCommandHandler())
        setCommandHandler(CommandNames.LIST, new ListCommandHandler())
        setCommandHandler(CommandNames.MKD, new MkdCommandHandler())
        setCommandHandler(CommandNames.MODE, new ModeCommandHandler())
        setCommandHandler(CommandNames.NLST, new NlstCommandHandler())
        setCommandHandler(CommandNames.NOOP, new NoopCommandHandler())
        setCommandHandler(CommandNames.PASS, new PassCommandHandler())
        setCommandHandler(CommandNames.PASV, new PasvCommandHandler())
        setCommandHandler(CommandNames.PWD, new PwdCommandHandler())
        setCommandHandler(CommandNames.PORT, new PortCommandHandler())
        setCommandHandler(CommandNames.QUIT, new QuitCommandHandler())
        setCommandHandler(CommandNames.REIN, new ReinCommandHandler())
        setCommandHandler(CommandNames.REST, new RestCommandHandler())
        setCommandHandler(CommandNames.RETR, new RetrCommandHandler())
        setCommandHandler(CommandNames.RMD, new RmdCommandHandler())
        setCommandHandler(CommandNames.RNFR, new RnfrCommandHandler())
        setCommandHandler(CommandNames.RNTO, new RntoCommandHandler())
        setCommandHandler(CommandNames.SITE, new SiteCommandHandler())
        setCommandHandler(CommandNames.STOR, new StorCommandHandler())
        setCommandHandler(CommandNames.STOU, new StouCommandHandler())
        setCommandHandler(CommandNames.STRU, new StruCommandHandler())
        setCommandHandler(CommandNames.SYST, new SystCommandHandler())
        setCommandHandler(CommandNames.TYPE, new TypeCommandHandler())
        setCommandHandler(CommandNames.USER, new UserCommandHandler())
        setCommandHandler(CommandNames.XPWD, new PwdCommandHandler())
    }

    /**
     * Initialize a CommandHandler that has been registered to this server. If the CommandHandler implements
     * the <code>ServerConfigurationAware</code> interface, then set its <code>ServerConfiguration</code>
     * property to <code>this</code>.
     *
     * @param commandHandler - the CommandHandler to initialize
     */
    protected void initializeCommandHandler(CommandHandler commandHandler) {
        if (commandHandler instanceof ServerConfigurationAware) {
            commandHandler.serverConfiguration = this
        }
    }

    /**
     * @return the {@link UserAccount}        configured for this server for the specified user name
     */
    public UserAccount getUserAccount(String username) {
        userAccounts[username]
    }

    /**
     * Return the help text for a command or the default help text if no command name is specified
     * @param name - the command name; may be empty or null to indicate  a request for the default help text
     * @return the help text for the named command or the default help text if no name is supplied
     */
    String getHelpText(String name) {
        def key = name == null ? '' : name
        return helpText[key];
    }

    /**
     * Add the UserAccount objects in the <code>userAccountList</code> to the set of UserAccounts.
     * @param userAccountList - the List of UserAccount objects to add
     */
    void setUserAccounts(List userAccountList) {
        userAccountList.each {userAccount -> userAccounts[userAccount.username] = userAccount }
    }

}