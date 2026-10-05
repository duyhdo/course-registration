package com.ptit.courseregistration.service;

import com.ptit.courseregistration.dto.MonHocForm;
import com.ptit.courseregistration.entity.Khoa;
import com.ptit.courseregistration.entity.LopHocPhan;
import com.ptit.courseregistration.entity.MonHoc;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.KhoaRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.repository.MonHocRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonHocServiceTest {

    private static final String MA_MON_HOC = "MH001";
    private static final String MA_KHOA = "CNTT";

    @Mock
    private MonHocRepository monHocRepository;
    @Mock
    private KhoaRepository khoaRepository;
    @Mock
    private LopHocPhanRepository lopHocPhanRepository;

    @InjectMocks
    private MonHocService monHocService;

    private Khoa khoa(String maKhoa, String trangThai) {
        Khoa khoa = new Khoa();
        khoa.setMaKhoa(maKhoa);
        khoa.setTenKhoa("Khoa " + maKhoa);
        khoa.setTrangThai(trangThai);
        return khoa;
    }

    private MonHoc monHoc(String trangThai, Khoa khoa) {
        MonHoc monHoc = new MonHoc();
        monHoc.setMaMonHoc(MA_MON_HOC);
        monHoc.setKhoa(khoa);
        monHoc.setTenMonHoc("Ten cu");
        monHoc.setSoTinChi(3);
        monHoc.setSoTietLyThuyet(30);
        monHoc.setSoTietThucHanh(15);
        monHoc.setMoTa("Mo ta cu");
        monHoc.setTrangThai(trangThai);
        return monHoc;
    }

    private MonHocForm form() {
        MonHocForm form = new MonHocForm();
        form.setMaMonHoc(MA_MON_HOC);
        form.setMaKhoa(MA_KHOA);
        form.setTenMonHoc("Ten moi");
        form.setSoTinChi(3);
        form.setSoTietLyThuyet(45);
        form.setSoTietThucHanh(15);
        form.setMoTa("Mo ta moi");
        return form;
    }

    private LopHocPhan lopHocPhan() {
        LopHocPhan lopHocPhan = new LopHocPhan();
        lopHocPhan.setMaLopHocPhan("LHP001");
        return lopHocPhan;
    }

    @Test
    void shouldReturnAllSubjects() {
        List<MonHoc> subjects = List.of(monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE")));
        when(monHocRepository.findAll()).thenReturn(subjects);

        assertSame(subjects, monHocService.findAll());
    }

    @Test
    void shouldReturnSubjectWhenExists() {
        MonHoc subject = monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE"));
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));

        assertSame(subject, monHocService.findById(MA_MON_HOC));
    }

    @Test
    void shouldThrowWhenSubjectDoesNotExist() {
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> monHocService.findById(MA_MON_HOC));
    }

    @Test
    void shouldReturnAllWhenKeywordIsNull() {
        List<MonHoc> subjects = List.of(monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE")));
        when(monHocRepository.findAll()).thenReturn(subjects);

        assertSame(subjects, monHocService.search(null));
    }

    @Test
    void shouldReturnAllWhenKeywordIsBlank() {
        List<MonHoc> subjects = List.of(monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE")));
        when(monHocRepository.findAll()).thenReturn(subjects);

        assertSame(subjects, monHocService.search("  "));
    }

    @Test
    void shouldSearchByCodeOrName() {
        List<MonHoc> subjects = List.of(monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE")));
        when(monHocRepository
                .findByMaMonHocContainingIgnoreCaseOrTenMonHocContainingIgnoreCase("MH", "MH"))
                .thenReturn(subjects);

        assertSame(subjects, monHocService.search(" MH "));
    }

    @Test
    void shouldCreateSubjectSuccessfully() {
        Khoa khoa = khoa(MA_KHOA, "ACTIVE");
        when(monHocRepository.existsById(MA_MON_HOC)).thenReturn(false);
        when(khoaRepository.findById(MA_KHOA)).thenReturn(Optional.of(khoa));
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonHoc result = monHocService.create(form());

        assertNotNull(result);
        assertEquals(MA_MON_HOC, result.getMaMonHoc());
        assertSame(khoa, result.getKhoa());
        assertEquals("ACTIVE", result.getTrangThai());
        verify(monHocRepository).save(result);
    }

    @Test
    void shouldRejectDuplicateSubjectCode() {
        when(monHocRepository.existsById(MA_MON_HOC)).thenReturn(true);

        assertThrows(BusinessException.class, () -> monHocService.create(form()));

        verify(monHocRepository, never()).save(any(MonHoc.class));
    }

    @Test
    void shouldRejectUnknownDepartmentWhenCreatingSubject() {
        when(monHocRepository.existsById(MA_MON_HOC)).thenReturn(false);
        when(khoaRepository.findById(MA_KHOA)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> monHocService.create(form()));

        verify(monHocRepository, never()).save(any(MonHoc.class));
    }

    @Test
    void shouldUpdateSubjectSuccessfully() {
        Khoa khoa = khoa(MA_KHOA, "ACTIVE");
        MonHoc subject = monHoc("ACTIVE", khoa);
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonHoc result = monHocService.update(MA_MON_HOC, form());

        assertEquals("Ten moi", result.getTenMonHoc());
        assertEquals(3, result.getSoTinChi());
        assertEquals(45, result.getSoTietLyThuyet());
        assertEquals(15, result.getSoTietThucHanh());
        assertEquals("Mo ta moi", result.getMoTa());
        assertSame(khoa, result.getKhoa());
        verify(monHocRepository).save(subject);
    }

    @Test
    void shouldKeepSubjectIdImmutable() {
        Khoa khoa = khoa(MA_KHOA, "ACTIVE");
        MonHoc subject = monHoc("ACTIVE", khoa);
        MonHocForm form = form();
        form.setMaMonHoc("DIFFERENT");
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonHoc result = monHocService.update(MA_MON_HOC, form);

        assertEquals(MA_MON_HOC, result.getMaMonHoc());
        verify(monHocRepository).findById(MA_MON_HOC);
    }

    @Test
    void shouldRejectUpdateWhenSubjectDoesNotExist() {
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> monHocService.update(MA_MON_HOC, form()));

        verify(monHocRepository, never()).save(any(MonHoc.class));
    }

    @Test
    void shouldRejectUnknownDepartmentWhenUpdatingSubject() {
        MonHoc subject = monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE"));
        MonHocForm form = form();
        form.setMaKhoa("UNKNOWN");
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(khoaRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> monHocService.update(MA_MON_HOC, form));

        verify(monHocRepository, never()).save(any(MonHoc.class));
    }

    @Test
    void shouldAllowCreditChangeWhenSubjectHasNoCourseSection() {
        MonHoc subject = monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE"));
        MonHocForm form = form();
        form.setSoTinChi(4);
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(lopHocPhanRepository.findByMonHoc_MaMonHoc(MA_MON_HOC)).thenReturn(List.of());
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonHoc result = monHocService.update(MA_MON_HOC, form);

        assertEquals(4, result.getSoTinChi());
        verify(monHocRepository).save(subject);
    }

    @Test
    void shouldRejectCreditChangeWhenSubjectHasCourseSection() {
        MonHoc subject = monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE"));
        MonHocForm form = form();
        form.setSoTinChi(4);
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(lopHocPhanRepository.findByMonHoc_MaMonHoc(MA_MON_HOC)).thenReturn(List.of(lopHocPhan()));

        assertThrows(BusinessException.class, () -> monHocService.update(MA_MON_HOC, form));

        assertEquals(3, subject.getSoTinChi());
        verify(monHocRepository, never()).save(any(MonHoc.class));
    }

    @Test
    void shouldAllowOtherFieldChangesWhenSubjectHasCourseSection() {
        MonHoc subject = monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE"));
        MonHocForm form = form();
        form.setTenMonHoc("Ten da cap nhat");
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        lenient()
                .when(lopHocPhanRepository.findByMonHoc_MaMonHoc(MA_MON_HOC))
                .thenReturn(List.of(lopHocPhan()));
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonHoc result = monHocService.update(MA_MON_HOC, form);

        assertEquals(3, result.getSoTinChi());
        assertEquals("Ten da cap nhat", result.getTenMonHoc());
        verify(monHocRepository).save(subject);
    }

    @Test
    void shouldAllowKeepingCurrentInactiveDepartment() {
        Khoa currentKhoa = khoa(MA_KHOA, "INACTIVE");
        MonHoc subject = monHoc("ACTIVE", currentKhoa);
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonHoc result = monHocService.update(MA_MON_HOC, form());

        assertSame(currentKhoa, result.getKhoa());
        verify(khoaRepository, never()).findById(any());
    }

    @Test
    void shouldAllowChangingToActiveDepartment() {
        Khoa activeKhoa = khoa("ATTT", "ACTIVE");
        MonHoc subject = monHoc("ACTIVE", khoa(MA_KHOA, "INACTIVE"));
        MonHocForm form = form();
        form.setMaKhoa("ATTT");
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(khoaRepository.findById("ATTT")).thenReturn(Optional.of(activeKhoa));
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonHoc result = monHocService.update(MA_MON_HOC, form);

        assertSame(activeKhoa, result.getKhoa());
    }

    @Test
    void shouldRejectChangingToInactiveDepartment() {
        Khoa inactiveKhoa = khoa("ATTT", "INACTIVE");
        MonHoc subject = monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE"));
        MonHocForm form = form();
        form.setMaKhoa("ATTT");
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(khoaRepository.findById("ATTT")).thenReturn(Optional.of(inactiveKhoa));

        assertThrows(BusinessException.class, () -> monHocService.update(MA_MON_HOC, form));

        verify(monHocRepository, never()).save(any(MonHoc.class));
    }

    @Test
    void shouldChangeActiveToInactive() {
        MonHoc subject = monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE"));
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        monHocService.toggleStatus(MA_MON_HOC);

        assertEquals("INACTIVE", subject.getTrangThai());
        verify(monHocRepository).save(subject);
    }

    @Test
    void shouldChangeInactiveToActive() {
        MonHoc subject = monHoc("INACTIVE", khoa(MA_KHOA, "ACTIVE"));
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        monHocService.toggleStatus(MA_MON_HOC);

        assertEquals("ACTIVE", subject.getTrangThai());
        verify(monHocRepository).save(subject);
    }

    @Test
    void shouldRejectUnsupportedStatus() {
        MonHoc subject = monHoc("PENDING", khoa(MA_KHOA, "ACTIVE"));
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));

        assertThrows(BusinessException.class, () -> monHocService.toggleStatus(MA_MON_HOC));

        assertEquals("PENDING", subject.getTrangThai());
        verify(monHocRepository, never()).save(any(MonHoc.class));
    }

    @Test
    void shouldRejectToggleWhenSubjectDoesNotExist() {
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> monHocService.toggleStatus(MA_MON_HOC));

        verify(monHocRepository, never()).save(any(MonHoc.class));
    }

    @Test
    void shouldNotModifyCourseSectionsWhenStatusChanges() {
        MonHoc subject = monHoc("ACTIVE", khoa(MA_KHOA, "ACTIVE"));
        when(monHocRepository.findById(MA_MON_HOC)).thenReturn(Optional.of(subject));
        when(monHocRepository.save(any(MonHoc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        monHocService.toggleStatus(MA_MON_HOC);

        assertEquals("INACTIVE", subject.getTrangThai());
        verifyNoInteractions(lopHocPhanRepository);
        verify(monHocRepository).save(subject);
    }
}
