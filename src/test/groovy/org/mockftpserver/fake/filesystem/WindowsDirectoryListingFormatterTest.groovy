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
import org.junit.jupiter.api.Test
import org.mockftpserver.test.AbstractGroovyTestCase

import java.text.SimpleDateFormat

/**
 * Tests for WindowsDirectoryListingFormatter
 *
 * @author Chris Mair
 */
class WindowsDirectoryListingFormatterTest extends AbstractGroovyTestCase {

    private static final String NAME = "def.txt"
    private static final String PATH = "/dir/$NAME"
    private static final Date LAST_MODIFIED = new Date()
    private static final int SIZE_WIDTH = WindowsDirectoryListingFormatter.SIZE_WIDTH

    private WindowsDirectoryListingFormatter formatter = new WindowsDirectoryListingFormatter()
    private SimpleDateFormat dateFormat = new SimpleDateFormat(WindowsDirectoryListingFormatter.DATE_FORMAT)
    private String lastModifiedFormatted = dateFormat.format(LAST_MODIFIED)
    private Locale defaultLocale = Locale.default

    @Test
    void testFormat_File() {
        def fileEntry = new FileEntry(path: PATH, contents: 'abcd', lastModified: LAST_MODIFIED)
        def sizeStr = 4.toString().padLeft(SIZE_WIDTH)
        def expected = "$lastModifiedFormatted  $sizeStr  $NAME"
        def result = formatter.format(fileEntry)
        LOG.info("result=$result")
        assert result == expected
    }

    @Test
    void testFormat_Directory() {
        def fileEntry = new DirectoryEntry(path: PATH, lastModified: LAST_MODIFIED)
        def dirStr = "<DIR>".padRight(SIZE_WIDTH)
        def expected = "$lastModifiedFormatted  $dirStr  $NAME"
        def result = formatter.format(fileEntry)
        LOG.info("result=$result")
        assert result == expected
    }

    @Test
    void testFormat_File_NonEnglishDefaultLocale() {
        Locale.setDefault(Locale.GERMAN)
        def fileEntry = new FileEntry(path: PATH, contents: 'abcd', lastModified: LAST_MODIFIED)
        def sizeStr = 4.toString().padLeft(SIZE_WIDTH)
        def expected = "$lastModifiedFormatted  $sizeStr  $NAME"
        def result = formatter.format(fileEntry)
        LOG.info("result=$result")
        assert result == expected
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(defaultLocale)
    }

}