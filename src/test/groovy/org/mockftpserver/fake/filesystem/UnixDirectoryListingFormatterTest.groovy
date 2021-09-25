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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockftpserver.test.AbstractGroovyTestCase

import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant

/**
 * Tests for UnixDirectoryListingFormatter
 *
 * @author Chris Mair
 */
class UnixDirectoryListingFormatterTest extends AbstractGroovyTestCase {

    private static final String FILE_NAME = "def.txt"
    private static final String FILE_PATH = "/dir/$FILE_NAME"
    private static final String DIR_NAME = "etc"
    private static final String DIR_PATH = "/dir/$DIR_NAME"
    private static final String OWNER = 'owner123'
    private static final String GROUP = 'group456'
    private static final Date LAST_MODIFIED_RECENT = new Date()
    private static final Date LAST_MODIFIED_OLDER = Date.from(Instant.now().minus(Duration.ofDays(181)))
    private static final Permissions FILE_PERMISSIONS = new Permissions('rw-r--r--')
    private static final Permissions DIR_PERMISSIONS = new Permissions('rwxr-xr-x')

    private UnixDirectoryListingFormatter formatter = new UnixDirectoryListingFormatter()
    private Locale defaultLocale = Locale.default
    private String lastModifiedRecentFormatted
    private String lastModifiedOlderFormatted

    // "-rw-rw-r--    1 ftp      ftp           254 Feb 23  2007 robots.txt"
    // "-rw-r--r--    1 ftp      ftp      30014925 Apr 15 00:19 md5.sums.gz"
    // "-rwxr-xr-x   1 henry    users       5778 Dec  1  2005 planaccess.sql"
    // "drwxr-xr-x   2 c096336  iawebgrp    8192 Nov  7  2006 tmp"
    // "drwxr-xr-x   39 ftp      ftp          4096 Mar 19  2004 a"

    @Test
    void testFormat_File_Recent() {
        def fileSystemEntry = new FileEntry(path: FILE_PATH, contents: '12345678901', lastModified: LAST_MODIFIED_RECENT,
                owner: OWNER, group: GROUP, permissions: FILE_PERMISSIONS)
        verifyFormat(fileSystemEntry, "-rw-r--r--  1 owner123 group456              11 $lastModifiedRecentFormatted def.txt")
    }

    @Test
    void testFormat_File_Older() {
        def fileSystemEntry = new FileEntry(path: FILE_PATH, contents: '12345678901', lastModified: LAST_MODIFIED_OLDER,
                owner: OWNER, group: GROUP, permissions: FILE_PERMISSIONS)
        verifyFormat(fileSystemEntry, "-rw-r--r--  1 owner123 group456              11 $lastModifiedOlderFormatted def.txt")
    }

    @Test
    void testFormat_File_Defaults() {
        def fileSystemEntry = new FileEntry(path: FILE_PATH, contents: '12345678901', lastModified: LAST_MODIFIED_RECENT)
        verifyFormat(fileSystemEntry, "-rwxrwxrwx  1 none     none                  11 $lastModifiedRecentFormatted def.txt")
    }

    @Test
    void testFormat_File_NonEnglishDefaultLocale() {
        Locale.setDefault(Locale.GERMAN)
        def fileSystemEntry = new FileEntry(path: FILE_PATH, contents: '12345678901', lastModified: LAST_MODIFIED_RECENT)
        verifyFormat(fileSystemEntry, "-rwxrwxrwx  1 none     none                  11 $lastModifiedRecentFormatted def.txt")
    }

    @Test
    void testFormat_File_NonEnglishLocale() {
        formatter.setLocale(Locale.FRENCH)
        def fileSystemEntry = new FileEntry(path: FILE_PATH, contents: '12345678901', lastModified: LAST_MODIFIED_OLDER)
        LOG.info(fileSystemEntry.toString())
        def dateFormat = new SimpleDateFormat(UnixDirectoryListingFormatter.DATE_FORMAT_YEAR, Locale.FRENCH)
        def formattedDate = dateFormat.format(LAST_MODIFIED_OLDER)
        def result = formatter.format(fileSystemEntry)
        assert result.contains(formattedDate)
    }

    @Test
    void testFormat_Directory_Recent() {
        def fileSystemEntry = new DirectoryEntry(path: DIR_PATH, lastModified: LAST_MODIFIED_RECENT,
                owner: OWNER, group: GROUP, permissions: DIR_PERMISSIONS)
        verifyFormat(fileSystemEntry, "drwxr-xr-x  1 owner123 group456               0 $lastModifiedRecentFormatted etc")
    }

    @Test
    void testFormat_Directory_Older() {
        def fileSystemEntry = new DirectoryEntry(path: DIR_PATH, lastModified: LAST_MODIFIED_OLDER,
                owner: OWNER, group: GROUP, permissions: DIR_PERMISSIONS)
        verifyFormat(fileSystemEntry, "drwxr-xr-x  1 owner123 group456               0 $lastModifiedOlderFormatted etc")
    }

    @Test
    void testFormat_Directory_Defaults() {
        def fileSystemEntry = new DirectoryEntry(path: DIR_PATH, lastModified: LAST_MODIFIED_RECENT)
        verifyFormat(fileSystemEntry, "drwxrwxrwx  1 none     none                   0 $lastModifiedRecentFormatted etc")
    }

    @BeforeEach
    void setUp() {
        def dateFormatRecent = new SimpleDateFormat(UnixDirectoryListingFormatter.DATE_FORMAT_HOURS_MINUTES, Locale.ENGLISH)
        lastModifiedRecentFormatted = dateFormatRecent.format(LAST_MODIFIED_RECENT)

        def dateFormatOlder = new SimpleDateFormat(UnixDirectoryListingFormatter.DATE_FORMAT_YEAR, Locale.ENGLISH)
        lastModifiedOlderFormatted = dateFormatOlder.format(LAST_MODIFIED_OLDER)
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(defaultLocale)
    }

    private void verifyFormat(FileSystemEntry fileSystemEntry, String expectedResult) {
        LOG.info(fileSystemEntry.toString())
        def result = formatter.format(fileSystemEntry)
        LOG.info("result=  [$result]")
        LOG.info("expected=[$expectedResult]")
        assert result == expectedResult
    }

}