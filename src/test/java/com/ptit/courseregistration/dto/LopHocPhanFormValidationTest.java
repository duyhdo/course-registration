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

class LopHocPhanFormValidationTest {

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

    private LopHocPhanForm validForm() {
        LopHocPhanForm form = new LopHocPhanForm();
        form.setMaLopHocPhan("LHP001");
        form.setMaMonHoc("MH001");
        form.setMaHocKy("HK2026");
        form.setMaGV("GV001");
        form.setPhongHoc("A101");
        form.setThuHoc(2);
        form.setTietBatDau(1);
        form.setTietKetThuc(3);
        form.setSiSoToiDa(40);
        return form;
    }

    private void assertViolationOn(LopHocPhanForm form, String property) {
        Set<ConstraintViolation<LopHocPhanForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(
                violation -> violation.getPropertyPath().toString().equals(property)),
                "Expected violation on " + property + " but got " + violations);
    }

    @Test
    void validFormShouldHaveNoViolations() {
        assertEquals(0, validator.validate(validForm()).size());
    }

    @Test
    void blankCourseSectionCodeShouldFail() {
        LopHocPhanForm form = validForm();
        form.setMaLopHocPhan(" ");

        assertViolationOn(form, "maLopHocPhan");
    }

    @Test
    void courseSectionCodeOverMaxLengthShouldFail() {
        LopHocPhanForm form = validForm();
        form.setMaLopHocPhan("A".repeat(21));

        assertViolationOn(form, "maLopHocPhan");
    }

    @Test
    void blankSubjectShouldFail() {
        LopHocPhanForm form = validForm();
        form.setMaMonHoc("");

        assertViolationOn(form, "maMonHoc");
    }

    @Test
    void blankSemesterShouldFail() {
        LopHocPhanForm form = validForm();
        form.setMaHocKy(" ");

        assertViolationOn(form, "maHocKy");
    }

    @Test
    void nullCapacityShouldFail() {
        LopHocPhanForm form = validForm();
        form.setSiSoToiDa(null);

        assertViolationOn(form, "siSoToiDa");
    }

    @Test
    void capacityBelowOneShouldFail() {
        LopHocPhanForm form = validForm();
        form.setSiSoToiDa(0);

        assertViolationOn(form, "siSoToiDa");
    }

    @Test
    void roomOverMaxLengthShouldFail() {
        LopHocPhanForm form = validForm();
        form.setPhongHoc("A".repeat(51));

        assertViolationOn(form, "phongHoc");
    }

    @Test
    void weekdayBelowTwoShouldFail() {
        LopHocPhanForm form = validForm();
        form.setThuHoc(1);

        assertViolationOn(form, "thuHoc");
    }

    @Test
    void weekdayAboveEightShouldFail() {
        LopHocPhanForm form = validForm();
        form.setThuHoc(9);

        assertViolationOn(form, "thuHoc");
    }

    @Test
    void startPeriodBelowOneShouldFail() {
        LopHocPhanForm form = validForm();
        form.setTietBatDau(0);

        assertViolationOn(form, "tietBatDau");
    }

    @Test
    void endPeriodBelowOneShouldFail() {
        LopHocPhanForm form = validForm();
        form.setTietKetThuc(0);

        assertViolationOn(form, "tietKetThuc");
    }
}
