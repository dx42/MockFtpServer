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
import org.mockftpserver.test.AbstractGroovyTest

/**
 * Tests for UnixDirectoryListingFormatter
 *
 * @version $Revision: 78 $ - $Date: 2008-07-02 20:47:17 -0400 (Wed, 02 Jul 2008) $
 *
 * @author Chris Mair
 */
class UnixDirectoryListingFormatterTest extends AbstractGroovyTest {

    static final FILE_NAME = "def.txt"
    static final DIR_NAME = "etc"
    static final OWNER = 'owner123'
    static final GROUP = 'group456'
    static final SIZE = 1234567L
    static final LAST_MODIFIED = new Date()

    private formatter
    private dateFormat
    private lastModifiedFormatted

    // "-rw-rw-r--    1 ftp      ftp           254 Feb 23  2007 robots.txt"
    // "-rw-r--r--    1 ftp      ftp      30014925 Apr 15 00:19 md5.sums.gz"
    // "-rwxr-xr-x   1 c096336  iawebgrp    5778 Dec  1  2005 FU_WyCONN_updateplanaccess.sql"
    // "drwxr-xr-x   2 c096336  iawebgrp    8192 Nov  7  2006 tmp"
    // "drwxr-xr-x   39 ftp      ftp          4096 Mar 19  2004 a"

    void testFormat_File() {
        def fileInfo = FileInfo.forFile(FILE_NAME, SIZE, LAST_MODIFIED, OWNER, GROUP)
        verifyFormat(fileInfo, "-rwxrwxrwx  1 owner123 group456         1234567 $lastModifiedFormatted def.txt")
    }

    void testFormat_Directory() {
        def fileInfo = FileInfo.forDirectory(DIR_NAME, LAST_MODIFIED, OWNER, GROUP)
        verifyFormat(fileInfo, "drwxrwxrwx  1 owner123 group456               0 $lastModifiedFormatted etc")
    }

    void setUp() {
        super.setUp()
        formatter = new UnixDirectoryListingFormatter()
        dateFormat = new SimpleDateFormat(UnixDirectoryListingFormatter.DATE_FORMAT)
        lastModifiedFormatted = dateFormat.format(LAST_MODIFIED)
    }

    private void verifyFormat(FileInfo fileInfo, String expectedResult) {
        def result = formatter.format(fileInfo)
        LOG.info("result=  [$result]")
        LOG.info("expected=[$expectedResult]")
        assert result == expectedResult
    }

}