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

import org.apache.log4j.Logger
import org.mockftpserver.core.util.IoUtil

/**
 * Tests for DefaultFileSystem
 * 
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
class DefaultFileSystemTest extends AbstractFileSystemTest {

     static final LOG = Logger.getLogger(DefaultFileSystemTest)
     static final EXISTING_FILENAME = "TestFile.txt"
     static final NEW_FILENAME = "NewFile.txt"

     private File newFile
     private DefaultFileSystem defaultFileSystem

     /**
      * Constructor. Initialize test-specific values used by superclass.
      */
     DefaultFileSystemTest() {
         EXISTING_DIR = "testdir"
         EXISTING_FILE = EXISTING_DIR + "/" + EXISTING_FILENAME
         NEW_DIR = EXISTING_DIR + "/" + AbstractFileSystemTest.NEW_DIRNAME
         NEW_FILE = EXISTING_DIR + "/" + NEW_FILENAME
         NO_SUCH_DIR = "x:/xx/yy"
         NO_SUCH_FILE = "x:/xx/yy/zz.txt"
     }
     
     /**
      * Test the exists() method, setting the root dir 
      */
     void testExists_WithRoot() {
         defaultFileSystem.setRoot(EXISTING_DIR)
         assert defaultFileSystem.exists(EXISTING_FILENAME)
     }

     /**
      * Test the isDirectory() method, settig the root dir 
      */
     void testIsDirectory_WithRoot() {
         defaultFileSystem.setRoot(EXISTING_DIR)
         assert defaultFileSystem.isDirectory("")
     }
     
     /**
      * Test the isFile() method, setting the root dir 
      */
     void testIsFile_WithRoot() {
         defaultFileSystem.setRoot(EXISTING_DIR)
         assert defaultFileSystem.isFile(EXISTING_FILENAME)
     }

     /**
      * Test the createDirectory() method, when the root path is non-null 
      */
     void testCreateDirectory_WithRoot() {
         defaultFileSystem.setRoot(EXISTING_DIR)
         assert defaultFileSystem.createDirectory(NEW_FILENAME)
         assert newFile.exists()
     }
     
     /**
      * Test the createFile() method, when the root path is non-null 
      */
     void testCreateFile_WithRoot() {
         defaultFileSystem.setRoot(EXISTING_DIR)
         assert defaultFileSystem.createFile(NEW_FILENAME)
         assert newFile.exists()
     }
     
     /**
      * Test the createInputStream() method, setting the root directory 
      */
     void testCreateInputStream_WithRoot() {
         defaultFileSystem.setRoot(EXISTING_DIR)
         InputStream input = defaultFileSystem.createInputStream(EXISTING_FILENAME)
         assert EXISTING_FILE_CONTENTS.getBytes() == IoUtil.readBytes(input)
     }
     
     /**
      * Test the createOutputStream() method, when the root path is non-null 
      */
     void testCreateOutputStream_WithRoot() {
         defaultFileSystem.setRoot(EXISTING_DIR)
         OutputStream out = defaultFileSystem.createOutputStream(NEW_FILENAME, false)
         out.write(EXISTING_FILE_CONTENTS.getBytes())
         out.close()
         assert EXISTING_FILE_CONTENTS.getBytes() == readFileContents(EXISTING_DIR + "/" + NEW_FILENAME)
     }
         
     /**
      * Test the listNames() method, setting the file system root 
      */
     void testListNames_WithRoot() {
         assert defaultFileSystem.createDirectory(NEW_DIR)
         assert defaultFileSystem.createFile(NEW_DIR + "/" + FILENAME1)
         assert defaultFileSystem.createFile(NEW_DIR + "/" + FILENAME2)

         defaultFileSystem.setRoot(EXISTING_DIR)
         assert [FILENAME1, FILENAME2] as Set == defaultFileSystem.listNames(NEW_DIRNAME) as Set
     }
     
     /**
      * Test listFiles() method, setting the root path
      */
     void testListFiles_WithRoot() {
         assert defaultFileSystem.createDirectory(NEW_DIR)
         assert defaultFileSystem.createFile(NEW_DIR + "/" + FILENAME1)
         FileInfo fileInfo1 = new FileInfo(FILENAME1, 0)
         assert defaultFileSystem.createFile(NEW_DIR + "/" + FILENAME2)
         FileInfo fileInfo2 = new FileInfo(FILENAME2, 0)
         defaultFileSystem.setRoot(EXISTING_DIR)
         assert [fileInfo1, fileInfo2] == defaultFileSystem.listFiles(NEW_DIRNAME)
     }
     
     /**
      * Test the delete() method, setting the root path 
      */
     void testDelete_WithRoot() {
         assert defaultFileSystem.createFile(NEW_FILE)
         defaultFileSystem.setRoot(EXISTING_DIR)
         assert defaultFileSystem.delete(NEW_FILENAME)
     }
     
     /**
      * Test the rename() method, when the root path is non-null 
      */
     void testRename_WithRoot() {
         defaultFileSystem.setRoot(EXISTING_DIR)
         final String FROM_FILE = NEW_FILENAME + "2"
         assert defaultFileSystem.createFile(FROM_FILE)
         
         assert defaultFileSystem.rename(FROM_FILE, NEW_FILENAME)
         assert newFile.exists()
     }
     
     //-------------------------------------------------------------------------
     // Tests for path-related methods
     //-------------------------------------------------------------------------
     
     private static final String SEP = File.separator
    
     /**
      * Test the path() method 
      */
     void testPath() {
        assert "" == fileSystem.path(null, null)
        assert "abc" == fileSystem.path(null, "abc")
        assert "abc" == fileSystem.path("abc", null)
        assert "" == fileSystem.path("", "")
        assert "abc" == fileSystem.path("", "abc")
        assert "abc" == fileSystem.path("abc", "")
        assert "abc" + SEP + "def" == fileSystem.path("abc", "def")
        assert "abc\\def" == fileSystem.path("abc\\", "def")
        assert "abc/def" == fileSystem.path("abc/", "def")
        assert "abc\\def" == fileSystem.path("abc", "\\def")
        assert "abc/def" == fileSystem.path("abc", "/def")
     }

    /**
     * Test the normalize() method
     * TODO Test passing in an illegal(?) path that causes an IOException 
     */
    void testNormalize() {
        assertEquals("<empty>", absPath(""), fileSystem.normalize(""))
        assertEquals("abc", absPath("abc"), fileSystem.normalize("abc"))
        assertEquals("abc\\def", absPath("abc","def"), fileSystem.normalize("abc\\def"))
        assertEquals("abc/def", absPath("abc","def"), fileSystem.normalize("abc/def"))
        assertEquals("abc/def/..", absPath("abc"), fileSystem.normalize("abc/def/.."))
        assertEquals("abc\\def\\.", absPath("abc","def"), fileSystem.normalize("abc\\def\\."))
        assertEquals("\\abc", absPath("\\abc"), fileSystem.normalize("\\abc"))
        assertEquals("/abc", absPath("/abc"), fileSystem.normalize("/abc"))
        assertEquals("c:\\abc", p("c:","abc"), fileSystem.normalize("c:\\abc").toLowerCase())
        assertEquals("c:/abc", p("c:","abc"), fileSystem.normalize("c:/abc").toLowerCase())
        assertEquals("z:/abc", p("z:","abc"), fileSystem.normalize("z:/abc").toLowerCase())
    }
    
    /**
     * Test the getName() method 
     */
    void testGetName() {
        assertEquals("<empty>", "", fileSystem.getName(""))
        assertEquals("abc", "abc", fileSystem.getName("abc"))
        assertEquals("abc\\def", "def", fileSystem.getName("abc\\def"))
        assertEquals("abc/def", "def", fileSystem.getName("abc/def"))
        assertEquals("\\abc", "abc", fileSystem.getName("\\abc"))
        assertEquals("/abc", "abc", fileSystem.getName("/abc"))
        assertEquals("c:\\abc", "abc", fileSystem.getName("c:\\abc"))
        assertEquals("c:/abc", "abc", fileSystem.getName("c:/abc"))
        assertEquals("c:\\abc\\def", "def", fileSystem.getName("c:\\abc\\def"))
        assertEquals("c:/abc/def", "def", fileSystem.getName("c:/abc/def"))
    }
    
    /**
     * Test the getParent() method 
     */
    void testGetParent() {
        assertEquals("<empty>", null, fileSystem.getParent(""))
        assertEquals("abc", null, fileSystem.getParent("abc"))
        assertEquals("abc\\def", "abc", fileSystem.getParent("abc\\def"))
        assertEquals("abc/def", "abc", fileSystem.getParent("abc/def"))
        assertEquals("\\abc", SEP, fileSystem.getParent("\\abc"))
        assertEquals("/abc", SEP, fileSystem.getParent("/abc"))
        assertEquals("c:\\abc", "c:" + SEP, fileSystem.getParent("c:\\abc"))
        assertEquals("c:/abc", "c:" + SEP, fileSystem.getParent("c:/abc"))
        assertEquals("c:\\abc\\def", "c:" + SEP + "abc", fileSystem.getParent("c:\\abc\\def"))
        assertEquals("c:/abc/def", "c:" + SEP + "abc", fileSystem.getParent("c:/abc/def"))
    }
    
    /**
     * Return the specified paths concatenated with the system-dependent separator in between
     * @param p1 - the first path
     * @param p2 - the second path
     * @return p1 + SEPARATOR + p2
     */
    private String p(String p1, String p2) {
        return p1 + SEP + p2
    }
    
    /**
     * Return the absolute path for the specified relative path
     * @param path - the relative path
     * @return - the absolute path
     */
    private String absPath(String path) {
        return new File(path).getAbsolutePath()
    } 
    
    /**
     * Return the absolute path for the specified relative path
     * @param path - the relative path
     * @return - the absolute path
     */
    private String absPath(String path1, String path2) {
        return new File(p(path1, path2)).getAbsolutePath()
    } 
    

    
     //-------------------------------------------------------------------------
     // Test setup and tear-down
     //-------------------------------------------------------------------------
     
     /**
      * @see org.mockftpserver.test.AbstractTest#setUp()
      */
     void setUp() {
         super.setUp()
         newFile = new File(NEW_FILE)
         defaultFileSystem = (DefaultFileSystem) fileSystem
     }
     
     /**
      * @see org.mockftpserver.test.AbstractTest#tearDown()
      */
     void tearDown() {
         super.tearDown()
         deleteDirectory(new File(EXISTING_DIR))
     }
     
     //-------------------------------------------------------------------------
     // Internal Helper Methods
     //-------------------------------------------------------------------------

     /**
      * Delete the directory and all its contents (recursively)
      * @param dirFile - the File representing the directory to delete
      */
     private void deleteDirectory(File dirFile) {
         File[] files = dirFile.listFiles()
         files.each { file ->
             if (file.isDirectory()) {
                 deleteDirectory(file)
             }
             else {
                 boolean deleted = file.delete()
                 LOG.info("Deleted [" + file.getName() + "]: " + deleted)
             }
         }
         boolean deleted = dirFile.delete()
         LOG.info("Deleted [" + dirFile.getName() + "]: " + deleted)
     }
     
     /**
      * Return a new instance of the FileSystem implementation class under test
      * @return a new FileSystem instance
      * @throws Exception
      */
     protected FileSystem createFileSystem() {
         DefaultFileSystem defaultFileSystem = new DefaultFileSystem()
         assertTrue("creating " + EXISTING_DIR, new File(EXISTING_DIR).mkdir())
         writeFileContents(defaultFileSystem, EXISTING_FILE, EXISTING_FILE_CONTENTS.getBytes(), false)
         return defaultFileSystem
     }

     /**
      * Verify the contents of the file at the specified path
      * @see org.mockftpserver.fake.filesystem.AbstractFileSystemTest#verifyFileContents(FileSystem, String, String)
      */
     protected void verifyFileContents(FileSystem fileSystem, String path, String expectedContents) {
         assertEquals(path, expectedContents.getBytes(), readFileContents(path))
     }
     
     /**
      * Return the contents of the file at the specified path as a byte[].
      * 
      * @param path - the path of the file
      * @return the contents of the file as a byte[]
      * 
      * @throws AssertionError - if path is null
      */
     private byte[] readFileContents(String path) {
         InputStream input = new FileInputStream(path)
         return IoUtil.readBytes(input)
     }

     /**
      * Write the specified byte[] contents to the file at the specified path.
      * @param fileSystem - the fileSystem instance
      * @param path - the path of the file
      * @param contents - the contents to write to the file
      * @param append - true if the contents should be appended to the end of the file if the file already exists
      */
     private void writeFileContents(FileSystem fileSystem, String path, byte[] contents, boolean append) {
         OutputStream out = fileSystem.createOutputStream(path, append)
         try {
             out.write(contents)
         }
         finally {
             out.close()
         }
     }
}