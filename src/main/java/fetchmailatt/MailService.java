
package fetchmailatt;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.regex.*;
import java.util.logging.*;
import javax.mail.*;
import javax.mail.search.*;

public class MailService {
    private static final Logger log = Logger.getLogger(MailService.class.getName());

    /*
      Sample config:
      MailService.fetchAttachments(Util.asMap(
          "mail.store.protocol", "imaps",
          "mail.host", "imap.gmail.com",
          "mail.username", "USER@gmail.com",
          "mail.password", "PASSWORD",
          "mail.folder", "INBOX",
          "download.directory", "/tmp/att",
          "download.groupby", "address",
          "download.groupby.2nd", "year",
          "download.groupby.3rd", "month",
          "download.mail.limit", "0",
          "download.earliest.date", "07/07/2015",
          "download.last.days", "7"
      ));
    */
    public static void fetchAttachments(Map<String, String> config, boolean quiet) throws Exception {
        Path        downloadDir = Paths.get(Util.ensure(config, "download.directory"));
        String      groupby  = Util.defval(Util.ensure(config, "download.groupby", ""), "none");
        String      groupby2 = Util.defval(Util.ensure(config, "download.groupby.2nd", ""), "none");
        String      groupby3 = Util.defval(Util.ensure(config, "download.groupby.3rd", ""), "none");
        int         downloadLimit = Util.toInt(Util.ensure(config, "download.mail.limit", "0"), 0);
        Date        earliestDate = parseDate(Util.ensure(config, "download.earliest.date", ""));
        int         previousDays = Util.toInt(Util.ensure(config, "download.previous.days", "0"), 0);

        Session     session = Session.getInstance(Util.asProperties("mail.store.protocol", Util.ensure(config, "mail.store.protocol", "imaps")));
        Store       store = session.getStore();
        store.connect(Util.ensure(config, "mail.host"), Util.ensure(config, "mail.username"), Util.ensure(config, "mail.password"));

        Folder      mailbox = store.getFolder(Util.ensure(config, "mail.folder", "INBOX"));
        mailbox.open(Folder.READ_ONLY);

        if (previousDays > 0) {
            earliestDate = Util.later(earliestDate, Util.addDays(new Date(), -previousDays));
        }

        Message[]   messages;
        if (earliestDate == null) {
            messages = mailbox.getMessages();
        } else {
            SearchTerm  newerThan = new ReceivedDateTerm(ComparisonTerm.GE, earliestDate);
            messages = mailbox.search(newerThan);
        }

        int         latest = messages.length;
        int         oldest = downloadLimit <= 0 ? 0 : (messages.length < downloadLimit ? 0 : messages.length - downloadLimit);

        if (!quiet) System.out.println("Message count: " + messages.length + ", oldest: " + (oldest+1) + ", latest: " + latest);
        
        for (int i = latest; i > oldest; i--) {
            if (!quiet) System.out.println("Message #" + i);
            Message         msg = messages[i - 1];
            long            msgTime = msg.getReceivedDate().getTime();
            Path            downloadPath = addGroupbyPath(addGroupbyPath(addGroupbyPath(downloadDir, groupby, msg), groupby2, msg), groupby3, msg);
            List<BodyPart>  attachmentParts = getAttachmentParts(msg, new ArrayList<BodyPart>());
            if (attachmentParts.size() > 0 && !Files.exists(downloadPath))
                Files.createDirectories(downloadPath);

            if (!quiet) System.out.println("MAIL DATE: " + msg.getReceivedDate() + ", FROM: " + getFromName(msg) + " " + getFromAddress(msg));
            for (BodyPart bp : attachmentParts) {
                Path    file = downloadPath.resolve(bp.getFileName());

                if (Files.exists(file)) {
                    if (msgTime <= Files.getLastModifiedTime(file).toMillis()) {
                        if (!quiet) System.out.println("Skip existing " + file);
                        continue;
                    }
                }

                if (!quiet) System.out.println("Downloading " + file);
                Files.copy(bp.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
                Files.setLastModifiedTime(file, FileTime.fromMillis(msgTime));
            }

        }
    }

    public static void dumpMessages(Map<String, String> config) {
        try {
            Session     session = Session.getInstance(Util.asProperties("mail.store.protocol", Util.ensure(config, "mail.store.protocol", "imaps")));
            Store       store = session.getStore();
            store.connect(Util.ensure(config, "mail.host"), Util.ensure(config, "mail.username"), Util.ensure(config, "mail.password"));

            Folder      mailbox = store.getFolder(Util.ensure(config, "mail.folder", "INBOX"));
            mailbox.open(Folder.READ_ONLY);

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
                    System.out.println((String)content);
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

                        System.out.println("PART CONTENT: " + bp.getContent());
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

    private static String getFromName(Message msg) throws Exception {
        for (Address address : msg.getFrom()) {
            return address.toString().replaceAll("<.*>", "").trim().replaceAll("^\"|\"$", "").trim();   // remove <xx>, trim, remove "", trim
        }
        return "";
    }

    private static final Pattern    ADDRESS_REGEX = Pattern.compile("<(.+?)>");
    private static String getFromAddress(Message msg) throws Exception {
        for (Address address : msg.getFrom()) {
            Matcher matcher = ADDRESS_REGEX.matcher(address.toString());
            if (matcher.find())
                return matcher.group(1).trim();
            return "";
        }
        return "";
    }

    private static List<BodyPart> getAttachmentParts(Message message, List<BodyPart> attachmentParts) throws Exception {
        Object  content = message.getContent();
        if (!(content instanceof String) && content instanceof Multipart) {
            Multipart   mp = (Multipart) content;
            for (int i = 0; i < mp.getCount(); i++) {
                getAttachmentParts(mp.getBodyPart(i), attachmentParts);
            }
        }
        return attachmentParts;
    }

    private static List<BodyPart> getAttachmentParts(BodyPart part, List<BodyPart> attachmentParts) throws Exception {
        Object  content = part.getContent();

        // Handle straight attachment
        if (content instanceof InputStream || content instanceof String) {
            if (Util.iequals(Part.ATTACHMENT, part.getDisposition()) && !Util.empty(part.getFileName())) {
                attachmentParts.add(part);
            }
            return attachmentParts;
        }

        // Handle nested attachments
        if (content instanceof Multipart) {
            Multipart   mp = (Multipart) content;
            for (int i = 0; i < mp.getCount(); i++) {
                getAttachmentParts(mp.getBodyPart(i), attachmentParts);
            }
        }

        return attachmentParts;
    }

    private static Path addGroupbyPath(Path basePath, String groupby, Message msg) throws Exception {
        if (Util.iequals(groupby, "none")) {
            return basePath;
        } else if (Util.iequals(groupby, "date")) {
            return basePath.resolve(Util.formatDateYYYYMMdd(msg.getReceivedDate()));
        } else if (Util.iequals(groupby, "month")) {
            return basePath.resolve(Util.formatDateYYYYMM(msg.getReceivedDate()));
        } else if (Util.iequals(groupby, "year")) {
            return basePath.resolve(Util.formatDateYYYY(msg.getReceivedDate()));
        } else if (Util.iequals(groupby, "from")) {
            return basePath.resolve(Util.defval(getFromName(msg), "NONE"));
        } else if (Util.iequals(groupby, "address")) {
            return basePath.resolve(Util.defval(getFromAddress(msg), "NOADDRESS"));
        }
        return basePath;
    }

    private static Date parseDate(String dateStr) {
        dateStr = dateStr.trim().replaceAll("/", "-");
        return Util.parseDate(dateStr, Util.getDateYYYYMMdd(), Util.getDateMMddyyyy2());
    }

}
