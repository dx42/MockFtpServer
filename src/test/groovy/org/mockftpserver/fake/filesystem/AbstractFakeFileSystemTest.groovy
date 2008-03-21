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

import java.io.IOException
import java.io.InputStream

import org.apache.log4j.Logger
import org.mockftpserver.core.util.IoUtil

/**
 * Tests for the FakeFileSystem class
 * 
 * @version $Revision: $ - $Date: $
 * 
 * @author Chris Mair
 */
abstract class AbstractFakeFileSystemTest extends AbstractFileSystemTest {

     // TODO tests for illegal filenames (#@$%^&* etc.)

     private static final Logger LOG = Logger.getLogger(AbstractFakeFileSystemTest)

     // -------------------------------------------------------------------------
     // Tests
     // -------------------------------------------------------------------------

     /**
      * Test the addEntry() method
      */
     void testAddEntry() {
         AbstractFakeFileSystem fakeFileSystem = (AbstractFakeFileSystem) fileSystem
         assertFalse("exists before", fileSystem.exists(NEW_FILE))
         fakeFileSystem.addEntry(new DirectoryEntry(NEW_DIR))
         assertTrue("/ exists after", fileSystem.exists(NEW_DIR))
         fakeFileSystem.addEntry(new FileEntry(NEW_FILE))
         assertTrue("exists after", fileSystem.exists(NEW_FILE))

         // Try adding entry that already exists
         shouldFail(FileSystemException) { fakeFileSystem.addEntry(new FileEntry(NEW_FILE)) }

         // Try adding entry for path whose parent does not already exist
         shouldFail(FileSystemException) { fakeFileSystem.addEntry(new FileEntry("/abc/def/ghi")) }
     }

     /**
      * Verify the contents of the file at the specified path read from its InputSteam
      * 
      * @param fileSystem - the FileSystem instance
      * @param expectedContents - the expected contents
      * @throws IOException
      * @see org.mockftpserver.fake.filesystem.AbstractFileSystemTest#verifyFileContents(FileSystem,
      *      String, String)
      */
     protected void verifyFileContents(FileSystem fileSystem, String path, String expectedContents) throws IOException {
         InputStream input = fileSystem.createInputStream(path)
         byte[] bytes = IoUtil.readBytes(input)
         LOG.info("bytes=[" + new String(bytes) + "]")
         assertEquals("contents: actual=[" + new String(bytes) + "]", expectedContents.getBytes(), bytes)
     }

     /**
      * Test the toString() method 
      */
     void testToString() {
         String toString = fileSystem.toString()
         LOG.info("toString=" + toString)
         assert toString.contains(fileSystem.normalize(EXISTING_DIR))
         assert toString.contains(fileSystem.normalize(EXISTING_FILE))
     }
     
}