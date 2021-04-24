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

import org.junit.jupiter.api.Test

import java.lang.reflect.Constructor
import org.mockftpserver.test.AbstractGroovyTestCase

/**
 * Abstract test superclass for subclasses of AbstractFileSystemEntry
 *
 * @author Chris Mair
 */
public abstract class AbstractFileSystemEntryTestCase extends AbstractGroovyTestCase {

    protected static final PATH = "c:/test/dir"
    protected static final NEW_PATH = "d:/other/dir"
    protected static final USER = 'user77'
    protected static final GROUP = 'group88'
    protected static final PERMISSIONS = new Permissions('rwxrwx---')
    protected static final LAST_MODIFIED = new Date()

    @Test
    void testConstructor_NoArgs() {
        AbstractFileSystemEntry entry = (AbstractFileSystemEntry) getImplementationClass().newInstance()
        assert entry.getPath() == null
        entry.setPath(PATH)
        assert entry.getPath() == PATH
        assert isDirectory() == entry.isDirectory()
    }

    @Test
    void testConstructor_Path() {
        Constructor constructor = getImplementationClass().getConstructor([String.class] as Class[])
        AbstractFileSystemEntry entry = (AbstractFileSystemEntry) constructor.newInstance([PATH] as Object[])
        LOG.info(entry.toString())
        assert entry.getPath() == PATH
        entry.setPath("")
        assert entry.getPath() == ""
        assert isDirectory() == entry.isDirectory()
    }

    @Test
    void testLockPath() {
        def entry = createFileSystemEntry(PATH)
        entry.lockPath()
        shouldFail { entry.path = 'abc' }
        assert entry.path == PATH
    }

    @Test
    void testGetName() {
        assert createFileSystemEntry('abc').name == 'abc'
        assert createFileSystemEntry('/abc').name == 'abc'
        assert createFileSystemEntry('/dir/abc').name == 'abc'
        assert createFileSystemEntry('\\abc').name == 'abc'
    }

    @Test
    void testSetPermissionsFromString() {
        def entry = createFileSystemEntry('abc')
        final PERM = 'rw-r---wx'
        entry.setPermissionsFromString(PERM)
        assert entry.permissions == new Permissions(PERM)
    }

    protected AbstractFileSystemEntry createFileSystemEntry(String path) {
        def entry = (AbstractFileSystemEntry) getImplementationClass().newInstance()
        entry.setPath(path)
        return entry
    }

    /**
     * @return the subclass of AbstractFileSystemEntry to be tested
     */
    protected abstract Class getImplementationClass()

    /**
     * @return true if the class being tested represents a directory entry 
     */
    protected abstract boolean isDirectory()

}
