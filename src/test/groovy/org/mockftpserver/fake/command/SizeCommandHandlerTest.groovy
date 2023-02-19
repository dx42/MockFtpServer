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

/**
 * Tests for SizeCommandHandler
 *
 * @author Edoardo Luppi
 */
class SizeCommandHandlerTest extends AbstractFakeCommandHandlerTestCase {
    private static final String DIR = '/usr'
    private static final String NAME = 'abc.txt'
    private static final String FILE = p(DIR, NAME);
    private static final String CONTENTS = 'abc-def-ghi'

    @Test
    void testHandleCommand_FileSizeOk() {
        def bytes = CONTENTS.getBytes()
        handleCommand([FILE])
        assertSessionReply(ReplyCodes.SIZE_OK, ['size', bytes.length.toString()])
    }

    @Test
    void testHandleCommand_PathDoesNotExist() {
        def path = '/path/DoesNotExist.txt'
        handleCommand([path])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.doesNotExist', path])
    }

    @Test
    void testHandleCommand_IsNotAFile() {
        handleCommand([DIR])
        assertSessionReply(ReplyCodes.READ_FILE_ERROR, ['filesystem.doesNotExist', DIR])
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    CommandHandler createCommandHandler() {
        new SizeCommandHandler()
    }

    Command createValidCommand() {
        return new Command(CommandNames.SIZE, [FILE])
    }

    @BeforeEach
    void setUp() {
        createDirectory(DIR)
        createFile(FILE, CONTENTS)
    }
}
