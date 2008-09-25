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

import org.apache.log4j.Logger
import org.mockftpserver.core.util.PatternUtil

/**
 * Abstract superclass for implementation of the FileSystem interface that manage the files 
 * and directories in memory, simulating a real file system.
 * <p>
 * If the <code>createParentDirectoriesAutomatically</code> property is set to <code>true</code>,
 * then creating a directory or file will automatically create any parent directories (recursively)
 * that do not already exist. If <code>false</code>, then creating a directory or file throws an
 * exception if its parent directory does not exist. This value defaults to <code>true</code>.
 * <p>
 * The <code>directoryListingFormatter</code> property holds an instance of   {@link DirectoryListingFormatter}                 ,
 * used by the   {@link #formatDirectoryListing}   method to format directory listings in a
 * filesystem-specific manner. This property must be initialized by concrete subclasses.
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
abstract class AbstractFakeFileSystem implements FileSystem {

    private static final LOG = Logger.getLogger(AbstractFakeFileSystem)

    /**
     * If <code>true</code>, creating a directory or file will automatically create
     * any parent directories (recursively) that do not already exist. If <code>false</code>,
     * then creating a directory or file throws an exception if its parent directory
     * does not exist. This value defaults to <code>true</code>.
     */
    boolean createParentDirectoriesAutomatically = true

    /**
     * The   {@link DirectoryListingFormatter}   used by the   {@link #formatDirectoryListing(FileSystemEntry)}
     * method. This must be initialized by concrete subclasses. 
     */
    DirectoryListingFormatter directoryListingFormatter

    private Map entries = new HashMap()

    //-------------------------------------------------------------------------
    // Public API
    //-------------------------------------------------------------------------

    /**
     * Add the specified file system entry (file or directory) to this file system
     *
     * @param entry - the FileSystemEntry to add
     */
    public void addEntry(FileSystemEntry entry) {
        def normalized = normalize(entry.path)
        if (entries.get(normalized) != null) {
            throw new FileSystemException(normalized, 'filesystem.pathAlreadyExists')
        }

        // Make sure parent directory exists, if there is a parent
        String parent = getParent(normalized)
        if (parent != null) {
            verifyPathExists(parent)
        }

        // Set lastModified, if not already set
        if (!entry.lastModified) {
            entry.lastModified = new Date()
        }

        entries.put(normalized, entry)
        entry.lockPath()
    }

    /**
     * Creates an empty file with the specified pathname.
     *
     * @param path - the path of the filename to create
     * @return true if and only if the file was created false otherwise
     *
     * @throws AssertionError - if path is null
     * @throws FileSystemException - if the parent directory of the path does not exist or if an I/O error occurs
     * @throws InvalidFilenameException - if path specifies an invalid (illegal) filename
     *
     * @see FileSystem#createFile(String)
     */
    public boolean createFile(String path) {
        assert path != null
        checkForInvalidFilename(path)

        // TODO Consider refactoring into addEntry()
        if (!parentDirectoryExists(path)) {
            String parent = getParent(path)
            if (createParentDirectoriesAutomatically) {
                if (!createDirectory(parent)) {
                    return false
                }
            }
            else {
                throw new FileSystemException(parent, 'filesystem.parentDirectoryDoesNotExist')
            }
        }

        if (exists(path)) {
            return false
        }
        String normalizedPath = normalize(path)
        addEntry(new FileEntry(normalizedPath))
        return true
    }

    /**
     * Creates the directory named by the specified pathname.
     *
     * @param path - the path of the directory to create
     * @return true if and only if the directory was created false otherwise
     *
     * @throws AssertionError - if path is null
     * @throws FileSystemException - if the parent directory of the path does not exist or if an I/O error occurs
     *
     * @see org.mockftpserver.fake.filesystem.FileSystem#createDirectory(java.lang.String)
     */
    public boolean createDirectory(String path) {
        assert path != null
        String normalizedPath = normalize(path)

        if (!parentDirectoryExists(path)) {
            String parent = getParent(path)
            if (createParentDirectoriesAutomatically) {
                if (!createDirectory(parent)) {
                    return false
                }
            }
            else {
                throw new FileSystemException(parent, 'filesystem.parentDirectoryDoesNotExist')
            }
        }
        try {
            addEntry(new DirectoryEntry(normalizedPath))
            return true
        }
        catch (FileSystemException e) {
            return false
        }
    }

    /**
     * Create and return a new InputStream for reading from the file at the specified path
     * @param path - the path of the file
     *
     * @throws AssertionError - if path is null
     * @throws FileSystemException - wraps a FileNotFoundException if thrown
     *
     * @see org.mockftpserver.fake.filesystem.FileSystem#createInputStream(java.lang.String)
     */
    public InputStream createInputStream(String path) {
        verifyPathExists(path)
        verifyIsFile(path)
        FileEntry fileEntry = (FileEntry) getEntry(path)
        return fileEntry.createInputStream()
    }

    /**
     * Create and return a new OutputStream for writing to the file at the specified path
     * @param path - the path of the file
     * @param append - true if the OutputStream should append to the end of the file if the file already exists
     *
     * @throws AssertionError - if path is null
     * @throws FileSystemException - wraps a FileNotFoundException if thrown
     *
     * @see org.mockftpserver.fake.filesystem.FileSystem#createOutputStream(java.lang.String, boolean)
     */
    public OutputStream createOutputStream(String path, boolean append) {
        checkForInvalidFilename(path)
        verifyParentDirectoryExists(path)
        if (exists(path)) {
            verifyIsFile(path)
        }
        else {
            addEntry(new FileEntry(path))
        }
        FileEntry fileEntry = (FileEntry) getEntry(path)
        return fileEntry.createOutputStream(append)
    }

    /**
     * Delete the file or directory specified by the path. Return true if the file is successfully
     * deleted, false otherwise. If the path refers to a directory, it must be empty. Return false
     * if the path does not refer to a valid file or directory or if it is a non-empty directory.
     *
     * @param path - the path of the file or directory to delete
     * @return true if the file or directory is successfully deleted
     *
     * @throws AssertionError - if path is null
     *
     * @see org.mockftpserver.fake.filesystem.FileSystem#delete(java.lang.String)
     */
    public boolean delete(String path) {
        assert path != null
        String key = normalize(path)
        FileSystemEntry entry = getEntry(key)

        if (entry != null && !hasChildren(path)) {
            entries.remove(key)
            return true
        }
        return false
    }

    /**
     * Return true if there exists a file or directory at the specified path
     *
     * @param path - the path
     * @return true if the file/directory exists
     *
     * @throws AssertionError - if path is null
     *
     * @see org.mockftpserver.fake.filesystem.FileSystem#exists(java.lang.String)
     */
    public boolean exists(String path) {
        assert path != null
        return getEntry(path) != null
    }

    /**
     * Return true if the specified path designates an existing directory, false otherwise
     *
     * @param path - the path
     * @return true if path is a directory, false otherwise
     *
     * @throws AssertionError - if path is null
     *
     * @see org.mockftpserver.fake.filesystem.FileSystem#isDirectory(java.lang.String)
     */
    public boolean isDirectory(String path) {
        assert path != null
        FileSystemEntry entry = getEntry(path)
        return entry != null && entry.isDirectory()
    }

    /**
     * Return true if the specified path designates an existing file, false otherwise
     *
     * @param path - the path
     * @return true if path is a file, false otherwise
     *
     * @throws AssertionError - if path is null
     *
     * @see org.mockftpserver.fake.filesystem.FileSystem#isFile(java.lang.String)
     */
    public boolean isFile(String path) {
        assert path != null
        FileSystemEntry entry = getEntry(path)
        return entry != null && !entry.isDirectory()
    }

    /**
     * Return the List of FileSystemEntry objects for the files in the specified directory or group of
     * files. If the path specifies a single file, then return a list with a single FileSystemEntry
     * object representing that file. If the path does not refer to an existing directory or
     * group of files, then an empty List is returned.
     *
     * @param path - the path specifying a directory or group of files; may contain wildcards (? or *)
     * @return the List of FileSystemEntry objects for the specified directory or file; may be empty
     *
     * @see org.mockftpserver.fake.filesystem.FileSystem#listFiles(java.lang.String)
     */
    public List listFiles(String path) {
        if (isFile(path)) {
            return [getEntry(path)]
        }

        List entryList = []
        List children = children(path)
        children?.each {childPath ->
            def fileSystemEntry = getEntry(childPath)
            entryList.add(fileSystemEntry)
        }
        return entryList
    }

    /**
     * Return the List of filenames in the specified directory path or file path. If the path specifies
     * a single file, then return that single filename. The returned filenames do not
     * include a path. If the path does not refer to a valid directory or file path, then an empty List
     * is returned.
     *
     * @param path - the path specifying a directory or group of files; may contain wildcards (? or *)
     * @return the List of filenames (not including paths) for all files in the specified directory
     *         or file path; may be empty
     *
     * @throws AssertionError - if path is null
     *
     * @see org.mockftpserver.fake.filesystem.FileSystem#listNames(java.lang.String)
     */
    public List listNames(String path) {
        if (isFile(path)) {
            return [getName(path)]
        }

        List filenames = new ArrayList()
        List children = children(path)
        children.each {childPath ->
            filenames.add(getName(childPath))
        }
        return filenames
    }

    /**
     * Rename the file or directory. Specify the FROM path and the TO path. Return true if the file
     * is successfully renamed, false otherwise. Return false if the path does not refer to a valid
     * file or directory.
     *
     * @param path - the path of the file or directory to delete
     * @param fromPath - the source (old) path + filename
     * @param toPath - the target (new) path + filename
     * @return true if the file or directory is successfully renamed
     *
     * @throws AssertionError - if fromPath or toPath is null
     * @throws FileSystemException - if either the FROM path or the parent directory of the TO path do not exist
     */
    public boolean rename(String fromPath, String toPath) {
        assert toPath != null
        assert fromPath != null

        FileSystemEntry entry = getRequiredEntry(fromPath)

        String normalizedFromPath = normalize(fromPath)
        String normalizedToPath = normalize(toPath)

        if (!entry.isDirectory()) {
            renamePath(entry, normalizedToPath)
            return true
        }

        // Create the TO directory entry first so that the destination path exists when you
        // move the children. Remove the FROM path after all children have been moved
        if (!createDirectory(normalizedToPath)) {
            throw new FileSystemException(normalizedToPath, 'filesystem.parentDirectoryDoesNotExist')
        }

        List children = descendents(fromPath)
        children.each {childPath ->
            FileSystemEntry child = getRequiredEntry(childPath)
            String normalizedChildPath = normalize(child.getPath())
            assert normalizedChildPath.startsWith(normalizedFromPath), "Starts with FROM path"
            String childToPath = normalizedToPath + normalizedChildPath.substring(normalizedFromPath.length())
            renamePath(child, childToPath)
        }
        assert children(normalizedFromPath) == []
        entries.remove(normalizedFromPath)
        return true
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.class.name + entries
    }

    /**
     * Return the formatted directory listing entry for the file represented by the specified FileSystemEntry
     * @param fileSystemEntry - the FileSystemEntry representing the file or directory entry to be formatted
     * @return the the formatted directory listing entry
     */
    public String formatDirectoryListing(FileSystemEntry fileSystemEntry) {
        assert directoryListingFormatter
        assert fileSystemEntry
        return directoryListingFormatter.format(fileSystemEntry)
    }

    /**
     * Build a path from the two path components. Concatenate path1 and path2. Insert the path
     * separator character in between if necessary (i.e., if both are non-empty and path1 does not already
     * end with a separator character AND path2 does not begin with one).
     *
     * @param path1 - the first path component may be null or empty
     * @param path2 - the second path component may be null or empty
     * @return the path resulting from concatenating path1 to path2
     */
    String path(String path1, String path2) {
        StringBuffer buf = new StringBuffer()
        if (path1 != null && path1.length() > 0) {
            buf.append(path1)
        }
        if (path2 != null && path2.length() > 0) {
            if ((path1 != null && path1.length() > 0)
                    && (!isSeparator(path1.charAt(path1.length() - 1)))
                    && (!isSeparator(path2.charAt(0)))) {
                buf.append(this.separator)
            }
            buf.append(path2)
        }
        return buf.toString()
    }

    /**
     * Return the standard, normalized form of the path. 
     * @param path - the path
     * @return the path in a standard, unique, canonical form
     *
     * @throws AssertionError - if path is null
     */
    String normalize(String path) {
        return componentsToPath(normalizedComponents(path))
    }

    /**
     * Return the parent path of the specified path. If <code>path</code> specifies a filename,
     * then this method returns the path of the directory containing that file. If <code>path</code>
     * specifies a directory, the this method returns its parent directory. If <code>path</code> is
     * empty or does not have a parent component, then return an empty string.
     * <p>
     * All path separators in the returned path are converted to the system-dependent separator character.
     * @param path - the path
     * @return the parent of the specified path, or null if <code>path</code> has no parent
     *
     * @throws AssertionError - if path is null
     */
    String getParent(String path) {
        def parts = normalizedComponents(path)
        if (parts.size() < 2) {
            return null
        }
        parts.remove(parts.size() - 1)
        return componentsToPath(parts)
    }

    /**
     * Returns the name of the file or directory denoted by this abstract
     * pathname.  This is just the last name in the pathname's name
     * sequence.  If the pathname's name sequence is empty, then the empty
     * string is returned.
     *
     * @return The name of the file or directory denoted by this abstract pathname, or the
     *          empty string if this pathname's name sequence is empty
     *
     * @see File#getName()
     */
    String getName(String path) {
        assert path != null
        def normalized = normalize(path)
        int separatorIndex = normalized.lastIndexOf(this.separator)
        return (separatorIndex == -1) ? normalized : normalized.substring(separatorIndex + 1)
    }

    /**
     * Returns the FileSystemEntry object representing the file system entry at the specified path, or null
     * if the path does not specify an existing file or directory within this file system.
     * @param path - the path of the file or directory within this file system
     * @return the FileSystemEntry containing the information for the file or directory, or else null
     *
     * @see FileSystem#getEntry(String)
     */
    FileSystemEntry getEntry(String path) {
        return (FileSystemEntry) entries.get(normalize(path))
    }

    //-------------------------------------------------------------------------
    // Abstract Methods
    //-------------------------------------------------------------------------

    /**
     * @return true if the specified dir/file path name is valid according to the current filesystem.
     */
    protected abstract boolean isValidName(String path)

    /**
     * @return the file system-specific file separator
     */
    protected abstract String getSeparator()

    /**
     * @return true if the specified path component is a root for this filesystem
     */
    protected abstract boolean isRoot(String pathComponent)

    /**
     * Return true if the specified char is a separator character for this filesystem
     * @param c - the character to test
     * @return true if the specified char is a separator character
     */
    protected abstract boolean isSeparator(char c)

    ;

    //-------------------------------------------------------------------------
    // Internal Helper Methods
    //-------------------------------------------------------------------------

    /**
     * Throw an InvalidFilenameException if the specified path is not valid.
     */
    protected void checkForInvalidFilename(String path) {
        if (!isValidName(path)) {
            throw new InvalidFilenameException(path);
        }
    }

    /**
     * Rename the file system entry to the specified path name
     * @param entry - the file system entry
     * @param toPath - the TO path (normalized)
     */
    protected void renamePath(FileSystemEntry entry, String toPath) {
        def normalizedFrom = normalize(entry.path)
        def normalizedTo = normalize(toPath)
        LOG.info("renaming from [" + normalizedFrom + "] to [" + normalizedTo + "]")
        def newEntry = entry.cloneWithNewPath(normalizedTo)
        addEntry(newEntry)
        // Do this at the end, in case the addEntry() failed
        entries.remove(normalizedFrom)
    }

    /**
     * Return the FileSystemEntry for the specified path. Throw FileSystemException if the
     * specified path does not exist.
     *
     * @param path - the path
     * @return the FileSystemEntry
     *
     * @throws FileSystemException - if the specified path does not exist
     */
    protected FileSystemEntry getRequiredEntry(String path) {
        FileSystemEntry entry = getEntry(path)
        if (entry == null) {
            LOG.error("Path does not exist: $path")
            throw new FileSystemException(normalize(path), 'filesystem.pathDoesNotExist')
        }
        return entry
    }

    /**
     * Return the components of the specified path as a List. The components are normalized, and
     * the returned List does not include path separator characters.
     */
    protected List normalizedComponents(String path) {
        assert path != null
        def otherSeparator = this.separator == '/' ? '\\' : '/'
        def p = path.replace(otherSeparator, this.separator)

        // TODO better way to do this
        if (p == this.separator) {
            return [""]
        }

        def parts = p.split("\\" + this.separator) as List
        def result = []
        parts.each {part ->
            if (part == "..") {
                result.remove(result.size() - 1)
            }
            else if (part != ".") {
                result << part
            }
        }
        return result
    }

    /**
     * Build a path from the specified list of path components
     * @param components - the list of path components
     * @return the resulting path
     */
    protected String componentsToPath(List components) {
        if (components.size() == 1) {
            def first = components[0]
            if (first == "" || isRoot(first)) {
                return first + this.separator
            }
        }
        return components.join(this.separator)
    }

    /**
     * Return true if the specified path designates an absolute file path.
     *
     * @param path - the path
     * @return true if path is absolute, false otherwise
     *
     * @throws AssertionError - if path is null
     */
    boolean isAbsolute(String path) {
        return isValidName(path)
    }

    /**
     * Return true if the specified path exists
     *
     * @param path - the path
     * @return true if the path exists
     */
    private boolean pathExists(String path) {
        return entries.get(normalize(path)) != null
    }

    /**
     * Throw AssertionError if the path is null. Throw FileSystemException if the specified
     * path does not exist.
     *
     * @param path - the path
     * @throws AssertionError - if the specified path is null
     * @throws FileSystemException - if the specified path does not exist
     */
    private void verifyPathExists(String path) {
        assert path != null
        getRequiredEntry(path)
    }

    /**
     * Verify that the path refers to an existing directory. Throw AssertionError if the path is null. Throw
     * FileSystemException if the specified path does not exist or is not a directory.
     *
     * @param path - the path
     *
     * @throws AssertionError - if the specified path is null
     * @throws FileSystemException - if the specified path does not exist or is not a directory
     */
    private void verifyIsDirectory(String path) {
        assert path != null
        FileSystemEntry entry = getRequiredEntry(path)
        if (!entry.isDirectory()) {
            throw new FileSystemException(path, 'filesystem.isDirectory')
        }
    }

    /**
     * Verify that the path refers to an existing file. Throw AssertionError if the path is null. Throw
     * FileSystemException if the specified path does not exist or is not a file.
     *
     * @param path - the path
     *
     * @throws AssertionError - if the specified path is null
     * @throws FileSystemException - if the specified path does not exist or is not a file
     */
    private void verifyIsFile(String path) {
        assert path != null
        FileSystemEntry entry = getRequiredEntry(path)
        if (entry.isDirectory()) {
            throw new FileSystemException(path, 'filesystem.isFile')
        }
    }

    /**
     * Throw a FileSystemException if the parent directory for the specified path does not exist.
     * @param path - the path
     * @throws FileSystemException - if the parent directory of the path does not exist
     */
    private void verifyParentDirectoryExists(String path) throws FileSystemException {
        if (!parentDirectoryExists(path)) {
            throw new FileSystemException(getParent(path), 'filesystem.parentDirectoryDoesNotExist')
        }
    }

    /**
     * If the specified path has a parent, then verify that the parent exists
     * @param path - the path
     */
    private boolean parentDirectoryExists(String path) {
        String parent = getParent(path)
        if (parent != null) {
            return pathExists(parent)
        }
        return true
    }

    /**
     * Return true if the specified path represents a directory that contains one or more files or subdirectories
     * @param path - the path
     * @return true if the path has child entries
     */
    private boolean hasChildren(String path) {
        if (!isDirectory(path)) {
            return false
        }
        String normalizedPath = normalize(path)
        return entries.keySet().find {p -> p.startsWith(normalizedPath) && !normalizedPath.equals(p) }
    }

    /**
     * Return the List of files or subdirectory paths that are descendents of the specified path
     * @param path - the path
     * @return the List of the paths for the files and subdirectories that are children, grandchildren, etc.
     */
    private List descendents(String path) {
        if (isDirectory(path)) {
            String normalizedPath = normalize(path)
            String separator = (normalizedPath.endsWith(SEPARATOR)) ? '' : SEPARATOR
            String normalizedDirPrefix = normalizedPath + separator
            List descendents = new ArrayList()
            entries.keySet().each {p ->
                if (p.startsWith(normalizedDirPrefix) && !normalizedPath.equals(p)) {
                    descendents.add(p)
                }
            }
            return descendents
        }
        return Collections.EMPTY_LIST
    }

    /**
     * Return the List of files or subdirectory paths that are children of the specified path
     * @param path - the path
     * @return the List of the paths for the files and subdirectories that are children
     */
    private List children(String path) {
        def lastComponent = getName(path)
        def containsWildcards = PatternUtil.containsWildcards(lastComponent)
        def dir = containsWildcards ? getParent(path) : path
        def pattern = containsWildcards ? PatternUtil.convertStringWithWildcardsToRegex(getName(path)) : null
        LOG.info("path=$path  lastComponent=$lastComponent  containsWildcards=$containsWildcards  dir=$dir  pattern=$pattern")

        List descendents = descendents(dir)
        List children = []
        String normalizedDir = normalize(dir)
        descendents.each {descendentPath ->
            if (normalizedDir.equals(getParent(descendentPath))) {
                if (!pattern || (pattern && getName(descendentPath) ==~ pattern)) {
                    children.add(descendentPath)
                }
            }
        }
        return children
    }

}