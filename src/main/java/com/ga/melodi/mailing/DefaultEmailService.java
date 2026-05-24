package com.ga.melodi.mailing;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class DefaultEmailService implements EmailService {

	@Value("${spring.mail.from}")
	private String fromEmail;

	private final JavaMailSender emailSender;
	private final SpringTemplateEngine templateEngine;

	@Override
	public void sendMail(AbstractEmailContext email) {
		try {
			MimeMessage message = emailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(
					message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

			Context context = new Context();
			context.setVariables(email.getContext());

			String emailContent = templateEngine.process(email.getTemplateLocation(), context);

			helper.setTo(email.getTo());
			helper.setSubject(email.getSubject());
			helper.setFrom(fromEmail, "Melodi Instruments");
			helper.setText(emailContent, true);

			emailSender.send(message);
			System.out.println("✓ Email sent to " + email.getTo());
		} catch (Exception e) {
			throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
		}
	}
}
