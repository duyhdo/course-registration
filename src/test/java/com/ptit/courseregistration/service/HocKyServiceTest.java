package com.ptit.courseregistration.service;

import com.ptit.courseregistration.dto.HocKyConfigForm;
import com.ptit.courseregistration.entity.HocKy;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.HocKyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HocKyServiceTest {

    private static final String MA = "HK1_2026";
    private static final LocalDateTime OPEN = LocalDateTime.of(2026, 8, 1, 8, 0);
    private static final LocalDateTime CLOSE = LocalDateTime.of(2026, 8, 15, 17, 0);

    @Mock
    private HocKyRepository hocKyRepository;

    @InjectMocks
    private HocKyService hocKyService;

    private HocKy hocKy;

    @BeforeEach
    void setUp() {
        hocKy = new HocKy();
        hocKy.setMaHocKy(MA);
        hocKy.setTenHocKy("Hoc ky 1");
        hocKy.setNamHoc("2026-2027");
        hocKy.setNgayBatDau(LocalDate.of(2026, 9, 5));
        hocKy.setNgayKetThuc(LocalDate.of(2027, 1, 15));
        hocKy.setNgayMoDangKy(LocalDateTime.of(2026, 1, 1, 0, 0));
        hocKy.setNgayDongDangKy(LocalDateTime.of(2026, 1, 2, 0, 0));
        hocKy.setTinChiToiThieu(5);
        hocKy.setTinChiToiDa(10);
    }

    private HocKyConfigForm form(LocalDateTime open, LocalDateTime close, Integer min, Integer max) {
        return new HocKyConfigForm(open, close, min, max);
    }

    private void stubFound() {
        when(hocKyRepository.findById(MA)).thenReturn(Optional.of(hocKy));
        when(hocKyRepository.save(any(HocKy.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void assertRejected(HocKyConfigForm form) {
        when(hocKyRepository.findById(MA)).thenReturn(Optional.of(hocKy));
        assertThrows(BusinessException.class, () -> hocKyService.updateEnrollmentConfig(MA, form));
        verify(hocKyRepository, never()).save(any());
        assertEquals(5, hocKy.getTinChiToiThieu());
        assertEquals(10, hocKy.getTinChiToiDa());
    }

    @Test
    void shouldReturnAllSemesters() {
        List<HocKy> list = List.of(hocKy);
        when(hocKyRepository.findAllByOrderByNgayBatDauDesc()).thenReturn(list);

        assertSame(list, hocKyService.findAll());
    }

    @Test
    void shouldReturnSemesterWhenExists() {
        when(hocKyRepository.findById(MA)).thenReturn(Optional.of(hocKy));

        assertSame(hocKy, hocKyService.findById(MA));
    }

    @Test
    void shouldThrowWhenSemesterDoesNotExist() {
        when(hocKyRepository.findById("X")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> hocKyService.findById("X"));
        assertEquals("Học kỳ không tồn tại.", ex.getMessage());
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentSemester() {
        when(hocKyRepository.findById("X")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> hocKyService.updateEnrollmentConfig("X", form(OPEN, CLOSE, 1, 2)));
        verify(hocKyRepository, never()).save(any());
    }

    @Test
    void shouldUpdateEnrollmentConfigSuccessfully() {
        stubFound();

        HocKy result = hocKyService.updateEnrollmentConfig(MA, form(OPEN, CLOSE, 12, 24));

        assertEquals(OPEN, result.getNgayMoDangKy());
        assertEquals(CLOSE, result.getNgayDongDangKy());
        assertEquals(12, result.getTinChiToiThieu());
        assertEquals(24, result.getTinChiToiDa());
    }

    @Test
    void shouldAllowMinimumCreditsEqualMaximumCredits() {
        stubFound();

        HocKy result = hocKyService.updateEnrollmentConfig(MA, form(OPEN, CLOSE, 15, 15));

        assertEquals(15, result.getTinChiToiThieu());
        assertEquals(15, result.getTinChiToiDa());
    }

    @Test
    void shouldRejectMinimumCreditsGreaterThanMaximumCredits() {
        when(hocKyRepository.findById(MA)).thenReturn(Optional.of(hocKy));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> hocKyService.updateEnrollmentConfig(MA, form(OPEN, CLOSE, 25, 24)));
        assertEquals("Tín chỉ tối thiểu không được lớn hơn tín chỉ tối đa.", ex.getMessage());
        verify(hocKyRepository, never()).save(any());
    }

    @Test
    void shouldAllowNullMinimumCreditsIfCurrentSchemaAndServicePermitIt() {
        stubFound();

        HocKy result = hocKyService.updateEnrollmentConfig(MA, form(OPEN, CLOSE, null, 24));

        assertNull(result.getTinChiToiThieu());
        assertEquals(24, result.getTinChiToiDa());
    }

    @Test
    void shouldRejectMaximumCreditsNull() {
        assertRejected(form(OPEN, CLOSE, 5, null));
    }

    @Test
    void shouldRejectMaximumCreditsZero() {
        assertRejected(form(OPEN, CLOSE, null, 0));
    }

    @Test
    void shouldRejectMaximumCreditsNegative() {
        assertRejected(form(OPEN, CLOSE, null, -3));
    }

    @Test
    void shouldRejectMinimumCreditsNegative() {
        assertRejected(form(OPEN, CLOSE, -1, 24));
    }

    @Test
    void shouldRejectRegistrationOpenDateNull() {
        assertRejected(form(null, CLOSE, 5, 24));
    }

    @Test
    void shouldRejectRegistrationCloseDateNull() {
        assertRejected(form(OPEN, null, 5, 24));
    }

    @Test
    void shouldRejectOpenDateAfterCloseDate() {
        when(hocKyRepository.findById(MA)).thenReturn(Optional.of(hocKy));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> hocKyService.updateEnrollmentConfig(MA, form(CLOSE, OPEN, 5, 24)));
        assertEquals("Thời gian mở đăng ký không được sau thời gian đóng đăng ký.", ex.getMessage());
        verify(hocKyRepository, never()).save(any());
    }

    @Test
    void shouldAllowOpenDateEqualCloseDate() {
        stubFound();

        HocKy result = hocKyService.updateEnrollmentConfig(MA, form(OPEN, OPEN, 5, 24));

        assertEquals(OPEN, result.getNgayMoDangKy());
        assertEquals(OPEN, result.getNgayDongDangKy());
    }

    @Test
    void shouldAllowRegistrationWindowBeforeSemesterStart() {
        stubFound();
        LocalDateTime open = hocKy.getNgayBatDau().minusMonths(2).atStartOfDay();
        LocalDateTime close = hocKy.getNgayBatDau().minusMonths(1).atStartOfDay();

        HocKy result = hocKyService.updateEnrollmentConfig(MA, form(open, close, 5, 24));

        assertEquals(open, result.getNgayMoDangKy());
        assertEquals(close, result.getNgayDongDangKy());
    }

    @Test
    void shouldNotModifySemesterIdentityFields() {
        stubFound();

        HocKy result = hocKyService.updateEnrollmentConfig(MA, form(OPEN, CLOSE, 12, 24));

        assertEquals(MA, result.getMaHocKy());
        assertEquals("Hoc ky 1", result.getTenHocKy());
        assertEquals("2026-2027", result.getNamHoc());
        assertEquals(LocalDate.of(2026, 9, 5), result.getNgayBatDau());
        assertEquals(LocalDate.of(2027, 1, 15), result.getNgayKetThuc());
    }

    @Test
    void shouldSaveUpdatedSemester() {
        stubFound();

        HocKy result = hocKyService.updateEnrollmentConfig(MA, form(OPEN, CLOSE, 12, 24));

        verify(hocKyRepository).save(hocKy);
        assertSame(hocKy, result);
    }

    @Test
    void shouldNotInteractWithEnrollmentData() {
        stubFound();

        hocKyService.updateEnrollmentConfig(MA, form(OPEN, CLOSE, 12, 24));

        verify(hocKyRepository).findById(MA);
        verify(hocKyRepository).save(hocKy);
        verifyNoMoreInteractions(hocKyRepository);
        for (Field field : HocKyService.class.getDeclaredFields()) {
            assertEquals(HocKyRepository.class, field.getType());
        }
    }
}
