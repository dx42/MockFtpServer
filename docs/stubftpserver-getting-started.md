---
layout: default
title: StubFtpServer Getting Started
---  

# StubFtpServer - Getting Started

**StubFtpServer** is a "stub" implementation of an FTP server. It supports the main FTP commands by 
implementing command handlers for each of the corresponding low-level FTP server commands (e.g. RETR, 
DELE, LIST). These *CommandHandler*s can be individually configured to return custom data or reply codes, 
allowing simulation of a complete range of both success and failure scenarios. The *CommandHandler*s can 
also be interrogated to verify command invocation data such as command parameters and timestamps.

**StubFtpServer** works out of the box with reasonable defaults, but can be fully configured 
programmatically or within a [Spring Framework](http://www.springframework.org/) (or similar) container.

Here is how to start the **StubFtpServer** with the default configuration. This will return 
success reply codes, and return empty data (for retrieved files, directory listings, etc.).

```  
    StubFtpServer stubFtpServer = new StubFtpServer();
    stubFtpServer.start();
```  

If you are running on a system where the default port (21) is already in use or cannot be bound
from a user process (such as Unix), you will need to use a different server control port. Use the
`StubFtpServer.setServerControlPort(int serverControlPort)` method to use a different port
number. If you specify a value of `0`, then the server will use a free port number. Then call
`getServerControlPort()` AFTER calling `start()` has been called to determine the actual port
number being used. Or, you can pass in a specific port number, such as 9187.

## CommandHandlers

*CommandHandler*s are the heart of the **StubFtpServer**.

**StubFtpServer** creates an appropriate default *CommandHandler* for each (supported) FTP server 
command. See the list of *CommandHandler* classes associated with FTP server commands in 
[FTP Commands and CommandHandlers](./stubftpserver-commandhandlers.html).

You can retrieve the existing *CommandHandler* defined for an FTP server command by calling the
`StubFtpServer.getCommandHandler(String name)` method, passing in the FTP server command
name. For example:
  
```  
    PwdCommandHandler pwdCommandHandler = (PwdCommandHandler) stubFtpServer.getCommandHandler("PWD");
```  

You can replace the existing *CommandHandler* defined for an FTP server command by calling the
`StubFtpServer.setCommandHandler(String name, CommandHandler commandHandler)` method, passing 
in the FTP server command name, such as `"STOR"` or `"USER"`, and the 
`CommandHandler` instance. For example:
  
```  
    PwdCommandHandler pwdCommandHandler = new PwdCommandHandler();
    pwdCommandHandler.setDirectory("some/dir");
    stubFtpServer.setCommandHandler("PWD", pwdCommandHandler);
```  

### Generic CommandHandlers

**StubFtpServer** includes a couple generic *CommandHandler* classes that can be used to replace
the default command handler for an FTP command. See the Javadoc for more information.
  
 * **StaticReplyCommadHandler**
   `StaticReplyCommadHandler` is a *CommandHandler* that always sends back the configured reply 
   code and text. This can be a useful replacement for a default *CommandHandler* if you want a 
   certain FTP command to always send back an error reply code.
  
 * **SimpleCompositeCommandHandler**
   `SimpleCompositeCommandHandler` is a composite *CommandHandler* that manages an internal 
   ordered list of *CommandHandler*s to which it delegates. Starting with the first 
   *CommandHandler* in the list, each invocation of this composite handler will invoke (delegate to) 
   the current internal *CommandHander*. Then it moves on the next *CommandHandler* in the internal list.


### Configuring CommandHandler for a New (Unsupported) Command

If you want to add support for a command that is not provided out of the box by **StubFtpServer**,
you can create a *CommandHandler* instance and set it within the **StubFtpServer** using the
`StubFtpServer.setCommandHandler(String name, CommandHandler commandHandler)` method in the
same way that you replace an existing *CommandHandler* (see above). The following example uses
the `StaticReplyCommandHandler` to add support for the FEAT command.

```
    final String FEAT_TEXT = "Extensions supported:\n" +
            "MLST size*;create;modify*;perm;media-type\n" +
            "SIZE\n" +
            "COMPRESSION\n" +
            "END";
    StaticReplyCommandHandler featCommandHandler = new StaticReplyCommandHandler(211, FEAT_TEXT);
    stubFtpServer.setCommandHandler("FEAT", featCommandHandler);
```


### Creating Your Own Custom CommandHandler Class

If one of the existing *CommandHandler* classes does not fulfill your needs, you can alternately create
your own custom *CommandHandler* class. The only requirement is that it implement the
`org.mockftpserver.core.command.CommandHandler` interface. You would, however, likely benefit from
inheriting from one of the existing abstract *CommandHandler* superclasses, such as
`org.mockftpserver.stub.command.AbstractStubCommandHandler` or
`org.mockftpserver.core.command.AbstractCommandHandler`. See the javadoc of these classes for
more information.


## Retrieving Command Invocation Data

Each predefined **StubFtpServer** *CommandHandler* manages a List of `InvocationRecord` objects -- one
for each time the *CommandHandler* is invoked. An `InvocationRecord` contains the `Command` 
that triggered the invocation (containing the command name and parameters), as well as the invocation
timestamp and client host address. The `InvocationRecord` also contains a `Map`, with optional
*CommandHandler*-specific data. See the Javadoc for more information.

You can retrieve the `InvocationRecord` from a *CommandHandler* by calling the
`getInvocation(int index)` method, passing in the (zero-based) index of the desired
invocation. You can get the number of invocations for a *CommandHandler* by calling
`numberOfInvocations()`. The [Example Test Using Stub Ftp Server](#Example) below illustrates 
retrieving and interrogating an `InvocationRecord` from a *CommandHandler*.
  

## Test Using StubFtpServer

This section includes a simplified example of FTP client code to be tested, and a JUnit 
test for it that uses **StubFtpServer**.

### FTP Client Code

The following `RemoteFile` class includes a `readFile()` method that retrieves a remote 
ASCII file and returns its contents as a String. This class uses the `FTPClient` from the
[Apache Commons Net](http://commons.apache.org/net/) framework.

```  
    public class RemoteFile {
    
        private String server;
    
        public String readFile(String filename) throws SocketException, IOException {
    
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(server);
    
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean success = ftpClient.retrieveFile(filename, outputStream);
            ftpClient.disconnect();
    
            if (!success) {
                throw new IOException("Retrieve file failed: " + filename);
            }
            return outputStream.toString();
        }
        
        public void setServer(String server) {
            this.server = server;
        }
        
        // Other methods ...
    }
```  

### JUnit Test For FTP Client Code Using StubFtpServer

The following `RemoteFileTest` class includes a couple of JUnit tests that use 
**StubFtpServer**. The test illustrates replacing the default *CommandHandler* with
a customized handler.

```  
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.core.command.CommandNames;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.stub.StubFtpServer;
import org.mockftpserver.stub.command.RetrCommandHandler;
import org.mockftpserver.test.AbstractTestCase;
import org.mockftpserver.test.IntegrationTest;

import java.io.IOException;

/**
 * Example test using StubFtpServer, with programmatic configuration.
 */
class RemoteFileTest extends AbstractTestCase implements IntegrationTest {

    private static final int PORT = 9981;
    private static final String FILENAME = "dir/sample.txt";

    private RemoteFile remoteFile;
    private StubFtpServer stubFtpServer;

    @Test
    void testReadFile() throws Exception {

        final String CONTENTS = "abcdef 1234567890";

        // Replace the default RETR CommandHandler; customize returned file contents
        RetrCommandHandler retrCommandHandler = new RetrCommandHandler();
        retrCommandHandler.setFileContents(CONTENTS);
        stubFtpServer.setCommandHandler(CommandNames.RETR, retrCommandHandler);
        
        stubFtpServer.start();
        
        String contents = remoteFile.readFile(FILENAME);

        // Verify returned file contents
        assertEquals(CONTENTS, contents);
        
        // Verify the submitted filename
        InvocationRecord invocationRecord = retrCommandHandler.getInvocation(0);
        String filename = invocationRecord.getString(RetrCommandHandler.PATHNAME_KEY);
        assertEquals(FILENAME, filename);
    }

    @Test
    void testReadFileThrowsException() {

        // Replace the default RETR CommandHandler; return failure reply code
        RetrCommandHandler retrCommandHandler = new RetrCommandHandler();
        retrCommandHandler.setFinalReplyCode(550);
        stubFtpServer.setCommandHandler(CommandNames.RETR, retrCommandHandler);
        
        stubFtpServer.start();

        try {
            remoteFile.readFile(FILENAME);
            fail("Expected IOException");
        }
        catch (IOException expected) {
            // Expected this
        }
    }
    
    @BeforeEach
    void setUp() throws Exception {
        remoteFile = new RemoteFile();
        remoteFile.setServer("localhost");
        remoteFile.setPort(PORT);
        stubFtpServer = new StubFtpServer();
        stubFtpServer.setServerControlPort(PORT);
    }

    @AfterEach
    void tearDown() throws Exception {
        stubFtpServer.stop();
    }

}
```  

Things to note about the above test:
  
 * The `StubFtpServer` instance is created in the `setUp()` method, but is not started
   there because it must be configured differently for each test. The `StubFtpServer` instance 
   is stopped in the `tearDown()` method, to ensure that it is stopped, even if the test fails.
  

## Spring Configuration

You can easily configure a **StubFtpServer** instance in the
[Spring Framework](http://www.springframework.org/). The following example shows a *Spring*
configuration file.

```
    <?xml version="1.0" encoding="UTF-8"?>
    
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
               http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
    
      <bean id="stubFtpServer" class="org.mockftpserver.stub.StubFtpServer">
    
        <property name="commandHandlers">
          <map>
            <entry key="LIST">
              <bean class="org.mockftpserver.stub.command.ListCommandHandler">
                <property name="directoryListing">
                  <value>
                    11-09-01 12:30PM  406348 File2350.log
                    11-01-01 1:30PM &lt;DIR&gt; 0 archive
                  </value>
                </property>
              </bean>
            </entry>
    
            <entry key="PWD">
              <bean class="org.mockftpserver.stub.command.PwdCommandHandler">
                <property name="directory" value="foo/bar" />
              </bean>
            </entry>
    
            <entry key="DELE">
              <bean class="org.mockftpserver.stub.command.DeleCommandHandler">
                <property name="replyCode" value="450" />
              </bean>
            </entry>
    
            <entry key="RETR">
              <bean class="org.mockftpserver.stub.command.RetrCommandHandler">
                <property name="fileContents"
                  value="Sample file contents line 1&#10;Line 2&#10;Line 3"/>
              </bean>
            </entry>
    
          </map>
        </property>
      </bean>
    
    </beans>
```

This example overrides the default handlers for the following FTP commands:
 * LIST - replies with a predefined directory listing
 * PWD - replies with a predefined directory pathname
 * DELE - replies with an error reply code (450)
 * RETR - replies with predefined contents for a retrieved file

And here is the Java code to load the above *Spring* configuration file and start the
configured **StubFtpServer**.

```
    ApplicationContext context = new ClassPathXmlApplicationContext("stubftpserver-beans.xml");
    stubFtpServer = (StubFtpServer) context.getBean("stubFtpServer");
    stubFtpServer.start();
```


## FTP Command Reply Text ResourceBundle

The default text associated with each FTP command reply code is contained within the
"ReplyText.properties" ResourceBundle file. You can customize these messages by providing a
locale-specific ResourceBundle file on the CLASSPATH, according to the normal lookup rules of 
the ResourceBundle class (e.g., "ReplyText_de.properties"). Alternatively, you can completely
 replace the ResourceBundle file by calling the `StubFtpServer.setReplyTextBaseName(String)` method. 
