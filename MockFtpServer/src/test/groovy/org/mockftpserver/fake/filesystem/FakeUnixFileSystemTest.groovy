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
package org.mockftpserver.fake.filesystem;

import org.mockftpserver.core.util.AssertFailedException
import org.mockftpserver.core.util.IoUtil
import org.mockftpserver.fake.filesystem.AbstractFakeFileSystemTest

/**
 * Tests for FakeUnixFileSystem.
 * 
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
class FakeUnixFileSystemTest extends AbstractFakeFileSystemTest {

     private static final String SEP = "/"
     
     FakeUnixFileSystemTest() {
         // These need to be set in the constructor because these values are used in setUp()
         NEW_DIR = SEP + NEW_DIRNAME
         NEW_FILE = "/NewFile.txt"
         EXISTING_DIR = "/"
         EXISTING_FILE = "/ExistingFile.txt"
         NO_SUCH_DIR = "/xx/yy"
         NO_SUCH_FILE = "/xx/yy/zz.txt"
     }
     
     /**
      * Test the path() method 
      */
     void testPath() {
         assert fileSystem.path(null, null) == ""
         assert fileSystem.path(null, "abc") == "abc"
         assert fileSystem.path("abc", null) == "abc"
         assert fileSystem.path("", "") == ""
         assert fileSystem.path("", "abc") == "abc"
         assert fileSystem.path("abc", "") == "abc"
         assert fileSystem.path("abc", "DEF") == "abc/DEF"
         assert fileSystem.path("abc/", "def") == "abc/def"
         assert fileSystem.path("/abc/", "def") == "/abc/def"
         assert fileSystem.path("/ABC", "/def") == "/ABC/def"
         assert fileSystem.path("abc", "/def") == "abc/def"
     }

     /**
      * Test the normalize() method
      */
     void testNormalize() {
         assert fileSystem.normalize("/") == "/"
         assert fileSystem.normalize("/aBc") == p("aBc") 
         assert fileSystem.normalize("/abc/DEF") == p("abc","DEF")
         assert fileSystem.normalize("/Abc/def/..") == p("Abc")
         assert fileSystem.normalize("/abc/def/../ghi") == p("abc","ghi")
         assert fileSystem.normalize("/abc/def/.") == p("abc","def")
         assert fileSystem.normalize("/abc/def/./gHI") == p("abc","def","gHI")
     }
     
     /**
      * Test the getName() method 
      */
     void testGetName() {
         assert fileSystem.getName("/") == ""
         assert fileSystem.getName("/aBC") == "aBC"
         assert fileSystem.getName("/abc/def") == "def"
         assert fileSystem.getName("/abc/def/../GHI") == "GHI"
     }
     
     /**
      * Test the getParent() method 
      */
     public void testGetParent() {
         assert fileSystem.getParent("/") == null
         assert fileSystem.getParent("/abc") == "/"
         assert fileSystem.getParent("/abc/def") == p("abc") 
     }
     
     /**
      * Test the isValidName() method
      */
     void testIsValidName() {
         [ "/abc",
           "/ABC/def",
           "/abc/d!ef",
           "/abc/DEF/h(ij)!@#\$%^&*()-_+=~`,.<>?;:[]{}\\|abc",
           ].each {
             assert fileSystem.isValidName(it), "[$it]"    
           }
         
         [ "",
           "abc",
           "abc/def",
           "a:/abc:", 
           "//a*bc",
           "C:/?abc",
           ].each {
             assert !fileSystem.isValidName(it), "[$it]"    
         }
     }
     
    //-----------------------------------------------------------------------------------
    // Helper Methods
    //-----------------------------------------------------------------------------------
    
    /**
     * Return a new instance of the FileSystem implementation class under test
     * 
     * @return a new FileSystem instance
     */
    protected FileSystem createFileSystem() {
        FakeUnixFileSystem fs = new FakeUnixFileSystem()
        fs.addEntry(new DirectoryEntry(EXISTING_DIR))
        fs.addEntry(new FileEntry(EXISTING_FILE, EXISTING_FILE_CONTENTS))
        return fs
    }
    
     /**
      * Return the specified paths concatenated with the system-dependent separator in between
      * @param p1 - the first path
      * @param p2 - the second path
      * @return SEPARATOR + p1 + SEPARATOR + p2
      */
     private String p(String[] paths) {
         return SEP + paths.join(SEP)
     }

 }
