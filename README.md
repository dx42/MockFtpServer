# MockFtpServer

 - Home Page:  <https://mockftpserver.org/>
 - GitHub:     <https://github.com/dx42/MockFtpServer>

## Description

The MockFtpServer project provides mock/dummy FTP server implementations for testing FTP client
code. Two FTP Server implementations are provided, each at a different level of abstraction.

FakeFtpServer provides a higher-level abstraction. You define a virtual file system, including
directories and files, as well as a set of valid user accounts and credentials. The FakeFtpServer
then responds with appropriate replies and reply codes based on that configuration.

StubFtpServer, on the other hand, is a lower-level "stub" implementation. You configure the
individual FTP server commands to return custom data or reply codes, allowing simulation of
either success or failure scenarios. You can also verify expected command invocations.

MockFtpServer is written in Java, and is ideally suited to testing Java code. But because
communication with the FTP server is across sockets and TCP/IP, it can be used to test FTP client 
code written in any language.

See the online documentation for more information.

See the FTP Protocol Spec (http://www.ietf.org/rfc/rfc0959.txt) for information about 
FTP, commands, reply codes, etc..

## Dependencies

MockFtpServer requires 
 - Java (JDK) version 1.8 or later
 - The [SLF4J](http://www.slf4j.org/) API jar, accessible on the CLASSPATH. An SLF4J binding (logging
    framework-specific jar) is optional.
