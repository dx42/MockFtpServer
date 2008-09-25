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
package org.mockftpserver.fake.filesystem

import org.mockftpserver.core.util.IoUtil
import org.mockftpserver.test.AbstractGroovyTest

/**
 * Abstract superclass for tests of FileSystem implementation classes. Contains common
 * tests and test infrastructure. 
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
abstract class AbstractFileSystemTest extends AbstractGroovyTest {

    public static final FILENAME1 = "file1.txt"
    public static final FILENAME2 = "file2.txt"
    public static final DIR1 = "dir1"
    public static final NEW_DIRNAME = "testdir"
    public static final ILLEGAL_FILE = "xx/yy////z!<>?*z.txt"
    public static final EXISTING_FILE_CONTENTS = "abc 123 %^& xxx"
    public static final DATE = new Date()

    // These must be set by the concrete subclass (in its constructor)
    protected String NEW_DIR = null
    protected String NEW_FILE = null
    protected String EXISTING_DIR = null
    protected String EXISTING_FILE = null
    protected NO_SUCH_DIR = null
    protected NO_SUCH_FILE = null

    protected FileSystem fileSystem

    //-------------------------------------------------------------------------
    // Common Tests
    //-------------------------------------------------------------------------

    void testExists() {
        assert !fileSystem.exists(NEW_FILE)
        assert !fileSystem.exists(NEW_DIR)
        assert !fileSystem.exists(ILLEGAL_FILE)
        assert fileSystem.exists(EXISTING_FILE)
        assert fileSystem.exists(EXISTING_DIR)

        shouldFailWithMessageContaining("path") { fileSystem.exists(null) }
    }

    void testIsDirectory() {
        assert fileSystem.isDirectory(EXISTING_DIR)
        assert !fileSystem.isDirectory(EXISTING_FILE)
        assert !fileSystem.isDirectory(NO_SUCH_DIR)
        assert !fileSystem.isDirectory(NO_SUCH_FILE)
        assert !fileSystem.isDirectory(ILLEGAL_FILE)

        shouldFailWithMessageContaining("path") { fileSystem.isDirectory(null) }
    }

    void testIsFile() {
        assert fileSystem.isFile(EXISTING_FILE)
        assert !fileSystem.isFile(EXISTING_DIR)
        assert !fileSystem.isFile(NO_SUCH_DIR)
        assert !fileSystem.isFile(NO_SUCH_FILE)
        assert !fileSystem.isFile(ILLEGAL_FILE)

        shouldFailWithMessageContaining("path") { fileSystem.isFile(null) }
    }

    void testCreateDirectory() {
        assert !fileSystem.exists(NEW_DIR), "Before createDirectory"
        assert fileSystem.createDirectory(NEW_DIR)
        assert fileSystem.exists(NEW_DIR), "After createDirectory"

        // Duplicate directory
        assert !fileSystem.createDirectory(NEW_DIR), "Duplicate directory"

        // The parent of the path does not exist
        shouldFail(FileSystemException) { fileSystem.createDirectory(NEW_DIR + "/abc/def") }

        shouldFailWithMessageContaining("path") { fileSystem.createDirectory(null) }
    }

    void testCreateFile() {
        assert !fileSystem.exists(NEW_FILE), "Before createFile"
        assert fileSystem.createFile(NEW_FILE)
        assert fileSystem.exists(NEW_FILE), "After createFile"

        assert !fileSystem.createFile(NEW_FILE), "Duplicate"

        // The parent of the path does not exist
        shouldFail(FileSystemException) { fileSystem.createFile(NEW_DIR + "/abc/def") }

        shouldFail(FileSystemException) { fileSystem.createFile(NO_SUCH_DIR) }
        shouldFail(InvalidFilenameException) { fileSystem.createFile(ILLEGAL_FILE) }

        shouldFailWithMessageContaining("path") { fileSystem.createFile(null) }
    }

    void testCreateInputStream() {
        InputStream input = fileSystem.createInputStream(EXISTING_FILE)
        assert EXISTING_FILE_CONTENTS.getBytes() == IoUtil.readBytes(input)

        shouldFail(FileSystemException) { fileSystem.createInputStream(NO_SUCH_FILE) }
        shouldFail(FileSystemException) { fileSystem.createInputStream(EXISTING_DIR) }
        shouldFail(FileSystemException) { fileSystem.createInputStream("") }

        shouldFailWithMessageContaining("path") { fileSystem.createInputStream(null) }
    }

    void testCreateOutputStream() {
        // New, empty file
        OutputStream out = fileSystem.createOutputStream(NEW_FILE, false)
        out.close()
        verifyFileContents(fileSystem, NEW_FILE, "")

        // Append = false
        out = fileSystem.createOutputStream(NEW_FILE, false)
        out.write(EXISTING_FILE_CONTENTS.getBytes())
        out.close()
        verifyFileContents(fileSystem, NEW_FILE, EXISTING_FILE_CONTENTS)

        // Append = true
        out = fileSystem.createOutputStream(NEW_FILE, true)
        out.write(EXISTING_FILE_CONTENTS.getBytes())
        String expectedContents = EXISTING_FILE_CONTENTS.concat(EXISTING_FILE_CONTENTS)
        verifyFileContents(fileSystem, NEW_FILE, expectedContents)

        // Yet another OutputStream, append=true (so should append to accumulated contents)
        OutputStream out2 = fileSystem.createOutputStream(NEW_FILE, true)
        out2.write("abc".getBytes())
        out2.close()
        expectedContents = expectedContents + "abc"
        verifyFileContents(fileSystem, NEW_FILE, expectedContents)

        // Write with the previous OutputStream (simulate 2 OututStreams writing "concurrently")
        out.write("def".getBytes())
        out.close()
        expectedContents = expectedContents + "def"
        verifyFileContents(fileSystem, NEW_FILE, expectedContents)
    }

    void testCreateOutputStream_FileSystemException() {
        // Parent directory does not exist
        shouldFail(FileSystemException) { fileSystem.createOutputStream(NEW_DIR + "/abc.txt", true) }

        shouldFail(FileSystemException) { fileSystem.createOutputStream(EXISTING_DIR, true) }
        shouldFail(InvalidFilenameException) { fileSystem.createOutputStream(ILLEGAL_FILE, true) }
        shouldFail(FileSystemException) { fileSystem.createOutputStream("", true) }
    }

    void testCreateOutputStream_NullPath() {
        shouldFailWithMessageContaining("path") { fileSystem.createOutputStream(null, true) }
    }

    void testRename_NullFromPath() {
        shouldFailWithMessageContaining("fromPath") { fileSystem.rename(null, FILENAME1) }
    }

    void testRename_NullToPath() {
        shouldFailWithMessageContaining("toPath") { fileSystem.rename(FILENAME1, null) }
    }

    void testListNames() {
        assert fileSystem.createDirectory(NEW_DIR)
        assert fileSystem.listNames(NEW_DIR) == []

        assert fileSystem.createFile(p(NEW_DIR, FILENAME1))
        assert fileSystem.createFile(p(NEW_DIR, FILENAME2))
        assert fileSystem.createDirectory(p(NEW_DIR, DIR1))
        assert fileSystem.createFile(p(NEW_DIR, DIR1, "/abc.def"))

        List filenames = fileSystem.listNames(NEW_DIR)
        LOG.info("filenames=" + filenames)
        assertSameIgnoringOrder(filenames, [FILENAME1, FILENAME2, DIR1])

        // Specify a filename instead of a directory name
        assert [FILENAME1] == fileSystem.listNames(p(NEW_DIR, FILENAME1))

        assert [] == fileSystem.listNames(NO_SUCH_DIR)

        shouldFailWithMessageContaining("path") { fileSystem.listNames(null) }
    }

    void testListNames_Wildcards() {
        assert fileSystem.createDirectory(NEW_DIR)
        assert fileSystem.createFile(p(NEW_DIR, 'abc.txt'))
        assert fileSystem.createFile(p(NEW_DIR, 'def.txt'))

        assertSameIgnoringOrder(fileSystem.listNames(p(NEW_DIR, '*.txt')), ['abc.txt', 'def.txt'])
        assertSameIgnoringOrder(fileSystem.listNames(p(NEW_DIR, '*')), ['abc.txt', 'def.txt'])
        assertSameIgnoringOrder(fileSystem.listNames(p(NEW_DIR, '???.???')), ['abc.txt', 'def.txt'])
        assertSameIgnoringOrder(fileSystem.listNames(p(NEW_DIR, '*.exe')), [])
        assertSameIgnoringOrder(fileSystem.listNames(p(NEW_DIR, 'abc.???')), ['abc.txt'])
        assertSameIgnoringOrder(fileSystem.listNames(p(NEW_DIR, 'a?c.?xt')), ['abc.txt'])
        assertSameIgnoringOrder(fileSystem.listNames(p(NEW_DIR, 'd?f.*')), ['def.txt'])
    }

    void testListFiles() {
        assert fileSystem.createDirectory(NEW_DIR)
        assert [] == fileSystem.listFiles(NEW_DIR)

        assert fileSystem.createFile(p(NEW_DIR, FILENAME1))
        def fileEntry1 = new FileEntry(path: FILENAME1, lastModified: DATE)
        verifyEntries([fileEntry1], fileSystem.listFiles(NEW_DIR))

        // Specify a filename instead of a directory name
        verifyEntries([fileEntry1], fileSystem.listFiles(p(NEW_DIR, FILENAME1)))

        assert fileSystem.createFile(p(NEW_DIR, FILENAME2))
        def fileEntry2 = new FileEntry(path: FILENAME2, lastModified: DATE)
        verifyEntries(fileSystem.listFiles(NEW_DIR), [fileEntry1, fileEntry2])

        // Write to the file to get a non-zero length
        final byte[] CONTENTS = "1234567890".getBytes()
        OutputStream out = fileSystem.createOutputStream(NEW_DIR + "/" + FILENAME1, false)
        out.write(CONTENTS)
        out.close()
        fileEntry1 = new FileEntry(path: FILENAME1, contents: CONTENTS, lastModified: DATE)
        verifyEntries(fileSystem.listFiles(NEW_DIR), [fileEntry1, fileEntry2])

        assert fileSystem.createDirectory(p(NEW_DIR, DIR1))
        def dirEntry3 = new DirectoryEntry(path: DIR1, lastModified: DATE)
        verifyEntries(fileSystem.listFiles(NEW_DIR), [fileEntry1, fileEntry2, dirEntry3])

        assert fileSystem.listFiles(NO_SUCH_DIR) == []

        shouldFailWithMessageContaining("path") { fileSystem.listFiles(null) }
    }

    void testListFiles_Wildcards() {
        assert fileSystem.createDirectory(NEW_DIR)
        assert fileSystem.createFile(p(NEW_DIR, 'abc.txt'))
        assert fileSystem.createFile(p(NEW_DIR, 'def.txt'))

        def fileEntry1 = new FileEntry(path: 'abc.txt', lastModified: DATE)
        def fileEntry2 = new FileEntry(path: 'def.txt', lastModified: DATE)

        verifyEntries(fileSystem.listFiles(p(NEW_DIR, '*.txt')), [fileEntry1, fileEntry2])
        verifyEntries(fileSystem.listFiles(p(NEW_DIR, '*')), [fileEntry1, fileEntry2])
        verifyEntries(fileSystem.listFiles(p(NEW_DIR, '???.???')), [fileEntry1, fileEntry2])
        verifyEntries(fileSystem.listFiles(p(NEW_DIR, '*.exe')), [])
        verifyEntries(fileSystem.listFiles(p(NEW_DIR, 'abc.???')), [fileEntry1])
        verifyEntries(fileSystem.listFiles(p(NEW_DIR, 'a?c.?xt')), [fileEntry1])
        verifyEntries(fileSystem.listFiles(p(NEW_DIR, 'd?f.*')), [fileEntry2])
    }

    void testDelete() {
        assert fileSystem.createFile(NEW_FILE)
        assert fileSystem.delete(NEW_FILE)
        assert !fileSystem.exists(NEW_FILE)

        assert !fileSystem.delete(NO_SUCH_FILE)

        assert fileSystem.createDirectory(NEW_DIR)
        assert fileSystem.delete(NEW_DIR)
        assert !fileSystem.exists(NEW_DIR)

        assert fileSystem.createDirectory(NEW_DIR)
        assert fileSystem.createFile(NEW_DIR + "/abc.txt")

        assert !fileSystem.delete(NEW_DIR), "Directory containing files"
        assert fileSystem.exists(NEW_DIR)

        shouldFailWithMessageContaining("path") { fileSystem.delete(null) }
    }

    void testRename() {
        final String FROM_FILE = NEW_FILE + "2"
        assert fileSystem.createFile(FROM_FILE)

        assert fileSystem.rename(FROM_FILE, NEW_FILE)
        assert fileSystem.exists(NEW_FILE)

        fileSystem.createFile(NEW_FILE)
        fileSystem.createDirectory(NEW_DIR)

        // Rename existing directory
        final String TO_DIR = NEW_DIR + "2"
        assert fileSystem.rename(NEW_DIR, TO_DIR)
        assert !fileSystem.exists(NEW_DIR)
        assert fileSystem.exists(TO_DIR)
    }

    void testRename_FromPathDoesNotExist() {
        final String TO_FILE2 = NEW_FILE + "2"
        shouldFail(FileSystemException) { fileSystem.rename(NO_SUCH_FILE, TO_FILE2) }
        assert !fileSystem.exists(TO_FILE2), "After failed rename"
    }

    void testRename_DirectoryContainsFiles() {
        fileSystem.createDirectory(NEW_DIR)
        fileSystem.createFile(NEW_DIR + "/a.txt")
        fileSystem.createFile(NEW_DIR + "/b.txt")
        fileSystem.createDirectory(NEW_DIR + "/subdir")

        final String TO_DIR = NEW_DIR + "2"
        assert fileSystem.rename(NEW_DIR, TO_DIR)
        assert !fileSystem.exists(NEW_DIR)
        assert !fileSystem.exists(NEW_DIR + "/a.txt")
        assert !fileSystem.exists(NEW_DIR + "/b.txt")
        assert !fileSystem.exists(NEW_DIR + "/subdir")

        assert fileSystem.exists(TO_DIR)
        assert fileSystem.exists(TO_DIR + "/a.txt")
        assert fileSystem.exists(TO_DIR + "/b.txt")
        assert fileSystem.exists(TO_DIR + "/subdir")
    }

    void testRename_ParentOfToPathDoesNotExist() throws Exception {
        final String FROM_FILE = NEW_FILE
        final String TO_FILE = fileSystem.path(NO_SUCH_DIR, "abc")
        assert fileSystem.createFile(FROM_FILE)

        shouldFail(FileSystemException) { fileSystem.rename(FROM_FILE, TO_FILE) }
        assert fileSystem.exists(FROM_FILE)
        assert !fileSystem.exists(TO_FILE)
    }

    void testGetName_Null() {
        shouldFailWithMessageContaining("path") { fileSystem.getName(null) }
    }

    void testGetParent_Null() {
        shouldFailWithMessageContaining("path") { fileSystem.getParent(null) }
    }

//     void testNormalize_InvalidPaths() {
//        shouldFail(InvalidFilenameException) { fileSystem.normalize(ILLEGAL_FILE) }
//        LOG.info(fileSystem.normalize(ILLEGAL_FILE))
//     }

    void testNormalize_Null() {
        shouldFailWithMessageContaining("path") { fileSystem.normalize(null) }
    }

    //-------------------------------------------------------------------------
    // Test setup
    //-------------------------------------------------------------------------

    void setUp() {
        super.setUp()
        fileSystem = createFileSystem()
    }

    //-------------------------------------------------------------------------
    // Helper Methods
    //-------------------------------------------------------------------------

    private verifyEntries(List expected, List actual) {
        expected.eachWithIndex {entry, index ->
            def entryStr = entry.toString()
            LOG.info("expected=$entryStr")
            actual.find {actualEntry -> actualEntry.toString() == entryStr }
        }
    }

    protected void assertSameIgnoringOrder(list1, list2) {
        LOG.info("Comparing $list1 to $list2")
        assert list1 as Set == list2 as Set, "list1=$list1  list2=$list2"
    }

    /**
     * Return a new instance of the FileSystem implementation class under test
     * @return a new FileSystem instance
     * @throws Exception
     */
    protected abstract FileSystem createFileSystem()

    /**
     * Verify the contents of the file at the specified path read from its InputSteam
     *
     * @param fileSystem - the FileSystem instance
     * @param expectedContents - the expected contents
     * @throws IOException
     */
    protected abstract void verifyFileContents(FileSystem fileSystem, String path, String contents) throws Exception

}