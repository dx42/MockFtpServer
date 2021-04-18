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
package org.mockftpserver.stub.command;

import static org.mockito.ArgumentMatchers.*;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockftpserver.core.command.*;
import org.mockftpserver.core.command.AbstractCommandHandlerTestCase;
import org.mockftpserver.core.util.AssertFailedException;

import java.util.Arrays;

/**
 * Tests for the FileRetrCommandHandler class
 *
 * @author Chris Mair
 */
public final class FileRetrCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(FileRetrCommandHandlerTest.class);
    private static final byte BYTE1 = (byte) 7;
    private static final byte BYTE2 = (byte) 21;

    private FileRetrCommandHandler commandHandler;

    /**
     * Test the constructor that takes a String, passing in a null
     */
    public void testConstructor_String_Null() {
        try {
            new FileRetrCommandHandler(null);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    /**
     * Test the setFile(String) method, passing in a null
     */
    public void testSetFile_Null() {
        try {
            commandHandler.setFile(null);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    /**
     * Test the handleCommand(Command,Session) method. Create a temporary (binary) file, and
     * make sure its contents are written back
     *
     * @throws Exception - if an error occurs
     */
    public void testHandleCommand() throws Exception {

        final byte[] BUFFER = new byte[FileRetrCommandHandler.BUFFER_SIZE];
        Arrays.fill(BUFFER, BYTE1);

        session.sendReply(ReplyCodes.TRANSFER_DATA_INITIAL_OK, replyTextFor(ReplyCodes.TRANSFER_DATA_INITIAL_OK));
        session.openDataConnection();

        commandHandler.setFile("Sample.jpg");
        Command command = new Command(CommandNames.RETR, array(FILENAME1));
        commandHandler.handleCommand(command, session);

        Mockito.verify(session, Mockito.times(5)).sendData(any(), eq(512));
        Mockito.verify(session).sendData(any(), eq(3));
        Mockito.verify(session).closeDataConnection();
        Mockito.verify(session).sendReply(ReplyCodes.TRANSFER_DATA_FINAL_OK, replyTextFor(ReplyCodes.TRANSFER_DATA_FINAL_OK));

        verifyNumberOfInvocations(commandHandler, 1);
        verifyOneDataElement(commandHandler.getInvocation(0), FileRetrCommandHandler.PATHNAME_KEY, FILENAME1);
    }

    /**
     * Test the handleCommand() method, when no pathname parameter has been specified
     */
    public void testHandleCommand_MissingPathnameParameter() throws Exception {
        commandHandler.setFile("abc.txt");      // this property must be set
        testHandleCommand_InvalidParameters(commandHandler, CommandNames.RETR, EMPTY);
    }

    /**
     * Test the HandleCommand method, when the file property has not been set
     */
    public void testHandleCommand_FileNotSet() throws Exception {
        try {
            commandHandler.handleCommand(new Command(CommandNames.RETR, EMPTY), session);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    /**
     * Perform initialization before each test
     *
     * @see org.mockftpserver.core.command.AbstractCommandHandlerTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        commandHandler = new FileRetrCommandHandler();
        commandHandler.setReplyTextBundle(replyTextBundle);
    }

//    /**
//     * Create a sample binary file; 5 buffers full plus 3 extra bytes
//     */
//    private void createSampleFile() {
//        final String FILE_PATH = "test/org.mockftpserver/command/Sample.jpg";
//        final byte[] BUFFER = new byte[FileRetrCommandHandler.BUFFER_SIZE];
//        Arrays.fill(BUFFER, BYTE1);
//
//        File file = new File(FILE_PATH);
//        FileOutputStream out = new FileOutputStream(file);
//        for (int i = 0; i < 5; i++) {
//            out.write(BUFFER);
//        }
//        Arrays.fill(BUFFER, BYTE2);
//        out.write(BUFFER, 0, 3);
//        out.close();
//        LOG.info("Created temporary file [" + FILE_PATH + "]: length=" + file.length());
//    }

}
