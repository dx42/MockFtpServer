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
import org.mockftpserver.fake.filesystem.FileSystemException

/**
 * Tests for StouCommandHandler
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class StouCommandHandlerTest extends AbstractLoginRequiredCommandHandlerTest {

    def DIR = "/"
    def FILENAME = "file.txt"
    def FILE = p(DIR, FILENAME)
    def CONTENTS = "abc"

    void testHandleCommand_SpecifyBaseFilename() {
        handleCommand([FILENAME])
        testHandleCommand(FILENAME)
    }

    void testHandleCommand_UseDefaultBaseFilename() {
        handleCommand([])
        testHandleCommand('Temp')
    }

    void testHandleCommand_CreateOutputStreamThrowsException() {
        def newMethod = {String path, boolean append ->
            println "Calling createOutputStream() - throwing exception"
            throw new FileSystemException("bad")
        }
        overrideMethod(fileSystem, "createOutputStream", newMethod)

        handleCommand([])
        assertSessionReplies([ReplyCodes.TRANSFER_DATA_INITIAL_OK, ReplyCodes.NEW_FILE_ERROR])
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    CommandHandler createCommandHandler() {
        new StouCommandHandler()
    }

    Command createValidCommand() {
        return new Command(CommandNames.STOU, [])
    }

    void setUp() {
        super.setUp()
        assert fileSystem.createDirectory(DIR)
        setCurrentDirectory(DIR)
        session.dataToRead = CONTENTS.bytes
        serverConfiguration.setTextForKey('stou', 'STOU {0}')
    }

    private void testHandleCommand(String expectedBaseName) {
        assertSessionReply(0, ReplyCodes.TRANSFER_DATA_INITIAL_OK)
        assertSessionReply(1, ReplyCodes.TRANSFER_DATA_FINAL_OK, 'STOU')

        def names = fileSystem.listNames(DIR)
        def filename = names.find {name -> name.startsWith(expectedBaseName) }
        assert filename

        assert session.getReplyMessage(1).contains(filename)

        def absPath = p(DIR, filename)
        assert fileSystem.exists(absPath)
        def contents = fileSystem.createInputStream(absPath).text
        assert contents == CONTENTS
    }

}