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
 * Implementation of the       {@link FileSystem}       interface that simulates a Microsoft
 * Windows file system. The rules for file and directory names include: 
 * <ul>
 *   <li>Filenames are case-insensitive (and normalized to lower-case)</li>
 *   <li>Either forward slashes (/) or backward slashes (\) are valid path separators (but are normalized to '\')</li>  
 *   <li>An absolute path starts with a drive specifier (e.g. 'a:' or 'c:') followed 
 *   		by '\' or '/', or else if it starts with "\\"</li>  
 * </ul>
 *
 * The <code>directoryListingFormatter</code> property is automatically initialized to an instance
 * of    {@link WindowsDirectoryListingFormatter}   .
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class WindowsFakeFileSystem extends AbstractFakeFileSystem {

    public static final String SEPARATOR = "\\"
    static final VALID_PATTERN = /\p{Alpha}\:(\\|(\\[^\\\:\*\?\<\>\|\"]+)+)/
    static final LAN_PREFIX = "\\\\"

    /**
     * Construct a new instance and initialize the directoryListingFormatter to a WindowsDirectoryListingFormatter.
     */
    WindowsFakeFileSystem() {
        this.directoryListingFormatter = new WindowsDirectoryListingFormatter()
    }

    //-------------------------------------------------------------------------
    // Abstract Method Implementations
    //-------------------------------------------------------------------------

    protected String getSeparator() {
        return SEPARATOR
    }

    /**
     * Return true if the specified path designates a valid (absolute) file path. For Windows
     * paths, a path is valid if it starts with a drive specifier followed by
     * '\' or '/', or if it starts with "\\".
     *
     * @param path - the path
     * @return true if path is valid, false otherwise
     *
     * @throws AssertionError - if path is null
     */
    protected boolean isValidName(String path) {
        // \/:*?"<>|
        assert path != null
        def standardized = path.replace("/", "\\")
        return (standardized ==~ VALID_PATTERN) || standardized.startsWith(LAN_PREFIX)
    }

    /**
     * Return true if the specified char is a separator character ('\' or '/')
     * @param c - the character to test
     * @return true if the specified char is a separator character ('\' or '/')
     */
    protected boolean isSeparator(char c) {
        return c == '\\' || c == '/'
    }

    /**
     * @return true if the specified path component is a root for this filesystem
     */
    protected boolean isRoot(String pathComponent) {
        return pathComponent.contains(":")
    }

}