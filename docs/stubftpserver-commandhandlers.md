---
layout: default
title: StubFtpServer FTP Commands and CommandHandlers
---  

# StubFtpServer - FTP Commands and CommandHandlers

The following table lists the main FTP server commands with their corresponding FTP client commands,
and the **StubFtpServer** *CommandHandler* classes that implements support for the FTP server command.
See the Javadoc for each *CommandHandler* class for information on how to customize its behavior
through configuration, as well as what command invocation data is available.

| **FTP Server Command** | **FTP Client Command** | **CommandHandler Class(es)**    |
|------------------------|------------------------|---------------------------------|
| ABOR                   | --                     | AborCommandHandler              |
| ACCT                   | --                     | AcctCommandHandler              |
| ALLO                   | --                     | AlloCommandHandler              |
| APPE                   | APPEND                 | AppeCommandHandler              |
| CDUP                   | --                     | CdupCommandHandler              |
| CWD                    | CD                     | CwdCommandHandler               |
| DELE                   | DELETE                 | DeleCommandHandler              |
| EPRT                   | --                     | EprtCommandHandler              |
| EPSV                   | --                     | EpsvCommandHandler              |
| HELP                   | REMOTEHELP             | HelpCommandHandler              |
| LIST                   | DIR / LS               | ListCommandHandler              |
| MKD                    | MKDIR                  | MkdCommandHandler               |
| MODE                   | --                     | ModeCommandHandler              |
| NLST                   | --                     | NlstCommandHandler              |
| NOOP                   | --                     | NoopCommandHandler              |
| PASS                   | USER                   | PassCommandHandler              |
| PASV                   | --                     | PasvCommandHandler              |
| PORT                   | --                     | PortCommandHandler              |
| PWD                    | PWD                    | PwdCommandHandler               |
| QUIT                   | QUIT / BYE             | QuitCommandHandler              |
| REIN                   | --                     | ReinCommandHandler              |
| REST                   | --                     | RestCommandHandler              |
| RETR                   | GET / RECV             | RetrCommandHandler, FileRetrCommandHandler (1) |
| RMD                    | RMDIR                  | RmdCommandHandler               |
| RNFR                   | RENAME                 | RnfrCommandHandler              |
| RNTO                   | RENAME                 | RntoCommandHandler              |
| SITE                   | --                     | SiteCommandHandler              |
| SMNT                   | --                     | SmntCommandHandler              |
| STAT                   | STATUS                 | StatCommandHandler              |
| STOR                   | PUT / SEND             | StorCommandHandler              |
| STOU                   | --                     | StouCommandHandler              |
| STRU                   | --                     | StruCommandHandler              |
| SYST                   | --                     | SystCommandHandler              |
| TYPE                   | ASCII / BINARY / TYPE  | TypeCommandHandler              |
| USER                   | USER                   | UserCommandHandler              |

(1) An alternative to the default *CommandHandler* implementation. See its class Javadoc.


## Special Command Handlers

There are also *special* *CommandHandler* classes defined (in the **core** package).

 * **ConnectCommandHandler** - Sends a 220 reply code after the initial connection to the server.
     
 * **UnsupportedCommandHandler** - Sends a 502 reply when an unrecognized/unsupported
   command name is sent from a client.