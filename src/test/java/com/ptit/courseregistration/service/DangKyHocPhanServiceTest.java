package com.ptit.courseregistration.service;

import com.ptit.courseregistration.entity.DangKyHocPhan;
import com.ptit.courseregistration.entity.GiangVien;
import com.ptit.courseregistration.entity.HocKy;
import com.ptit.courseregistration.entity.Khoa;
import com.ptit.courseregistration.entity.LopHocPhan;
import com.ptit.courseregistration.entity.MonHoc;
import com.ptit.courseregistration.entity.SinhVien;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.DangKyHocPhanRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.repository.SinhVienRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DangKyHocPhanServiceTest {

    private static final String MA_SV = "SV001";
    private static final String MA_LOP = "LHP001";
    private static final String MA_MON = "MH001";
    private static final String MA_HK = "HK2026";

    @Mock
    private DangKyHocPhanRepository dangKyHocPhanRepository;
    @Mock
    private SinhVienRepository sinhVienRepository;
    @Mock
    private LopHocPhanRepository lopHocPhanRepository;

    @InjectMocks
    private DangKyHocPhanService dangKyHocPhanService;

    private SinhVien student(String status) {
        SinhVien student = new SinhVien();
        student.setMaSV(MA_SV);
        student.setHoTen("Sinh vien");
        student.setTrangThai(status);
        return student;
    }

    private HocKy semester() {
        HocKy semester = new HocKy();
        semester.setMaHocKy(MA_HK);
        semester.setTenHocKy("Hoc ky 1");
        semester.setNamHoc("2026-2027");
        semester.setNgayMoDangKy(LocalDateTime.now().minusDays(1));
        semester.setNgayDongDangKy(LocalDateTime.now().plusDays(1));
        semester.setTinChiToiDa(18);
        return semester;
    }

    private MonHoc subject(Integer credits) {
        MonHoc subject = new MonHoc();
        subject.setMaMonHoc(MA_MON);
        subject.setTenMonHoc("Mon hoc");
        subject.setSoTinChi(credits);
        Khoa khoa = new Khoa();
        khoa.setMaKhoa("CNTT");
        khoa.setTenKhoa("Cong nghe thong tin");
        subject.setKhoa(khoa);
        subject.setTrangThai("ACTIVE");
        return subject;
    }

    private LopHocPhan section(String status) {
        return section(status, subject(3), semester(), 2, 40);
    }

    private LopHocPhan section(String status, MonHoc subject, HocKy semester,
                               Integer currentCapacity, Integer maxCapacity) {
        LopHocPhan section = new LopHocPhan();
        section.setMaLopHocPhan(MA_LOP);
        section.setMonHoc(subject);
        section.setHocKy(semester);
        section.setTrangThai(status);
        section.setSiSoHienTai(currentCapacity);
        section.setSiSoToiDa(maxCapacity);
        return section;
    }

    private DangKyHocPhan registration(String status) {
        DangKyHocPhan registration = new DangKyHocPhan();
        registration.setMaDangKy(10L);
        registration.setSinhVien(student("ACTIVE"));
        registration.setLopHocPhan(section("OPEN"));
        registration.setNgayDangKy(LocalDateTime.now().minusHours(1));
        registration.setTrangThai(status);
        if ("CANCELLED".equals(status)) {
            registration.setNgayHuy(LocalDateTime.now().minusMinutes(30));
        }
        return registration;
    }

    private void stubStudent(SinhVien student) {
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(student));
    }

    private void stubLockedStudent(SinhVien student) {
        when(sinhVienRepository.findByMaSVForUpdate(MA_SV)).thenReturn(Optional.of(student));
    }

    private void stubLockedSection(LopHocPhan section) {
        when(lopHocPhanRepository.findByIdForUpdate(MA_LOP)).thenReturn(Optional.of(section));
    }

    private void stubSuccessfulRegister(LopHocPhan section) {
        SinhVien student = student("ACTIVE");
        stubLockedStudent(student);
        stubLockedSection(section);
        lenient().when(dangKyHocPhanRepository
                .existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThai(MA_SV, MA_LOP, "REGISTERED"))
                .thenReturn(false);
        lenient().when(dangKyHocPhanRepository.countRegisteredForSubjectInSemester(MA_SV, MA_MON, MA_HK))
                .thenReturn(0L);
        lenient().when(dangKyHocPhanRepository.sumRegisteredCredits(MA_SV, MA_HK)).thenReturn(0L);
        lenient().when(dangKyHocPhanRepository.save(any(DangKyHocPhan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubSuccessfulCancel(LopHocPhan section, DangKyHocPhan registration) {
        stubStudent(student("ACTIVE"));
        stubLockedSection(section);
        when(dangKyHocPhanRepository
                .findFirstBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThaiOrderByNgayDangKyDesc(
                        MA_SV, MA_LOP, "REGISTERED"))
                .thenReturn(Optional.of(registration));
    }

    @Test
    void shouldRegisterSuccessfully() {
        SinhVien student = student("ACTIVE");
        LopHocPhan section = section("OPEN");
        stubLockedStudent(student);
        stubLockedSection(section);
        when(dangKyHocPhanRepository
                .existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThai(MA_SV, MA_LOP, "REGISTERED"))
                .thenReturn(false);
        when(dangKyHocPhanRepository.countRegisteredForSubjectInSemester(MA_SV, MA_MON, MA_HK))
                .thenReturn(0L);
        when(dangKyHocPhanRepository.sumRegisteredCredits(MA_SV, MA_HK)).thenReturn(6L);
        when(dangKyHocPhanRepository.save(any(DangKyHocPhan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DangKyHocPhan result = dangKyHocPhanService.register(MA_SV, MA_LOP);

        assertSame(student, result.getSinhVien());
        assertSame(section, result.getLopHocPhan());
        assertEquals("REGISTERED", result.getTrangThai());
        assertNotNull(result.getNgayDangKy());
        assertNull(result.getNgayHuy());
        assertEquals(3, section.getSiSoHienTai());
        verify(dangKyHocPhanRepository).save(result);
    }

    @Test
    void shouldUseLockedCourseSectionWhenRegistering() {
        stubSuccessfulRegister(section("OPEN"));

        dangKyHocPhanService.register(MA_SV, MA_LOP);

        verify(lopHocPhanRepository).findByIdForUpdate(MA_LOP);
        verify(lopHocPhanRepository, never()).findById(MA_LOP);
    }

    @Test
    void shouldAcquireStudentLockBeforeSectionLockDuringRegistration() {
        LopHocPhan section = section("OPEN");
        stubSuccessfulRegister(section);

        dangKyHocPhanService.register(MA_SV, MA_LOP);

        InOrder inOrder = inOrder(sinhVienRepository, lopHocPhanRepository);
        inOrder.verify(sinhVienRepository).findByMaSVForUpdate(MA_SV);
        inOrder.verify(lopHocPhanRepository).findByIdForUpdate(MA_LOP);
    }

    @Test
    void shouldAllowRegistrationAtExactlyMaximumCredits() {
        LopHocPhan section = section("OPEN");
        section.getMonHoc().setSoTinChi(3);
        section.getHocKy().setTinChiToiDa(6);
        stubSuccessfulRegister(section);
        when(dangKyHocPhanRepository.sumRegisteredCredits(MA_SV, MA_HK)).thenReturn(3L);

        assertNotNull(dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectRegisterWhenStudentDoesNotExist() {
        when(sinhVienRepository.findByMaSVForUpdate(MA_SV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
        verifyNoInteractions(lopHocPhanRepository);
    }

    @Test
    void shouldRejectRegisterWhenStudentInactive() {
        stubLockedStudent(student("INACTIVE"));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
        verifyNoInteractions(lopHocPhanRepository);
    }

    @Test
    void shouldRejectRegisterWhenSectionDoesNotExist() {
        stubLockedStudent(student("ACTIVE"));
        when(lopHocPhanRepository.findByIdForUpdate(MA_LOP)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectRegisterWhenSectionIsDraft() {
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section("DRAFT"));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectRegisterWhenSectionIsClosed() {
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section("CLOSED"));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectRegisterWhenSectionIsCancelled() {
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section("CANCELLED"));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectWhenSectionIsFull() {
        LopHocPhan section = section("OPEN", subject(3), semester(), 40, 40);
        stubSuccessfulRegister(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        assertEquals(40, section.getSiSoHienTai());
        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldRejectInvalidNegativeCurrentCapacity() {
        LopHocPhan section = section("OPEN", subject(3), semester(), -1, 40);
        stubSuccessfulRegister(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        assertEquals(-1, section.getSiSoHienTai());
        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldRejectCurrentCapacityGreaterThanMaximum() {
        LopHocPhan section = section("OPEN", subject(3), semester(), 41, 40);
        stubSuccessfulRegister(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        assertEquals(41, section.getSiSoHienTai());
        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldRejectWhenRegistrationOpenDateIsNull() {
        LopHocPhan section = section("OPEN");
        section.getHocKy().setNgayMoDangKy(null);
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectWhenRegistrationCloseDateIsNull() {
        LopHocPhan section = section("OPEN");
        section.getHocKy().setNgayDongDangKy(null);
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectBeforeRegistrationWindow() {
        LopHocPhan section = section("OPEN");
        section.getHocKy().setNgayMoDangKy(LocalDateTime.now().plusDays(1));
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectAfterRegistrationWindow() {
        LopHocPhan section = section("OPEN");
        section.getHocKy().setNgayDongDangKy(LocalDateTime.now().minusDays(1));
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectExistingActiveRegistrationForSameSection() {
        LopHocPhan section = section("OPEN");
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);
        when(dangKyHocPhanRepository
                .existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThai(MA_SV, MA_LOP, "REGISTERED"))
                .thenReturn(true);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldAllowReEnrollmentAfterCancelledHistory() {
        DangKyHocPhan historical = registration("CANCELLED");
        LocalDateTime cancelledAt = historical.getNgayHuy();
        LopHocPhan section = section("OPEN");
        stubSuccessfulRegister(section);
        when(dangKyHocPhanRepository
                .existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThai(MA_SV, MA_LOP, "REGISTERED"))
                .thenReturn(false);

        DangKyHocPhan result = dangKyHocPhanService.register(MA_SV, MA_LOP);

        assertNotNull(result);
        assertNotSame(historical, result);
        assertEquals("CANCELLED", historical.getTrangThai());
        assertEquals(cancelledAt, historical.getNgayHuy());
        verify(dangKyHocPhanRepository).save(result);
    }

    @Test
    void shouldRejectAnotherRegisteredSectionOfSameSubjectAndSemester() {
        LopHocPhan section = section("OPEN");
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);
        when(dangKyHocPhanRepository
                .existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThai(MA_SV, MA_LOP, "REGISTERED"))
                .thenReturn(false);
        when(dangKyHocPhanRepository.countRegisteredForSubjectInSemester(MA_SV, MA_MON, MA_HK))
                .thenReturn(1L);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldIgnoreCancelledSameSubjectRegistration() {
        DangKyHocPhan historical = registration("CANCELLED");
        LopHocPhan section = section("OPEN");
        stubSuccessfulRegister(section);
        when(dangKyHocPhanRepository.countRegisteredForSubjectInSemester(MA_SV, MA_MON, MA_HK))
                .thenReturn(0L);

        DangKyHocPhan result = dangKyHocPhanService.register(MA_SV, MA_LOP);

        assertEquals("CANCELLED", historical.getTrangThai());
        assertNotNull(result);
        verify(dangKyHocPhanRepository).countRegisteredForSubjectInSemester(MA_SV, MA_MON, MA_HK);
    }

    @Test
    void shouldRejectWhenSemesterMaximumCreditsNotConfigured() {
        LopHocPhan section = section("OPEN");
        section.getHocKy().setTinChiToiDa(null);
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectWhenRegistrationExceedsMaximumCredits() {
        LopHocPhan section = section("OPEN");
        section.getHocKy().setTinChiToiDa(5);
        stubSuccessfulRegister(section);
        when(dangKyHocPhanRepository.sumRegisteredCredits(MA_SV, MA_HK)).thenReturn(3L);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldAllowRegistrationBelowMaximumCredits() {
        LopHocPhan section = section("OPEN");
        section.getHocKy().setTinChiToiDa(9);
        stubSuccessfulRegister(section);
        when(dangKyHocPhanRepository.sumRegisteredCredits(MA_SV, MA_HK)).thenReturn(3L);

        assertNotNull(dangKyHocPhanService.register(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectWhenSubjectCreditsAreNull() {
        LopHocPhan section = section("OPEN", subject(null), semester(), 2, 40);
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        assertEquals(2, section.getSiSoHienTai());
        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldRejectWhenSubjectCreditsAreZero() {
        LopHocPhan section = section("OPEN", subject(0), semester(), 2, 40);
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        assertEquals(2, section.getSiSoHienTai());
        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldRejectWhenSubjectCreditsAreNegative() {
        LopHocPhan section = section("OPEN", subject(-2), semester(), 2, 40);
        stubLockedStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        assertEquals(2, section.getSiSoHienTai());
        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldCancelActiveRegistrationSuccessfully() {
        LopHocPhan section = section("OPEN");
        DangKyHocPhan registration = registration("REGISTERED");
        stubSuccessfulCancel(section, registration);

        DangKyHocPhan result = dangKyHocPhanService.cancel(MA_SV, MA_LOP);

        assertSame(registration, result);
        assertEquals("CANCELLED", result.getTrangThai());
        assertNotNull(result.getNgayHuy());
        assertEquals(1, section.getSiSoHienTai());
        verify(dangKyHocPhanRepository, never()).delete(any(DangKyHocPhan.class));
    }

    @Test
    void shouldUseLockedCourseSectionWhenCancelling() {
        LopHocPhan section = section("OPEN");
        stubSuccessfulCancel(section, registration("REGISTERED"));

        dangKyHocPhanService.cancel(MA_SV, MA_LOP);

        verify(lopHocPhanRepository).findByIdForUpdate(MA_LOP);
        verify(lopHocPhanRepository, never()).findById(MA_LOP);
    }

    @Test
    void shouldCancelFromCapacityOneToZero() {
        LopHocPhan section = section("OPEN", subject(3), semester(), 1, 40);
        stubSuccessfulCancel(section, registration("REGISTERED"));

        dangKyHocPhanService.cancel(MA_SV, MA_LOP);

        assertEquals(0, section.getSiSoHienTai());
    }

    @Test
    void shouldRejectCancelWhenStudentDoesNotExist() {
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));

        verifyNoInteractions(lopHocPhanRepository);
    }

    @Test
    void shouldRejectCancelWhenSectionDoesNotExist() {
        stubStudent(student("ACTIVE"));
        when(lopHocPhanRepository.findByIdForUpdate(MA_LOP)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectCancelWhenSectionIsDraft() {
        stubStudent(student("ACTIVE"));
        stubLockedSection(section("DRAFT"));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectCancelWhenSectionIsClosed() {
        stubStudent(student("ACTIVE"));
        stubLockedSection(section("CLOSED"));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectCancelWhenSectionIsCancelled() {
        stubStudent(student("ACTIVE"));
        stubLockedSection(section("CANCELLED"));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectCancelOutsideRegistrationWindow() {
        LopHocPhan section = section("OPEN");
        section.getHocKy().setNgayDongDangKy(LocalDateTime.now().minusDays(1));
        stubStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));
        verify(dangKyHocPhanRepository, never())
                .findFirstBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThaiOrderByNgayDangKyDesc(
                        MA_SV, MA_LOP, "REGISTERED");
    }

    @Test
    void shouldRejectCancelWhenRegistrationWindowNotConfigured() {
        LopHocPhan section = section("OPEN");
        section.getHocKy().setNgayMoDangKy(null);
        stubStudent(student("ACTIVE"));
        stubLockedSection(section);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectCancelWhenNoActiveRegistrationExists() {
        LopHocPhan section = section("OPEN");
        stubStudent(student("ACTIVE"));
        stubLockedSection(section);
        when(dangKyHocPhanRepository
                .findFirstBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThaiOrderByNgayDangKyDesc(
                        MA_SV, MA_LOP, "REGISTERED"))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));
    }

    @Test
    void shouldRejectCancelWhenCurrentCapacityIsZero() {
        LopHocPhan section = section("OPEN", subject(3), semester(), 0, 40);
        stubSuccessfulCancel(section, registration("REGISTERED"));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));

        assertEquals(0, section.getSiSoHienTai());
    }

    @Test
    void shouldRejectCancelWhenCurrentCapacityIsNegative() {
        LopHocPhan section = section("OPEN", subject(3), semester(), -1, 40);
        stubSuccessfulCancel(section, registration("REGISTERED"));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));

        assertEquals(-1, section.getSiSoHienTai());
    }

    @Test
    void shouldReturnStudentEnrollmentHistory() {
        SinhVien student = student("ACTIVE");
        DangKyHocPhan historical = registration("CANCELLED");
        List<DangKyHocPhan> history = List.of(historical);
        stubStudent(student);
        when(dangKyHocPhanRepository.findBySinhVien_MaSVOrderByNgayDangKyDesc(MA_SV))
                .thenReturn(history);

        assertSame(history, dangKyHocPhanService.findStudentHistory(MA_SV));
        verify(dangKyHocPhanRepository).findBySinhVien_MaSVOrderByNgayDangKyDesc(MA_SV);
    }

    @Test
    void shouldReturnActiveRegistrations() {
        List<DangKyHocPhan> active = List.of(registration("REGISTERED"));
        stubStudent(student("ACTIVE"));
        when(dangKyHocPhanRepository.findBySinhVien_MaSVAndTrangThaiIn(MA_SV, List.of("REGISTERED")))
                .thenReturn(active);

        assertSame(active, dangKyHocPhanService.findActiveRegistrations(MA_SV));
        verify(dangKyHocPhanRepository)
                .findBySinhVien_MaSVAndTrangThaiIn(MA_SV, List.of("REGISTERED"));
    }

    @Test
    void shouldRejectHistoryLookupWhenStudentDoesNotExist() {
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.findStudentHistory(MA_SV));

        verifyNoInteractions(dangKyHocPhanRepository);
    }

    @Test
    void shouldRejectActiveRegistrationLookupWhenStudentDoesNotExist() {
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.findActiveRegistrations(MA_SV));

        verifyNoInteractions(dangKyHocPhanRepository);
    }

    @Test
    void shouldNotDeleteEnrollmentWhenCancelling() {
        LopHocPhan section = section("OPEN");
        DangKyHocPhan registration = registration("REGISTERED");
        stubSuccessfulCancel(section, registration);

        dangKyHocPhanService.cancel(MA_SV, MA_LOP);

        verify(dangKyHocPhanRepository, never()).delete(any(DangKyHocPhan.class));
        assertEquals("CANCELLED", registration.getTrangThai());
    }

    @Test
    void shouldNotReactivateHistoricalCancelledEnrollmentWhenRegisteringAgain() {
        DangKyHocPhan cancelled = registration("CANCELLED");
        LocalDateTime originalCancelledAt = cancelled.getNgayHuy();
        LopHocPhan section = section("OPEN");
        stubSuccessfulRegister(section);

        ArgumentCaptor<DangKyHocPhan> captor = ArgumentCaptor.forClass(DangKyHocPhan.class);
        DangKyHocPhan newRegistration = dangKyHocPhanService.register(MA_SV, MA_LOP);
        verify(dangKyHocPhanRepository).save(captor.capture());

        assertSame(newRegistration, captor.getValue());
        assertNotSame(cancelled, newRegistration);
        assertEquals("CANCELLED", cancelled.getTrangThai());
        assertEquals(originalCancelledAt, cancelled.getNgayHuy());
    }

    @Test
    void shouldIncrementCapacityOnlyAfterAllRegistrationValidationPasses() {
        LopHocPhan section = section("OPEN");
        stubSuccessfulRegister(section);
        when(dangKyHocPhanRepository.sumRegisteredCredits(MA_SV, MA_HK)).thenReturn(16L);
        section.getHocKy().setTinChiToiDa(18);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        assertEquals(2, section.getSiSoHienTai());
        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
    }

    @Test
    void shouldNotIncrementCapacityWhenRegistrationFails() {
        LopHocPhan section = section("OPEN");
        stubSuccessfulRegister(section);
        when(dangKyHocPhanRepository
                .existsBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThai(MA_SV, MA_LOP, "REGISTERED"))
                .thenReturn(true);

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.register(MA_SV, MA_LOP));

        assertEquals(2, section.getSiSoHienTai());
    }

    @Test
    void shouldNotDecrementCapacityWhenCancellationFails() {
        LopHocPhan section = section("OPEN");
        stubStudent(student("ACTIVE"));
        stubLockedSection(section);
        when(dangKyHocPhanRepository
                .findFirstBySinhVien_MaSVAndLopHocPhan_MaLopHocPhanAndTrangThaiOrderByNgayDangKyDesc(
                        MA_SV, MA_LOP, "REGISTERED"))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.cancel(MA_SV, MA_LOP));

        assertEquals(2, section.getSiSoHienTai());
    }

    // ---------- Step 11: course-section detail and credit summary ----------

    private DangKyHocPhan registeredWithCredits(Integer credits) {
        DangKyHocPhan registration = registration("REGISTERED");
        registration.setLopHocPhan(section("OPEN", subject(credits), semester(), 2, 40));
        return registration;
    }

    private void stubRegistered(List<DangKyHocPhan> registrations) {
        stubStudent(student("ACTIVE"));
        when(dangKyHocPhanRepository.findBySinhVien_MaSVAndTrangThaiIn(MA_SV, List.of("REGISTERED")))
                .thenReturn(registrations);
    }

    @Test
    void shouldReturnOpenCourseSectionDetail() {
        LopHocPhan section = section("OPEN");
        when(lopHocPhanRepository.findWithDetailsById(MA_LOP)).thenReturn(Optional.of(section));

        assertSame(section, dangKyHocPhanService.findOpenCourseSectionDetail(MA_LOP));
    }

    @Test
    void shouldRejectMissingCourseSection() {
        when(lopHocPhanRepository.findWithDetailsById(MA_LOP)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> dangKyHocPhanService.findOpenCourseSectionDetail(MA_LOP));
        assertEquals("Lớp học phần không tồn tại.", ex.getMessage());
    }

    @Test
    void shouldRejectDraftCourseSection() {
        when(lopHocPhanRepository.findWithDetailsById(MA_LOP)).thenReturn(Optional.of(section("DRAFT")));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.findOpenCourseSectionDetail(MA_LOP));
    }

    @Test
    void shouldRejectClosedCourseSection() {
        when(lopHocPhanRepository.findWithDetailsById(MA_LOP)).thenReturn(Optional.of(section("CLOSED")));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.findOpenCourseSectionDetail(MA_LOP));
    }

    @Test
    void shouldRejectCancelledCourseSection() {
        when(lopHocPhanRepository.findWithDetailsById(MA_LOP)).thenReturn(Optional.of(section("CANCELLED")));

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.findOpenCourseSectionDetail(MA_LOP));
    }

    @Test
    void shouldAllowNullLecturer() {
        LopHocPhan section = section("OPEN");
        section.setGiangVien(null);
        when(lopHocPhanRepository.findWithDetailsById(MA_LOP)).thenReturn(Optional.of(section));

        assertNull(dangKyHocPhanService.findOpenCourseSectionDetail(MA_LOP).getGiangVien());
    }

    @Test
    void shouldReturnSubjectSemesterAndLecturerAssociations() {
        LopHocPhan section = section("OPEN");
        GiangVien lecturer = new GiangVien();
        lecturer.setMaGV("GV001");
        lecturer.setHoTen("Giang vien");
        section.setGiangVien(lecturer);
        when(lopHocPhanRepository.findWithDetailsById(MA_LOP)).thenReturn(Optional.of(section));

        LopHocPhan result = dangKyHocPhanService.findOpenCourseSectionDetail(MA_LOP);

        assertEquals(MA_MON, result.getMonHoc().getMaMonHoc());
        assertEquals(3, result.getMonHoc().getSoTinChi());
        assertEquals(MA_HK, result.getHocKy().getMaHocKy());
        assertEquals("GV001", result.getGiangVien().getMaGV());
        verify(lopHocPhanRepository).findWithDetailsById(MA_LOP);
    }

    @Test
    void courseSectionDetailShouldNotModifySection() {
        LopHocPhan section = section("OPEN");
        when(lopHocPhanRepository.findWithDetailsById(MA_LOP)).thenReturn(Optional.of(section));

        dangKyHocPhanService.findOpenCourseSectionDetail(MA_LOP);

        assertEquals("OPEN", section.getTrangThai());
        assertEquals(2, section.getSiSoHienTai());
        assertEquals(40, section.getSiSoToiDa());
        assertEquals(3, section.getMonHoc().getSoTinChi());
    }

    @Test
    void courseSectionDetailShouldNotSaveSection() {
        when(lopHocPhanRepository.findWithDetailsById(MA_LOP)).thenReturn(Optional.of(section("OPEN")));

        dangKyHocPhanService.findOpenCourseSectionDetail(MA_LOP);

        verify(lopHocPhanRepository, never()).save(any());
        verify(lopHocPhanRepository, never()).findByIdForUpdate(any());
        verifyNoInteractions(dangKyHocPhanRepository);
    }

    @Test
    void shouldCalculateTotalRegisteredCredits() {
        stubRegistered(List.of(registeredWithCredits(3), registeredWithCredits(3), registeredWithCredits(4)));

        assertEquals(10, dangKyHocPhanService.calculateTotalRegisteredCredits(MA_SV));
    }

    @Test
    void shouldReturnZeroWhenNoRegisteredEnrollments() {
        stubRegistered(List.of());

        assertEquals(0, dangKyHocPhanService.calculateTotalRegisteredCredits(MA_SV));
    }

    @Test
    void shouldSumAcrossReturnedRegisteredEnrollments() {
        DangKyHocPhan otherSemester = registeredWithCredits(2);
        HocKy another = semester();
        another.setMaHocKy("HK2027");
        otherSemester.setLopHocPhan(section("OPEN", subject(2), another, 2, 40));
        stubRegistered(List.of(registeredWithCredits(3), otherSemester));

        assertEquals(5, dangKyHocPhanService.calculateTotalRegisteredCredits(MA_SV));
        verify(dangKyHocPhanRepository, never()).sumRegisteredCredits(any(), any());
    }

    @Test
    void shouldIgnoreCancelledEnrollment() {
        stubRegistered(List.of(registeredWithCredits(3)));

        assertEquals(3, dangKyHocPhanService.calculateTotalRegisteredCredits(MA_SV));

        verify(dangKyHocPhanRepository).findBySinhVien_MaSVAndTrangThaiIn(MA_SV, List.of("REGISTERED"));
        verify(dangKyHocPhanRepository, never()).findBySinhVien_MaSVOrderByNgayDangKyDesc(any());
    }

    @Test
    void shouldHandleSingleRegisteredEnrollment() {
        stubRegistered(List.of(registeredWithCredits(4)));

        assertEquals(4, dangKyHocPhanService.calculateTotalRegisteredCredits(MA_SV));
    }

    @Test
    void shouldNotApplyMaximumCreditValidationDuringRead() {
        stubRegistered(List.of(registeredWithCredits(10), registeredWithCredits(10), registeredWithCredits(5)));

        assertEquals(25, dangKyHocPhanService.calculateTotalRegisteredCredits(MA_SV));
    }

    @Test
    void shouldNotModifyEnrollmentData() {
        DangKyHocPhan registration = registeredWithCredits(3);
        stubRegistered(List.of(registration));

        dangKyHocPhanService.calculateTotalRegisteredCredits(MA_SV);

        assertEquals("REGISTERED", registration.getTrangThai());
        assertNull(registration.getNgayHuy());
        assertEquals(2, registration.getLopHocPhan().getSiSoHienTai());
    }

    @Test
    void shouldNotSaveEnrollment() {
        stubRegistered(List.of(registeredWithCredits(3)));

        dangKyHocPhanService.calculateTotalRegisteredCredits(MA_SV);

        verify(dangKyHocPhanRepository, never()).save(any());
        verify(lopHocPhanRepository, never()).save(any());
        verifyNoInteractions(lopHocPhanRepository);
    }

    @Test
    void shouldRejectCreditSummaryWhenStudentDoesNotExist() {
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> dangKyHocPhanService.calculateTotalRegisteredCredits(MA_SV));
        verifyNoInteractions(dangKyHocPhanRepository);
    }
}