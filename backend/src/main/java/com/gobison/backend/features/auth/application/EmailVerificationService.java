package com.gobison.backend.features.auth.application;

import com.gobison.backend.features.auth.infrastructure.email.EmailService;
import com.gobison.backend.features.user.domain.model.User;
import com.gobison.backend.features.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.gobison.backend.features.auth.application.EmailVerificationException.Reason.*;

@Service
public class EmailVerificationService {

    private static final Pattern UM_NET_ID = Pattern.compile("[A-Za-z0-9]+(?:[._-][A-Za-z0-9]+)*");
    private static final Pattern CODE = Pattern.compile("[0-9]{6}");
    private static final String EMAIL_DOMAIN = "@myumanitoba.ca";

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom random;

    @Autowired
    public EmailVerificationService(UserRepository userRepository, EmailService emailService) {
        this(userRepository, emailService, new SecureRandom());
    }

    EmailVerificationService(UserRepository userRepository, EmailService emailService, SecureRandom random) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.random = random;
    }

    // Call after registration has committed. SMTP failure does not undo the saved code.
    public void sendVerificationCode(String umNetId) {
        User user = findUnverifiedUser(umNetId);
        String storedUmNetId = user.getUmNetId();
        if (storedUmNetId == null || storedUmNetId.length() > 64
                || !UM_NET_ID.matcher(storedUmNetId).matches()) {
            throw new IllegalArgumentException("The stored UMNetID is malformed.");
        }

        String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        user.setVerificationCode(code);
        userRepository.save(user);
        emailService.sendVerificationCode(storedUmNetId + EMAIL_DOMAIN, code);
    }

    @Transactional
    public void verifyEmail(String umNetId, String code) {
        if (code == null || !CODE.matcher(code).matches()) {
            throw new EmailVerificationException(MALFORMED_CODE);
        }

        User user = findUnverifiedUser(umNetId);
        if (!code.equals(user.getVerificationCode())) {
            throw new EmailVerificationException(INCORRECT_CODE);
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);
    }

    private User findUnverifiedUser(String umNetId) {
        if (umNetId == null || umNetId.isBlank()) {
            throw new EmailVerificationException(UNAUTHENTICATED);
        }
        User user = userRepository.findByUmNetId(umNetId)
                .orElseThrow(() -> new EmailVerificationException(UNAUTHENTICATED));
        if (user.isVerified()) {
            throw new EmailVerificationException(ALREADY_VERIFIED);
        }
        return user;
    }
}
