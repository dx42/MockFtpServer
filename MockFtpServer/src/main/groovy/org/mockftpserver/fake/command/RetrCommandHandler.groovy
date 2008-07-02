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
import org.mockftpserver.core.util.IoUtil
import org.mockftpserver.fake.command.AbstractFakeCommandHandler



/**
 * CommandHandler for the RETR command. Handler logic:
 * <ol>
 *  <li>If the user has not logged in, then reply with 530 and terminate</li>
 *  <li>If the required pathname parameter is missing, then reply with 501 and terminate</li>
 *  <li>Send an initial reply of 150</li>
 *  <li>If the pathname parameter does not specify a valid, existing filename, then reply with 550 and terminate</li>
 *  <li>Send the contents of the named file across the data connection</li>
 *  <li>If there is an error reading the file, then reply with 550 and terminate</li>
 *  <li>Send a final reply with 226</li>
 * </ol>
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class RetrCommandHandler extends AbstractFakeCommandHandler {

    protected void handle(Command command, Session session) {
        verifyLoggedIn(session)
        this.replyCodeForFileSystemException = ReplyCodes.EXISTING_FILE_ERROR

        def path = getRealPath(session, command.getRequiredParameter(0))
        verifyFileSystemCondition(fileSystem.isFile(path), path)

        sendReply(session, ReplyCodes.SEND_DATA_INITIAL_OK)
        def input = fileSystem.createInputStream(path)
        session.openDataConnection();
        input.withStream {
            def bytes = IoUtil.readBytes(it)
            session.sendData(bytes, bytes.length)
        }
        session.closeDataConnection();
        sendReply(session, ReplyCodes.SEND_DATA_FINAL_OK)
    }

}