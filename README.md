
# Introduction <a name="Introduction"/>

FetchMailAtt downloads email attachments from mail servers.
It supports a rich set of configuration options for easy customiztion
of the download process.

## Quick Rundown of the Features

- Supports multiple mail providers: GMail, Yahoo Mail, Outlook.com, and others.
- Encrypted connection to mail server via IMAPS.
- Grouping of downloads into folders, with support for hierarchical folders.
- Group by: Year, Month, Date, From, Address, and Subject.
- Limit processing with date range, with previous N days, and with message limit.
- Incremental download from the last download date.
- Parallel downloads via multi-thread workers.
- Filtering on emails by: Subject, From, Address, To/CC/BCC.
- Filtering on files by: file type, file name, min/max sizes.

# Quick Start <a name="QuickStart"/>

## Download Release Package

Go to the [Releases](https://github.com/williamw520/fetchmailatt/releases) page to download a version of the package. 

## Setup

The FetchMailAtt release package comes as a zip file.  Unzip it to install.  The directory layout of the installation:

- bin, the start scripts
- conf, the configuration files to control download
- lib, the jar files.

## Configuration

The download configuration is contained in a config file.  The conf directory
is the usual place for holding the config files.  The config file is in properties
file format, with name=value lines.

### Required Properties for Configuration

Set the mail server connection credential and the mail server host

    mail.username = myusername@gmail.com
    mail.password = mypassword
    mail.host = imap.gmail.com

Set the download directory

    download.directory = /opt/download

For the other optional properties, see the conf/default.conf for details.

## Running

FetchMailAtt is launched by running the one of start scripts in bin.

    fetchmailatt

Run fetchmailatt -h for the command line options.

    fetchmailatt -h

To run with a specific config file,

    fetchmailatt -c myconfig.conf
    fetchmailatt -c /full/path/myconfig.conf

Without the full path to the config file, the classpath is searched
for the myconfig.conf file.  The conf directory is part of the classpath;
any config file in it can be used.

To run with the default.conf file,

    fetchmailatt

This runs the default.conf in the conf directory.

## Periodic Run

When it runs, FetchMailAtt runs through the relevant mails once and then
ends.  To periodically look for new emails, schedule it to run periodically
as a task on Windows Task Manager or schedule it with cron on Linux.  The config 
property process.resume.from.last can be set to examine new emails
since the last run.

You can set up tasks to run on different config files to download from different servers.
Pass the specific config file as argument when setting up the task.

    fetchmailatt -c server1.conf
    fetchmailatt -c server2.conf
    fetchmailatt -c server3.conf

## Reset Saved State

When the config property process.resume.from.last is enabled, FetchMailAtt would
only process new emails while skipping all the old emails.  To force it to re-process
the old emails, reset the saved state file.

    fetchmailatt -r
    fetchmailatt -r -c server1.conf

This removes the saved state file and allows FetchMailAtt to process the emails from
beginning.

## Secure the Configuration Files

Since the conf files contain the email user credential, it's prudent to restrict
read access to it besides the Java program.

## Dependency

FetchMailAtt has minimal dependency.  See below.  Note that the jar files
are packaged in the distribution zip file.

* Java 8 JRE.
* JavaMail jar.
* Activation jar, for secured authentication to mail server.


# Build Guide <a name="DevGuide"/>

This project uses Gradle and its Java plugin for building.  Set them up before building.
The build file is at gradle.build.

To build a distribution,

    gradle

The distribution is in build/distributions/fetchmailatt.zip, which contains
fetchmailatt.jar and all the dependent jars, the start scripts, and the default.conf.

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

