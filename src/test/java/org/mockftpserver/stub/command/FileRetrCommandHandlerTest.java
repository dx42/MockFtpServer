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
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.command.AbstractCommandHandlerTestCase;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandNames;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.util.AssertFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Tests for the FileRetrCommandHandler class
 *
 * @author Chris Mair
 */
class FileRetrCommandHandlerTest extends AbstractCommandHandlerTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(FileRetrCommandHandlerTest.class);
    private static final byte BYTE1 = (byte) 7;
    private static final byte BYTE2 = (byte) 21;

    private FileRetrCommandHandler commandHandler;

    @Test
    void testConstructor_String_Null() {
        try {
            new FileRetrCommandHandler(null);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @Test
    void testSetFile_Null() {
        try {
            commandHandler.setFile(null);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @Test
    void testHandleCommand() throws Exception {

        final byte[] BUFFER = new byte[FileRetrCommandHandler.BUFFER_SIZE];
        Arrays.fill(BUFFER, BYTE1);

        session.sendReply(ReplyCodes.TRANSFER_DATA_INITIAL_OK, replyTextFor(ReplyCodes.TRANSFER_DATA_INITIAL_OK));
        session.openDataConnection();

        commandHandler.setFile("Sample.jpg");
        Command command = new Command(CommandNames.RETR, array(FILENAME1));
        commandHandler.handleCommand(command, session);

        verify(session, times(5)).sendData(any(), eq(512));
        verify(session).sendData(any(), eq(3));
        verify(session).closeDataConnection();
        verify(session).sendReply(ReplyCodes.TRANSFER_DATA_FINAL_OK, replyTextFor(ReplyCodes.TRANSFER_DATA_FINAL_OK));

        verifyNumberOfInvocations(commandHandler, 1);
        verifyOneDataElement(commandHandler.getInvocation(0), FileRetrCommandHandler.PATHNAME_KEY, FILENAME1);
    }

    @Test
    void testHandleCommand_MissingPathnameParameter() throws Exception {
        commandHandler.setFile("abc.txt");      // this property must be set
        testHandleCommand_InvalidParameters(commandHandler, CommandNames.RETR, EMPTY);
    }

    @Test
    void testHandleCommand_FileNotSet() throws Exception {
        try {
            commandHandler.handleCommand(new Command(CommandNames.RETR, EMPTY), session);
            fail("Expected AssertFailedException");
        }
        catch (AssertFailedException expected) {
            LOG.info("Expected: " + expected);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
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
