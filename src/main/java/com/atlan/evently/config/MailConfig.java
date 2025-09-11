package com.atlan.evently.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:localhost}")
    private String mailHost;

    @Value("${spring.mail.port:1025}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // MailHog configuration for local development
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        
        // MailHog doesn't require authentication
        if (!mailUsername.isEmpty()) {
            mailSender.setUsername(mailUsername);
            mailSender.setPassword(mailPassword);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", !mailUsername.isEmpty());
        props.put("mail.smtp.starttls.enable", false); // MailHog doesn't support TLS
        props.put("mail.debug", "false");

        return mailSender;
    }
}
