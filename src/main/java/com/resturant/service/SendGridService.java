package com.resturant.service;


import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.sender.email}")
    private String senderEmail;

    public void sendEmail(String to, String subject, String contentHtml) throws IOException {
        Email from = new Email(senderEmail);
        Email recipient = new Email(to);
        Content content = new Content("text/html", contentHtml);
        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() != 202) {
            throw new IOException("Failed to send email. Status code: " + response.getStatusCode());
        }
    }
}
