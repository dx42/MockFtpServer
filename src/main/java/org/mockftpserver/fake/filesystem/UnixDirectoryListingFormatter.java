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
package org.mockftpserver.fake.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockftpserver.core.util.StringUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

/**
 * Unix-specific implementation of the DirectoryListingFormatter interface.
 *
 * @author Chris Mair
 */
public class UnixDirectoryListingFormatter implements DirectoryListingFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(UnixDirectoryListingFormatter.class);

    protected static final String DATE_FORMAT_YEAR = "MMM dd  yyyy";
    protected static final String DATE_FORMAT_HOURS_MINUTES = "MMM dd HH:mm";
    private static final int SIZE_WIDTH = 15;
    private static final int OWNER_WIDTH = 8;
    private static final int GROUP_WIDTH = 8;
    private static final String NONE = "none";

    private Locale locale = Locale.ENGLISH;

    /**
     * Format the directory listing for a single file/directory entry.
     *
     * @param fileSystemEntry - the FileSystemEntry for a single file system entry
     * @return the formatted directory listing
     */
    public String format(FileSystemEntry fileSystemEntry) {
        String dateStr = formatLastModifiedDate(fileSystemEntry);
        String dirOrFile = fileSystemEntry.isDirectory() ? "d" : "-";
        String permissionsStr = formatPermissions(fileSystemEntry);
        String linkCountStr = "1";
        String ownerStr = StringUtil.padRight(stringOrNone(fileSystemEntry.getOwner()), OWNER_WIDTH);
        String groupStr = StringUtil.padRight(stringOrNone(fileSystemEntry.getGroup()), GROUP_WIDTH);
        String sizeStr = StringUtil.padLeft(Long.toString(fileSystemEntry.getSize()), SIZE_WIDTH);
        String listing = "" + dirOrFile + permissionsStr + "  " + linkCountStr + " " + ownerStr + " " + groupStr + " " + sizeStr + " " + dateStr + " " + fileSystemEntry.getName();
        LOG.info("listing=[" + listing + "]");
        return listing;
    }

    /**
     * Set the Locale to be used in formatting the date within file/directory listings
     * @param locale - the Locale instance
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private String formatLastModifiedDate(FileSystemEntry fileSystemEntry) {
        Date showYearThresholdDate = Date.from(Instant.now().minus(Duration.ofDays(180)));
        String formatString = fileSystemEntry.getLastModified().before(showYearThresholdDate) ? DATE_FORMAT_YEAR : DATE_FORMAT_HOURS_MINUTES;
        DateFormat dateFormat = new SimpleDateFormat(formatString, locale);
        return dateFormat.format(fileSystemEntry.getLastModified());
    }

    private String formatPermissions(FileSystemEntry fileSystemEntry) {
        Permissions permissions = fileSystemEntry.getPermissions() != null ? fileSystemEntry.getPermissions() : Permissions.DEFAULT;
        return StringUtil.padRight(permissions.asRwxString(), 9);
    }

    private String stringOrNone(String string) {
        return (string == null) ? NONE : string;
    }

}
