package org.mockftpserver.fake.command;

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystemEntry;

import java.util.Collections;

/**
 * CommandHandler for the SIZE command. Handler logic:
 * <ol>
 *   <li>If the user has not logged in, then reply with 530 and terminate</li>
 *   <li>If the required pathname parameter is missing, then reply with 501 and terminate</li>
 *   <li>If the pathname parameter does not specify a valid, existing filename, then reply with 550 and terminate</li>
 *   <li>
 *       If the current user does not have read access to the file at the specified path or execute permission
 *       to its directory, then reply with 550 and terminate
 *   </li>
 *   <li>Otherwise, reply with 213</li>
 * </ol>
 *
 * @author Edoardo Luppi
 */
public class SizeCommandHandler extends AbstractFakeCommandHandler {
    @Override
    protected void handle(final Command command, final Session session) {
        verifyLoggedIn(session);

        final String path = getRealPath(session, command.getRequiredParameter(0));
        final FileSystemEntry entry = getFileSystem().getEntry(path);

        verifyFileSystemCondition(entry != null, path, "filesystem.doesNotExist");
        verifyFileSystemCondition(!entry.isDirectory(), path, "filesystem.doesNotExist");
        verifyReadPermission(session, path);
        verifyExecutePermission(session, getFileSystem().getParent(path));

        final FileEntry fileEntry = (FileEntry) entry;
        final String size = String.valueOf(fileEntry.getSize());
        sendReply(session, ReplyCodes.SIZE_OK, "size", Collections.singletonList(size));
    }
}
