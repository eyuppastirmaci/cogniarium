package com.eyuppastirmaci.cogniariumbackend.features.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.thymeleaf.TemplateEngine
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

@SpringBootTest
@TestPropertySource(properties = [
    "spring.mail.host=localhost",
    "spring.mail.port=1025"
])
class EmailTemplateServiceTest {

    private lateinit var emailTemplateService: EmailTemplateService

    @BeforeEach
    fun setUp() {
        val templateEngine = SpringTemplateEngine()
        val templateResolver = ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            templateMode = TemplateMode.HTML
            characterEncoding = "UTF-8"
            isCacheable = false
        }
        templateEngine.setTemplateResolver(templateResolver)
        emailTemplateService = EmailTemplateService(templateEngine)
    }

    @Test
    fun `should render verification email template`() {
        val verificationLink = "http://localhost:8080/api/auth/verify-email?token=test-token-123"
        val html = emailTemplateService.renderVerificationEmail(verificationLink)

        assertNotNull(html)
        assertTrue(html.isNotBlank())
        assertTrue(html.contains(verificationLink))
        assertTrue(html.contains("Cogniarium"))
        assertTrue(html.contains("Verify Your Email"))
    }

    @Test
    fun `should include verification link in rendered template`() {
        val verificationLink = "http://example.com/verify?token=abc123"
        val html = emailTemplateService.renderVerificationEmail(verificationLink)

        assertTrue(html.contains(verificationLink))
    }

    @Test
    fun `should render HTML structure correctly`() {
        val verificationLink = "http://localhost:8080/api/auth/verify-email?token=test"
        val html = emailTemplateService.renderVerificationEmail(verificationLink)

        assertTrue(html.contains("<!DOCTYPE html>") || html.contains("<html"))
        assertTrue(html.contains("</html>") || html.contains("</body>"))
    }
}

