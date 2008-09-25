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

import java.text.SimpleDateFormat

/**
 * Windows-specific implementation of the DirectoryListingFormatter interface.
 *
 * @version $Revision: 86 $ - $Date: 2008-07-23 21:16:27 -0400 (Wed, 23 Jul 2008) $
 *
 * @author Chris Mair
 */
class WindowsDirectoryListingFormatter implements DirectoryListingFormatter {

    static final DATE_FORMAT = "MM-dd-yy hh:mmaa"
    static final SIZE_WIDTH = 15

    /**
     * Format the directory listing for a single file/directory entry.
     * @param fileSystemEntry - the FileSystemEntry for a single file system entry
     * @return the formatted directory listing
     */
    String format(FileSystemEntry fileSystemEntry) {
        def dateFormat = new SimpleDateFormat(DATE_FORMAT)
        def dateStr = dateFormat.format(fileSystemEntry.lastModified)
        def dirOrSize = fileSystemEntry.directory ? "<DIR>".padRight(SIZE_WIDTH) : fileSystemEntry.size.toString().padLeft(SIZE_WIDTH)
        return "$dateStr  $dirOrSize  ${fileSystemEntry.name}"
    }

}