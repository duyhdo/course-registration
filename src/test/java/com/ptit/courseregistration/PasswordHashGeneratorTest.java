package com.ptit.courseregistration;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordHashGeneratorTest {

    @Test
    void generatePasswordHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println(
                "ADMIN = " + encoder.encode("Admin@123")
        );

        System.out.println(
                "STUDENT = " + encoder.encode("Student@123")
        );
    }
}