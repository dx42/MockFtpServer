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
package org.mockftpserver.fake.command

import java.text.MessageFormat
import org.apache.log4j.Logger
import org.mockftpserver.core.CommandSyntaxException
import org.mockftpserver.core.IllegalStateException
import org.mockftpserver.core.NotLoggedInException
import org.mockftpserver.core.command.Command
import org.mockftpserver.core.command.CommandHandler
import org.mockftpserver.core.command.ReplyCodes
import org.mockftpserver.core.session.Session
import org.mockftpserver.core.session.SessionKeys
import org.mockftpserver.fake.ServerConfiguration
import org.mockftpserver.fake.ServerConfigurationAware
import org.mockftpserver.fake.filesystem.ExistingFileOperationException
import org.mockftpserver.fake.filesystem.FileSystem
import org.mockftpserver.fake.filesystem.FileSystemException
import org.mockftpserver.fake.filesystem.InvalidFilenameException
import org.mockftpserver.fake.filesystem.NewFileOperationException
import org.mockftpserver.fake.user.UserAccount

/**
 * Abstract superclass for CommandHandler classes for the "Fake" server.
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
abstract class AbstractFakeCommandHandler implements CommandHandler, ServerConfigurationAware {

    final Logger LOG = Logger.getLogger(this.class)
    ServerConfiguration serverConfiguration

    /**
     * Reply code sent back when a FileSystemException is caught by the     {@link #handleCommand(Command, Session)}
     * This defaults to ReplyCodes.EXISTING_FILE_ERROR (550). 
     */
    int replyCodeForFileSystemException = ReplyCodes.EXISTING_FILE_ERROR

    /**
     * Use template method to centralize and ensure common validation
     */
    void handleCommand(Command command, Session session) {
        assert serverConfiguration != null
        assert command != null
        assert session != null

        try {
            handle(command, session)
        }
        catch (CommandSyntaxException e) {
            handleException(command, session, e, ReplyCodes.COMMAND_SYNTAX_ERROR)
        }
        catch (IllegalStateException e) {
            handleException(command, session, e, ReplyCodes.ILLEGAL_STATE)
        }
        catch (NotLoggedInException e) {
            handleException(command, session, e, ReplyCodes.NOT_LOGGED_IN)
        }
        catch (ExistingFileOperationException e) {
            handleException(command, session, e, ReplyCodes.EXISTING_FILE_ERROR, [e.path])
        }
        catch (NewFileOperationException e) {
            handleException(command, session, e, ReplyCodes.NEW_FILE_ERROR, [e.path])
        }
        catch (InvalidFilenameException e) {
            handleException(command, session, e, ReplyCodes.FILENAME_NOT_VALID, [e.path])
        }
        catch (FileSystemException e) {
            handleException(command, session, e, replyCodeForFileSystemException, [e.path])
        }
    }

    /**
     * Convenience method to return the FileSystem stored in the ServerConfiguration
     */
    protected FileSystem getFileSystem() {
        serverConfiguration.fileSystem
    }

    /**
     * Subclasses must implement this
     */
    protected abstract void handle(Command command, Session session)

    // -------------------------------------------------------------------------
    // Utility methods for subclasses
    // -------------------------------------------------------------------------

    /**
     * Send a reply for this command on the control connection.
     *
     * The reply code is designated by the <code>replyCode</code> property, and the reply text
     * is retrieved from the <code>replyText</code> ResourceBundle, using the specified messageKey.
     *
     * @param session - the Session
     * @param replyCode - the reply code
     * @param messageKey - the resource bundle key for the reply text
     * @param args - the optional message arguments; defaults to []
     *
     * @throws AssertionError - if session is null
     *
     * @see MessageFormat
     */
    protected void sendReply(Session session, int replyCode, String messageKey, List args = []) {
        assert session
        assertValidReplyCode(replyCode);

        String text = getTextForKey(messageKey)
        String replyText = (args) ? MessageFormat.format(text, args as Object[]) : text;

        String replyTextToLog = (replyText == null) ? "" : " " + replyText;
        // TODO change to LOG.debug()
        def argsToLog = (args) ? " args=$args" : ""
        LOG.info("Sending reply [" + replyCode + replyTextToLog + "]" + argsToLog);
        session.sendReply(replyCode, replyText);
    }

    /**
     * Send a reply for this command on the control connection.
     *
     * The reply code is designated by the <code>replyCode</code> property, and the reply text
     * is retrieved from the <code>replyText</code> ResourceBundle, using the reply code as the key.
     *
     * @param session - the Session
     * @param replyCode - the reply code
     * @param args - the optional message arguments; defaults to []
     *
     * @throws AssertionError - if session is null
     *
     * @see MessageFormat
     */
    protected void sendReply(Session session, int replyCode, List args = []) {
        sendReply(session, replyCode, replyCode.toString(), args)
    }

    /**
     * Handle the exception caught during handleCommand()
     * @param command - the Command
     * @param session - the Session
     * @param exception - the caught exception
     * @param replyCode - the reply code that should be sent back
     * @param args - the optional args for the reply (message)
     *
     */
    private handleException(Command command, Session session, Throwable exception, int replyCode, args = []) {
        LOG.warn("Error handling command: $command; ${exception}", exception)
        sendReply(session, replyCode, args)
    }

    /**
     * Assert that the specified number is a valid reply code
     * @param replyCode - the reply code to check
     *
     * @throws AssertionError - if the replyCode is invalid
     */
    protected void assertValidReplyCode(int replyCode) {
        assert replyCode > 0, "The number [" + replyCode + "] is not a valid reply code"
    }

    /**
     * Return the value of the command's parameter at the specified index.
     * @param command - the Command
     * @param index - the index of the parameter to retrieve; defaults to zero
     * @return the value of the command parameter
     * @throws CommandSyntaxException if the Command does not have a parameter at that index
     */
    protected String getRequiredParameter(Command command, int index = 0) {
        String value = command.getParameter(index)
        if (!value) {
            throw new CommandSyntaxException("$command missing required parameter at index [$index]")
        }
        return value
    }

    /**
     * Return the value of the named attribute within the session.
     * @param session - the Session
     * @param name - the name of the session attribute to retrieve
     * @return the value of the named session attribute
     * @throws IllegalStateException - if the Session does not contain the named attribute
     */
    protected Object getRequiredSessionAttribute(Session session, String name) {
        Object value = session.getAttribute(name)
        if (value == null) {
            throw new IllegalStateException("Session missing required attribute [$name]")
        }
        return value
    }

    /**
     * Verify that the current user (if any) has already logged in successfully.
     * @param session - the Session
     */
    protected void verifyLoggedIn(Session session) {
        if (session.getAttribute(SessionKeys.USER_ACCOUNT) == null) {
            throw new NotLoggedInException("User has not logged in")
        }
    }

    /**
     * Verify that the specified condition related to the file system is true,
     * otherwise throw a FileSystemException.
     *
     * @param condition - the condition that must be true
     * @param path - the path involved in the operation; this will be included in the
     * 		error message if the condition is not true.
     * @throws FileSystemException - if the condition is not true
     */
    protected void verifyFileSystemCondition(condition, path) {
        if (!condition) {
            throw new FileSystemException((String) path, "path [$path]")
        }
    }

    /**
     * Return the full, absolute path for the specified abstract pathname.
     * If path is null, return the current directory (stored in the session). If
     * path represents an absolute path, then return path as is. Otherwise, path
     * is relative, so assemble the full path from the current directory
     * and the specified relative path.
     * @param Session - the Session
     * @param path - the abstract pathname; may be null
     * @return the resulting full, absolute path
     */
    protected String getRealPath(Session session, String path) {
        def currentDirectory = session.getAttribute(SessionKeys.CURRENT_DIRECTORY)
        if (path == null) {
            return currentDirectory
        }
        if (fileSystem.isAbsolute(path)) {
            return path
        }
        return fileSystem.path(currentDirectory, path)
    }

    /**
     * Return the end-of-line character(s) used when building multi-line responses
     */
    protected String endOfLine() {
        "\n"
    }

    private String getTextForKey(key) {
        try {
            return serverConfiguration.replyTextBundle.getString(key.toString())
        }
        catch (MissingResourceException e) {
            // No reply text is mapped for the specified key
            LOG.warn("No reply text defined for key [${key.toString()}]");
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Login Support (used by USER and PASS commands)
    // -------------------------------------------------------------------------

    /**
     * Validate the UserAccount for the specified username. If valid, return true. If the UserAccount does
     * not exist or is invalid, log an error message, send back a reply code of 530 with an appropriate
     * error message, and return false. A UserAccount is considered invalid if the homeDirectory property
     * is not set or is set to a non-existent directory.
     * @param username - the username
     * @param session - the session; used to send back an error reply if necessary
     * @return true only if the UserAccount for the named user is valid
     */
    protected boolean validateUserAccount(String username, Session session) {
        def userAccount = serverConfiguration.getUserAccount(username)
        if (userAccount == null || !userAccount.valid) {
            LOG.error("UserAccount missing or not valid for username [$username]: $userAccount")
            sendReply(session, ReplyCodes.USER_ACCOUNT_NOT_VALID, "userAccountNotValid", [username])
            return false
        }

        def home = userAccount.homeDirectory
        if (!getFileSystem().isDirectory(home)) {
            LOG.error("Home directory configured for username [$username] is not valid: $home")
            sendReply(session, ReplyCodes.USER_ACCOUNT_NOT_VALID, "homeDirectoryNotValid", [username, home])
            return false
        }

        return true
    }

    /**
     * Log in the specified user for the current session. Send back a reply of 230 and set the UserAccount
     * and current directory (homeDirectory) in the session
     * @param userAccount - the userAccount for the user to be logged in
     * @param session - the session
     */
    protected void login(UserAccount userAccount, Session session) {
        sendReply(session, ReplyCodes.PASS_OK)
        session.setAttribute(SessionKeys.USER_ACCOUNT, userAccount)
        session.setAttribute(SessionKeys.CURRENT_DIRECTORY, userAccount.homeDirectory)
    }

}