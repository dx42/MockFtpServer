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

import org.mockftpserver.test.AbstractGroovyTestCase

/**
 * Tests for the Permissions class
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class PermissionsTest extends AbstractGroovyTestCase {

    void testConstructor() {
        doTestConstructorWithValidString('rwxrwxrwx')
        doTestConstructorWithValidString('rwxr--r--')
        doTestConstructorWithValidString('---------')
    }

    void testConstructor_InvalidString() {
        doTestConstructorWithInvalidString('')
        doTestConstructorWithInvalidString('------')
        doTestConstructorWithInvalidString('-')
        doTestConstructorWithInvalidString('r')
        doTestConstructorWithInvalidString('rwx')
        doTestConstructorWithInvalidString('rwxrwxrw')
        doTestConstructorWithInvalidString('123456789')
        doTestConstructorWithInvalidString('rwxrZxrwx')
        doTestConstructorWithInvalidString('--------Z')
    }

    void testCanReadWriteExecute() {
        doTestCanReadWriteExecute('rwxrwxrwx', true, true, true, true, true, true, true, true, true)
        doTestCanReadWriteExecute('r--r--r--', true, false, false, true, false, false, true, false, false)
        doTestCanReadWriteExecute('-w-r----x', false, true, false, true, false, false, false, false, true)
        doTestCanReadWriteExecute('---------', false, false, false, false, false, false, false, false, false)
    }

    void testHashCode() {
        assert new Permissions('rwxrwxrwx').hashCode() == Permissions.DEFAULT.hashCode()
        assert new Permissions('---------').hashCode() == Permissions.NONE.hashCode()
    }

    void testEquals() {
        assert new Permissions('rwxrwxrwx').equals(Permissions.DEFAULT)
        assert new Permissions('---------').equals(Permissions.NONE)
        assert Permissions.NONE.equals(Permissions.NONE)

        assert !(new Permissions('------rwx').equals(Permissions.NONE))
        assert !Permissions.NONE.equals(null)
        assert !Permissions.NONE.equals(123)
    }

    //--------------------------------------------------------------------------
    // Helper Methods
    //--------------------------------------------------------------------------

    private doTestCanReadWriteExecute(rwxString,
                                      canUserRead, canUserWrite, canUserExecute,
                                      canGroupRead, canGroupWrite, canGroupExecute,
                                      canWorldRead, canWorldWrite, canWorldExecute) {

        def permissions = new Permissions(rwxString)
        LOG.info("Testing can read/write/execute for $permissions")
        assert permissions.canUserRead() == canUserRead
        assert permissions.canUserWrite() == canUserWrite
        assert permissions.canUserExecute() == canUserExecute
        assert permissions.canGroupRead() == canGroupRead
        assert permissions.canGroupWrite() == canGroupWrite
        assert permissions.canGroupExecute() == canGroupExecute
        assert permissions.canWorldRead() == canWorldRead
        assert permissions.canWorldWrite() == canWorldWrite
        assert permissions.canWorldExecute() == canWorldExecute
    }

    private doTestConstructorWithInvalidString(String string) {
        LOG.info("Verifying invalid: [$string]")
        shouldFail { new Permissions(string) }
    }

    private doTestConstructorWithValidString(String string) {
        LOG.info("Verifying valid: [$string]")
        def permissions = new Permissions(string)
        LOG.info(permissions.toString())
        assert permissions.asRwxString() == string
    }
}