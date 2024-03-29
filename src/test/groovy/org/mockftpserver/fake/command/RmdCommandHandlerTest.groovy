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
import org.mockftpserver.core.session.SessionKeys
import org.mockftpserver.fake.filesystem.FileSystemException
import org.mockftpserver.fake.filesystem.Permissions

/**
 * Tests for RmdCommandHandler
 *
 * @author Chris Mair
 */
class RmdCommandHandlerTest extends AbstractFakeCommandHandlerTestCase {

    private static final String PARENT = '/'
    private static final String DIR = p(PARENT, "usr")

    @Test
    void testHandleCommand() {
        createDirectory(DIR)
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.RMD_OK, ['rmd', DIR])
        assert fileSystem.exists(DIR) == false
    }

    @Test
    void testHandleCommand_PathIsRelative() {
        def SUB = "sub"
        createDirectory(p(DIR, SUB))
        session.setAttribute(SessionKeys.CURRENT_DIRECTORY, DIR)
        handleCommand([SUB])
        assertSessionReply(ReplyCodes.RMD_OK, ['rmd', SUB])
        assert fileSystem.exists(p(DIR, SUB)) == false
    }

    @Test
    void testHandleCommand_PathDoesNotExistInFileSystem() {
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.doesNotExist', DIR])
    }

    @Test
    void testHandleCommand_PathSpecifiesAFile() {
        createFile(DIR)
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.isNotADirectory', DIR])
        assert fileSystem.exists(DIR)
    }

    @Test
    void testHandleCommand_DirectoryIsNotEmpty() {
        final FILE = DIR + "/file.txt"
        createFile(FILE)
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.directoryIsNotEmpty', DIR])
        assert fileSystem.exists(DIR)
        assert fileSystem.exists(FILE)
    }

    @Test
    void testHandleCommand_MissingPathParameter() {
        testHandleCommand_MissingRequiredParameter([])
    }

    @Test
    void testHandleCommand_ListNamesThrowsException() {
        createDirectory(DIR)
        fileSystem.listNamesMethodException = new FileSystemException("bad", ERROR_MESSAGE_KEY)
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ERROR_MESSAGE_KEY)
    }

    @Test
    void testHandleCommand_DeleteThrowsException() {
        createDirectory(DIR)
        fileSystem.deleteMethodException = new FileSystemException("bad", ERROR_MESSAGE_KEY)
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ERROR_MESSAGE_KEY)
    }

    @Test
    void testHandleCommand_NoWriteAccessToParentDirectory() {
        createDirectory(DIR)
        fileSystem.getEntry(PARENT).permissions = new Permissions('r-xr-xr-x')
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.cannotWrite', PARENT])
        assert fileSystem.exists(DIR)
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    CommandHandler createCommandHandler() {
        new RmdCommandHandler()
    }

    Command createValidCommand() {
        return new Command(CommandNames.RMD, [DIR])
    }

}