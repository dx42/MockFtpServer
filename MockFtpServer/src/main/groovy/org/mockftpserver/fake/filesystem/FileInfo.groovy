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

/**
 * Represents the information describing a single file or directory in the file system. 
 * 
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
class FileInfo {

    //private String filename
    //private String parentDirectory
    private String path
    private long length
    
    FileInfo(String path, long length) {
        this.path = path
        this.length = length
    }

    /**
     * @return the length
     */
    long getLength() {
        return length
    }

    /**
     * @return the path
     */
    String getPath() {
        return path
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    boolean equals(Object obj) {
        (obj 
            && obj.class == this.class 
            && obj.hashCode() == hashCode())
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    int hashCode() {
        String str = path + ":" + length
        return str.hashCode()
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "FileInfo[path=" + path + ", length=" + length + "]"
    }
}
