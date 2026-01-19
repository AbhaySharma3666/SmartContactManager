package com.scm.services;

import org.springframework.web.multipart.MultipartFile;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    void sendEmailWithHtml();

    void sendEmailWithAttachment();

    void sendEmailWithAttachment(String to, String subject, String body, MultipartFile attachment);

}
