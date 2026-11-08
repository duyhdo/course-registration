package com.ptit.courseregistration.service;

import com.ptit.courseregistration.dto.LopHocPhanForm;
import com.ptit.courseregistration.entity.GiangVien;
import com.ptit.courseregistration.entity.HocKy;
import com.ptit.courseregistration.entity.LopHocPhan;
import com.ptit.courseregistration.entity.MonHoc;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.DangKyHocPhanRepository;
import com.ptit.courseregistration.repository.GiangVienRepository;
import com.ptit.courseregistration.repository.HocKyRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.repository.MonHocRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LopHocPhanServiceTest {

    private static final String MA_LOP = "LHP001";
    private static final String MA_MON = "MH001";
    private static final String MA_HK = "HK2026";
    private static final String MA_GV = "GV001";

    @Mock
    private LopHocPhanRepository lopHocPhanRepository;
    @Mock
    private DangKyHocPhanRepository dangKyHocPhanRepository;
    @Mock
    private MonHocRepository monHocRepository;
    @Mock
    private GiangVienRepository giangVienRepository;
    @Mock
    private HocKyRepository hocKyRepository;

    @InjectMocks
    private LopHocPhanService lopHocPhanService;

    private MonHoc subject(String id, String status) {
        MonHoc subject = new MonHoc();
        subject.setMaMonHoc(id);
        subject.setTenMonHoc("Mon " + id);
        subject.setTrangThai(status);
        return subject;
    }

    private HocKy semester(String id) {
        HocKy semester = new HocKy();
        semester.setMaHocKy(id);
        semester.setTenHocKy("Hoc ky");
        semester.setNamHoc("2026-2027");
        return semester;
    }

    private GiangVien lecturer(String id, String status) {
        GiangVien lecturer = new GiangVien();
        lecturer.setMaGV(id);
        lecturer.setHoTen("Giang vien " + id);
        lecturer.setTrangThai(status);
        return lecturer;
    }

    private LopHocPhan section(String status) {
        return section(status, subject(MA_MON, "ACTIVE"), semester(MA_HK), null);
    }

    private LopHocPhan section(String status, MonHoc subject, HocKy semester, GiangVien lecturer) {
        LopHocPhan section = new LopHocPhan();
        section.setMaLopHocPhan(MA_LOP);
        section.setMonHoc(subject);
        section.setHocKy(semester);
        section.setGiangVien(lecturer);
        section.setPhongHoc("A101");
        section.setThuHoc(2);
        section.setTietBatDau(1);
        section.setTietKetThuc(3);
        section.setSiSoToiDa(40);
        section.setSiSoHienTai(10);
        section.setTrangThai(status);
        return section;
    }

    private LopHocPhanForm form() {
        LopHocPhanForm form = new LopHocPhanForm();
        form.setMaLopHocPhan(MA_LOP);
        form.setMaMonHoc(MA_MON);
        form.setMaHocKy(MA_HK);
        form.setSiSoToiDa(40);
        form.setThuHoc(2);
        form.setTietBatDau(1);
        form.setTietKetThuc(3);
        form.setPhongHoc("A101");
        return form;
    }

    private void stubExisting(LopHocPhan section) {
        when(lopHocPhanRepository.findById(MA_LOP)).thenReturn(Optional.of(section));
    }

    private void stubSave() {
        when(lopHocPhanRepository.save(any(LopHocPhan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubDraftUpdateDependencies(LopHocPhanForm form) {
        when(hocKyRepository.findById(form.getMaHocKy()))
                .thenReturn(Optional.of(semester(form.getMaHocKy())));
    }

    private void stubOpenDependencies(LopHocPhan section) {
        when(monHocRepository.findById(section.getMonHoc().getMaMonHoc()))
                .thenReturn(Optional.of(section.getMonHoc()));
        when(hocKyRepository.findById(section.getHocKy().getMaHocKy()))
                .thenReturn(Optional.of(section.getHocKy()));
        when(giangVienRepository.findById(section.getGiangVien().getMaGV()))
                .thenReturn(Optional.of(section.getGiangVien()));
    }

    @Test
    void shouldReturnAllCourseSections() {
        List<LopHocPhan> sections = List.of(section("DRAFT"));
        when(lopHocPhanRepository.findAll()).thenReturn(sections);

        assertSame(sections, lopHocPhanService.findAll());
    }

    @Test
    void shouldReturnCourseSectionWhenExists() {
        LopHocPhan section = section("DRAFT");
        stubExisting(section);

        assertSame(section, lopHocPhanService.findById(MA_LOP));
    }

    @Test
    void shouldThrowWhenCourseSectionDoesNotExist() {
        when(lopHocPhanRepository.findById(MA_LOP)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> lopHocPhanService.findById(MA_LOP));
    }

    @Test
    void shouldSearchWithProvidedFilters() {
        List<LopHocPhan> sections = List.of(section("OPEN"));
        when(lopHocPhanRepository.findByFilters("LHP", MA_HK, "OPEN")).thenReturn(sections);

        assertSame(sections, lopHocPhanService.search(" LHP ", " " + MA_HK + " ", " OPEN "));
    }

    @Test
    void shouldNormalizeBlankFiltersBeforeSearch() {
        when(lopHocPhanRepository.findByFilters(null, null, null)).thenReturn(List.of());

        lopHocPhanService.search("  ", null, "\t");

        verify(lopHocPhanRepository).findByFilters(null, null, null);
    }

    @Test
    void shouldCreateDraftCourseSectionSuccessfully() {
        MonHoc subject = subject(MA_MON, "ACTIVE");
        HocKy semester = semester(MA_HK);
        GiangVien lecturer = lecturer(MA_GV, "INACTIVE");
        LopHocPhanForm form = form();
        form.setMaGV(MA_GV);
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(false);
        when(monHocRepository.findById(MA_MON)).thenReturn(Optional.of(subject));
        when(hocKyRepository.findById(MA_HK)).thenReturn(Optional.of(semester));
        when(giangVienRepository.findById(MA_GV)).thenReturn(Optional.of(lecturer));
        stubSave();

        LopHocPhan result = lopHocPhanService.create(form);

        assertEquals(MA_LOP, result.getMaLopHocPhan());
        assertSame(subject, result.getMonHoc());
        assertSame(semester, result.getHocKy());
        assertSame(lecturer, result.getGiangVien());
        assertEquals("DRAFT", result.getTrangThai());
        assertEquals(0, result.getSiSoHienTai());
        assertEquals(40, result.getSiSoToiDa());
        verify(lopHocPhanRepository).save(result);
    }

    @Test
    void shouldCreateDraftWithoutLecturer() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(false);
        when(monHocRepository.findById(MA_MON)).thenReturn(Optional.of(subject(MA_MON, "ACTIVE")));
        when(hocKyRepository.findById(MA_HK)).thenReturn(Optional.of(semester(MA_HK)));
        stubSave();

        LopHocPhan result = lopHocPhanService.create(form());

        assertNull(result.getGiangVien());
        assertEquals("DRAFT", result.getTrangThai());
    }

    @Test
    void shouldCreateDraftWithIncompleteScheduleWhenAllowed() {
        LopHocPhanForm form = form();
        form.setThuHoc(null);
        form.setTietBatDau(null);
        form.setTietKetThuc(null);
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(false);
        when(monHocRepository.findById(MA_MON)).thenReturn(Optional.of(subject(MA_MON, "ACTIVE")));
        when(hocKyRepository.findById(MA_HK)).thenReturn(Optional.of(semester(MA_HK)));
        stubSave();

        LopHocPhan result = lopHocPhanService.create(form);

        assertNull(result.getThuHoc());
        assertNull(result.getTietBatDau());
        assertNull(result.getTietKetThuc());
    }

    @Test
    void shouldRejectDuplicateCourseSectionCode() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);

        assertThrows(BusinessException.class, () -> lopHocPhanService.create(form()));

        verify(lopHocPhanRepository, never()).save(any(LopHocPhan.class));
    }

    @Test
    void shouldRejectUnknownSubject() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(false);
        when(monHocRepository.findById(MA_MON)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> lopHocPhanService.create(form()));
    }

    @Test
    void shouldRejectInactiveSubjectOnCreate() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(false);
        when(monHocRepository.findById(MA_MON)).thenReturn(Optional.of(subject(MA_MON, "INACTIVE")));

        assertThrows(BusinessException.class, () -> lopHocPhanService.create(form()));

        verify(lopHocPhanRepository, never()).save(any(LopHocPhan.class));
    }

    @Test
    void shouldRejectUnknownSemester() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(false);
        when(monHocRepository.findById(MA_MON)).thenReturn(Optional.of(subject(MA_MON, "ACTIVE")));
        when(hocKyRepository.findById(MA_HK)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> lopHocPhanService.create(form()));
    }

    @Test
    void shouldRejectUnknownLecturerWhenProvided() {
        LopHocPhanForm form = form();
        form.setMaGV(MA_GV);
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(false);
        when(monHocRepository.findById(MA_MON)).thenReturn(Optional.of(subject(MA_MON, "ACTIVE")));
        when(hocKyRepository.findById(MA_HK)).thenReturn(Optional.of(semester(MA_HK)));
        when(giangVienRepository.findById(MA_GV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> lopHocPhanService.create(form));
    }

    @Test
    void shouldRejectInvalidScheduleWhereEndPeriodBeforeStartPeriod() {
        LopHocPhanForm form = form();
        form.setTietBatDau(4);
        form.setTietKetThuc(3);

        assertThrows(BusinessException.class, () -> lopHocPhanService.create(form));

        verifyNoInteractions(monHocRepository, hocKyRepository, giangVienRepository);
    }

    @Test
    void shouldUpdateDraftCourseSectionSuccessfully() {
        LopHocPhan section = section("DRAFT");
        LopHocPhanForm form = form();
        form.setPhongHoc("B202");
        stubExisting(section);
        stubDraftUpdateDependencies(form);
        stubSave();

        LopHocPhan result = lopHocPhanService.update(MA_LOP, form);

        assertEquals("B202", result.getPhongHoc());
        assertEquals(40, result.getSiSoToiDa());
        assertEquals("DRAFT", result.getTrangThai());
        assertEquals(10, result.getSiSoHienTai());
        verify(lopHocPhanRepository).save(section);
    }

    @Test
    void shouldKeepCourseSectionIdImmutable() {
        LopHocPhan section = section("DRAFT");
        LopHocPhanForm form = form();
        form.setMaLopHocPhan("CHANGED");
        stubExisting(section);
        stubDraftUpdateDependencies(form);
        stubSave();

        LopHocPhan result = lopHocPhanService.update(MA_LOP, form);

        assertEquals(MA_LOP, result.getMaLopHocPhan());
    }

    @Test
    void shouldAllowDraftToChangeSubject() {
        LopHocPhan section = section("DRAFT");
        MonHoc target = subject("MH002", "ACTIVE");
        LopHocPhanForm form = form();
        form.setMaMonHoc("MH002");
        stubExisting(section);
        when(monHocRepository.findById("MH002")).thenReturn(Optional.of(target));
        stubDraftUpdateDependencies(form);
        stubSave();

        assertSame(target, lopHocPhanService.update(MA_LOP, form).getMonHoc());
    }

    @Test
    void shouldAllowDraftToChangeSemester() {
        LopHocPhan section = section("DRAFT");
        HocKy target = semester("HK2027");
        LopHocPhanForm form = form();
        form.setMaHocKy("HK2027");
        stubExisting(section);
        when(hocKyRepository.findById("HK2027")).thenReturn(Optional.of(target));
        stubSave();

        assertSame(target, lopHocPhanService.update(MA_LOP, form).getHocKy());
    }

    @Test
    void shouldAllowDraftToChangeLecturer() {
        LopHocPhan section = section("DRAFT");
        GiangVien target = lecturer("GV002", "ACTIVE");
        LopHocPhanForm form = form();
        form.setMaGV("GV002");
        stubExisting(section);
        stubDraftUpdateDependencies(form);
        when(giangVienRepository.findById("GV002")).thenReturn(Optional.of(target));
        stubSave();

        assertSame(target, lopHocPhanService.update(MA_LOP, form).getGiangVien());
    }

    @Test
    void shouldRejectUpdateWhenCourseSectionDoesNotExist() {
        when(lopHocPhanRepository.findById(MA_LOP)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form()));

        verify(lopHocPhanRepository, never()).save(any(LopHocPhan.class));
    }

    @Test
    void shouldRejectCapacityBelowCurrentEnrollment() {
        LopHocPhan section = section("DRAFT");
        LopHocPhanForm form = form();
        form.setSiSoToiDa(9);
        stubExisting(section);

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));

        verify(lopHocPhanRepository, never()).save(any(LopHocPhan.class));
    }

    @Test
    void shouldRejectInvalidScheduleOnUpdate() {
        LopHocPhan section = section("DRAFT");
        LopHocPhanForm form = form();
        form.setTietBatDau(5);
        form.setTietKetThuc(4);
        stubExisting(section);

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));

        verify(lopHocPhanRepository, never()).save(any(LopHocPhan.class));
    }

    @Test
    void shouldRejectUnknownSubjectOnUpdate() {
        LopHocPhan section = section("DRAFT");
        LopHocPhanForm form = form();
        form.setMaMonHoc("UNKNOWN");
        stubExisting(section);
        when(monHocRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));
    }

    @Test
    void shouldRejectUnknownSemesterOnUpdate() {
        LopHocPhan section = section("DRAFT");
        LopHocPhanForm form = form();
        form.setMaHocKy("UNKNOWN");
        stubExisting(section);
        when(hocKyRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));
    }

    @Test
    void shouldRejectUnknownLecturerOnUpdate() {
        LopHocPhan section = section("DRAFT");
        LopHocPhanForm form = form();
        form.setMaGV(MA_GV);
        stubExisting(section);
        stubDraftUpdateDependencies(form);
        when(giangVienRepository.findById(MA_GV)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));
    }

    @Test
    void shouldAllowOpenSectionToUpdateOperationalFields() {
        GiangVien currentTeacher = lecturer(MA_GV, "ACTIVE");
        LopHocPhan section = section("OPEN", subject(MA_MON, "ACTIVE"), semester(MA_HK), currentTeacher);
        GiangVien newTeacher = lecturer("GV002", "ACTIVE");
        LopHocPhanForm form = form();
        form.setMaGV("GV002");
        form.setPhongHoc("C303");
        form.setThuHoc(5);
        form.setTietBatDau(4);
        form.setTietKetThuc(6);
        form.setSiSoToiDa(50);
        stubExisting(section);
        when(giangVienRepository.findById("GV002")).thenReturn(Optional.of(newTeacher));
        stubSave();

        LopHocPhan result = lopHocPhanService.update(MA_LOP, form);

        assertSame(newTeacher, result.getGiangVien());
        assertEquals("C303", result.getPhongHoc());
        assertEquals(5, result.getThuHoc());
        assertEquals(4, result.getTietBatDau());
        assertEquals(6, result.getTietKetThuc());
        assertEquals(50, result.getSiSoToiDa());
        assertEquals(MA_MON, result.getMonHoc().getMaMonHoc());
        assertEquals(MA_HK, result.getHocKy().getMaHocKy());
        assertEquals(10, result.getSiSoHienTai());
        assertEquals("OPEN", result.getTrangThai());
    }

    @Test
    void shouldRejectChangingSubjectWhenOpen() {
        LopHocPhan section = section("OPEN");
        LopHocPhanForm form = form();
        form.setMaMonHoc("MH002");
        stubExisting(section);

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));
    }

    @Test
    void shouldRejectChangingSemesterWhenOpen() {
        LopHocPhan section = section("OPEN");
        LopHocPhanForm form = form();
        form.setMaHocKy("HK2027");
        stubExisting(section);

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));
    }

    @Test
    void shouldRejectCapacityBelowCurrentEnrollmentWhenOpen() {
        LopHocPhan section = section("OPEN");
        LopHocPhanForm form = form();
        form.setSiSoToiDa(9);
        stubExisting(section);

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));
    }

    @Test
    void shouldRejectUpdateWhenClosed() {
        stubExisting(section("CLOSED"));

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form()));

        verify(lopHocPhanRepository, never()).save(any(LopHocPhan.class));
    }

    @Test
    void shouldRejectUpdateWhenCancelled() {
        stubExisting(section("CANCELLED"));

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form()));

        verify(lopHocPhanRepository, never()).save(any(LopHocPhan.class));
    }

    @Test
    void shouldAllowRetainingCurrentInactiveSubjectWhenApplicable() {
        MonHoc inactiveSubject = subject(MA_MON, "INACTIVE");
        LopHocPhan section = section("DRAFT", inactiveSubject, semester(MA_HK), null);
        stubExisting(section);
        stubDraftUpdateDependencies(form());
        stubSave();

        assertSame(inactiveSubject, lopHocPhanService.update(MA_LOP, form()).getMonHoc());
        verify(monHocRepository, never()).findById(MA_MON);
    }

    @Test
    void shouldAllowRetainingCurrentInactiveLecturer() {
        GiangVien inactiveTeacher = lecturer(MA_GV, "INACTIVE");
        LopHocPhan section = section("DRAFT", subject(MA_MON, "ACTIVE"), semester(MA_HK), inactiveTeacher);
        LopHocPhanForm form = form();
        form.setMaGV(MA_GV);
        stubExisting(section);
        stubDraftUpdateDependencies(form);
        when(giangVienRepository.findById(MA_GV)).thenReturn(Optional.of(inactiveTeacher));
        stubSave();

        assertSame(inactiveTeacher, lopHocPhanService.update(MA_LOP, form).getGiangVien());
    }

    @Test
    void shouldRejectChangingToAnotherInactiveSubject() {
        LopHocPhan section = section("DRAFT");
        LopHocPhanForm form = form();
        form.setMaMonHoc("MH002");
        stubExisting(section);
        when(monHocRepository.findById("MH002")).thenReturn(Optional.of(subject("MH002", "INACTIVE")));

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));
    }

    @Test
    void shouldRejectChangingToAnotherInactiveLecturer() {
        LopHocPhan section = section("DRAFT");
        LopHocPhanForm form = form();
        form.setMaGV("GV002");
        stubExisting(section);
        stubDraftUpdateDependencies(form);
        when(giangVienRepository.findById("GV002"))
                .thenReturn(Optional.of(lecturer("GV002", "INACTIVE")));

        assertThrows(BusinessException.class, () -> lopHocPhanService.update(MA_LOP, form));
    }

    @Test
    void shouldOpenValidDraftCourseSection() {
        GiangVien teacher = lecturer(MA_GV, "ACTIVE");
        LopHocPhan section = section("DRAFT", subject(MA_MON, "ACTIVE"), semester(MA_HK), teacher);
        stubExisting(section);
        stubOpenDependencies(section);
        stubSave();

        lopHocPhanService.open(MA_LOP);

        assertEquals("OPEN", section.getTrangThai());
        verify(lopHocPhanRepository).save(section);
    }

    @Test
    void shouldRejectOpenWhenCurrentStateIsNotDraft() {
        stubExisting(section("OPEN"));

        assertThrows(BusinessException.class, () -> lopHocPhanService.open(MA_LOP));
    }

    @Test
    void shouldRejectOpenWithoutLecturer() {
        stubExisting(section("DRAFT"));

        assertThrows(BusinessException.class, () -> lopHocPhanService.open(MA_LOP));

        verify(lopHocPhanRepository, never()).save(any(LopHocPhan.class));
    }

    @Test
    void shouldRejectOpenWithInactiveLecturer() {
        GiangVien teacher = lecturer(MA_GV, "INACTIVE");
        LopHocPhan section = section("DRAFT", subject(MA_MON, "ACTIVE"), semester(MA_HK), teacher);
        stubExisting(section);
        stubOpenDependencies(section);

        assertThrows(BusinessException.class, () -> lopHocPhanService.open(MA_LOP));
    }

    @Test
    void shouldRejectOpenWithInactiveSubject() {
        GiangVien teacher = lecturer(MA_GV, "ACTIVE");
        LopHocPhan section = section("DRAFT", subject(MA_MON, "INACTIVE"), semester(MA_HK), teacher);
        stubExisting(section);
        when(monHocRepository.findById(MA_MON)).thenReturn(Optional.of(section.getMonHoc()));

        assertThrows(BusinessException.class, () -> lopHocPhanService.open(MA_LOP));
    }

    @Test
    void shouldRejectOpenWithoutValidSchedule() {
        GiangVien teacher = lecturer(MA_GV, "ACTIVE");
        LopHocPhan section = section("DRAFT", subject(MA_MON, "ACTIVE"), semester(MA_HK), teacher);
        section.setThuHoc(null);
        stubExisting(section);
        stubOpenDependencies(section);

        assertThrows(BusinessException.class, () -> lopHocPhanService.open(MA_LOP));
    }

    @Test
    void shouldRejectOpenWhenEndPeriodBeforeStartPeriod() {
        GiangVien teacher = lecturer(MA_GV, "ACTIVE");
        LopHocPhan section = section("DRAFT", subject(MA_MON, "ACTIVE"), semester(MA_HK), teacher);
        section.setTietBatDau(5);
        section.setTietKetThuc(4);
        stubExisting(section);
        stubOpenDependencies(section);

        assertThrows(BusinessException.class, () -> lopHocPhanService.open(MA_LOP));
    }

    @Test
    void shouldAllowOpenWithoutRoom() {
        GiangVien teacher = lecturer(MA_GV, "ACTIVE");
        LopHocPhan section = section("DRAFT", subject(MA_MON, "ACTIVE"), semester(MA_HK), teacher);
        section.setPhongHoc(null);
        stubExisting(section);
        stubOpenDependencies(section);
        stubSave();

        lopHocPhanService.open(MA_LOP);

        assertEquals("OPEN", section.getTrangThai());
    }

    @Test
    void shouldCloseOpenCourseSection() {
        LopHocPhan section = section("OPEN");
        stubExisting(section);
        stubSave();

        lopHocPhanService.close(MA_LOP);

        assertEquals("CLOSED", section.getTrangThai());
        verify(lopHocPhanRepository).save(section);
    }

    @Test
    void shouldRejectCloseWhenDraft() {
        stubExisting(section("DRAFT"));

        assertThrows(BusinessException.class, () -> lopHocPhanService.close(MA_LOP));
    }

    @Test
    void shouldRejectCloseWhenAlreadyClosed() {
        stubExisting(section("CLOSED"));

        assertThrows(BusinessException.class, () -> lopHocPhanService.close(MA_LOP));
    }

    @Test
    void shouldRejectCloseWhenCancelled() {
        stubExisting(section("CANCELLED"));

        assertThrows(BusinessException.class, () -> lopHocPhanService.close(MA_LOP));
    }

    @Test
    void shouldCancelDraftCourseSection() {
        LopHocPhan section = section("DRAFT");
        stubExistingForUpdate(section);
        when(dangKyHocPhanRepository
                .existsByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED")).thenReturn(false);
        stubSave();

        lopHocPhanService.cancel(MA_LOP);

        assertEquals("CANCELLED", section.getTrangThai());
        verify(lopHocPhanRepository).save(section);
    }

    @Test
    void shouldCancelOpenCourseSection() {
        LopHocPhan section = section("OPEN");
        stubExistingForUpdate(section);
        when(dangKyHocPhanRepository
                .existsByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED")).thenReturn(false);
        stubSave();

        lopHocPhanService.cancel(MA_LOP);

        assertEquals("CANCELLED", section.getTrangThai());
    }

    @Test
    void shouldCancelWhenNoRegisteredStudents() {
        LopHocPhan section = section("OPEN");
        stubExistingForUpdate(section);
        when(dangKyHocPhanRepository
                .existsByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED")).thenReturn(false);
        stubSave();

        lopHocPhanService.cancel(MA_LOP);

        assertEquals("CANCELLED", section.getTrangThai());
        verify(lopHocPhanRepository).save(section);
    }

    @Test
    void shouldRejectCancelWhenRegisteredStudentsExist() {
        LopHocPhan section = section("OPEN");
        stubExistingForUpdate(section);
        when(dangKyHocPhanRepository
                .existsByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED")).thenReturn(true);

        assertThrows(BusinessException.class, () -> lopHocPhanService.cancel(MA_LOP));

        assertEquals("OPEN", section.getTrangThai());
        verify(lopHocPhanRepository, never()).save(any(LopHocPhan.class));
        verify(dangKyHocPhanRepository)
                .existsByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED");
        verify(dangKyHocPhanRepository, never()).save(any());
    }

    @Test
    void shouldRejectCancelWhenClosed() {
        stubExistingForUpdate(section("CLOSED"));

        assertThrows(BusinessException.class, () -> lopHocPhanService.cancel(MA_LOP));
    }

    @Test
    void shouldRejectCancelWhenAlreadyCancelled() {
        stubExistingForUpdate(section("CANCELLED"));

        assertThrows(BusinessException.class, () -> lopHocPhanService.cancel(MA_LOP));
    }

    @Test
    void shouldNotModifyEnrollmentDataWhenCancelling() {
        LopHocPhan section = section("DRAFT");
        stubExistingForUpdate(section);
        stubSave();

        lopHocPhanService.cancel(MA_LOP);

        assertEquals("CANCELLED", section.getTrangThai());
        verify(lopHocPhanRepository).save(section);
    }

    @Test
    void shouldUseLockWhenCancellingSection() {
        LopHocPhan section = section("OPEN");
        stubExistingForUpdate(section);
        when(dangKyHocPhanRepository
                .existsByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED")).thenReturn(false);
        stubSave();

        lopHocPhanService.cancel(MA_LOP);

        verify(lopHocPhanRepository).findByIdForUpdate(MA_LOP);
        verify(lopHocPhanRepository, never()).findById(MA_LOP);
    }

    @Test
    void shouldCheckRegisteredEnrollmentsAfterAcquiringLock() {
        LopHocPhan section = section("OPEN");
        stubExistingForUpdate(section);
        when(dangKyHocPhanRepository
                .existsByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED")).thenReturn(false);
        stubSave();

        lopHocPhanService.cancel(MA_LOP);

        InOrder inOrder = inOrder(lopHocPhanRepository, dangKyHocPhanRepository);
        inOrder.verify(lopHocPhanRepository).findByIdForUpdate(MA_LOP);
        inOrder.verify(dangKyHocPhanRepository)
                .existsByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED");
    }

    private void stubExistingForUpdate(LopHocPhan section) {
        when(lopHocPhanRepository.findByIdForUpdate(MA_LOP)).thenReturn(Optional.of(section));
    }
}
