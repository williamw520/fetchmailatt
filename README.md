
# Introduction <a name="Introduction"/>

FetchMailAtt downloads email attachments from mail servers.
It supports a rich set of download options to allow easy customiztion
of the download process.

## Quick Rundown on Features

- Supports multiple mail providers: GMail, Yahoo Mail, Outlook.com, and others.
- Encrypted connection to mail server via IMAPS.
- Grouping of downloads into folders, with support for hierarchical folders.
- Group by: Year, Month, Date, From, Address, and Subject.
- Throttle limit for processing emails.
- Limit processing From and To date range.
- Limit processing to the previous N days of mails from the current date.
- Incremental download with resuming from the last download date.
- Support parallel downloads via multi-thread workers.
- Support filtering on email by: Subject, From, Address, To/CC/BCC.
- Support filtering on file by: file type, file name, min/max sizes.

# Quick Start <a name="QuickStart"/>

## Setup

The FetchMailAtt distribution comes as a zip file.  Unzip it for installation.

The directory layout of the installation is:

- bin, start scripts
- conf, the download configuration files
- lib, the jar files.

## Configuration

All the download configuration is contained in a config file.  The config directory
is the usual place for holding the config files.

The config file is in properties file format, with name=value lines.

### Required Configuration

Set the mail server connection credential

    mail.username=myusername@gmail.com
    mail.password=mypassword

Set the mail server host

    mail.host=imap.gmail.com

Set the download directory

    download.directory=/opt/download

## Running

FetchMailAtt is launched by running the one of start scripts in bin.

    fetchmailatt

It accepts a number of arguments.  Run fetchmailatt -h for the usage.

To run with a specific config file,

   fetchmailatt -c myconfig.conf
   fetchmailatt -c /full/path/myconfig.conf

Without the full path to the config file, the conf directory is searched
for the myconfig.conf file.

To run with the default.conf file,

   fetchmailatt

This runs the default.conf in the conf directory.


## Dependency

FetchMailAtt has minimal dependency.  See below.  Note that the jar files
are packaged in the distribution zip file.

* Java 8 JRE.
* JavaMail jar.
* Activation jar, for secured authentication to mail server.


# Build Guide <a name="DevGuide"/>

This project uses Gradle for building.  Download and set up Gradle before building.
The build file is at gradle.build.

To build a distribution,

    gradle

The distribution zip file is in build/distributions/fetchmailatt.zip, which contains
fetchmailatt.jar and all the dependent jar files in lib.  The start scripts for Windows
and Linux are in bin.  The default.conf is in conf.

To run the app with build.gradle, which uses the conf/default.conf

   gradle run

To pass arguments,

   gradle run -Dargs="-h"
   gradle run -Dargs="-v"
   gradle run -Dargs="-c myserver.conf"
   gradle run -Dargs="-r"


# License

FetchMailAtt is licensed under the Mozilla Public License 2.0 (MPL).  See the
LICENSE file for detail.  Basically you can incorporate the project into your
work however you like (open source or proprietary), but when making change
to the project itself, you need to release the changes under MPL.

