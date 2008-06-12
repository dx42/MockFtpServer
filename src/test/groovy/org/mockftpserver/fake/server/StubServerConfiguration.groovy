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
package org.mockftpserver.fake.server

import org.mockftpserver.fake.filesystem.FileSystem
import org.mockftpserver.fake.server.ServerConfiguration
import org.mockftpserver.fake.user.UserAccount

/**
 * Stub implementation of the     {@link ServerConfiguration}     interface for testing
 *
 * @version $Revision$ - $Date$
 *
 * @author Chris Mair
 */
class StubServerConfiguration implements ServerConfiguration {

    ResourceBundle replyTextBundle
    Map userAccounts = [:]
    private Map textForReplyCodeMap = [:]
    FileSystem fileSystem

    UserAccount getUserAccount(String username) {
        (UserAccount) userAccounts[username]
    }

    ResourceBundle getReplyTextBundle() {
        return new TestResources(map: textForReplyCodeMap)
    }

    //-------------------------------------------------------------------------
    // Stub-specific API - Helper methods not part of ServerConfiguration interface
    //-------------------------------------------------------------------------

    /**
     * Set the text to be returned for the specified key by the
     * {@link #getReplyTextBundle()}   resource bundle.
     */
    void setTextForKey(key, String text) {
        textForReplyCodeMap[key.toString()] = text
    }

}

/**
 * Map-based implementation of ResourceBundle for testing
 */
class TestResources extends ResourceBundle {

    Map map

    Object handleGetObject(String key) {
        return map[key] ?: "$key {0}".toString()
    }

    public Enumeration getKeys() {
        return new Vector(map.keySet()).elements()
    }
}
