/*
 * Copyright 2007 the original author or authors.
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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.test.AbstractTestCase;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

/**
 * Abstract superclass for CommandHandler tests
 *
 * @author Chris Mair
 */
public abstract class AbstractCommandHandlerTestCase extends AbstractTestCase {

    // Some common test constants
    protected static final String DIR1 = "dir1";
    protected static final String DIR2 = "dir2";
    protected static final String FILENAME1 = "sample1.txt";
    protected static final String FILENAME2 = "sample2.txt";

    protected Session session;
    protected ResourceBundle replyTextBundle;

    /**
     * Test the handleCommand() method, when one or more parameter is missing or invalid
     *
     * @param commandHandler - the CommandHandler to test
     * @param commandName    - the name for the Command
     * @param parameters     - the Command parameters
     */
    protected void testHandleCommand_InvalidParameters(AbstractTrackingCommandHandler commandHandler,
                                                       String commandName, String[] parameters) throws Exception {
        Command command = new Command(commandName, parameters);

        commandHandler.handleCommand(command, session);
        verify(session).sendReply(ReplyCodes.COMMAND_SYNTAX_ERROR, replyTextFor(ReplyCodes.COMMAND_SYNTAX_ERROR));

        verifyNumberOfInvocations(commandHandler, 1);
        verifyNoDataElements(commandHandler.getInvocation(0));
    }

    /**
     * Verify that the CommandHandler contains the specified number of invocation records
     *
     * @param commandHandler - the CommandHandler
     * @param expected       - the expected number of invocations
     */
    protected void verifyNumberOfInvocations(InvocationHistory commandHandler, int expected) {
        assertEquals(expected, commandHandler.numberOfInvocations());
    }

    /**
     * Verify that the InvocationRecord contains no data elements
     *
     * @param invocationRecord - the InvocationRecord
     */
    protected void verifyNoDataElements(InvocationRecord invocationRecord) {
        log("Verifying: " + invocationRecord);
        assertEquals(0, invocationRecord.keySet().size(), "number of data elements");
    }

    /**
     * Verify that the InvocationRecord contains exactly one data element, with the specified key
     * and value.
     *
     * @param invocationRecord - the InvocationRecord
     * @param key              - the expected key
     * @param value            - the expected value
     */
    protected void verifyOneDataElement(InvocationRecord invocationRecord, String key, Object value) {
        log("Verifying: " + invocationRecord);
        assertEquals(1, invocationRecord.keySet().size(), "number of data elements");
        assertEqualsAllTypes("value:" + value, value, invocationRecord.getObject(key));
    }

    /**
     * Verify that the InvocationRecord contains exactly two data element, with the specified keys
     * and values.
     *
     * @param invocationRecord - the InvocationRecord
     * @param key1             - the expected key1
     * @param value1           - the expected value1
     * @param key2             - the expected key2
     * @param value2-          the expected value2
     */
    protected void verifyTwoDataElements(InvocationRecord invocationRecord, String key1, Object value1, String key2, Object value2) {
        log("Verifying: " + invocationRecord);
        assertEquals(2, invocationRecord.keySet().size(), "number of data elements");
        assertEqualsAllTypes("value1:" + value1, value1, invocationRecord.getObject(key1));
        assertEqualsAllTypes("value2:" + value2, value2, invocationRecord.getObject(key2));
    }

    /**
     * Assert that the actual is equal to the expected, using arrays equality comparison if
     * necessary
     *
     * @param message  - the message, used if the comparison fails
     * @param expected - the expected value
     * @param actual   - the actual value
     */
    private void assertEqualsAllTypes(String message, Object expected, Object actual) {
        if (expected instanceof byte[] || actual instanceof byte[]) {
            assertArrayEquals((byte[]) expected, (byte[]) actual, message);
        } else if (expected instanceof Object[] || actual instanceof Object[]) {
            assertArrayEquals((Object[]) expected, (Object[]) actual, message);
        } else {
            assertEquals(expected, actual, message);
        }
    }

    @BeforeEach
    void setUp_AbstractCommandHandlerTestCase() {
        session = mock(Session.class);
        when(session.getClientHost()).thenReturn(DEFAULT_HOST);

        replyTextBundle = new ListResourceBundle() {
            protected Object[][] getContents() {
                return new Object[][]{
                        {"150", replyTextFor(150)},
                        {"200", replyTextFor(200)},
                        {"211", replyTextWithParameterFor(211)},
                        {"213", replyTextWithParameterFor(213)},
                        {"214", replyTextWithParameterFor(214)},
                        {"215", replyTextWithParameterFor(215)},
                        {"220", replyTextFor(220)},
                        {"221", replyTextFor(221)},
                        {"226", replyTextFor(226)},
                        {"226.WithFilename", replyTextWithParameterFor("226.WithFilename")},
                        {"227", replyTextWithParameterFor(227)},
                        {"229", replyTextWithParameterFor(229)},
                        {"230", replyTextFor(230)},
                        {"250", replyTextFor(250)},
                        {"257", replyTextWithParameterFor(257)},
                        {"331", replyTextFor(331)},
                        {"350", replyTextFor(350)},
                        {"501", replyTextFor(501)},
                        {"502", replyTextFor(502)},
                };
            }
        };
    }

    /**
     * Return the test-specific reply text for the specified reply code
     *
     * @param replyCode - the reply code
     * @return the reply text for the specified reply code
     */
    protected String replyTextFor(int replyCode) {
        return "Reply for " + replyCode;
    }

    /**
     * Return the test-specific parameterized reply text for the specified reply code
     *
     * @param replyCode - the reply code
     * @return the reply text for the specified reply code
     */
    protected String replyTextWithParameterFor(int replyCode) {
        return "Reply for " + replyCode + ":{0}";
    }

    /**
     * Return the test-specific parameterized reply text for the specified messageKey
     *
     * @param messageKey - the messageKey
     * @return the reply text for the specified messageKey
     */
    protected String replyTextWithParameterFor(String messageKey) {
        return "Reply for " + messageKey + ":{0}";
    }

    /**
     * Return the test-specific reply text for the specified reply code and message parameter
     *
     * @param replyCode - the reply code
     * @param parameter - the message parameter value
     * @return the reply text for the specified reply code
     */
    protected String formattedReplyTextFor(int replyCode, Object parameter) {
        return MessageFormat.format(replyTextWithParameterFor(replyCode), objArray(parameter));
    }

    /**
     * Return the test-specific reply text for the specified message key and message parameter
     *
     * @param messageKey - the messageKey
     * @param parameter  - the message parameter value
     * @return the reply text for the specified message key and parameter
     */
    protected String formattedReplyTextFor(String messageKey, Object parameter) {
        return MessageFormat.format(replyTextWithParameterFor(messageKey), objArray(parameter));
    }

}
