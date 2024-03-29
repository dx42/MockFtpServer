---
layout: default
title: FakeFtpServer Getting Started
---  

# FakeFtpServer - Getting Started

**FakeFtpServer** is a "fake" implementation of an FTP server. It provides a high-level abstraction for
an FTP Server and is suitable for most testing and simulation scenarios. You define a virtual filesystem
(internal, in-memory) containing an arbitrary set of files and directories. These files and directories can
(optionally) have associated access permissions. You also configure a set of one or more user accounts that
control which users can login to the FTP server, and their home (default) directories. The user account is
also used when assigning file and directory ownership for new files.

**FakeFtpServer** processes FTP client requests and responds with reply codes and reply messages
consistent with its configured file system and user accounts, including file and directory permissions,
if they have been configured.

See the [FakeFtpServer Features and Limitations](./fakeftpserver-features.html) page for more information on
which features and scenarios are supported.

In general the steps for setting up and starting the `FakeFtpServer` are:

 * Create a new `FakeFtpServer` instance, and optionally set the server control port (use a value of 0
   to automatically choose a free port number).

 * Create and configure a `FileSystem`, and attach to the `FakeFtpServer` instance.

 * Create and configure one or more `UserAccount` objects and attach to the `FakeFtpServer` instance.

Here is an example showing configuration and starting of an **FakeFtpServer** with a single user
account and a (simulated) Windows file system, defining a directory containing two files.

```
    FakeFtpServer fakeFtpServer = new FakeFtpServer();
    fakeFtpServer.addUserAccount(new UserAccount("user", "password", "c:\\data"));
    
    FileSystem fileSystem = new WindowsFakeFileSystem();
    fileSystem.add(new DirectoryEntry("c:\\data"));
    fileSystem.add(new FileEntry("c:\\data\\file1.txt", "abcdef 1234567890"));
    fileSystem.add(new FileEntry("c:\\data\\run.exe"));
    fakeFtpServer.setFileSystem(fileSystem);
    
    fakeFtpServer.start();
```

If you are running on a system where the default port (21) is already in use or cannot be bound
from a user process (such as Unix), you probably need to use a different server control port. Use the
`FakeFtpServer.setServerControlPort(int serverControlPort)` method to use a different port
number. If you specify a value of `0`, then the server will use a free port number. Then call
`getServerControlPort()` AFTER calling `start()` has been called to determine the actual port
number being used. Or, you can pass in a specific port number, such as 9187.

