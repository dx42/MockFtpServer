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
 * CommandHandler for the LIST command. Handler logic:
 * <ol>
 *  <li>If the user has not logged in, then reply with 530 and terminate</li>
 *  <li>Send an initial reply of 150</li>
 *  <li>If the optional pathname parameter is missing, then send a directory listing for 
 *  		the current directory across the data connection</li>
 *  <li>Otherwise, if the optional pathname parameter specifies a directory or group of files, 
 *  		then send a directory listing for the specified directory across the data connection</li>
 *  <li>Otherwise, if the optional pathname parameter specifies a filename, then send information 
 *  		for the specified file across the data connection</li>
 *  <li>Send a final reply with 226</li>
 * </ol>
 * 
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
class ListCommandHandler extends AbstractFakeCommandHandler {

    protected void handle(Command command, Session session) {
        verifyLoggedIn(session)
        sendReply(session, ReplyCodes.SEND_DATA_INITIAL_OK)

        // TODO ...
        
        sendReply(session, ReplyCodes.SEND_DATA_FINAL_OK)

//        def path = getRequiredParameter(command)
//
//        verifyForExistingFile(fileSystem.exists(path), path)
//        verifyForExistingFile(fileSystem.isDirectory(path), path)
//
//        session.setAttribute(SessionKeys.CURRENT_DIRECTORY, path)
//        sendReply(session, ReplyCodes.CWD_OK, [path])
    }

}