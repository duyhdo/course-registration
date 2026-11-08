package com.ptit.courseregistration.service;

import com.ptit.courseregistration.dto.StudentForm;
import com.ptit.courseregistration.entity.Khoa;
import com.ptit.courseregistration.entity.SinhVien;
import com.ptit.courseregistration.entity.TaiKhoan;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.KhoaRepository;
import com.ptit.courseregistration.repository.SinhVienRepository;
import com.ptit.courseregistration.repository.TaiKhoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private static final String MA_SV = "B21DCCN001";

    @Mock
    private SinhVienRepository sinhVienRepository;
    @Mock
    private KhoaRepository khoaRepository;
    @Mock
    private TaiKhoanRepository taiKhoanRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    // ---------- fixtures ----------

    private Khoa khoa(String ma) {
        Khoa k = new Khoa();
        k.setMaKhoa(ma);
        k.setTenKhoa("Khoa " + ma);
        k.setTrangThai("ACTIVE");
        return k;
    }

    private TaiKhoan taiKhoan(String status) {
        TaiKhoan t = new TaiKhoan();
        t.setTaiKhoanId(1L);
        t.setTenDangNhap(MA_SV);
        t.setMatKhauHash("hash");
        t.setVaiTro("STUDENT");
        t.setTrangThai(status);
        t.setNgayTao(LocalDateTime.of(2024, 1, 1, 0, 0));
        return t;
    }

    private SinhVien student(String status, TaiKhoan account) {
        SinhVien sv = new SinhVien();
        sv.setMaSV(MA_SV);
        sv.setKhoa(khoa("CNTT"));
        sv.setHoTen("Old Name");
        sv.setEmail("old@ptit.edu.vn");
        sv.setTrangThai(status);
        sv.setTaiKhoan(account);
        return sv;
    }

    private StudentForm form() {
        StudentForm f = new StudentForm();
        f.setMaSV(MA_SV);
        f.setMaKhoa("ATTT");
        f.setHoTen("Nguyen Van A");
        f.setNgaySinh(LocalDate.of(2003, 5, 20));
        f.setGioiTinh("Nam");
        f.setEmail("a@ptit.edu.vn");
        f.setSoDienThoai("0901234567");
        f.setDiaChi("Ha Noi");
        f.setKhoaHoc("2021");
        return f;
    }

    // ---------- A. findAll / findById ----------

    @Test
    void shouldReturnAllStudents() {
        List<SinhVien> all = List.of(student("ACTIVE", null));
        when(sinhVienRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(all));

        assertEquals(all, studentService.findAll(PageRequest.of(0, 10)).getContent());
    }

    @Test
    void shouldReturnStudentWhenStudentExists() {
        SinhVien sv = student("ACTIVE", null);
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(sv));

        assertSame(sv, studentService.findById(MA_SV));
    }

    @Test
    void shouldThrowBusinessExceptionWhenStudentDoesNotExist() {
        when(sinhVienRepository.findById("X")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> studentService.findById("X"));
    }

    // ---------- B. search ----------

    @Test
    void shouldReturnAllStudentsWhenKeywordIsNull() {
        List<SinhVien> all = List.of(student("ACTIVE", null));
        when(sinhVienRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(all));

        assertEquals(all, studentService.search(null, PageRequest.of(0, 10)).getContent());
        verify(sinhVienRepository, never())
                .findByMaSVContainingIgnoreCaseOrHoTenContainingIgnoreCase(any(), any(), any());
    }

    @Test
    void shouldReturnAllStudentsWhenKeywordIsBlank() {
        List<SinhVien> all = List.of(student("ACTIVE", null));
        when(sinhVienRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(all));

        assertEquals(all, studentService.search("   ", PageRequest.of(0, 10)).getContent());
        verify(sinhVienRepository, never())
                .findByMaSVContainingIgnoreCaseOrHoTenContainingIgnoreCase(any(), any(), any());
    }

    @Test
    void shouldSearchByStudentCodeOrNameWhenKeywordProvided() {
        List<SinhVien> found = List.of(student("ACTIVE", null));
        when(sinhVienRepository.findByMaSVContainingIgnoreCaseOrHoTenContainingIgnoreCase(
                eq("B21"), eq("B21"), any())).thenReturn(new PageImpl<>(found));

        assertEquals(found, studentService.search(" B21 ", PageRequest.of(0, 10)).getContent());
        verify(sinhVienRepository, never()).findAll();
    }

    // ---------- C. create ----------

    @Test
    void shouldCreateStudentAndAccountSuccessfully() {
        Khoa attt = khoa("ATTT");
        when(sinhVienRepository.existsById(MA_SV)).thenReturn(false);
        when(sinhVienRepository.existsByEmail("a@ptit.edu.vn")).thenReturn(false);
        when(khoaRepository.findById("ATTT")).thenReturn(Optional.of(attt));
        when(taiKhoanRepository.existsByTenDangNhap(MA_SV)).thenReturn(false);
        when(passwordEncoder.encode("Student@123")).thenReturn("encoded-hash");
        when(taiKhoanRepository.save(any(TaiKhoan.class))).thenAnswer(i -> i.getArgument(0));
        when(sinhVienRepository.save(any(SinhVien.class))).thenAnswer(i -> i.getArgument(0));

        SinhVien result = studentService.create(form());

        ArgumentCaptor<TaiKhoan> accountCaptor = ArgumentCaptor.forClass(TaiKhoan.class);
        ArgumentCaptor<SinhVien> studentCaptor = ArgumentCaptor.forClass(SinhVien.class);
        InOrder order = inOrder(taiKhoanRepository, sinhVienRepository);
        order.verify(taiKhoanRepository).save(accountCaptor.capture());
        order.verify(sinhVienRepository).save(studentCaptor.capture());

        TaiKhoan account = accountCaptor.getValue();
        assertEquals(MA_SV, account.getTenDangNhap());
        assertEquals("STUDENT", account.getVaiTro());
        assertEquals("ACTIVE", account.getTrangThai());
        assertNotNull(account.getNgayTao());
        assertNull(account.getNgayCapNhat());
        assertEquals("encoded-hash", account.getMatKhauHash());
        verify(passwordEncoder).encode("Student@123");

        SinhVien saved = studentCaptor.getValue();
        assertSame(saved, result);
        assertEquals(MA_SV, saved.getMaSV());
        assertSame(attt, saved.getKhoa());
        assertEquals("ACTIVE", saved.getTrangThai());
        assertSame(account, saved.getTaiKhoan());
        assertEquals("Nguyen Van A", saved.getHoTen());
        assertEquals("a@ptit.edu.vn", saved.getEmail());
    }

    @Test
    void shouldRejectCreateWhenStudentIdAlreadyExists() {
        when(sinhVienRepository.existsById(MA_SV)).thenReturn(true);

        assertThrows(BusinessException.class, () -> studentService.create(form()));

        verifyNoInteractions(passwordEncoder);
        verify(taiKhoanRepository, never()).save(any());
        verify(sinhVienRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreateWhenEmailAlreadyExists() {
        when(sinhVienRepository.existsById(MA_SV)).thenReturn(false);
        when(sinhVienRepository.existsByEmail("a@ptit.edu.vn")).thenReturn(true);

        assertThrows(BusinessException.class, () -> studentService.create(form()));

        verifyNoInteractions(passwordEncoder);
        verify(taiKhoanRepository, never()).save(any());
        verify(sinhVienRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreateWhenDepartmentDoesNotExist() {
        when(sinhVienRepository.existsById(MA_SV)).thenReturn(false);
        when(sinhVienRepository.existsByEmail("a@ptit.edu.vn")).thenReturn(false);
        when(khoaRepository.findById("ATTT")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> studentService.create(form()));

        verifyNoInteractions(passwordEncoder);
        verify(taiKhoanRepository, never()).save(any());
        verify(sinhVienRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreateWhenUsernameAlreadyExists() {
        when(sinhVienRepository.existsById(MA_SV)).thenReturn(false);
        when(sinhVienRepository.existsByEmail("a@ptit.edu.vn")).thenReturn(false);
        when(khoaRepository.findById("ATTT")).thenReturn(Optional.of(khoa("ATTT")));
        when(taiKhoanRepository.existsByTenDangNhap(MA_SV)).thenReturn(true);

        assertThrows(BusinessException.class, () -> studentService.create(form()));

        verifyNoInteractions(passwordEncoder);
        verify(taiKhoanRepository, never()).save(any());
        verify(sinhVienRepository, never()).save(any());
    }

    // ---------- D. update ----------

    @Test
    void shouldUpdateStudentSuccessfully() {
        TaiKhoan account = taiKhoan("ACTIVE");
        SinhVien existing = student("ACTIVE", account);
        Khoa attt = khoa("ATTT");
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(existing));
        when(sinhVienRepository.existsByEmailAndMaSVNot("a@ptit.edu.vn", MA_SV)).thenReturn(false);
        when(khoaRepository.findById("ATTT")).thenReturn(Optional.of(attt));
        when(sinhVienRepository.save(any(SinhVien.class))).thenAnswer(i -> i.getArgument(0));

        SinhVien result = studentService.update(MA_SV, form());

        assertEquals("Nguyen Van A", result.getHoTen());
        assertSame(attt, result.getKhoa());
        assertEquals(LocalDate.of(2003, 5, 20), result.getNgaySinh());
        assertEquals("Nam", result.getGioiTinh());
        assertEquals("a@ptit.edu.vn", result.getEmail());
        assertEquals("0901234567", result.getSoDienThoai());
        assertEquals("Ha Noi", result.getDiaChi());
        assertEquals("2021", result.getKhoaHoc());

        assertEquals(MA_SV, result.getMaSV());
        assertEquals("ACTIVE", result.getTrangThai());
        assertSame(account, result.getTaiKhoan());
    }

    @Test
    void shouldRejectUpdateWhenStudentDoesNotExist() {
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> studentService.update(MA_SV, form()));

        verify(sinhVienRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenEmailBelongsToAnotherStudent() {
        SinhVien existing = student("ACTIVE", null);
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(existing));
        when(sinhVienRepository.existsByEmailAndMaSVNot("a@ptit.edu.vn", MA_SV)).thenReturn(true);

        assertThrows(BusinessException.class, () -> studentService.update(MA_SV, form()));

        assertEquals("Old Name", existing.getHoTen());
        verify(sinhVienRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenDepartmentDoesNotExist() {
        SinhVien existing = student("ACTIVE", null);
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(existing));
        when(sinhVienRepository.existsByEmailAndMaSVNot("a@ptit.edu.vn", MA_SV)).thenReturn(false);
        when(khoaRepository.findById("ATTT")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> studentService.update(MA_SV, form()));

        assertEquals("Old Name", existing.getHoTen());
        verify(sinhVienRepository, never()).save(any());
    }

    @Test
    void shouldNotChangeStudentIdStatusOrAccountDuringUpdate() {
        TaiKhoan account = taiKhoan("LOCKED");
        LocalDateTime accountCreated = account.getNgayTao();
        SinhVien existing = student("LOCKED", account);
        StudentForm f = form();
        f.setMaSV("DIFFERENT_ID");
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(existing));
        when(sinhVienRepository.existsByEmailAndMaSVNot("a@ptit.edu.vn", MA_SV)).thenReturn(false);
        when(khoaRepository.findById("ATTT")).thenReturn(Optional.of(khoa("ATTT")));
        when(sinhVienRepository.save(any(SinhVien.class))).thenAnswer(i -> i.getArgument(0));

        SinhVien result = studentService.update(MA_SV, f);

        assertEquals(MA_SV, result.getMaSV());
        assertEquals("LOCKED", result.getTrangThai());
        assertSame(account, result.getTaiKhoan());
        assertEquals("LOCKED", account.getTrangThai());
        assertEquals(MA_SV, account.getTenDangNhap());
        assertEquals("hash", account.getMatKhauHash());
        assertEquals(accountCreated, account.getNgayTao());
        assertNull(account.getNgayCapNhat());
        verify(taiKhoanRepository, never()).save(any());
    }

    // ---------- E. toggle status ----------

    @Test
    void shouldChangeActiveStudentAndAccountToLocked() {
        TaiKhoan account = taiKhoan("ACTIVE");
        SinhVien sv = student("ACTIVE", account);
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(sv));
        LocalDateTime before = LocalDateTime.now();

        studentService.toggleStatus(MA_SV);

        assertEquals("LOCKED", sv.getTrangThai());
        assertEquals("LOCKED", account.getTrangThai());
        assertNotNull(account.getNgayCapNhat());
        assertTrue(!account.getNgayCapNhat().isBefore(before));
    }

    @Test
    void shouldChangeLockedStudentAndAccountToActive() {
        TaiKhoan account = taiKhoan("LOCKED");
        SinhVien sv = student("LOCKED", account);
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(sv));

        studentService.toggleStatus(MA_SV);

        assertEquals("ACTIVE", sv.getTrangThai());
        assertEquals("ACTIVE", account.getTrangThai());
        assertNotNull(account.getNgayCapNhat());
    }

    @Test
    void shouldToggleStudentWithoutAccountSafely() {
        SinhVien sv = student("ACTIVE", null);
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(sv));

        studentService.toggleStatus(MA_SV);

        assertEquals("LOCKED", sv.getTrangThai());
        assertNull(sv.getTaiKhoan());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnsupportedStudentStatus() {
        TaiKhoan account = taiKhoan("ACTIVE");
        SinhVien sv = student("GRADUATED", account);
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(sv));

        assertThrows(BusinessException.class, () -> studentService.toggleStatus(MA_SV));

        assertEquals("GRADUATED", sv.getTrangThai());
        assertEquals("ACTIVE", account.getTrangThai());
        assertNull(account.getNgayCapNhat());
    }

    @Test
    void shouldRejectToggleWhenStudentDoesNotExist() {
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> studentService.toggleStatus(MA_SV));

        verify(taiKhoanRepository, never()).save(any());
    }

    // ---------- Step 11: student profile read ----------

    @Test
    void shouldReturnStudentProfileWhenStudentExists() {
        SinhVien sv = student("ACTIVE", taiKhoan("ACTIVE"));
        sv.setNgaySinh(LocalDate.of(2003, 5, 20));
        sv.setGioiTinh("Nam");
        sv.setSoDienThoai("0901234567");
        sv.setDiaChi("Ha Noi");
        sv.setKhoaHoc("2021");
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(sv));

        SinhVien profile = studentService.findById(MA_SV);

        assertSame(sv, profile);
        assertEquals(MA_SV, profile.getMaSV());
        assertEquals("Old Name", profile.getHoTen());
        assertEquals(LocalDate.of(2003, 5, 20), profile.getNgaySinh());
        assertEquals("Nam", profile.getGioiTinh());
        assertEquals("old@ptit.edu.vn", profile.getEmail());
        assertEquals("0901234567", profile.getSoDienThoai());
        assertEquals("Ha Noi", profile.getDiaChi());
        assertEquals("2021", profile.getKhoaHoc());
        assertEquals("ACTIVE", profile.getTrangThai());
    }

    @Test
    void shouldRejectProfileWhenStudentDoesNotExist() {
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> studentService.findById(MA_SV));
    }

    @Test
    void shouldLoadAssociatedKhoaRequiredByProfile() {
        SinhVien sv = student("ACTIVE", null);
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(sv));

        SinhVien profile = studentService.findById(MA_SV);

        assertNotNull(profile.getKhoa());
        assertEquals("Khoa CNTT", profile.getKhoa().getTenKhoa());
    }

    @Test
    void profileReadShouldNotModifyStudent() {
        SinhVien sv = student("ACTIVE", null);
        sv.setDiaChi("Ha Noi");
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(sv));

        SinhVien profile = studentService.findById(MA_SV);

        assertEquals("Old Name", profile.getHoTen());
        assertEquals("old@ptit.edu.vn", profile.getEmail());
        assertEquals("ACTIVE", profile.getTrangThai());
        assertEquals("Ha Noi", profile.getDiaChi());
        assertEquals("CNTT", profile.getKhoa().getMaKhoa());
    }

    @Test
    void profileReadShouldNotSaveStudent() {
        when(sinhVienRepository.findById(MA_SV)).thenReturn(Optional.of(student("ACTIVE", null)));

        studentService.findById(MA_SV);

        verify(sinhVienRepository, never()).save(any());
        verifyNoInteractions(taiKhoanRepository, khoaRepository, passwordEncoder);
    }
}