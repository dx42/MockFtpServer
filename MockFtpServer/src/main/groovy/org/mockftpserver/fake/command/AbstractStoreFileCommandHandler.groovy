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
 * Abstract superclass for CommandHandlers that that store a file (STOR, STOU, APPE). Handler logic:
 * <ol>
 *  <li>If the user has not logged in, then reply with 530 and terminate</li>
 *  <li>If the pathname parameter is required but missing, then reply with 501 and terminate</li>
 *  <li>If the required pathname parameter does not specify a valid filename, then reply with 553 and terminate</li>
 *  <li>Send an initial reply of 150</li>
 *  <li>Read all available bytes from the data connection and store/append to the named file in the server file system</li>
 *  <li>If file write/store fails, then reply with 553 and terminate</li>
 *  <li>Send a final reply with 226</li>
 * </ol>
 *
 * @version $Revision: 78 $ - $Date: 2008-07-02 20:47:17 -0400 (Wed, 02 Jul 2008) $
 *
 * @author Chris Mair
 */
abstract class AbstractStoreFileCommandHandler extends AbstractFakeCommandHandler {

    protected void handle(Command command, Session session) {
        verifyLoggedIn(session)
        this.replyCodeForFileSystemException = ReplyCodes.NEW_FILE_ERROR

//        def filename = command.getRequiredParameter(0)
        def filename = getOutputFile(command)
        def path = getRealPath(session, filename)
        verifyFileSystemCondition(!fileSystem.isDirectory(path), path)
        def parent = fileSystem.getParent(path)
        verifyFileSystemCondition(fileSystem.isDirectory(parent), parent)

        sendReply(session, ReplyCodes.TRANSFER_DATA_INITIAL_OK)

        session.openDataConnection();
        def contents = session.readData()
        session.closeDataConnection();

        def out = fileSystem.createOutputStream(path, appendToOutputFile())
        out.withStream { it.write(contents) }
        sendReply(session, ReplyCodes.TRANSFER_DATA_FINAL_OK, getMessageKey(), [filename])
    }

    /**
     * Return the path (absolute or relative) for the output file. The default behavior is to return
     * the required first parameter for the specified Command. Subclasses may override the default behavior.
     */
    protected String getOutputFile(Command command) {
        command.getRequiredParameter(0)
    }

    /**
     * @return true if this command should append the transferred contents to the output file; false means
     *      overwrite an existing file. This default implentation returns false.
     */
    protected boolean appendToOutputFile() {
        return false
    }

    /**
     * @return the message key for the reply message sent with the final (226) reply
     */
    protected abstract String getMessageKey()

}