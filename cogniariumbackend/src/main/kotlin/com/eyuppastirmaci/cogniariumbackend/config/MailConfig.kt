package com.eyuppastirmaci.cogniariumbackend.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

/**
 * Mail configuration for Spring Boot.
 * This ensures JavaMailSender bean is available even if mail properties are not fully configured.
 * Spring Boot's auto-configuration will create the bean if mail properties are set,
 * otherwise this fallback configuration will be used.
 */
@Configuration
class MailConfig {

    /**
     * Creates a JavaMailSender bean as fallback.
     * This is only used if Spring Boot's auto-configuration doesn't create one.
     * Configured for MailHog (localhost:1025) by default.
     */
    @Bean
    @ConditionalOnMissingBean(JavaMailSender::class)
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = "localhost"
        mailSender.port = 1025
        mailSender.username = ""
        mailSender.password = ""
        
        val props = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "false"
        props["mail.smtp.starttls.enable"] = "false"
        props["mail.smtp.connectiontimeout"] = "5000"
        props["mail.smtp.timeout"] = "5000"
        props["mail.smtp.writetimeout"] = "5000"
        
        return mailSender
    }
}