**FakeFtpServer**  can be fully configured programmatically or within the
[Spring Framework](http://www.springframework.org/) or other dependency-injection container.
The [Example Test Using FakeFtpServer](#Example) below illustrates programmatic configuration of
`FakeFtpServer`. Alternatively, the [Configuration](#Spring) section later on illustrates how to use
the *Spring Framework* to configure a `FakeFtpServer` instance.

## Test Using FakeFtpServer

This section includes a simplified example of FTP client code to be tested, and a JUnit 
test for it that programmatically configures and uses **FakeFtpServer**.

### FTP Client Code

The following `RemoteFile` class includes a `readFile()` method that retrieves a remote 
ASCII file and returns its contents as a String. This class uses the `FTPClient` from the
[Apache Commons Net](http://commons.apache.org/net/) framework.

```  
    public class RemoteFile {
    
        public static final String USERNAME = "user";
        public static final String PASSWORD = "password";
    
        private String server;
        private int port;
    
        public String readFile(String filename) throws IOException {
    
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(server, port);
            ftpClient.login(USERNAME, PASSWORD);
    
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
    
        public void setPort(int port) {
            this.port = port;
        }
    
        // Other methods ...
    }
```

### JUnit Test For FTP Client Code Using FakeFtpServer

The following `RemoteFileTest` class includes a couple of JUnit tests that use **FakeFtpServer**.

```  
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockftpserver.stub.example.RemoteFile;
import org.mockftpserver.test.AbstractTestCase;
import org.mockftpserver.test.IntegrationTest;

import java.io.IOException;

/**
 * Example test using FakeFtpServer, with programmatic configuration.
 */
class RemoteFileTest extends AbstractTestCase implements IntegrationTest {

    private static final String HOME_DIR = "/";
    private static final String FILE = "/dir/sample.txt";
    private static final String CONTENTS = "abcdef 1234567890";

    private RemoteFile remoteFile;
    private FakeFtpServer fakeFtpServer;

    @Test
    void testReadFile() throws Exception {
        String contents = remoteFile.readFile(FILE);
        assertEquals(CONTENTS, contents);
    }

    @Test
    void testReadFileThrowsException() {
        assertThrows(IOException.class, () -> remoteFile.readFile("NoSuchFile.txt"));
    }

    @BeforeEach
    void before() throws Exception {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(0);  // use any free port

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new FileEntry(FILE, CONTENTS));
        fakeFtpServer.setFileSystem(fileSystem);

        UserAccount userAccount = new UserAccount(RemoteFile.USERNAME, RemoteFile.PASSWORD, HOME_DIR);
        fakeFtpServer.addUserAccount(userAccount);

        fakeFtpServer.start();
        int port = fakeFtpServer.getServerControlPort();

        remoteFile = new RemoteFile();
        remoteFile.setServer("localhost");
        remoteFile.setPort(port);
    }

    @AfterEach
    void after() throws Exception {
        fakeFtpServer.stop();
    }

}
```

Things to note about the above test:
  
 * The `FakeFtpServer` instance is created and started in the `before()` method and
   stopped in the `after()` method, to ensure that it is stopped, even if the test fails.

 * The server control port is set to 0 using `fakeFtpServer.setServerControlPort(PORT)`.
   This means it will dynamically choose a free port. This is necessary if you are running on a
   system where the default port (21) is already in use or cannot be bound from a user process (such as Unix).

 * The `UnixFakeFileSystem` filesystem is configured and attached to the `FakeFtpServer` instance
   in the `setUp()` method. That includes creating a predefined `"/dir/sample.txt"` file with the
   specified file contents. The `UnixFakeFileSystem` has a `createParentDirectoriesAutomatically`
   attribute, which defaults to `true`, meaning that parent directories will be created automatically,
   as necessary. In this case, that means that the `"/"` and `"/dir"` parent directories will be created,
   even though not explicitly specified.

 * A single `UserAccount` with the specified username, password and home directory is configured and
   attached to the `FakeFtpServer` instance in the `setUp()` method. That configured user ("user")
   is the only one that will be able to sucessfully log in to the `FakeFtpServer`. 


## Spring Configuration

You can easily configure a `FakeFtpServer` instance in the
[Spring Framework](http://www.springframework.org/) or another, similar dependency-injection container.

### Simple Spring Configuration Example

The following example shows a *Spring* configuration file for a simple `FakeFtpServer` instance.

```
    <?xml version="1.0" encoding="UTF-8"?>
    
    <beans xmlns="http://www.springframework.org/schema/beans
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
                http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
    
        <bean id="fakeFtpServer" class="org.mockftpserver.fake.FakeFtpServer">
            <property name="serverControlPort" value="9981"/>
            <property name="systemName" value="UNIX"/>
            <property name="userAccounts">
                <list>
                    <bean class="org.mockftpserver.fake.UserAccount">
                        <property name="username" value="joe"/>
                        <property name="password" value="password"/>
                        <property name="homeDirectory" value="/"/*
                    </bean>
                </list>
            </property>
    
            <property name="fileSystem">
                <bean class="org.mockftpserver.fake.filesystem.UnixFakeFileSystem">
                    <property name="createParentDirectoriesAutomatically" value="false"/>
                    <property name="entries">
                        <list>
                            <bean class="org.mockftpserver.fake.filesystem.DirectoryEntry">
                                <property name="path" value="/"/>
                            </bean>
                            <bean class="org.mockftpserver.fake.filesystem.FileEntry">
                                <property name="path" value="/File.txt"/>
                                <property name="contents" value="abcdefghijklmnopqrstuvwxyz"/>
                            </bean>
                        </list>
                    </property>
                </bean>
            </property>
    
        </bean>
    
    </beans>
```

Things to note about the above example:

 * The `FakeFtpServer` instance has a single user account for username "joe", password "password"
   and home (default) directory of "/".

 * A `UnixFakeFileSystem` instance is configured with a predefined directory of "/" and a
   "/File.txt" file with the specified contents.

And here is the Java code to load the above *Spring* configuration file and start the
configured **FakeFtpServer**.

```
    ApplicationContext context = new ClassPathXmlApplicationContext("fakeftpserver-beans.xml");
    FakeFtpServer = (FakeFtpServer) context.getBean("FakeFtpServer");
    FakeFtpServer.start();
```


### Spring Configuration Example With File and Directory Permissions

The following example shows a *Spring* configuration file for a `FakeFtpServer` instance that
also configures file and directory permissions. This will enable the `FakeFtpServer` to reply
with proper error codes when the logged in user does not have the required permissions to access
directories or files.

```
    <?xml version="1.0" encoding="UTF-8"?>
    
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
                http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
    
        <bean id="fakeFtpServer" class="org.mockftpserver.fake.FakeFtpServer">
            <property name="serverControlPort" value="9981"/>
            <property name="userAccounts">
                <list>
                    <bean class="org.mockftpserver.fake.UserAccount">
                        <property name="username" value="joe"/>
                        <property name="password" value="password"/>
                        <property name="homeDirectory" value="c:\"/>
                    </bean>
                </list>
            </property>
    
            <property name="fileSystem">
                <bean class="org.mockftpserver.fake.filesystem.WindowsFakeFileSystem">
                    <property name="createParentDirectoriesAutomatically" value="false"/>
                    <property name="entries">
                        <list>
                            <bean class="org.mockftpserver.fake.filesystem.DirectoryEntry">
                                <property name="path" value="c:\"/>
                                <property name="permissionsFromString" value="rwxrwxrwx"/>
                                <property name="owner" value="joe"/>
                                <property name="group" value="users"/>
                            </bean>
                            <bean class="org.mockftpserver.fake.filesystem.FileEntry">
                                <property name="path" value="c:\File1.txt"/>
                                <property name="contents" value="1234567890"/>
                                <property name="permissionsFromString" value="rwxrwxrwx"/>
                                <property name="owner" value="peter"/>
                                <property name="group" value="users"/>
                            </bean>
                            <bean class="org.mockftpserver.fake.filesystem.FileEntry">
                                <property name="path" value="c:\File2.txt"/>
                                <property name="contents" value="abcdefghijklmnopqrstuvwxyz"/>
                                <property name="permissions">
                                    <bean class="org.mockftpserver.fake.filesystem.Permissions">
                                        <constructor-arg value="rwx------"/>
                                    </bean>
                                </property>
                                <property name="owner" value="peter"/>
                                <property name="group" value="users"/>
                            </bean>
                        </list>
                    </property>
                </bean>
            </property>
    
        </bean>
    </beans>
```


Things to note about the above example:

 * The `FakeFtpServer` instance is configured with a `WindowsFakeFileSystem` and a "c:\" root
   directory containing two files. Permissions and owner/group are specified for that directory, as well
   as the two predefined files contained within it.

 * The permissions for "File1.txt" (`"rwxrwxrwx"`) are specified using the "permissionsFromString" shortcut
   method, while the permissions for "File2.txt" (`"rwx------"`) are specified using the "permissions" setter,
   which takes an instance of the `Permissions` class. Either method is fine.


## Configuring Custom CommandHandlers

**FakeFtpServer** is intentionally designed to keep the lower-level details of FTP server implementation
hidden from the user. In most cases, you can simply define the files and directories in the file
system, configure one or more login users, and then fire up the server, expecting it to behave like
a *real* FTP server.

There are some cases, however, where you might want to further customize the internal behavior of the
server. Such cases might include:

 * You want to have a particular FTP server command return a predetermined error reply

 * You want to add support for a command that is not provided out of the box by **FakeFtpServer**

Note that if you need the FTP server to reply with entirely predetermined (canned) responses, then
you may want to consider using **StubFtpServer** instead.  


### Using a StaticReplyCommandHandler

You can use one of the *CommandHandler* classes defined within the `org.mockftpserver.core.command`
package to configure a custom *CommandHandler*. The following example uses the `StaticReplyCommandHandler`
from that package to add support for the FEAT command. Note that in this case, we are setting the
*CommandHandler* for a new command (i.e., one that is not supported out of the box by **FakeFtpServer**).
We could just as easily set the *CommandHandler* for an existing command, overriding the default *CommandHandler*.

```
    import org.mockftpserver.core.command.StaticReplyCommandHandler
    
    FakeFtpServer ftpServer = new FakeFtpServer()
    // ... set up files, directories and user accounts as usual
    
    StaticReplyCommandHandler featCommandHandler = new StaticReplyCommandHandler(211, "No Features");
    ftpServer.setCommandHandler("FEAT", featCommandHandler);
    
    // ...
    ftpServer.start()
```


### Using a Stub CommandHandler

You can also use a **StubFtpServer** *CommandHandler* -- i.e., one defined within the
`org.mockftpserver.stub.command` package. The following example uses the *stub* version of the
`CwdCommandHandler` from that package.

```
    import org.mockftpserver.stub.command.CwdCommandHandler
    
    FakeFtpServer ftpServer = new FakeFtpServer()
    // ... set up files, directories and user accounts as usual
    
    final int REPLY_CODE = 502;
    CwdCommandHandler cwdCommandHandler = new CwdCommandHandler();
    cwdCommandHandler.setReplyCode(REPLY_CODE);
    ftpServer.setCommandHandler(CommandNames.CWD, cwdCommandHandler);
    
    // ...
    ftpServer.start()
```


### Creating Your Own Custom CommandHandler Class

If one of the existing *CommandHandler* classes does not fulfill your needs, you can alternately create
your own custom *CommandHandler* class. The only requirement is that it implement the
`org.mockftpserver.core.command.CommandHandler` interface. You would, however, likely benefit from
inheriting from one of the existing abstract *CommandHandler* superclasses, such as
`org.mockftpserver.core.command.AbstractStaticReplyCommandHandler` or
`org.mockftpserver.core.command.AbstractCommandHandler`. See the javadoc of these classes for
more information.


## FTP Command Reply Text ResourceBundle

The default text asociated with each FTP command reply code is contained within the
"ReplyText.properties" ResourceBundle file. You can customize these messages by providing a
locale-specific ResourceBundle file on the CLASSPATH, according to the normal lookup rules of 
the ResourceBundle class (e.g., "ReplyText_de.properties"). Alternatively, you can 
completely replace the ResourceBundle file by calling the calling the 
`FakeFtpServer.setReplyTextBaseName(String)` method.

## SLF4J Configuration Required to See Log Output

Note that **FakeFtpServer** uses [SLF4J](http://www.slf4j.org/) for logging. If you want to
see the logging output, then you must configure **SLF4J**. (If no binding is found on the class
path, then **SLF4J** will default to a no-operation implementation.)

See the [SLF4J User Manual](http://www.slf4j.org/manual.html) for more information.
