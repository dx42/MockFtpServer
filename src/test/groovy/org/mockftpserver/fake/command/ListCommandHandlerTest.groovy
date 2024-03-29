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
import org.mockftpserver.fake.filesystem.DirectoryEntry
import org.mockftpserver.fake.filesystem.DirectoryListingFormatter
import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.fake.filesystem.FileSystemEntry
import org.mockftpserver.fake.filesystem.FileSystemException
import org.mockftpserver.fake.filesystem.Permissions

/**
 * Tests for ListCommandHandler
 *
 * @author Chris Mair
 */
class ListCommandHandlerTest extends AbstractFakeCommandHandlerTestCase {

    private static final String DIR = "/usr"
    private static final String NAME = "abc.txt"
    private static final Date LAST_MODIFIED = new Date()

    @Test
    void testHandleCommand_SingleFile() {
        final entry = new FileEntry(path: p(DIR, NAME), lastModified: LAST_MODIFIED, contents: "abc")
        fileSystem.add(entry)
        handleCommandAndVerifySendDataReplies([DIR])
        assertSessionDataWithEndOfLine(listingFor(entry))
    }

    @Test
    void testHandleCommand_FilesAndDirectories() {
        def DATA3 = "".padRight(1000, 'x')
        final entry1 = new FileEntry(path: p(DIR, "abc.txt"), lastModified: LAST_MODIFIED, contents: "abc")
        final entry2 = new DirectoryEntry(path: p(DIR, "OtherFiles"), lastModified: LAST_MODIFIED)
        final entry3 = new FileEntry(path: p(DIR, "another_file.doc"), lastModified: LAST_MODIFIED, contents: DATA3)
        fileSystem.add(entry1)
        fileSystem.add(entry2)
        fileSystem.add(entry3)

        handleCommandAndVerifySendDataReplies([DIR])

        def actualLines = session.sentData[0].tokenize(endOfLine()) as Set
        LOG.info("actualLines=$actualLines")
        def EXPECTED = [
                listingFor(entry1),
                listingFor(entry2),
                listingFor(entry3)] as Set
        assert actualLines == EXPECTED
        assertSessionDataEndsWithEndOfLine()
    }

    @Test
    void testHandleCommand_NoPath_UseCurrentDirectory() {
        final entry = new FileEntry(path: p(DIR, NAME), lastModified: LAST_MODIFIED, contents: "abc")
        fileSystem.add(entry)
        session.setAttribute(SessionKeys.CURRENT_DIRECTORY, DIR)
        handleCommandAndVerifySendDataReplies([])
        assertSessionDataWithEndOfLine(listingFor(entry))
    }

    @Test
    void testHandleCommand_EmptyDirectory() {
        handleCommandAndVerifySendDataReplies([DIR])
        assertSessionData("")
    }

    @Test
    void testHandleCommand_PathSpecifiesAFile() {
        final entry = new FileEntry(path: p(DIR, NAME), lastModified: LAST_MODIFIED, contents: "abc")
        fileSystem.add(entry)
        handleCommandAndVerifySendDataReplies([p(DIR, NAME)])
        assertSessionDataWithEndOfLine(listingFor(entry))
    }

    @Test
    void testHandleCommand_PathDoesNotExist() {
        handleCommandAndVerifySendDataReplies(["/DoesNotExist"])
        assertSessionData("")
    }

    @Test
    void testHandleCommand_NoReadAccessToDirectory() {
        fileSystem.getEntry(DIR).permissions = new Permissions('-wx-wx-wx')
        handleCommand([DIR])
        assertSessionReply(0, ReplyCodes.READ_FILE_ERROR, ['filesystem.cannotRead', DIR])
    }

    @Test
    void testHandleCommand_ListFilesThrowsException() {
        fileSystem.listFilesMethodException = new FileSystemException("bad", ERROR_MESSAGE_KEY)
        handleCommand([DIR])
        assertSessionReply(0, ReplyCodes.SYSTEM_ERROR, ERROR_MESSAGE_KEY)
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    CommandHandler createCommandHandler() {
        new ListCommandHandler()
    }

    Command createValidCommand() {
        return new Command(CommandNames.LIST, [DIR])
    }

    @BeforeEach
    void setUp() {
        createDirectory(DIR)
        fileSystem.directoryListingFormatter = [format: {entry -> entry.toString()}] as DirectoryListingFormatter
    }

    private listingFor(FileSystemEntry fileSystemEntry) {
        fileSystemEntry.toString()
    }

}