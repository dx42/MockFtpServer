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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockftpserver.core.command.Command
import org.mockftpserver.core.command.CommandHandler
import org.mockftpserver.core.command.CommandNames
import org.mockftpserver.core.command.ReplyCodes
import org.mockftpserver.core.session.SessionKeys
import org.mockftpserver.fake.UserAccount
import org.mockftpserver.fake.filesystem.FileSystemException
import org.mockftpserver.fake.filesystem.Permissions

/**
 * Tests for MkdCommandHandler
 *
 * @author Chris Mair
 */
class MkdCommandHandlerTest extends AbstractFakeCommandHandlerTestCase {

    private static final String PARENT = '/'
    private static final String DIRNAME = "usr"
    private static final String DIR = p(PARENT, DIRNAME)
    private static final Permissions PERMISSIONS = new Permissions('rwx------')

    @Test
    void testHandleCommand() {
        userAccount.defaultPermissionsForNewDirectory = PERMISSIONS
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.MKD_OK, ['mkd', DIR])
        assert fileSystem.exists(DIR)
        def dirEntry = fileSystem.getEntry(DIR)
        assert dirEntry.permissions == PERMISSIONS
    }

    @Test
    void testHandleCommand_PathIsRelative() {
        session.setAttribute(SessionKeys.CURRENT_DIRECTORY, '/')
        handleCommand([DIRNAME])
        assertSessionReply(ReplyCodes.MKD_OK, ['mkd', DIRNAME])
        assert fileSystem.exists(DIR)
        def dirEntry = fileSystem.getEntry(DIR)
        assert dirEntry.permissions == UserAccount.DEFAULT_PERMISSIONS_FOR_NEW_DIRECTORY
    }

    @Test
    void testHandleCommand_ParentDirectoryDoesNotExist() {
        handleCommand(['/abc/def'])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.doesNotExist', '/abc'])
    }

    @Test
    void testHandleCommand_PathSpecifiesAFile() {
        createFile(DIR)
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.alreadyExists', DIR])
        assert fileSystem.exists(DIR)
    }

    @Test
    void testHandleCommand_MissingPathParameter() {
        testHandleCommand_MissingRequiredParameter([])
    }

    @Test
    void testHandleCommand_NoWriteAccessToParentDirectory() {
        fileSystem.getEntry(PARENT).permissions = new Permissions('r-xr-xr-x')
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.cannotWrite', PARENT])
    }

    @Test
    void testHandleCommand_NoExecuteAccessToParentDirectory() {
        fileSystem.getEntry(PARENT).permissions = new Permissions('rw-rw-rw-')
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.cannotExecute', PARENT])
    }

    @Test
    void testHandleCommand_CreateDirectoryThrowsException() {
        fileSystem.addMethodException = new FileSystemException("bad", ERROR_MESSAGE_KEY)
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ERROR_MESSAGE_KEY)
    }

    @BeforeEach
    void setUp() {
        createDirectory(PARENT)
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    CommandHandler createCommandHandler() {
        new MkdCommandHandler()
    }

    Command createValidCommand() {
        return new Command(CommandNames.MKD, [DIR])
    }

}