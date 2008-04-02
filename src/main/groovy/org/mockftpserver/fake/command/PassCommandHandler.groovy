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

import org.mockftpserver.fake.command.AbstractFakeCommandHandlerimport org.mockftpserver.core.command.Commandimport org.mockftpserver.core.session.Sessionimport org.mockftpserver.core.session.SessionKeys
import org.mockftpserver.core.command.ReplyCodes

/**
 * CommandHandler for the PASS command
 * 
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
class PassCommandHandler extends AbstractFakeCommandHandler {

    protected void handle(Command command, Session session) {
        def password = getRequiredParameter(command, 0)
        def username = getRequiredSessionAttribute(session, SessionKeys.USERNAME)
        
        def userAccount = serverConfiguration.getUserAccount(username)
        if (userAccount == null) {
            sendReply(session, ReplyCodes.PASS_LOG_IN_FAILED)
            return
        }
        
        if (userAccount.isValidPassword(password)) {
            sendReply(session, ReplyCodes.PASS_OK)
            session.setAttribute(SessionKeys.USER_ACCOUNT, userAccount)
        }
        else {
            sendReply(session, ReplyCodes.PASS_LOG_IN_FAILED)
        }
    }

}