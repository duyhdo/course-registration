package com.ptit.courseregistration.controller;

import com.ptit.courseregistration.repository.GiangVienRepository;
import com.ptit.courseregistration.repository.HocKyRepository;
import com.ptit.courseregistration.repository.KhoaRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.repository.MonHocRepository;
import com.ptit.courseregistration.service.AdminEnrollmentService;
import com.ptit.courseregistration.service.DangKyHocPhanService;
import com.ptit.courseregistration.service.LopHocPhanService;
import com.ptit.courseregistration.service.MonHocService;
import com.ptit.courseregistration.service.StudentService;
import com.ptit.courseregistration.web.PaginationSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaginationControllerTest {

    @Mock
    private StudentService studentService;
    @Mock
    private KhoaRepository khoaRepository;
    @InjectMocks
    private StudentAdminController studentAdminController;

    @Mock
    private MonHocService monHocService;
    @Mock
    private LopHocPhanService lopHocPhanService;
    @Mock
    private MonHocRepository monHocRepository;
    @Mock
    private GiangVienRepository giangVienRepository;
    @Mock
    private HocKyRepository hocKyRepository;
    @InjectMocks
    private MonHocAdminController monHocAdminController;
    @InjectMocks
    private LopHocPhanAdminController lopHocPhanAdminController;

    @Mock
    private AdminEnrollmentService adminEnrollmentService;
    @Mock
    private LopHocPhanRepository lopHocPhanRepository;
    @InjectMocks
    private AdminEnrollmentController adminEnrollmentController;

    @Mock
    private DangKyHocPhanService dangKyHocPhanService;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private StudentEnrollmentController studentEnrollmentController;

    @Test
    void studentSearchPaginationPreservesKeyword() {
        when(studentService.search(eq("B21"), any(Pageable.class))).thenAnswer(invocation ->
                page(invocation.getArgument(1)));
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("admin/students/list", studentAdminController.list("B21", 1, 20, model));

        Pageable pageable = capturedPageable();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertTrue(pagination(model).getNextUrl().contains("keyword=B21"));
    }

    @Test
    void subjectSearchPaginationPreservesKeyword() {
        when(monHocService.search(eq("java"), any(Pageable.class))).thenAnswer(invocation ->
                page(invocation.getArgument(1)));
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("admin/subjects/list", monHocAdminController.list("java", 0, 10, model));

        assertTrue(pagination(model).getNextUrl().contains("keyword=java"));
    }

    @Test
    void courseSectionPaginationPreservesAllFilters() {
        when(lopHocPhanService.search(eq("java"), eq("HK1"), eq("OPEN"), any(Pageable.class)))
                .thenAnswer(invocation -> page(invocation.getArgument(3)));
        when(hocKyRepository.findAllByOrderByNgayBatDauDesc()).thenReturn(List.of());
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("admin/course-sections/list",
                lopHocPhanAdminController.list("java", "HK1", "OPEN", 0, 10, model));

        String nextUrl = pagination(model).getNextUrl();
        assertTrue(nextUrl.contains("keyword=java"));
        assertTrue(nextUrl.contains("maHocKy=HK1"));
        assertTrue(nextUrl.contains("trangThai=OPEN"));
    }

    @Test
    void enrollmentPaginationPreservesAllFilters() {
        when(adminEnrollmentService.search(eq("java"), eq("HK1"), eq("LHP1"), eq("REGISTERED"),
                any(Pageable.class))).thenAnswer(invocation -> page(invocation.getArgument(4)));
        when(hocKyRepository.findAllByOrderByNgayBatDauDesc()).thenReturn(List.of());
        when(lopHocPhanRepository.findAll()).thenReturn(List.of());
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("admin/enrollments/list",
                adminEnrollmentController.list("java", "HK1", "LHP1", "REGISTERED", 0, 10, model));

        String nextUrl = pagination(model).getNextUrl();
        assertTrue(nextUrl.contains("keyword=java"));
        assertTrue(nextUrl.contains("maHocKy=HK1"));
        assertTrue(nextUrl.contains("maLopHocPhan=LHP1"));
        assertTrue(nextUrl.contains("trangThai=REGISTERED"));
    }

    @Test
    void availableSectionsPaginationPreservesFiltersAndRequestsOpenOnly() {
        when(lopHocPhanRepository.findByFilters(eq("java"), eq("HK1"), eq("OPEN"), any(Pageable.class)))
                .thenAnswer(invocation -> page(invocation.getArgument(3)));
        when(hocKyRepository.findAllByOrderByNgayBatDauDesc()).thenReturn(List.of());
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("student/enrollment/available",
                studentEnrollmentController.available("java", "HK1", 0, 10, model));

        String nextUrl = pagination(model).getNextUrl();
        assertTrue(nextUrl.contains("keyword=java"));
        assertTrue(nextUrl.contains("maHocKy=HK1"));
    }

    @Test
    void historyPaginationKeepsExistingRouteAndSortOrder() {
        when(authentication.getName()).thenReturn("SV001");
        when(dangKyHocPhanService.findStudentHistory(eq("SV001"), any(Pageable.class)))
                .thenAnswer(invocation -> page(invocation.getArgument(1)));
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("student/enrollment/history",
                studentEnrollmentController.history(authentication, 0, 10, model));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(dangKyHocPhanService).findStudentHistory(eq("SV001"), pageable.capture());
        assertEquals("ngayDangKy: DESC,maDangKy: DESC", pageable.getValue().getSort().toString());
        assertTrue(pagination(model).getNextUrl().startsWith("/student/enrollment-history?"));
    }

    private Page<?> page(Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 45);
    }

    private Pageable capturedPageable() {
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(studentService).search(eq("B21"), pageable.capture());
        return pageable.getValue();
    }

    private PaginationSupport.PaginationView pagination(ExtendedModelMap model) {
        return (PaginationSupport.PaginationView) model.get("pagination");
    }
}
