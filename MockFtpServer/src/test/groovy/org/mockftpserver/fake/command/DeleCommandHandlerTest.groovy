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

/**
 * Tests for DeleCommandHandler
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class DeleCommandHandlerTest extends AbstractFakeCommandHandlerTest {

    def FILENAME = "f.txt"
    def FILE = "/" + FILENAME

    void testHandleCommand() {
        assert fileSystem.createFile(FILE)
        commandHandler.handleCommand(createCommand([FILE]), session)
        assertSessionReply(ReplyCodes.DELE_OK, ['dele', FILE])
        assert fileSystem.exists(FILE) == false
    }

    void testHandleCommand_PathIsRelative() {
        assert fileSystem.createFile(FILE)
        setCurrentDirectory("/")
        commandHandler.handleCommand(createCommand([FILENAME]), session)
        assertSessionReply(ReplyCodes.DELE_OK, ['dele', FILENAME])
        assert fileSystem.exists(FILE) == false
    }

    void testHandleCommand_PathDoesNotExistInFileSystem() {
        commandHandler.handleCommand(createCommand([FILE]), session)
        assertSessionReply(ReplyCodes.EXISTING_FILE_ERROR, ['filesystem.isNotAFile', FILE])
    }

    void testHandleCommand_PathSpecifiesADirectory() {
        assert fileSystem.createDirectory(FILE)
        commandHandler.handleCommand(createCommand([FILE]), session)
        assertSessionReply(ReplyCodes.EXISTING_FILE_ERROR, ['filesystem.isNotAFile', FILE])
        assert fileSystem.exists(FILE)
    }

    void testHandleCommand_MissingPathParameter() {
        testHandleCommand_MissingRequiredParameter([])
    }

    void testHandleCommand_DeleteThrowsException() {
        assert fileSystem.createFile(FILE)
        overrideMethodToThrowFileSystemException("delete")
        handleCommand([FILE])
        assertSessionReply(ReplyCodes.EXISTING_FILE_ERROR, ERROR_MESSAGE_KEY)
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    CommandHandler createCommandHandler() {
        new DeleCommandHandler()
    }

    Command createValidCommand() {
        return new Command(CommandNames.DELE, [FILE])
    }

}