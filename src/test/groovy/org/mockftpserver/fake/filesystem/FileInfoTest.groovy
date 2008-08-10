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

import org.mockftpserver.test.AbstractGroovyTest

/**
 * Tests for FileInfo
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class FileInfoTest extends AbstractGroovyTest {

    static final NAME = "def.txt"
    static final SIZE = 1234567L
    static final LAST_MODIFIED = new Date()
    static final OWNER = 'owner123'
    static final GROUP = 'group456'

    private FileInfo fileInfoFile
    private FileInfo fileInfoDirectory

    void testForFile() {
        assert fileInfoFile.isDirectory() == false
        assert fileInfoFile.getName() == NAME
        assert fileInfoFile.getSize() == SIZE
        assert fileInfoFile.lastModified == LAST_MODIFIED
        assert fileInfoFile.owner == null
        assert fileInfoFile.group == null
    }

    void testForFile_OwnerAndGroup() {
        def fileInfo = FileInfo.forFile(NAME, SIZE, LAST_MODIFIED, OWNER, GROUP)
        assert fileInfo.owner == OWNER
        assert fileInfo.group == GROUP
    }

    void testForDirectory() {
        assert fileInfoDirectory.isDirectory()
        assert fileInfoDirectory.getName() == NAME
        assert fileInfoDirectory.getSize() == 0
        assert fileInfoDirectory.lastModified == LAST_MODIFIED
        assert fileInfoDirectory.owner == null
        assert fileInfoDirectory.group == null
    }

    void testForDirectory_OwnerAndGroup() {
        def fileInfo = FileInfo.forDirectory(NAME, LAST_MODIFIED, OWNER, GROUP)
        assert fileInfo.owner == OWNER
        assert fileInfo.group == GROUP
    }

    void testEquals() {
        assert fileInfoFile.equals(fileInfoFile)
        assert fileInfoFile.equals(FileInfo.forFile(NAME, SIZE, LAST_MODIFIED))
        assert fileInfoFile.equals(FileInfo.forFile(NAME, SIZE, new Date())) // lastModified ignored

        assert !fileInfoFile.equals(FileInfo.forFile("xyz", SIZE, LAST_MODIFIED))
        assert !fileInfoFile.equals(FileInfo.forFile(NAME, 999L, LAST_MODIFIED))
        assert !fileInfoFile.equals("ABC")
        assert !fileInfoFile.equals(null)
    }

    void testHashCode() {
        assert fileInfoFile.hashCode() == fileInfoFile.hashCode()
        assert fileInfoFile.hashCode() == FileInfo.forFile(NAME, SIZE, LAST_MODIFIED).hashCode()
        assert fileInfoFile.hashCode() == FileInfo.forFile(NAME, SIZE, new Date()).hashCode()  // lastModified ignored

        assert fileInfoFile.hashCode() != FileInfo.forFile("xyz", SIZE, LAST_MODIFIED).hashCode()
        assert fileInfoFile.hashCode() != FileInfo.forFile(NAME, 33, LAST_MODIFIED).hashCode()

        assert fileInfoDirectory.hashCode() == FileInfo.forDirectory(NAME, LAST_MODIFIED).hashCode()
    }

    void testToString() {
        String toString = fileInfoFile.toString()
        assert toString.contains(NAME)
        assert toString.contains(Long.toString(SIZE))
        assert toString.contains(LAST_MODIFIED.toString())
    }

    void setUp() {
        super.setUp()
        fileInfoFile = FileInfo.forFile(NAME, SIZE, LAST_MODIFIED)
        fileInfoDirectory = FileInfo.forDirectory(NAME, LAST_MODIFIED)
    }
}
