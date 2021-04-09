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
package org.mockftpserver.fake

import org.mockftpserver.fake.filesystem.FileEntry
import org.mockftpserver.fake.filesystem.FileSystemEntry
import org.mockftpserver.fake.filesystem.Permissions
import org.mockftpserver.test.AbstractGroovyTestCase

/**
 * Tests for UserAccount
 *
 * @author Chris Mair
 */
class UserAccountTest extends AbstractGroovyTestCase {

    private static final USERNAME = "user123"
    private static final PASSWORD = "password123"
    private static final HOME_DIR = "/usr/user123"
    private static final GROUP = 'group'

    private UserAccount userAccount

    void testConstructor() {
        def acct = new UserAccount(USERNAME, PASSWORD, HOME_DIR)
        assert acct.username == USERNAME
        assert acct.password == PASSWORD
        assert acct.homeDirectory == HOME_DIR
    }

    void testGetPrimaryGroup() {
        assert userAccount.primaryGroup == UserAccount.DEFAULT_GROUP

        userAccount.groups = ['abc']
        assert userAccount.primaryGroup == 'abc'

        userAccount.groups.add('def')
        assert userAccount.primaryGroup == 'abc'

        userAccount.groups = []
        assert userAccount.primaryGroup == UserAccount.DEFAULT_GROUP
    }

    void testIsValidPassword() {
        userAccount.username = USERNAME
        userAccount.password = PASSWORD
        assert userAccount.isValidPassword(PASSWORD)

        assert !userAccount.isValidPassword("")
        assert !userAccount.isValidPassword("wrong")
        assert !userAccount.isValidPassword(null)
    }

    void testIsValidPassword_UsernameNullOrEmpty() {
        userAccount.password = PASSWORD
        shouldFailWithMessageContaining('username') { userAccount.isValidPassword(PASSWORD) }

        userAccount.username = ''
        shouldFailWithMessageContaining('username') { userAccount.isValidPassword(PASSWORD) }
    }

    void testIsValidPassword_OverrideComparePassword() {
        def customUserAccount = new CustomUserAccount()
        customUserAccount.username = USERNAME
        customUserAccount.password = PASSWORD
        println customUserAccount
        assert customUserAccount.isValidPassword(PASSWORD) == false
        assert customUserAccount.isValidPassword(PASSWORD + "123")
    }

    void testIsValidPassword_PasswordNotCheckedDuringValidation() {
        userAccount.username = USERNAME
        userAccount.password = PASSWORD
        userAccount.passwordCheckedDuringValidation = false
        assert userAccount.isValidPassword("wrong")
    }

    void testIsValid() {
        assert !userAccount.valid
        userAccount.homeDirectory = ""
        assert !userAccount.valid
        userAccount.homeDirectory = "/abc"
        assert userAccount.valid
    }

    void testCanRead() {
        // No file permissions - readable by all
        doTestCanRead(USERNAME, GROUP, null, true)

        // UserAccount has no username or group; use World permissions
        doTestCanRead(USERNAME, GROUP, '------r--', true)
        doTestCanRead(USERNAME, GROUP, 'rwxrwx-wx', false)

        userAccount.username = USERNAME
        userAccount.groups = [GROUP]

        doTestCanRead(USERNAME, GROUP, 'rwxrwxrwx', true)     // ALL
        doTestCanRead(USERNAME, GROUP, '---------', false)    // NONE

        doTestCanRead(USERNAME, null, 'r--------', true)      // User
        doTestCanRead(USERNAME, null, '-wxrwxrwx', false)

        doTestCanRead(null, GROUP, '---r-----', true)         // Group
        doTestCanRead(null, GROUP, 'rwx-wxrwx', false)

        doTestCanRead(null, null, '------r--', true)          // World
        doTestCanRead(null, null, 'rwxrwx-wx', false)
    }

    void testCanWrite() {
        // No file permissions - writable by all
        doTestCanWrite(USERNAME, GROUP, null, true)

        // UserAccount has no username or group; use World permissions
        doTestCanWrite(USERNAME, GROUP, '-------w-', true)
        doTestCanWrite(USERNAME, GROUP, 'rwxrwxr-x', false)

        userAccount.username = USERNAME
        userAccount.groups = [GROUP]

        doTestCanWrite(USERNAME, GROUP, 'rwxrwxrwx', true)     // ALL
        doTestCanWrite(USERNAME, GROUP, '---------', false)    // NONE

        doTestCanWrite(USERNAME, null, '-w-------', true)      // User
        doTestCanWrite(USERNAME, null, 'r-xrwxrwx', false)

        doTestCanWrite(null, GROUP, '----w----', true)         // Group
        doTestCanWrite(null, GROUP, 'rwxr-xrwx', false)

        doTestCanWrite(null, null, '-------w-', true)          // World
        doTestCanWrite(null, null, 'rwxrwxr-x', false)
    }

    void testCanExecute() {
        // No file permissions - executable by all
        doTestCanExecute(USERNAME, GROUP, null, true)

        // UserAccount has no username or group; use World permissions
        doTestCanExecute(USERNAME, GROUP, '--------x', true)
        doTestCanExecute(USERNAME, GROUP, 'rwxrwxrw-', false)

        userAccount.username = USERNAME
        userAccount.groups = [GROUP]

        doTestCanExecute(USERNAME, GROUP, 'rwxrwxrwx', true)     // ALL
        doTestCanExecute(USERNAME, GROUP, '---------', false)    // NONE

        doTestCanExecute(USERNAME, null, '--x------', true)      // User
        doTestCanExecute(USERNAME, null, 'rw-rwxrwx', false)

        doTestCanExecute(null, GROUP, '-----x---', true)         // Group
        doTestCanExecute(null, GROUP, 'rwxrw-rwx', false)

        doTestCanExecute(null, null, '--------x', true)          // World
        doTestCanExecute(null, null, 'rwxrwxrw-', false)
    }

    void testDefaultPermissions() {
        assert userAccount.defaultPermissionsForNewFile == new Permissions('rw-rw-rw-')
        assert userAccount.defaultPermissionsForNewDirectory == Permissions.ALL
    }

    //--------------------------------------------------------------------------
    // Helper Methods
    //--------------------------------------------------------------------------

    private void doTestCanRead(owner, group, permissionsString, expectedResult) {
        def file = createFileEntry(owner, permissionsString, group)
        assert userAccount.canRead(file) == expectedResult, file
    }

    private void doTestCanWrite(owner, group, permissionsString, expectedResult) {
        def file = createFileEntry(owner, permissionsString, group)
        assert userAccount.canWrite(file) == expectedResult, file
    }

    private void doTestCanExecute(owner, group, permissionsString, expectedResult) {
        def file = createFileEntry(owner, permissionsString, group)
        assert userAccount.canExecute(file) == expectedResult, file
    }

    private FileSystemEntry createFileEntry(owner, permissionsString, group) {
        def permissions = permissionsString ? new Permissions(permissionsString) : null
        return new FileEntry(path: '', owner: owner, group: group, permissions: permissions)
    }

    void setUp() {
        super.setUp()
        userAccount = new UserAccount()
    }
}