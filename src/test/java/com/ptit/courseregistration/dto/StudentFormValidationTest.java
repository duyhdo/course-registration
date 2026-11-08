package com.ptit.courseregistration.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentFormValidationTest {

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

    private StudentForm validForm() {
        StudentForm f = new StudentForm();
        f.setMaSV("B21DCCN001");
        f.setMaKhoa("CNTT");
        f.setHoTen("Nguyen Van A");
        f.setNgaySinh(LocalDate.of(2003, 5, 20));
        f.setGioiTinh("Nam");
        f.setEmail("a@ptit.edu.vn");
        f.setSoDienThoai("0901234567");
        f.setDiaChi("Ha Noi");
        f.setKhoaHoc("2021");
        return f;
    }

    private void assertViolationsOnlyOn(StudentForm form, String property) {
        Set<ConstraintViolation<StudentForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(property)),
                "Expected violation on " + property + " but got " + violations);
        assertTrue(violations.stream().allMatch(v -> v.getPropertyPath().toString().equals(property)),
                "Unexpected violations: " + violations);
    }

    @Test
    void validStudentFormShouldHaveNoViolations() {
        assertEquals(0, validator.validate(validForm()).size());
    }

    @Test
    void blankStudentIdShouldFail() {
        StudentForm f = validForm();
        f.setMaSV("  ");
        assertViolationsOnlyOn(f, "maSV");
    }

    @Test
    void blankStudentNameShouldFail() {
        StudentForm f = validForm();
        f.setHoTen("");
        assertViolationsOnlyOn(f, "hoTen");
    }

    @Test
    void blankDepartmentShouldFail() {
        StudentForm f = validForm();
        f.setMaKhoa(null);
        assertViolationsOnlyOn(f, "maKhoa");
    }

    @Test
    void blankEmailShouldFail() {
        StudentForm f = validForm();
        f.setEmail(" ");
        assertViolationsOnlyOn(f, "email");
    }

    @Test
    void invalidEmailShouldFail() {
        StudentForm f = validForm();
        f.setEmail("not-an-email");
        assertViolationsOnlyOn(f, "email");
    }

    @Test
    void studentIdOverMaxLengthShouldFail() {
        StudentForm f = validForm();
        f.setMaSV("A".repeat(21));
        assertViolationsOnlyOn(f, "maSV");
    }

    @Test
    void studentNameOverMaxLengthShouldFail() {
        StudentForm f = validForm();
        f.setHoTen("A".repeat(101));
        assertViolationsOnlyOn(f, "hoTen");
    }

    @Test
    void emailOverMaxLengthShouldFail() {
        StudentForm f = validForm();
        f.setEmail("a".repeat(93) + "@ptit.vn");
        assertViolationsOnlyOn(f, "email");
    }

    @Test
    void phoneOverMaxLengthShouldFail() {
        StudentForm f = validForm();
        f.setSoDienThoai("1".repeat(21));
        assertViolationsOnlyOn(f, "soDienThoai");
    }

    @Test
    void addressOverMaxLengthShouldFail() {
        StudentForm f = validForm();
        f.setDiaChi("A".repeat(256));
        assertViolationsOnlyOn(f, "diaChi");
    }

    @Test
    void courseYearOverMaxLengthShouldFail() {
        StudentForm f = validForm();
        f.setKhoaHoc("2".repeat(21));
        assertViolationsOnlyOn(f, "khoaHoc");
    }

    @Test
    void genderOverMaxLengthShouldFail() {
        StudentForm f = validForm();
        f.setGioiTinh("G".repeat(11));
        assertViolationsOnlyOn(f, "gioiTinh");
    }
}
