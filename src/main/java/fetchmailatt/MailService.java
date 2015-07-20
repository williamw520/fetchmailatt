/******************************************************************************
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.  If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for 
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is: FetchMailAtt
 * The Initial Developer of the Original Code is: William Wong (williamw520@gmail.com)
 * Portions created by William Wong are Copyright (C) 2015 William Wong, All Rights Reserved.
 *
 ******************************************************************************/

package fetchmailatt;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.regex.*;
import java.util.logging.*;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.stream.*;
import javax.mail.*;
import javax.mail.search.*;

public class MailService {
    private static final Logger log = Logger.getLogger(MailService.class.getName());

    /*
      Sample config:
      MailService.fetchAttachments(Util.asMap(
          "mail.username", "USER@gmail.com",
          "mail.password", "PASSWORD",
          "mail.host", "imap.gmail.com",
          "mail.store.protocol", "imaps",
          "mail.folder", "INBOX",
          "download.directory", "/tmp/att",
          "download.groupby", "address",
          "download.groupby.2nd", "year",
          "download.groupby.3rd", "month",
          "process.mail.limit", "0",
          "process.from.date", "07/07/2015",
          "process.previous.days", "7",
          "process.resume.from.last", "yes"
      ), false, true);
    */
    public static void fetchAttachments(Map<String, String> config, String stateFilename, boolean quiet, boolean test) throws Exception {
        long                    startMS = System.currentTimeMillis();
        Cfg                     cfg = new Cfg(config);
        Cfg                     state = new Cfg(Util.toMap(Util.loadProperties(Util.getStateFile(stateFilename))));
        Path                    downloadDir = Paths.get(cfg.ensure("download.directory"));
        boolean                 downloadInline = cfg.asBoolean("download.inline").orElse(Boolean.FALSE);
        List                    pathers = buildGroupbyPathers(cfg);
        Predicate<Message>      mailMatchers = buildMailMatchers(cfg);
        Predicate<BodyPart>     fileMatchers = buildFileMatchers(cfg);
        Optional<SearchTerm>    dateRange = getDateRange(cfg, state, quiet);
        int                     processLimit = cfg.asInt("process.mail.limit").orElse(0);
        Message[]               messages;
        Folder                  mailbox = openMailbox(cfg);

        if (dateRange.isPresent()) {
            messages = mailbox.search(dateRange.get());
        } else {
            messages = mailbox.getMessages();
        }

        int                     latest = messages.length;
        int                     oldest = processLimit <= 0 ? 0 : (messages.length < processLimit ? 0 : messages.length - processLimit);
        Date                    lastDate = null;

        if (!quiet) System.out.println("Message count: " + messages.length + ", fetch from: " + (oldest+1) + ", to: " + latest);

        for (int i = oldest + 1; i <= latest; i++) {
            if (!quiet) System.out.println("Mail Message #" + i);
            try {
                Message         msg = messages[i - 1];
                long            msgTime = msg.getReceivedDate().getTime();

                lastDate = Util.later(lastDate, msg.getReceivedDate());
                if (!quiet) System.out.println("DATE: " + Util.formatTimeMMddHHmm(msg.getReceivedDate()) +
                                               ", FROM: " + getFromName(msg) + " " + getFromAddress(msg));

                if (!mailMatchers.test(msg)) {
                    log.info("mail not matched");
                    continue;
                } else {
                    log.info("mail matched");
                }

                Path            downloadPath = resolveDownloadPath(pathers, downloadDir, msg);
                List<BodyPart>  attachmentParts = getAttachmentParts(msg, downloadInline, new ArrayList<BodyPart>());
                if (attachmentParts.size() > 0 && !Files.exists(downloadPath))
                    Files.createDirectories(downloadPath);

                for (BodyPart bp : attachmentParts) {
                    log.info("Processing file: " + bp.getFileName() + ", size: " + bp.getSize());

                    if (!fileMatchers.test(bp)) {
                        log.info("file not matched");
                        continue;
                    } else {
                        log.info("file matched");
                    }
                    
                    Path        file = downloadPath.resolve(bp.getFileName());

                    if (Files.exists(file) && msgTime <= Files.getLastModifiedTime(file).toMillis()) {
                        if (!quiet) System.out.println("Skip existing file: " + file);
                        continue;
                    }

                    if (!test) {
                        if (!quiet) System.out.println("Downloading file: " + file);
                        Files.copy(bp.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
                        Files.setLastModifiedTime(file, FileTime.fromMillis(msgTime));
                    } else {
                        if (!quiet) System.out.println("File to download: " + file);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        Path    statePath = Util.getStateFile(stateFilename);
        if (!quiet) System.out.println("State file for config: " + statePath);
        if (!test && lastDate != null) {
            Util.saveProperties(statePath, Util.asProperties("download.last.date", Util.formatDateYYYYMMdd(lastDate)));
        }
        
        double  durationSec = (double)((System.currentTimeMillis() - startMS) / 100 * 100) / 1000;
        if (!quiet) System.out.println("fetchAttachments finished.  duration: " + durationSec  + "s");

        cfg.asLong("process.sleep.before.exit").ifPresent( sec -> Util.sleep(sec) );
    }

    public static void dumpMessages(Map<String, String> config) {
        Cfg             cfg = new Cfg(config);
        try {
            Folder      mailbox = openMailbox(cfg);
            int         msgCount = mailbox.getMessageCount();
            System.out.println("msgCount: " + msgCount);

            for (int i = msgCount; i > 0; i--) {
                System.out.println("Msg #" + i);
                Message     msg = mailbox.getMessage(i);

                Address[]   in = msg.getFrom();
                for (Address address : in) {
                    System.out.println("FROM: " + address.toString());
                }

                System.out.println("RECEIVED DATE: " + msg.getReceivedDate());
                System.out.println("SENT DATE: " + msg.getSentDate());
                System.out.println("SUBJECT: " + msg.getSubject());

                Object      content = msg.getContent();
                if (content instanceof String) {
                    System.out.println("MSG Type: String");
                    System.out.println("---------------------------");
                    System.out.println(Util.maxStr((String)content, 80));
                } else if (content instanceof Multipart) {
                    System.out.println("MSG Type: Multipart");
                    System.out.println("---------------------------");
                    Multipart   mp = (Multipart)content;
                    for (int j = 0; j < mp.getCount(); j++) {
                        BodyPart    bp = mp.getBodyPart(j);
                        System.out.println("PART CONTENT TYPE:  " + bp.getContentType());
                        System.out.println("PART DISPOSITION:   " + bp.getDisposition());
                        System.out.println("PART FILENAME:      " + bp.getFileName());
                        System.out.println("PART SIZE (bytes):  " + bp.getSize());
                        if ("text/html".equals(bp.getContentType())){
                            System.out.println("html content");
                        }
                        //System.out.println("PART CONTENT: " + Util.maxStr((String)bp.getContent(), 80));
                    }
                } else if (content instanceof Message) {
                    System.out.println("Msg Type: Nested Message");
                    System.out.println("---------------------------");
                } else if (content instanceof InputStream) {
                    System.out.println("Msg Type: input stream");
                    System.out.println("---------------------------");
                    InputStream is = (InputStream)content;
                    int c;
                    while ((c = is.read()) != -1)
                        System.out.write(c);
                }
                System.out.println("-----");
            }
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

    private static Folder openMailbox(Cfg cfg) throws Exception {
        Session     session = Session.getInstance(Util.asProperties("mail.store.protocol", cfg.val("mail.store.protocol").orElse("imaps")));
        Store       store = session.getStore();
        store.connect(cfg.ensure("mail.host"), cfg.ensure("mail.username"), cfg.ensure("mail.password"));

        Folder      mailbox = store.getFolder(cfg.val("mail.folder").orElse("INBOX"));
        mailbox.open(Folder.READ_ONLY);
        return mailbox;
    }
    
    private static Date parseDate(String dateStr) {
        dateStr = dateStr.trim().replaceAll("/", "-");
        return Util.parseDate(dateStr, Util.getDateYYYYMMdd(), Util.getDateMMddyyyy2());
    }

    private static Optional<SearchTerm> getDateRange(Cfg cfg, Cfg state, boolean quiet) {
        boolean     resumeFromLast = cfg.asBoolean("process.resume.from.last").orElse(Boolean.TRUE);
        Date        downloadLastDate = parseDate(state.val("download.last.date").orElse(""));
        Date        fromDate = parseDate(cfg.val("process.from.date").orElse(""));
        Date        toDate = parseDate(cfg.val("process.to.date").orElse(""));
        int         previousDays = cfg.asInt("process.previous.days").orElse(0);

        if (previousDays > 0) {
            // previousDays overrides fromDate and toDate.
            toDate = new Date();
            fromDate = Util.later(fromDate, Util.addDays(toDate, -previousDays));
        }

        if (resumeFromLast) {
            if (!quiet) System.out.println("Resume from last download date: " + Util.formatDateYYYYMMdd(downloadLastDate));
            fromDate = Util.later(fromDate, downloadLastDate);
        }

        if (fromDate != null && toDate != null)
            return Optional.of(new AndTerm(new ReceivedDateTerm(ComparisonTerm.GE, fromDate),
                                           new ReceivedDateTerm(ComparisonTerm.LE, toDate)));
        if (fromDate != null)
            return Optional.of(new ReceivedDateTerm(ComparisonTerm.GE, fromDate));
        if (toDate != null)
            return Optional.of(new ReceivedDateTerm(ComparisonTerm.LE, toDate));
        return Optional.empty();
    }


    private static Date getReceivedDate(Message msg) {
        try {
            return msg.getReceivedDate();
        } catch(Exception e) {
            Dbg.error(log, e);
            return new Date(0);
        }
    }

    private static String getFromName(Message msg) {
        try {
            for (Address address : msg.getFrom()) {
                return address.toString().replaceAll("<.*>", "").trim().replaceAll("^\"|\"$", "").trim();   // remove <xx>, trim, remove "", trim
            }
        } catch(Exception e) {
            Dbg.error(log, e);
        }
        return "";
    }

    private static String getFromNames(Message msg) {
        try {
            return Util.asStream(msg.getFrom())
                .map(address -> address.toString().replaceAll("<.*>", "").trim().replaceAll("^\"|\"$", "").trim())
                .collect(Collectors.joining(";"));
        } catch(Exception e) {
            return "";
        }
    }
    
    private static final Pattern    ADDRESS_REGEX = Pattern.compile("<(.+?)>");
    private static String getFromAddress(Message msg) {
        try {
            for (Address address : msg.getFrom()) {
                Matcher matcher = ADDRESS_REGEX.matcher(address.toString());
                if (matcher.find())
                    return matcher.group(1).trim();
                return "";
            }
        } catch(Exception e) {}
        return "";
    }

    private static String getFromAddresses(Message msg) {
        try {
            return Util.asList(msg.getFrom()).stream()
                .map(addr -> {
                        Matcher matcher = ADDRESS_REGEX.matcher(addr.toString());
                        return matcher.find() ? matcher.group(1).trim() : "";
                    })
                .collect(Collectors.joining(";"));
        } catch(Exception e) {
            return "";
        }
    }

    private static String getRecipients(Message msg, Message.RecipientType type) {
        try {
            return Util.asList(msg.getRecipients(type)).stream()
                .map(addr -> addr.toString())
                .collect(Collectors.joining(";"));
        } catch(Exception e) {
            return "";
        }
    }

    private static String getSubject(Message msg) {
        try {
            String subject = msg.getSubject();
            if (Util.empty(subject))
                return "";
            if (subject.startsWith("Re: "))
                subject = subject.substring(4);
            return subject.trim();
        } catch(Exception e) {
            return "";
        }
    }

    private static String getLowerFilename(BodyPart bp) {
        try {
            return Util.defval(bp.getFileName(), "").trim().toLowerCase();
        } catch(Exception e) {
            return "";
        }
    }

    private static long getFileSize(BodyPart bp) {
        try {
            return bp.getSize();
        } catch(Exception e) {
            return 0;
        }
    }

    private static List<BodyPart> getAttachmentParts(Message message, boolean downloadInline, List<BodyPart> attachmentParts) throws Exception {
        Object  content = message.getContent();
        if (!(content instanceof String) && content instanceof Multipart) {
            Multipart   mp = (Multipart) content;
            for (int i = 0; i < mp.getCount(); i++) {
                getAttachmentParts(mp.getBodyPart(i), downloadInline, attachmentParts);
            }
        }
        return attachmentParts;
    }

    private static List<BodyPart> getAttachmentParts(BodyPart part, boolean downloadInline, List<BodyPart> attachmentParts) throws Exception {
        Object  content = part.getContent();

        // Handle straight attachment
        if (content instanceof InputStream || content instanceof String) {
            if (!Util.empty(part.getFileName()) && (downloadInline || Util.iequals(Part.ATTACHMENT, part.getDisposition()))) {
                attachmentParts.add(part);
            }
            return attachmentParts;
        }

        // Handle nested attachments
        if (content instanceof Multipart) {
            Multipart   mp = (Multipart) content;
            for (int i = 0; i < mp.getCount(); i++) {
                getAttachmentParts(mp.getBodyPart(i), downloadInline, attachmentParts);
            }
        }

        return attachmentParts;
    }


    private static Predicate<Message> subjectContains(String param) {
        return msg -> getSubject(msg).toLowerCase().indexOf(param) > -1;
    }
    
    private static Predicate<Message> fromContains(String param) {
        return msg -> getFromNames(msg).toLowerCase().indexOf(param) > -1;
    }
    
    private static Predicate<Message> addressContains(String param) {
        return msg -> getFromAddresses(msg).toLowerCase().indexOf(param) > -1;
    }

    private static Predicate<Message> recipientsContains(String param, Message.RecipientType type) {
        return msg -> getRecipients(msg, type).indexOf(param) > -1;
    }

    private static Predicate<BodyPart> fileNameContains(String pattern) {
        return bp -> getLowerFilename(bp).indexOf(pattern) > -1;
    }

    private static Predicate<BodyPart> fileNameEndsWith(String pattern) {
        return bp -> getLowerFilename(bp).endsWith(pattern);
    }

    private static Predicate<BodyPart> fileSizeLess(String param) {
        return bp -> getFileSize(bp) <= Util.parseByteSize(param, 0);
    }

    private static Predicate<BodyPart> fileSizeGreater(String param) {
        return bp -> getFileSize(bp) >= Util.parseByteSize(param, 0);
    }

    private static Predicate<Message> buildMailMatchers(Cfg cfg) {

        Optional<Predicate<Message>>    subjectMatcher = cfg.asLowerCase("match.subject.contains").map(param -> subjectContains(param));
        Optional<Predicate<Message>>    fromMatcher    = cfg.asLowerCase("match.from.contains").map(param -> fromContains(param));
        Optional<Predicate<Message>>    addressMatcher = cfg.asLowerCase("match.address.contains").map(param -> addressContains(param));

        Optional<Predicate<Message>>    toMatcher = cfg.asLowerCase("match.to.contains").map(param -> recipientsContains(param, Message.RecipientType.TO));
        Optional<Predicate<Message>>    ccMatcher = cfg.asLowerCase("match.cc.contains").map(param -> recipientsContains(param, Message.RecipientType.CC));
        Optional<Predicate<Message>>    bccMatcher = cfg.asLowerCase("match.bcc.contains").map(param -> recipientsContains(param, Message.RecipientType.BCC));
        Stream<Predicate<Message>>      recipientMatchers = Util.flatOptionals(Util.asStream(toMatcher, ccMatcher, bccMatcher));
        Optional<Predicate<Message>>    recipientMatcher = recipientMatchers.reduce(Predicate::or); // OR the conditional parts.
        
        Stream<Predicate<Message>>      matchers = Util.flatOptionals(Util.asStream(subjectMatcher, fromMatcher, addressMatcher, recipientMatcher));
        return matchers.reduce(Predicate::and).orElse(msg -> true); // combine with AND, or always matched for empty list.
    }

    private static Predicate<BodyPart> buildFileMatchers(Cfg cfg) {
        Optional<Stream<Predicate<BodyPart>>>   nameMatchers = cfg.val("match.file.name.contains").map(
            param -> Util.splitParts(param, ",").map(part -> fileNameContains(part)) );
        Optional<Predicate<BodyPart>>           nameMatcher = nameMatchers.map(s -> s.reduce(Predicate::or).get());  // OR the conditional parts.
        
        Optional<Stream<Predicate<BodyPart>>>   typeMatchers = cfg.val("match.file.type.is").map(
            param -> Util.splitParts(param, ",").map(part -> fileNameEndsWith(part)) );
        Optional<Predicate<BodyPart>>           typeMatcher = typeMatchers.map(s -> s.reduce(Predicate::or).get());

        Optional<Predicate<BodyPart>>           lessMatcher = cfg.val("match.file.size.less.than").map(param -> fileSizeLess(param));

        Optional<Predicate<BodyPart>>           greaterMatcher = cfg.val("match.file.size.greater.than").map(param -> fileSizeGreater(param));

        Stream<Predicate<BodyPart>>             matchers = Util.flatOptionals(Util.asStream(nameMatcher, typeMatcher, lessMatcher, greaterMatcher));
        return matchers.reduce(Predicate::and).orElse(bp -> true);  // combine with AND, or always matched for unspecified empty list.
    }


    private static final int MAX_CHAR = 128;
    private static String pathFriendly(String str) {
        str = Util.cleanFilename(str);
        return Util.maxStr(str, MAX_CHAR).trim();
    }
    
    private static Path resolveDownloadPath(List<Function<Message, Path>> pathers, Path basePath, Message msg) {
        return pathers.stream()
            .map(f -> f.apply(msg))
            .reduce(basePath, (acc, path) -> acc.resolve(path));
    }

    private static List<Function<Message, Path>> buildGroupbyPathers(Cfg cfg) {
        return Util.asList("download.groupby", "download.groupby.2nd", "download.groupby.3rd")
            .stream().map(gb -> groupbyToPathers(cfg.val(gb).orElse("none"))).collect(Collectors.toList());
    }

    private static Function<Message, Path> groupbyToPathers(String groupby) {
        if (Util.iequals(groupby, "none")) {
            return msg -> Paths.get("");
        } else if (Util.iequals(groupby, "date")) {
            return msg -> Paths.get(Util.formatDateYYYYMMdd(getReceivedDate(msg)));
        } else if (Util.iequals(groupby, "month")) {
            return msg -> Paths.get(Util.formatDateYYYYMM(getReceivedDate(msg)));
        } else if (Util.iequals(groupby, "year")) {
            return msg -> Paths.get(Util.formatDateYYYY(getReceivedDate(msg)));
        } else if (Util.iequals(groupby, "from")) {
            return msg -> Paths.get(Util.defval(getFromName(msg), "NONE"));
        } else if (Util.iequals(groupby, "address")) {
            return msg -> Paths.get(Util.defval(getFromAddress(msg), "NOADDRESS"));
        } else if (Util.iequals(groupby, "subject")) {
            return msg -> Paths.get(Util.defval(pathFriendly(getSubject(msg)), "NOSUBJECT"));
        } else {
            return msg -> Paths.get("");
        }
    }

}
