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
import org.mockftpserver.fake.filesystem.FileSystemEntry
import org.mockftpserver.fake.filesystem.FileSystemException
import org.mockftpserver.fake.filesystem.Permissions

/**
 * Tests for MkdCommandHandler
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class MkdCommandHandlerTest extends AbstractFakeCommandHandlerTest {

    static final PARENT = '/'
    static final DIRNAME = "usr"
    static final DIR = p(PARENT, DIRNAME)

    void testHandleCommand() {
        commandHandler.handleCommand(createCommand([DIR]), session)
        assertSessionReply(ReplyCodes.MKD_OK, ['mkd', DIR])
        assert fileSystem.exists(DIR)
    }

    void testHandleCommand_PathIsRelative() {
        session.setAttribute(SessionKeys.CURRENT_DIRECTORY, '/')
        commandHandler.handleCommand(createCommand([DIRNAME]), session)
        assertSessionReply(ReplyCodes.MKD_OK, ['mkd', DIRNAME])
        assert fileSystem.exists(DIR)
    }

    void testHandleCommand_ParentDirectoryDoesNotExist() {
        commandHandler.handleCommand(createCommand(['/abc/def']), session)
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.doesNotExist', '/abc'])
    }

    void testHandleCommand_PathSpecifiesAFile() {
        createFile(DIR)
        commandHandler.handleCommand(createCommand([DIR]), session)
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.alreadyExists', DIR])
        assert fileSystem.exists(DIR)
    }

    void testHandleCommand_MissingPathParameter() {
        testHandleCommand_MissingRequiredParameter([])
    }

    void testHandleCommand_NoWriteAccessToParentDirectory() {
        fileSystem.getEntry(PARENT).permissions = new Permissions('r-xr-xr-x')
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.cannotWrite', PARENT])
    }

    void testHandleCommand_NoExecuteAccessToParentDirectory() {
        fileSystem.getEntry(PARENT).permissions = new Permissions('rw-rw-rw-')
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.cannotExecute', PARENT])
    }

    void testHandleCommand_CreateDirectoryThrowsException() {
        def newMethod = {FileSystemEntry entry -> throw new FileSystemException("Error thrown by method [$methodName]", ERROR_MESSAGE_KEY) }
        overrideMethod(fileSystem, "add", newMethod)

        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ERROR_MESSAGE_KEY)
    }

    void setUp() {
        super.setUp()
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