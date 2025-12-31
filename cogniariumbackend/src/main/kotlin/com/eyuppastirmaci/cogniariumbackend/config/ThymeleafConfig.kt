package com.eyuppastirmaci.cogniariumbackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.TemplateEngine
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver

/**
 * Configuration for Thymeleaf template engine.
 * Used for rendering email templates.
 */
@Configuration
class ThymeleafConfig {

    @Bean
    fun emailTemplateEngine(): TemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.setTemplateResolver(emailTemplateResolver())
        return templateEngine
    }

    private fun emailTemplateResolver(): ITemplateResolver {
        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.prefix = "templates/"
        templateResolver.suffix = ".html"
        templateResolver.templateMode = TemplateMode.HTML
        templateResolver.characterEncoding = "UTF-8"
        templateResolver.isCacheable = false // Disable cache for development
        return templateResolver
    }
}

