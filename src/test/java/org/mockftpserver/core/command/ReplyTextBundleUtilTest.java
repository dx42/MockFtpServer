/*
 * Copyright 2021 the original author or authors.
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
package org.mockftpserver.core.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.util.AssertFailedException;
import org.mockftpserver.test.AbstractTestCase;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

/**
 * Tests for the ReplyTextBundleUtil class.
 * 
 * @author Chris Mair
 */
class ReplyTextBundleUtilTest extends AbstractTestCase {

    private ResourceBundle resourceBundle1;
    private ResourceBundle resourceBundle2;

    @Test
    void testSetReplyTextBundleIfAppropriate_ReplyTextBundleAware_NotSetYet() {
        AbstractTrackingCommandHandler commandHandler = new StaticReplyCommandHandler();
        ReplyTextBundleUtil.setReplyTextBundleIfAppropriate(commandHandler, resourceBundle1);
        assertSame(resourceBundle1, commandHandler.getReplyTextBundle());
    }

    @Test
    void testSetReplyTextBundleIfAppropriate_ReplyTextBundleAware_AlreadySet() {
        AbstractTrackingCommandHandler commandHandler = new StaticReplyCommandHandler();
        commandHandler.setReplyTextBundle(resourceBundle2);
        ReplyTextBundleUtil.setReplyTextBundleIfAppropriate(commandHandler, resourceBundle1);
        assertSame(resourceBundle2, commandHandler.getReplyTextBundle());
    }

    @Test
    void testSetReplyTextBundleIfAppropriate_NotReplyTextBundleAware() {
        CommandHandler commandHandler = mock(CommandHandler.class);
        ReplyTextBundleUtil.setReplyTextBundleIfAppropriate(commandHandler, resourceBundle1);
        verifyNoInteractions(commandHandler);         // expect no method calls
    }

    @Test
    void testSetReplyTextBundleIfAppropriate_NullCommandHandler() {
        assertThrows(AssertFailedException.class, () -> ReplyTextBundleUtil.setReplyTextBundleIfAppropriate(null, resourceBundle1));
    }

    @BeforeEach
    void setUp() {
        resourceBundle1 = new ListResourceBundle() {
            protected Object[][] getContents() {
                return null;
            }
        };

        resourceBundle2 = new ListResourceBundle() {
            protected Object[][] getContents() {
                return new Object[][] { { "a", "b" } };
            }
        };
    }
    
}
