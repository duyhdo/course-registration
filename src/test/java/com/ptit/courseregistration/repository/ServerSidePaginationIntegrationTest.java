package com.ptit.courseregistration.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
class ServerSidePaginationIntegrationTest {

    @Autowired
    private SinhVienRepository sinhVienRepository;
    @Autowired
    private MonHocRepository monHocRepository;
    @Autowired
    private LopHocPhanRepository lopHocPhanRepository;
    @Autowired
    private DangKyHocPhanRepository dangKyHocPhanRepository;

    @Test
    void targetedListQueriesExecuteAsBoundedPages() {
        assertBounded(sinhVienRepository.findAll(
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "maSV"))));
        assertBounded(sinhVienRepository.findByMaSVContainingIgnoreCaseOrHoTenContainingIgnoreCase(
                "", "", PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "maSV"))));
        assertBounded(monHocRepository.findByMaMonHocContainingIgnoreCaseOrTenMonHocContainingIgnoreCase(
                "", "", PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "maMonHoc"))));
        assertBounded(lopHocPhanRepository.findByFilters(null, null, null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "maLopHocPhan"))));
        assertBounded(lopHocPhanRepository.findByFilters(null, null, "OPEN",
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "maLopHocPhan"))));
        assertBounded(dangKyHocPhanRepository.searchForAdmin(null, null, null, null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "ngayDangKy")
                        .and(Sort.by(Sort.Direction.DESC, "maDangKy")))));
        assertBounded(dangKyHocPhanRepository.findBySinhVien_MaSV("__pagination_test__",
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "ngayDangKy")
                        .and(Sort.by(Sort.Direction.DESC, "maDangKy")))));
    }

    private <T> void assertBounded(Page<T> page) {
        assertTrue(page.getPageable().isPaged());
        assertEquals(1, page.getSize());
        assertTrue(page.getContent().size() <= 1);
    }
}
