
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

FetchMailAtt is launched by running one of the start scripts in bin.

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
property *process.resume.from.last* can be set to examine new emails
since the last run.

You can set up tasks to run on different config files to download from different servers.
Pass the specific config file as argument when setting up the task.

    fetchmailatt -c server1.conf
    fetchmailatt -c server2.conf
    fetchmailatt -c server3.conf

## Reset Saved State

When the config property *process.resume.from.last* is enabled, FetchMailAtt would
only process new emails while skipping all the old emails.  To force it to re-process
the old emails, reset the saved state file.

    fetchmailatt -r
    fetchmailatt -r -c server1.conf

This removes the saved state file and allows FetchMailAtt to process the emails from
beginning.

## Dependency

FetchMailAtt has minimal dependency.  See below.  Note that the jar files
are packaged in the distribution zip file.

* Java 8 JRE.
* JavaMail jar.
* Activation jar, for secured authentication to mail server.


# Configuration Options

The config file controls all aspects of the download process.  The conf directory
is the usual place for holding the config files.

When running without any command line arugment, the program uses the *cond/default.conf*
for configuration.  When the *-c config.conf* is used, configuration properties are read
from the config.conf file.

## Secure the Configuration Files

Since the conf files contain the email user credential, it's prudent to restrict
read access to it besides the Java program.

## Mail host, protocol, and folder

The mail sever host can be set with the following.  Any IMAP server server is supported.

    mail.host = imap.gmail.com

Set the mail store proptocol.  It can be imap or imaps (for SSL secured connection).

    mail.store.protocol = imaps

Set the mail folder to download.  e.g. INBOX or SENT.

    mail.folder = INBOX

## Group downloaded files into folders

The downloaded files can be saved at different folders based on some of their attributes: 
year, month, date, from, address, and subject.

To groups files according to their YEAR, e.g. DOWNLOAD_DIRECTORY/{YEAR}/

    download.groupby = year

To group files in hierarchical folders, e.g. DOWNLOAD_DIRECTORY/{YEAR}/{MONTH}/{FROM}

    download.groupby = year
    download.groupby.2nd = month
    download.groupby.3rd = from

## Limit emails to process

There are a number of options to reduce the number of emails to process.

This limits the number of mails to process,

    process.mail.limit = 100

This only processes mails from date (mm/dd/yyyy) onward,

    process.from.date = 07/07/2015

This only processes mails upto date (mm/dd/yyyy),

    process.to.date = 07/20/2015

This only processes mails from the last N days.  This supercedes the From/To properties.

    process.previous.days = 7

This resumes processing from the last download date.  This helps in frequent periodic runnings
of the program, to avoid processing any old mails.

    process.resume.from.last = yes

## Parallel Download

The program supports downloading attachments in parallel.  This increases the download 
throughput.  To control the number of workers,

    process.parallel.workers = 10

## Pause at end

Often when running in the spawned process by the system scheduler, the output to the console
is gone when the spawned process has terminated.  The following property make the program sleep
a number of seconds before exiting, allowing you to read the console output.

    process.sleep.before.exit = 10

## Filter by email

To process only the emails whose subject contains a term,

    match.subject.contains = budget

To process only the emails sent by a user,

    match.from.contains = bill

To process only the emails sent by an email address,

    match.address.contains = foo@bar.com

To process only the emails sent to a recipient,

    match.to.contains = bar@baz.com

To process only the emails CC or BCC to a recipient,

    match.cc.contains = bar@baz.com
    match.bcc.contains = bar@baz.com

## Filter by file

To download only files whose names contain,

    match.file.name.contains = pattern1,pattern2,pattern3

To download only files whose file types are,

    match.file.type.is = csv,pdf,jpg,jpeg,png,gif

To download only files whose file sizes are less than,

    match.file.size.less.than=10m

To download only files whose file sizes are greater than,

    match.file.size.greater.than=1k


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

