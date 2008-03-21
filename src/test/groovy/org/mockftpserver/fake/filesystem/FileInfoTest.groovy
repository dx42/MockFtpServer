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
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
public final class FileInfoTest extends AbstractGroovyTest {

    private static final String PATH = "a/b/c/def.txt"
    private static final long LENGTH = 1234567L

    private FileInfo fileInfo

    /**
     * Test the constructor
     */
    void testConstructor() {
        assert fileInfo.getPath() == PATH
        assert fileInfo.getLength() == LENGTH
    }
    
    /**
     * Test the equals() method 
     */
    void testEquals() {
        assert fileInfo.equals(fileInfo)
        assert fileInfo.equals(new FileInfo(PATH, LENGTH))
        assert !fileInfo.equals(new FileInfo("xyz", LENGTH))
        assert !fileInfo.equals(new FileInfo(PATH, 999L))
        assert !fileInfo.equals("ABC")
        assert !fileInfo.equals(null)
    }
    
    /**
     * Test the hashCode() method 
     */
    void testHashCode() {
        assert fileInfo.hashCode() == fileInfo.hashCode()
        assert fileInfo.hashCode() == new FileInfo(PATH, LENGTH).hashCode()
        assert fileInfo.hashCode() != new FileInfo("xyz", LENGTH).hashCode()
    }
    
    /**
     * Test the toString() method 
     */
    void testToString() {
        String toString = fileInfo.toString() 
        assert toString.indexOf(PATH) != -1
        assert toString.indexOf(Long.toString(LENGTH)) != -1
    }

    /**
     * @see org.mockftpserver.test.AbstractTest#setUp()
     */
    void setUp() {
        super.setUp()
        fileInfo = new FileInfo(PATH, LENGTH)
    }
}
