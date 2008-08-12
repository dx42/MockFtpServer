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
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class FileInfo {

    final String name
    final long size
    final boolean directory
    final Date lastModified
    final String owner
    final String group
    final Permissions permissions

    /**
     * Construct and return a new instance representing a directory entry.
     * @param name - the directory name
     * @param lastModified - the lastModified Date for the directory
     * @param owner - the username of the owner of the directory; defaults to null
     * @param group - the name of the group owning the directory; defaults to null
     * @param permissions - the Permissions for the directory; defaults to null
     * @return a new FileInfo instance representing a directory
     */
    static FileInfo forDirectory(String name, Date lastModified, String owner = null, String group = null,
                                 Permissions permissions = null) {
        return new FileInfo(true, name, 0, lastModified, owner, group, permissions)
    }

    /**
     * Construct and return a new instance representing a file entry.
     * @param name - the directory name
     * @param size - the length of the file in bytes
     * @param lastModified - the lastModified Date for the directory
     * @param owner - the username of the owner of the file; defaults to null
     * @param group - the name of the group owning the file; defaults to null
     * @param permissions - the Permissions for the file; defaults to null
     * @return a new FileInfo instance representing a directory
     */
    static FileInfo forFile(String name, long size, Date lastModified, String owner = null, String group = null,
                            Permissions permissions = null) {
        return new FileInfo(false, name, size, lastModified, owner, group, permissions)
    }

    private FileInfo(boolean directory, String name, long size, Date lastModified, String owner, String group, Permissions permissions) {
        this.directory = directory
        this.name = name
        this.size = size
        this.lastModified = lastModified
        this.owner = owner
        this.group = group
        this.permissions = permissions
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
     * Return the hash code for this object. Note that only the directory (boolean),
     * name and length properties affect the hash code value. The lastModified
     * property is ignored.
     *
     * @see java.lang.Object#hashCode()
     */
    int hashCode() {
        String str = [directory, name, size].join(":")
        return str.hashCode()
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        "FileInfo[isDirectory=$directory, name=$name, size=$size, lastModified=$lastModified, owner=$owner, " +
                "group=$group, permissions=$permissions]"
    }

}
