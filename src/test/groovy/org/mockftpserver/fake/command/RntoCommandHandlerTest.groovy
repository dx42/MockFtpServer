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
import org.mockftpserver.core.command.CommandHandler
import org.mockftpserver.core.command.CommandNames
import org.mockftpserver.core.command.ReplyCodes
import org.mockftpserver.core.session.SessionKeys
import org.mockftpserver.fake.filesystem.FileSystemException

/**
 * Tests for RntoCommandHandler
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class RntoCommandHandlerTest extends AbstractFakeCommandHandlerTest {

    def FROM_FILE = "/from.txt"
    def TO_FILE = "/file.txt"

    void testHandleCommand() {
        assert fileSystem.createFile(FROM_FILE)
        commandHandler.handleCommand(createCommand([TO_FILE]), session)
        assertSessionReply(ReplyCodes.RNTO_OK, ['rnto', FROM_FILE, TO_FILE])
        assert !fileSystem.exists(FROM_FILE), FROM_FILE
        assert fileSystem.exists(TO_FILE), TO_FILE
        assert session.getAttribute(SessionKeys.RENAME_FROM) == null
    }

    void testHandleCommand_PathIsRelative() {
        assert fileSystem.createFile(FROM_FILE)
        session.setAttribute(SessionKeys.CURRENT_DIRECTORY, "/")
        commandHandler.handleCommand(createCommand(["file.txt"]), session)
        assertSessionReply(ReplyCodes.RNTO_OK, ['rnto', FROM_FILE, 'file.txt'])
        assert !fileSystem.exists(FROM_FILE), FROM_FILE
        assert fileSystem.exists(TO_FILE), TO_FILE
        assert session.getAttribute(SessionKeys.RENAME_FROM) == null
    }

    void testHandleCommand_FromFileNotSetInSession() {
        session.removeAttribute(SessionKeys.RENAME_FROM)
        testHandleCommand_MissingRequiredSessionAttribute()
    }

    void testHandleCommand_ToFilenameNotValid() {
        assert fileSystem.createFile(FROM_FILE)
        commandHandler.handleCommand(createCommand(["///"]), session)
        assertSessionReply(ReplyCodes.FILENAME_NOT_VALID, "///")
        assert session.getAttribute(SessionKeys.RENAME_FROM) == FROM_FILE
    }

    void testHandleCommand_ToFilenameSpecifiesADirectory() {
        assert fileSystem.createDirectory(TO_FILE)
        commandHandler.handleCommand(createCommand([TO_FILE]), session)
        assertSessionReply(ReplyCodes.NEW_FILE_ERROR, ['filesystem.isDirectory', TO_FILE])
        assert session.getAttribute(SessionKeys.RENAME_FROM) == FROM_FILE
    }

    void testHandleCommand_RenameFails() {
        commandHandler.handleCommand(createCommand([TO_FILE]), session)
        assertSessionReply(ReplyCodes.FILENAME_NOT_VALID, ['filesystem.renameFailed', TO_FILE])
        assert session.getAttribute(SessionKeys.RENAME_FROM) == FROM_FILE
    }

    void testHandleCommand_RenameThrowsException() {
        def newMethod = {String from, String to -> throw new FileSystemException("bad", 'msgkey') }
        overrideMethod(fileSystem, "rename", newMethod)

        commandHandler.handleCommand(createCommand([TO_FILE]), session)
        assertSessionReply(ReplyCodes.NEW_FILE_ERROR, ERROR_MESSAGE_KEY)
        assert session.getAttribute(SessionKeys.RENAME_FROM) == FROM_FILE
    }

    void testHandleCommand_MissingPathParameter() {
        testHandleCommand_MissingRequiredParameter([])
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    CommandHandler createCommandHandler() {
        new RntoCommandHandler()
    }

    Command createValidCommand() {
        return new Command(CommandNames.RNTO, [TO_FILE])
    }

    void setUp() {
        super.setUp()
        session.setAttribute(SessionKeys.RENAME_FROM, FROM_FILE)
    }

}