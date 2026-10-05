package com.ptit.courseregistration.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonHocFormValidationTest {

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

    private MonHocForm validForm() {
        MonHocForm form = new MonHocForm();
        form.setMaMonHoc("MH001");
        form.setMaKhoa("CNTT");
        form.setTenMonHoc("Lap trinh Java");
        form.setSoTinChi(3);
        form.setSoTietLyThuyet(30);
        form.setSoTietThucHanh(15);
        form.setMoTa("Mon hoc co so.");
        return form;
    }

    private void assertViolationOn(MonHocForm form, String property) {
        Set<ConstraintViolation<MonHocForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(
                violation -> violation.getPropertyPath().toString().equals(property)),
                "Expected violation on " + property + " but got " + violations);
    }

    @Test
    void validFormShouldHaveNoViolations() {
        assertEquals(0, validator.validate(validForm()).size());
    }

    @Test
    void blankSubjectCodeShouldFail() {
        MonHocForm form = validForm();
        form.setMaMonHoc(" ");

        assertViolationOn(form, "maMonHoc");
    }

    @Test
    void subjectCodeOverMaxLengthShouldFail() {
        MonHocForm form = validForm();
        form.setMaMonHoc("A".repeat(21));

        assertViolationOn(form, "maMonHoc");
    }

    @Test
    void blankDepartmentShouldFail() {
        MonHocForm form = validForm();
        form.setMaKhoa("");

        assertViolationOn(form, "maKhoa");
    }

    @Test
    void blankSubjectNameShouldFail() {
        MonHocForm form = validForm();
        form.setTenMonHoc(" ");

        assertViolationOn(form, "tenMonHoc");
    }

    @Test
    void subjectNameOverMaxLengthShouldFail() {
        MonHocForm form = validForm();
        form.setTenMonHoc("A".repeat(101));

        assertViolationOn(form, "tenMonHoc");
    }

    @Test
    void nullCreditsShouldFail() {
        MonHocForm form = validForm();
        form.setSoTinChi(null);

        assertViolationOn(form, "soTinChi");
    }

    @Test
    void creditsBelowOneShouldFail() {
        MonHocForm form = validForm();
        form.setSoTinChi(0);

        assertViolationOn(form, "soTinChi");
    }

    @Test
    void negativeTheoryHoursShouldFail() {
        MonHocForm form = validForm();
        form.setSoTietLyThuyet(-1);

        assertViolationOn(form, "soTietLyThuyet");
    }

    @Test
    void negativePracticeHoursShouldFail() {
        MonHocForm form = validForm();
        form.setSoTietThucHanh(-1);

        assertViolationOn(form, "soTietThucHanh");
    }

    @Test
    void descriptionOverMaxLengthShouldFail() {
        MonHocForm form = validForm();
        form.setMoTa("A".repeat(256));

        assertViolationOn(form, "moTa");
    }
}
