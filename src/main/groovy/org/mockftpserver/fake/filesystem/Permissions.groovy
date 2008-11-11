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
 * Represents and encapsulates the read/write/execute permissions for a file or directory.
 * This is conceptually (and somewhat loosely) based on the permissions flags within the Unix
 * file system. An instance of this class is immutable.
 *
 * @version $Revision: 86 $ - $Date: 2008-07-23 21:16:27 -0400 (Wed, 23 Jul 2008) $
 *
 * @author Chris Mair
 */
class Permissions {
    public static final Permissions ALL = new Permissions('rwxrwxrwx')
    public static final Permissions NONE = new Permissions('---------')
    public static final Permissions DEFAULT = ALL

    static final READ_CHAR = 'r'
    static final WRITE_CHAR = 'w'
    static final EXECUTE_CHAR = 'x'

    private String rwxString

    /**
     * Costruct a new instance for the specified read/write/execute specification String
     * @param rwxString - the read/write/execute specification String; must be 9 characters long, with chars
     *      at index 0,3,6 == '-' or 'r', chars at index 1,4,7 == '-' or 'w' and chars at index 2,5,8 == '-' or 'x'.
     */
    Permissions(String rwxString) {
        assert rwxString.size() == 9
        assert rwxString ==~ /(-|r)(-|w)(-|x)/ * 3
        this.rwxString = rwxString
    }

    /**
     * Return the read/write/execute specification String representing the set of permissions. For example:
     * "rwxrwxrwx" or "rw-r-----".
     * @return the String containing 9 characters that represent the read/write/execute permissions.
     */
    String asRwxString() {
        rwxString
    }

    /**
     * @return the RWX string for this instance
     */
    String getRwxString() {
        return rwxString
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    boolean equals(Object object) {
        (object
                && object.class == this.class
                && object.hashCode() == hashCode())
    }

    /**
     * Return the hash code for this object.
     * @see java.lang.Object#hashCode()
     */
    int hashCode() {
        return rwxString.hashCode()
    }

    /**
     * @return true if and only if the user has read permission
     */
    boolean canUserRead() {
        rwxString[0] == READ_CHAR
    }

    /**
     * @return true if and only if the user has write permission
     */
    boolean canUserWrite() {
        rwxString[1] == WRITE_CHAR
    }

    /**
     * @return true if and only if the user has execute permission
     */
    boolean canUserExecute() {
        rwxString[2] == EXECUTE_CHAR
    }

    /**
     * @return true if and only if the group has read permission
     */
    boolean canGroupRead() {
        rwxString[3] == READ_CHAR
    }

    /**
     * @return true if and only if the group has write permission
     */
    boolean canGroupWrite() {
        rwxString[4] == WRITE_CHAR
    }

    /**
     * @return true if and only if the group has execute permission
     */
    boolean canGroupExecute() {
        rwxString[5] == EXECUTE_CHAR
    }

    /**
     * @return true if and only if the world has read permission
     */
    boolean canWorldRead() {
        rwxString[6] == READ_CHAR
    }

    /**
     * @return true if and only if the world has write permission
     */
    boolean canWorldWrite() {
        rwxString[7] == WRITE_CHAR
    }

    /**
     * @return true if and only if the world has execute permission
     */
    boolean canWorldExecute() {
        rwxString[8] == EXECUTE_CHAR
    }

    /**
     * @return the String representation of this object.
     */
    String toString() {
        "Permissions[$rwxString]"
    }
}