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
 * The abstract superclass for concrete file system entry classes representing files and directories.
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
abstract class AbstractFileSystemEntry implements FileSystemEntry {

    private String path
    private boolean pathLocked = false
    Date lastModified
    String owner
    String group
    Permissions permissions

    /**
     * Construct a new instance without setting its path
     */
    AbstractFileSystemEntry() {
    }

    /**
     * Construct a new instance with the specified value for its path 
     * @param path - the value for path
     */
    AbstractFileSystemEntry(String path) {
        this.path = path
    }

    /**
     * @return the path for this entry
     */
    public String getPath() {
        return path
    }

    /**
     * @return the file name or directory name (no path) for this entry
     */
    public String getName() {
        int separatorIndex1 = path.lastIndexOf('/')
        int separatorIndex2 = path.lastIndexOf('\\')
        int separatorIndex = [separatorIndex1, separatorIndex2].max()
        return (separatorIndex == -1) ? path : path.substring(separatorIndex + 1)
    }

    /**
     * Set the path for this entry. Throw an exception if pathLocked is true.
     * @param path - the new path value
     */
    public void setPath(String path) {
        assert !pathLocked
        this.path = path
    }

    void lockPath() {
        this.pathLocked = true
    }

    void setPermissionsFromString(String permissionsString) {
        this.permissions = new Permissions(permissionsString)
    }

    /**
     * Abstract method -- must be implemented within concrete subclasses
     * @return true if this file system entry represents a directory
     */
    abstract boolean isDirectory()

}
