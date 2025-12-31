package com.eyuppastirmaci.cogniariumbackend.features.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import jakarta.mail.internet.MimeMessage

/**
 * Service for sending emails.
 * In development, emails can be logged or sent via MailHog.
 * In production, configure SMTP settings in application.yaml.
 */
@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val emailTemplateService: EmailTemplateService,
    @Value("\${spring.mail.from:noreply@cogniarium.local}")
    private val fromEmail: String
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    /**
     * Sends a verification email to the user.
     */
    fun sendVerificationEmail(email: String, verificationLink: String) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8").apply {
                setFrom(fromEmail)
                setTo(email)
                setSubject("Verify Your Email - Cogniarium")
                
                // Render HTML template
                val htmlContent = emailTemplateService.renderVerificationEmail(verificationLink)
                setText(buildPlainTextContent(verificationLink), htmlContent)
            }

            mailSender.send(message)
            logger.info("Verification email sent to: $email")
        } catch (e: Exception) {
            logger.error("Failed to send verification email to: $email", e)
            // In development, log the email even if sending fails
            logger.info("=== EMAIL (Failed to send, logging instead) ===")
            logger.info("To: $email")
            logger.info("Subject: Verify Your Email - Cogniarium")
            logger.info("Verification Link: $verificationLink")
            logger.info("================================================")
            // Don't throw exception in development to allow testing
            // throw EmailSendingException("Failed to send verification email", e)
        }
    }

    /**
     * Builds plain text fallback content for email clients that don't support HTML.
     */
    private fun buildPlainTextContent(verificationLink: String): String {
        return """
            Welcome to Cogniarium!
            
            Thank you for signing up. We're excited to have you on board!
            
            To get started, please verify your email address by clicking the link below:
            $verificationLink
            
            This link will expire in 24 hours.
            
            If you didn't create an account with Cogniarium, please ignore this email.
            
            Best regards,
            Cogniarium Team
        """.trimIndent()
    }
}

/**
 * Exception thrown when email sending fails.
 */
class EmailSendingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

