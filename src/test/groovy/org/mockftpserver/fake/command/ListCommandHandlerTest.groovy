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
import org.mockftpserver.fake.filesystem.DirectoryEntry
import org.mockftpserver.fake.filesystem.DirectoryListingFormatter
import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.fake.filesystem.FileInfo

/**
 * Tests for ListCommandHandler
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class ListCommandHandlerTest extends AbstractLoginRequiredCommandHandlerTest {

    private static final DIR = "/usr"
    private static final NAME = "abc.txt"
    private static final LAST_MODIFIED = new Date()

    void testHandleCommand_SingleFile() {
        fileSystem.addEntry(new FileEntry(path: p(DIR, NAME), lastModified: LAST_MODIFIED, contents: "abc"))
        handleCommandAndVerifySendDataReplies([DIR])
        assertSessionData(listingFor(FileInfo.forFile(NAME, 3, LAST_MODIFIED)))
    }

    void testHandleCommand_FilesAndDirectories() {
        def NAME1 = "abc.txt"
        def NAME2 = "OtherFiles"
        def NAME3 = "another_file.doc"
        def DATA1 = "abc"
        def DATA3 = "".padRight(1000, 'x')
        fileSystem.addEntry(new FileEntry(path: p(DIR, NAME1), lastModified: LAST_MODIFIED, contents: DATA1))
        fileSystem.addEntry(new DirectoryEntry(path: p(DIR, NAME2), lastModified: LAST_MODIFIED))
        fileSystem.addEntry(new FileEntry(path: p(DIR, NAME3), lastModified: LAST_MODIFIED, contents: DATA3))

        handleCommandAndVerifySendDataReplies([DIR])

        def actualLines = session.sentData[0].tokenize(endOfLine()) as Set
        LOG.info("actualLines=$actualLines")
        def EXPECTED = [
                listingFor(FileInfo.forFile(NAME1, DATA1.size(), LAST_MODIFIED)),
                listingFor(FileInfo.forDirectory(NAME2, LAST_MODIFIED)),
                listingFor(FileInfo.forFile(NAME3, DATA3.size(), LAST_MODIFIED))] as Set
        assert actualLines == EXPECTED
    }

    void testHandleCommand_NoPath_UseCurrentDirectory() {
        fileSystem.addEntry(new FileEntry(path: p(DIR, NAME), lastModified: LAST_MODIFIED, contents: "abc"))
        session.setAttribute(SessionKeys.CURRENT_DIRECTORY, DIR)
        handleCommandAndVerifySendDataReplies([])
        assertSessionData(listingFor(FileInfo.forFile(NAME, 3, LAST_MODIFIED)))
    }

    void testHandleCommand_EmptyDirectory() {
        handleCommandAndVerifySendDataReplies([DIR])
        assertSessionData("")
    }

    void testHandleCommand_PathSpecifiesAFile() {
        fileSystem.addEntry(new FileEntry(path: p(DIR, NAME), lastModified: LAST_MODIFIED, contents: "abc"))
        handleCommandAndVerifySendDataReplies([p(DIR, NAME)])
        assertSessionData(listingFor(FileInfo.forFile(NAME, 3, LAST_MODIFIED)))
    }

    void testHandleCommand_PathDoesNotExist() {
        handleCommandAndVerifySendDataReplies(["/DoesNotExist"])
        assertSessionData("")
    }

    void testHandleCommand_ListFilesThrowsException() {
        overrideMethodToThrowFileSystemException("listFiles")
        handleCommand([DIR])
        assertSessionReplies([ReplyCodes.TRANSFER_DATA_INITIAL_OK, ReplyCodes.SYSTEM_ERROR])
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

    void setUp() {
        super.setUp()
        assert fileSystem.createDirectory("/usr")
        fileSystem.directoryListingFormatter = [format: {fileInfo -> fileInfo.toString()}] as DirectoryListingFormatter
    }

    private listingFor(FileInfo fileInfo) {
        fileInfo.toString()
    }

}