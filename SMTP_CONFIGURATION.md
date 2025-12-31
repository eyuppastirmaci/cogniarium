# SMTP Configuration Guide

This guide explains how to configure SMTP settings for email sending in Cogniarium.

## Development Setup (MailHog)

For local development, we use MailHog, a local email testing tool that captures all outgoing emails.

### Starting MailHog

1. **Using Docker Compose:**
   ```bash
   docker compose --profile mail up -d mailhog
   ```
   Or with full stack:
   ```bash
   docker compose --profile full up -d
   ```

2. **Access MailHog Web UI:**
   - Open http://localhost:8025 in your browser
   - All emails sent by the application will appear here

3. **SMTP Configuration:**
   - SMTP Port: `1025`
   - No authentication required
   - Already configured in `application-dev.yml`

### Manual MailHog Setup

If you prefer to run MailHog separately:

```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

## Production Setup

For production, configure your SMTP provider settings via environment variables.

### Required Environment Variables

```bash
MAIL_HOST=smtp.example.com          # SMTP server hostname
MAIL_PORT=587                        # SMTP port (usually 587 for TLS, 465 for SSL)
MAIL_USERNAME=your-username          # SMTP username
MAIL_PASSWORD=your-password          # SMTP password
MAIL_FROM=noreply@yourdomain.com     # Sender email address
```

### Optional Environment Variables

```bash
MAIL_SMTP_SSL_TRUST=*                # SSL trust settings (if needed)
```

### Common SMTP Providers

#### Gmail
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password      # Use App Password, not regular password
MAIL_FROM=your-email@gmail.com
```

**Note:** Gmail requires App Passwords for SMTP. Enable 2FA and generate an App Password.

#### SendGrid
```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
MAIL_FROM=verified-sender@yourdomain.com
```

#### AWS SES
```bash
MAIL_HOST=email-smtp.us-east-1.amazonaws.com  # Use your region
MAIL_PORT=587
MAIL_USERNAME=your-ses-smtp-username
MAIL_PASSWORD=your-ses-smtp-password
MAIL_FROM=verified-sender@yourdomain.com
```

#### Mailgun
```bash
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=your-mailgun-username
MAIL_PASSWORD=your-mailgun-password
MAIL_FROM=verified-sender@yourdomain.com
```

### Testing SMTP Configuration

1. Start the application with production profile:
   ```bash
   SPRING_PROFILES_ACTIVE=prod java -jar cogniarium-backend.jar
   ```

2. Register a new user to trigger a verification email

3. Check your email inbox (and spam folder)

### Troubleshooting

#### Email not sending in development
- Ensure MailHog is running: `docker ps | grep mailhog`
- Check MailHog web UI at http://localhost:8025
- Check application logs for email sending errors

#### Email not sending in production
- Verify all environment variables are set correctly
- Check SMTP credentials are valid
- Ensure sender email is verified with your SMTP provider
- Check firewall rules allow outbound SMTP connections
- Review application logs for detailed error messages

#### Common Errors

**"Authentication failed"**
- Verify username and password are correct
- For Gmail, ensure you're using an App Password, not your regular password
- Check if 2FA is enabled (required for Gmail App Passwords)

**"Connection timeout"**
- Verify SMTP host and port are correct
- Check firewall/network settings
- Ensure SMTP server is accessible from your deployment environment

**"Sender not verified"**
- Verify the sender email address with your SMTP provider
- For AWS SES, verify the email in SES console
- For SendGrid, verify the sender in SendGrid dashboard

### Security Best Practices

1. **Never commit SMTP credentials to version control**
   - Use environment variables or secrets management
   - Use `.env` files (not committed) for local development

2. **Use App Passwords for Gmail**
   - Never use your main Gmail password
   - Generate App Passwords from Google Account settings

3. **Rotate credentials regularly**
   - Change SMTP passwords periodically
   - Use secrets management tools in production

4. **Use TLS/SSL**
   - Always use port 587 (TLS) or 465 (SSL)
   - Never use unencrypted SMTP (port 25) in production

5. **Verify sender domains**
   - Use verified sender addresses
   - Set up SPF, DKIM, and DMARC records for better deliverability

