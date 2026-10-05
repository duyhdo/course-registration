package com.ptit.courseregistration.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HocKyConfigFormValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private HocKyConfigForm validForm() {
        return new HocKyConfigForm(
                LocalDateTime.of(2026, 8, 1, 8, 0),
                LocalDateTime.of(2026, 8, 15, 17, 0),
                10, 24);
    }

    private void assertSingleViolation(HocKyConfigForm form, String property) {
        Set<ConstraintViolation<HocKyConfigForm>> violations = validator.validate(form);
        assertEquals(1, violations.size());
        assertEquals(property, violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void validFormShouldHaveNoViolations() {
        assertTrue(validator.validate(validForm()).isEmpty());
    }

    @Test
    void nullRegistrationOpenDateShouldFail() {
        HocKyConfigForm form = validForm();
        form.setNgayMoDangKy(null);
        assertSingleViolation(form, "ngayMoDangKy");
    }

    @Test
    void nullRegistrationCloseDateShouldFail() {
        HocKyConfigForm form = validForm();
        form.setNgayDongDangKy(null);
        assertSingleViolation(form, "ngayDongDangKy");
    }

    @Test
    void nullMaximumCreditsShouldFail() {
        HocKyConfigForm form = validForm();
        form.setTinChiToiDa(null);
        assertSingleViolation(form, "tinChiToiDa");
    }

    @Test
    void zeroMaximumCreditsShouldFail() {
        HocKyConfigForm form = validForm();
        form.setTinChiToiDa(0);
        assertSingleViolation(form, "tinChiToiDa");
    }

    @Test
    void negativeMaximumCreditsShouldFail() {
        HocKyConfigForm form = validForm();
        form.setTinChiToiDa(-5);
        assertSingleViolation(form, "tinChiToiDa");
    }

    @Test
    void negativeMinimumCreditsShouldFail() {
        HocKyConfigForm form = validForm();
        form.setTinChiToiThieu(-1);
        assertSingleViolation(form, "tinChiToiThieu");
    }

    @Test
    void nullMinimumCreditsShouldBeAllowed() {
        HocKyConfigForm form = validForm();
        form.setTinChiToiThieu(null);
        assertTrue(validator.validate(form).isEmpty());
    }
}
