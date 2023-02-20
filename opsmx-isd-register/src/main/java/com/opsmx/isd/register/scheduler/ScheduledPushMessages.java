package com.opsmx.isd.register.scheduler;

import com.opsmx.isd.register.entities.User;
import com.opsmx.isd.register.repositories.UserRepository;
import com.opsmx.isd.register.service.EmailService;
import com.opsmx.isd.register.util.NonStreamingExcelExport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ScheduledPushMessages {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private EmailService emailService;

    @Value("${sender.sendTo.to:#{null}}")
    private String to;

    @Value("${sender.sendTo.cc:#{null}}")
    private String cc;

    @Autowired
    private UserRepository userRepository;

    public ScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    //@Scheduled(fixedRate = 20000)
    // Every Monday at 1 AM ; second, minute, hour, day of month, month, day(s) of week
//    @Scheduled(cron = "0 0 1 * * MON")
    public void sendMessage() throws MessagingException, IOException {
        Date present = Date.from(Instant.now());
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date sevenDaysPast = new Date(present.getTime() - (7 * DAY_IN_MS));
        Iterable<User> userList1 = userRepository.findAllByCreatedAtBetween(sevenDaysPast, present);
        Iterator<User> iterator = userList1.iterator();
        List<User> userList = new ArrayList<>();
        while (iterator.hasNext()) {
            userList.add(iterator.next());
        }
        NonStreamingExcelExport nonStreamingExcelExport = new NonStreamingExcelExport(8, userList.size(),
                userList);
        Long endSheetTime = System.currentTimeMillis();
        String attachmentName = endSheetTime + "_" + nonStreamingExcelExport.getFilename();
        File tempFile = File.createTempFile("Spreadsheet", attachmentName, null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(nonStreamingExcelExport.getBytes());
        String subject = "Weekly automated sheet, 1 AM, Mon IST, " +
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.S").format(present);
        String text = "Spreadsheet";
        emailService.sendMessageWithAttachment(to, cc,
                subject, text, tempFile.getAbsolutePath(), "Spreadsheet_" + attachmentName);
    }

    // Every Monday at 1 AM ; second, minute, hour, day of month, month, day(s) of week
    @Scheduled(cron = "0 0 1 * * MON")
    public void sendUserRegistrationReports() throws MessagingException, IOException {
        log.info("Finding user registration reports for the week");
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.S");
        LocalDateTime present = LocalDateTime.now();
        LocalDateTime sevenDaysPast = present.minusDays(7);
        Optional<List<User>> userList = userRepository.findAllByCreatedAtBetween(sevenDaysPast, present);
        if (userList.isPresent()) {
            NonStreamingExcelExport nonStreamingExcelExport = new NonStreamingExcelExport(8, userList.get().size(),
                    userList.get());
            Long endSheetTime = System.currentTimeMillis();
            String attachmentName = endSheetTime + "_" + nonStreamingExcelExport.getFilename();
            File tempFile = File.createTempFile("Spreadsheet", attachmentName, null);
            try(FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(nonStreamingExcelExport.getBytes());
                String subject = "Weekly automated sheet, 1 AM, Mon IST, " +
                        present.format(format);
                String text = "Spreadsheet";
                emailService.sendMessageWithAttachment(to, cc,
                        subject, text, tempFile.getAbsolutePath(), "Spreadsheet_" + attachmentName);
            }

        } else {
            log.info("No Reports found between the dates : {} and {}", present.toString(), sevenDaysPast.toString());

        }
    }
}