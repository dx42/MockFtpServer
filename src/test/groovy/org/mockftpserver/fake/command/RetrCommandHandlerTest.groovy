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
import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.core.command.ReplyCodes

/**
 * Tests for RetrCommandHandler
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class RetrCommandHandlerTest extends AbstractLoginRequiredCommandHandlerTest {

    def DIR = "/"
    def FILENAME = "file.txt"
    def FILE = p(DIR, FILENAME)
    def CONTENTS = "abc"

    void testHandleCommand_MissingPathParameter() {
        testHandleCommand_MissingRequiredParameter([])
    }

    void testHandleCommand_AbsolutePath() {
        handleCommandAndVerifySendDataReplies([FILE])
        assertSessionData(CONTENTS)
    }

    void testHandleCommand_RelativePath() {
        setCurrentDirectory(DIR)
        handleCommandAndVerifySendDataReplies([FILENAME])
        assertSessionData(CONTENTS)
    }

    void testHandleCommand_PathSpecifiesAnExistingDirectory() {
        commandHandler.handleCommand(createCommand([DIR]), session)
        assertSessionReply(ReplyCodes.EXISTING_FILE_ERROR, DIR)
    }

    void testHandleCommand_CreateInputStreamThrowsException() {
        overrideMethodToThrowFileSystemException("createInputStream")
        handleCommand([FILE])
        assertSessionReplies([ReplyCodes.SEND_DATA_INITIAL_OK, ReplyCodes.EXISTING_FILE_ERROR])
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    CommandHandler createCommandHandler() {
        new RetrCommandHandler()
    }

    Command createValidCommand() {
        return new Command(CommandNames.RETR, [FILE])
    }

    void setUp() {
        super.setUp()
        assert fileSystem.createDirectory(DIR)
        fileSystem.addEntry(new FileEntry(path: FILE, contents: CONTENTS))
    }

}