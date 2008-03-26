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

import org.mockftpserver.test.AbstractGroovyTest
import org.mockftpserver.core.command.Command
import org.mockftpserver.core.command.CommandNamesimport org.mockftpserver.core.session.StubSession
import org.mockftpserver.fake.StubServerConfiguration
import org.apache.log4j.Loggerimport org.mockftpserver.core.command.ReplyCodes
/**
 * Abstract superclass for CommandHandler tests
 * 
 * @version $Revision: $ - $Date: $
 *
 * @author Chris Mair
 */
abstract class AbstractCommandHandlerTest extends AbstractGroovyTest {
    protected final Logger LOG = Logger.getLogger(this.class)
    
    def session
    def serverConfiguration
    def commandHandler

    /**
     * Create and return a new instance of the CommandHandler class under test. Concrete subclasses must implement.
     */
    abstract createCommandHandler()
    
	void setUp() {
	    super.setUp()
	    session = new StubSession()
	    serverConfiguration = new StubServerConfiguration()
	    commandHandler = createCommandHandler()
	    commandHandler.serverConfiguration = serverConfiguration 
	}

    /**
     * Assert that the specified reply code (and default message) was sent to the session.
     */
    void assertSessionReply(int replyCode) {
		LOG.info(session)
        assert session.sentReplies[0] == [replyCode, replyCode as String]
    }
    
 }