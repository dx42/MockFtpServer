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
package org.mockftpserver.fake.command

import org.mockftpserver.core.command.Command
import org.mockftpserver.core.command.ReplyCodes
import org.mockftpserver.core.session.Session
import org.mockftpserver.fake.command.AbstractFakeCommandHandler

/**
 * CommandHandler for the HELP command. Handler logic:
 * <ol>
 *  <li>If the optional command-name parameter is specified, then reply with 214 along with the
 *      help text configured for that command (or empty if none)</li>
 *  <li>Otherwise, reply with 214 along with the configured default help text that has been configured
 *      (or empty if none)</li>
 * </ol>
 * @see org.mockftpserver.fake.server.ServerConfiguration
 * @see org.mockftpserver.fake.server.FakeFtpServer
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class HelpCommandHandler extends AbstractFakeCommandHandler {

    protected void handle(Command command, Session session) {
        def key = command.parameters.join(' ')
        def help = serverConfiguration.getHelpText(key)
        if (help == null) {
            sendReply(session, ReplyCodes.HELP_OK, 'help.noHelpTextDefined', [key])
        }
        else {
            sendReply(session, ReplyCodes.HELP_OK, 'help', [help])
        }
    }

}