package com.stayseat.notification_service.service.impl;

import com.stayseat.notification_service.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {

        log.info("======================================");
        log.info("EMAIL");
        log.info("To      : {}", to);
        log.info("Subject : {}", subject);
        log.info("Body    : {}", body);
        log.info("======================================");

    }
}