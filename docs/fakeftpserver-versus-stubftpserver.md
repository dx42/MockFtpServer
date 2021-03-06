---
layout: default
title: FakeFtpServer versus StubFtpServer
---  


# FakeFtpServer or StubFtpServer?

The **MockFtpServer** project includes two separate *mock* implementations of an FTP Server. Which one you
use is dependent on what kind of FTP scenario(s) you wish to simulate, and what level of control you need
over exact server replies.

## FakeFtpServer

**FakeFtpServer** provides a high-level abstraction for an FTP Server and is suitable for most testing
and simulation scenarios. You define a filesystem (internal, in-memory) containing an arbitrary set of
files and directories. These files and directories can (optionally) have associated access permissions.
You also configure a set of one or more user accounts that control which users can login to the FTP server,
and their home (default) directories. The user account is also used when assigning file and directory
ownership for new files.

**FakeFtpServer** processes FTP client requests and responds with reply codes and reply messages
consistent with its configuration and the contents of its internal filesystem, including file and
directory permissions, if they have been configured.

**FakeFtpServer**  can be fully configured programmatically or within a
[Spring Framework](http://www.springframework.org/) or other dependency-injection container.

See the [FakeFtpServer Features and Limitations](./fakeftpserver-features.html) page for more information on
which features and scenarios are supported.

## StubFtpServer

**StubFtpServer** is a "stub" implementation of an FTP server. It supports the main FTP commands by
implementing command handlers for each of the corresponding low-level FTP server commands (e.g. RETR,
DELE, LIST). These *CommandHandler*s can be individually configured to return custom data or reply codes,
allowing simulation of a complete range of both success and failure scenarios. The *CommandHandler*s can
also be interrogated to verify command invocation data such as command parameters and timestamps.

**StubFtpServer** works out of the box with reasonable defaults, but can be fully configured programmatically
or within a [Spring Framework](http://www.springframework.org/) or other dependency-injection container.

See the [StubFtpServer Features and Limitations](./stubftpserver-features.html) page for more information on
which features and scenarios are supported.

## So, Which One Should I Use?

In general, if your testing and simulation needs are pretty straightforward, then using **FakeFtpServer** is
probably the best choice. See the [FakeFtpServer Features and Limitations](./fakeftpserver-features.html) page
for more information on which features and scenarios are supported.

Some reasons to use **StubFtpServer** include:

  * If you need to simulate an FTP server scenario not supported by **FakeFtpServer**.

  * You want to test a very specific and/or limited FTP scenario. In this case, the setup of the
    **StubFtpServer** might be simpler -- you don't have to setup fake files and directories and user accounts.

  * You are more comfortable with configuring and using the lower-level FTP server command reply codes and behavior.
