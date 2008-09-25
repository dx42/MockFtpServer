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
import org.apache.log4j.Logger


/**
 * Unix-specific implementation of the DirectoryListingFormatter interface.
 *
 * @version $Revision: 86 $ - $Date: 2008-07-23 21:16:27 -0400 (Wed, 23 Jul 2008) $
 *
 * @author Chris Mair
 */
class UnixDirectoryListingFormatter implements DirectoryListingFormatter {

    static final LOG = Logger.getLogger(UnixDirectoryListingFormatter)

    static final DATE_FORMAT = "MMM dd  yyyy"
    static final SIZE_WIDTH = 15
    static final OWNER_WIDTH = 8
    static final GROUP_WIDTH = 8

    // "-rw-rw-r--    1 ftp      ftp           254 Feb 23  2007 robots.txt"
    // "-rw-r--r--    1 ftp      ftp      30014925 Apr 15 00:19 md5.sums.gz"
    // "-rwxr-xr-x   1 c096336  iawebgrp    5778 Dec  1  2005 FU_WyCONN_updateplanaccess.sql"

    /**
     * Format the directory listing for a single file/directory entry.
     * @param fileSystemEntry - the FileSystemEntry for a single file system entry
     * @return the formatted directory listing
     */
    String format(FileSystemEntry fileSystemEntry) {
        def dateFormat = new SimpleDateFormat(DATE_FORMAT)
        def dateStr = dateFormat.format(fileSystemEntry.lastModified)
        def dirOrFile = fileSystemEntry.isDirectory() ? 'd' : '-'
        def permissions = fileSystemEntry.permissions ?: Permissions.DEFAULT
        def permissionsStr = padRight(permissions.asRwxString(), 9)
        def linkCountStr = '1'
        def ownerStr = padRight(fileSystemEntry.owner, OWNER_WIDTH)
        def groupStr = padRight(fileSystemEntry.group, GROUP_WIDTH)
        def sizeStr = padLeft(fileSystemEntry.size.toString(), SIZE_WIDTH)
        def listing = "$dirOrFile$permissionsStr  $linkCountStr $ownerStr $groupStr $sizeStr $dateStr ${fileSystemEntry.name}"
        LOG.info("listing=[$listing]")
        return listing
    }

    private String padRight(String string, int width) {
        (string ?: '').padRight(width)
    }

    private String padLeft(String string, int width) {
        (string ?: '').padLeft(width)
    }

}