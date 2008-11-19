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

/**
 * Test-only subclass of UnixFakeFileSystem. Groovy implementation enables access to metaclass.
 *
 * @version $Revision: 160 $ - $Date: 2008-11-15 08:46:23 -0500 (Sat, 15 Nov 2008) $
 *
 * @author Chris Mair
 */
class TestUnixFakeFileSystem extends UnixFakeFileSystem {

    Throwable addMethodException

    void add(FileSystemEntry entry) {
        if (addMethodException) {
            throw addMethodException
        }
        super.add(entry)
    }
}