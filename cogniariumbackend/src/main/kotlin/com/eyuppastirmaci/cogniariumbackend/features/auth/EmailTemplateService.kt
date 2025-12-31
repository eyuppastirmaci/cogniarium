package com.eyuppastirmaci.cogniariumbackend.features.auth

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

/**
 * Service for rendering email templates using Thymeleaf.
 */
@Service
class EmailTemplateService(
    @Qualifier("emailTemplateEngine")
    private val templateEngine: TemplateEngine
) {
    /**
     * Renders the email verification template.
     * @param verificationLink The email verification link
     * @return Rendered HTML content
     */
    fun renderVerificationEmail(verificationLink: String): String {
        val context = Context().apply {
            setVariable("verificationLink", verificationLink)
        }
        return templateEngine.process("emails/verification-email", context)
    }
}

