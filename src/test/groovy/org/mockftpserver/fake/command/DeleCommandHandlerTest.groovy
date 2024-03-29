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

import org.junit.jupiter.api.Test
import org.mockftpserver.core.command.Command
import org.mockftpserver.core.command.CommandHandler
import org.mockftpserver.core.command.CommandNames
import org.mockftpserver.core.command.ReplyCodes
import org.mockftpserver.fake.filesystem.FileSystemException
import org.mockftpserver.fake.filesystem.Permissions

/**
 * Tests for DeleCommandHandler
 *
 * @author Chris Mair
 */
class DeleCommandHandlerTest extends AbstractFakeCommandHandlerTestCase {

    private static final String DIR = '/'
    private static final String FILENAME = "f.txt"
    private static final String FILE = p(DIR, FILENAME)

    @Test
    void testHandleCommand() {
        createFile(FILE)
        handleCommand([FILE])
        assertSessionReply(ReplyCodes.DELE_OK, ['dele', FILE])
        assert fileSystem.exists(FILE) == false
    }

    @Test
    void testHandleCommand_PathIsRelative() {
        createFile(FILE)
        setCurrentDirectory("/")
        handleCommand([FILENAME])
        assertSessionReply(ReplyCodes.DELE_OK, ['dele', FILENAME])
        assert fileSystem.exists(FILE) == false
    }

    @Test
    void testHandleCommand_PathDoesNotExistInFileSystem() {
        handleCommand([FILE])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.isNotAFile', FILE])
    }

    @Test
    void testHandleCommand_PathSpecifiesADirectory() {
        createDirectory(FILE)
        handleCommand([FILE])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.isNotAFile', FILE])
        assert fileSystem.exists(FILE)
    }

    @Test
    void testHandleCommand_MissingPathParameter() {
        testHandleCommand_MissingRequiredParameter([])
    }

    @Test
    void testHandleCommand_DeleteThrowsException() {
        createFile(FILE)
//        overrideMethodToThrowFileSystemException("delete")
        fileSystem.deleteMethodException = new FileSystemException("bad", ERROR_MESSAGE_KEY)
        handleCommand([FILE])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ERROR_MESSAGE_KEY)
    }

    @Test
    void testHandleCommand_NoWriteAccessToParentDirectory() {
        createFile(FILE)
        fileSystem.getEntry(DIR).permissions = new Permissions('r-xr-xr-x')
        handleCommand([FILE])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.cannotWrite', DIR])
        assert fileSystem.exists(FILE)
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