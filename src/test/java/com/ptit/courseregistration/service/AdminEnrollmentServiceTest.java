package com.ptit.courseregistration.service;

import com.ptit.courseregistration.entity.DangKyHocPhan;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.DangKyHocPhanRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminEnrollmentServiceTest {

    private static final String MA_LOP = "LHP001";

    @Mock
    private DangKyHocPhanRepository dangKyHocPhanRepository;
    @Mock
    private LopHocPhanRepository lopHocPhanRepository;

    @InjectMocks
    private AdminEnrollmentService adminEnrollmentService;

    private void assertNoEnrollmentWrites() {
        verify(dangKyHocPhanRepository, never()).save(any(DangKyHocPhan.class));
        verify(dangKyHocPhanRepository, never()).delete(any(DangKyHocPhan.class));
    }

    @Test
    void shouldSearchWithAllFilters() {
        List<DangKyHocPhan> enrollments = List.of(new DangKyHocPhan());
        when(dangKyHocPhanRepository.searchForAdmin("SV001", "HK1", MA_LOP, "REGISTERED"))
                .thenReturn(enrollments);

        assertSame(enrollments,
                adminEnrollmentService.search(" SV001 ", " HK1 ", " LHP001 ", " REGISTERED "));

        verify(dangKyHocPhanRepository).searchForAdmin("SV001", "HK1", MA_LOP, "REGISTERED");
    }

    @Test
    void shouldConvertBlankKeywordToNull() {
        when(dangKyHocPhanRepository.searchForAdmin(null, "HK1", MA_LOP, "REGISTERED"))
                .thenReturn(List.of());

        adminEnrollmentService.search("  ", "HK1", MA_LOP, "REGISTERED");

        verify(dangKyHocPhanRepository).searchForAdmin(null, "HK1", MA_LOP, "REGISTERED");
    }

    @Test
    void shouldConvertBlankSemesterToNull() {
        when(dangKyHocPhanRepository.searchForAdmin("SV", null, MA_LOP, "REGISTERED"))
                .thenReturn(List.of());

        adminEnrollmentService.search("SV", " ", MA_LOP, "REGISTERED");

        verify(dangKyHocPhanRepository).searchForAdmin("SV", null, MA_LOP, "REGISTERED");
    }

    @Test
    void shouldConvertBlankCourseSectionToNull() {
        when(dangKyHocPhanRepository.searchForAdmin("SV", "HK1", null, "REGISTERED"))
                .thenReturn(List.of());

        adminEnrollmentService.search("SV", "HK1", "\t", "REGISTERED");

        verify(dangKyHocPhanRepository).searchForAdmin("SV", "HK1", null, "REGISTERED");
    }

    @Test
    void shouldConvertBlankStatusToNull() {
        when(dangKyHocPhanRepository.searchForAdmin("SV", "HK1", MA_LOP, null))
                .thenReturn(List.of());

        adminEnrollmentService.search("SV", "HK1", MA_LOP, " ");

        verify(dangKyHocPhanRepository).searchForAdmin("SV", "HK1", MA_LOP, null);
    }

    @Test
    void shouldSearchWithNoFilters() {
        when(dangKyHocPhanRepository.searchForAdmin(null, null, null, null)).thenReturn(List.of());

        adminEnrollmentService.search(null, null, null, null);

        verify(dangKyHocPhanRepository).searchForAdmin(null, null, null, null);
    }

    @Test
    void shouldReturnRepositoryResults() {
        List<DangKyHocPhan> enrollments = List.of(new DangKyHocPhan(), new DangKyHocPhan());
        when(dangKyHocPhanRepository.searchForAdmin(null, null, null, null)).thenReturn(enrollments);

        assertSame(enrollments, adminEnrollmentService.search(null, null, null, null));
    }

    @Test
    void shouldPreserveRepositoryOrdering() {
        DangKyHocPhan newest = new DangKyHocPhan();
        DangKyHocPhan oldest = new DangKyHocPhan();
        List<DangKyHocPhan> orderedResults = List.of(newest, oldest);
        when(dangKyHocPhanRepository.searchForAdmin(null, null, null, null))
                .thenReturn(orderedResults);

        List<DangKyHocPhan> result = adminEnrollmentService.search(null, null, null, null);

        assertSame(orderedResults, result);
        assertSame(newest, result.get(0));
        assertSame(oldest, result.get(1));
    }

    @Test
    void shouldReturnRegisteredStudentsByDefault() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, "REGISTERED"))
                .thenReturn(List.of());

        adminEnrollmentService.findByCourseSection(MA_LOP, null);

        verify(dangKyHocPhanRepository).findForAdminByCourseSection(MA_LOP, "REGISTERED");
    }

    @Test
    void shouldReturnRegisteredWhenStatusBlank() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, "REGISTERED"))
                .thenReturn(List.of());

        adminEnrollmentService.findByCourseSection(MA_LOP, "  ");

        verify(dangKyHocPhanRepository).findForAdminByCourseSection(MA_LOP, "REGISTERED");
    }

    @Test
    void shouldReturnRegisteredStudentsWhenExplicitlyRequested() {
        List<DangKyHocPhan> enrollments = List.of(new DangKyHocPhan());
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, "REGISTERED"))
                .thenReturn(enrollments);

        assertSame(enrollments, adminEnrollmentService.findByCourseSection(MA_LOP, "REGISTERED"));
    }

    @Test
    void shouldReturnCancelledStudents() {
        List<DangKyHocPhan> enrollments = List.of(new DangKyHocPhan());
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, "CANCELLED"))
                .thenReturn(enrollments);

        assertSame(enrollments, adminEnrollmentService.findByCourseSection(MA_LOP, "CANCELLED"));
    }

    @Test
    void shouldReturnAllEnrollmentHistory() {
        List<DangKyHocPhan> history = List.of(new DangKyHocPhan(), new DangKyHocPhan());
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, null)).thenReturn(history);

        assertSame(history, adminEnrollmentService.findByCourseSection(MA_LOP, "ALL"));
        verify(dangKyHocPhanRepository).findForAdminByCourseSection(MA_LOP, null);
    }

    @Test
    void shouldRejectUnsupportedEnrollmentStatus() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> adminEnrollmentService.findByCourseSection(MA_LOP, "UNKNOWN"));
    }

    @Test
    void shouldRejectStudentListWhenCourseSectionDoesNotExist() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> adminEnrollmentService.findByCourseSection(MA_LOP, null));

        verify(dangKyHocPhanRepository, never())
                .findForAdminByCourseSection(any(), any());
    }

    @Test
    void shouldAllowStudentListWhenCourseSectionExists() {
        List<DangKyHocPhan> enrollments = List.of(new DangKyHocPhan());
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, "REGISTERED"))
                .thenReturn(enrollments);

        assertSame(enrollments, adminEnrollmentService.findByCourseSection(MA_LOP, null));
        verify(lopHocPhanRepository).existsById(MA_LOP);
    }

    @Test
    void shouldReturnRegisteredCountForCourseSection() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.countByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED"))
                .thenReturn(4L);

        assertEquals(4L, adminEnrollmentService.countRegisteredByCourseSection(MA_LOP));
        verify(dangKyHocPhanRepository)
                .countByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED");
    }

    @Test
    void shouldRejectCountWhenCourseSectionDoesNotExist() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> adminEnrollmentService.countRegisteredByCourseSection(MA_LOP));

        verify(dangKyHocPhanRepository, never())
                .countByLopHocPhan_MaLopHocPhanAndTrangThai(any(), any());
    }

    @Test
    void shouldReturnZeroWhenNoRegisteredStudents() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.countByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED"))
                .thenReturn(0L);

        assertEquals(0L, adminEnrollmentService.countRegisteredByCourseSection(MA_LOP));
    }

    @Test
    void searchShouldNotSaveEnrollment() {
        when(dangKyHocPhanRepository.searchForAdmin(null, null, null, null)).thenReturn(List.of());

        adminEnrollmentService.search(null, null, null, null);

        assertNoEnrollmentWrites();
    }

    @Test
    void findByCourseSectionShouldNotSaveEnrollment() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, "REGISTERED"))
                .thenReturn(List.of());

        adminEnrollmentService.findByCourseSection(MA_LOP, null);

        assertNoEnrollmentWrites();
    }

    @Test
    void countShouldNotSaveEnrollment() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.countByLopHocPhan_MaLopHocPhanAndTrangThai(MA_LOP, "REGISTERED"))
                .thenReturn(0L);

        adminEnrollmentService.countRegisteredByCourseSection(MA_LOP);

        assertNoEnrollmentWrites();
    }

    @Test
    void shouldAllowCancelledHistoryToBeQueried() {
        List<DangKyHocPhan> cancelled = List.of(new DangKyHocPhan());
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, "CANCELLED"))
                .thenReturn(cancelled);

        assertSame(cancelled, adminEnrollmentService.findByCourseSection(MA_LOP, "CANCELLED"));
    }

    @Test
    void shouldAllowAllToReturnRegisteredAndCancelledRecords() {
        List<DangKyHocPhan> allStatuses = List.of(new DangKyHocPhan(), new DangKyHocPhan());
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, null))
                .thenReturn(allStatuses);

        assertSame(allStatuses, adminEnrollmentService.findByCourseSection(MA_LOP, "ALL"));
    }

    @Test
    void defaultCourseSectionViewShouldNotIncludeCancelledWhenRepositoryIsCalled() {
        when(lopHocPhanRepository.existsById(MA_LOP)).thenReturn(true);
        when(dangKyHocPhanRepository.findForAdminByCourseSection(MA_LOP, "REGISTERED"))
                .thenReturn(List.of());

        adminEnrollmentService.findByCourseSection(MA_LOP, null);

        verify(dangKyHocPhanRepository).findForAdminByCourseSection(MA_LOP, "REGISTERED");
        verify(dangKyHocPhanRepository, never()).findForAdminByCourseSection(MA_LOP, null);
    }
}
