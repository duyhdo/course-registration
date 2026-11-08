package com.ptit.courseregistration;

import com.ptit.courseregistration.entity.DangKyHocPhan;
import com.ptit.courseregistration.entity.GiangVien;
import com.ptit.courseregistration.entity.HocKy;
import com.ptit.courseregistration.entity.Khoa;
import com.ptit.courseregistration.entity.LopHocPhan;
import com.ptit.courseregistration.entity.MonHoc;
import com.ptit.courseregistration.entity.SinhVien;
import com.ptit.courseregistration.exception.BusinessException;
import com.ptit.courseregistration.repository.DangKyHocPhanRepository;
import com.ptit.courseregistration.repository.GiangVienRepository;
import com.ptit.courseregistration.repository.HocKyRepository;
import com.ptit.courseregistration.repository.KhoaRepository;
import com.ptit.courseregistration.repository.LopHocPhanRepository;
import com.ptit.courseregistration.repository.MonHocRepository;
import com.ptit.courseregistration.repository.SinhVienRepository;
import com.ptit.courseregistration.service.DangKyHocPhanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F-03 Concurrency Integration Test
 *
 * Verifies whether two concurrent cancel() requests for the SAME student
 * and SAME course section are serialized by the existing LopHocPhan
 * pessimistic lock.
 *
 * Expected behavior (H0):
 * - First cancel succeeds, SiSoHienTai decrements once
 * - Second cancel is rejected with BusinessException
 * - Enrollment state is consistent
 *
 * This test runs against the local SQL Server database configured for
 * the project and exercises real transaction/locking semantics.
 */
@SpringBootTest
@ActiveProfiles("local")
@DisplayName("F-03 Concurrent Student Enrollment Cancellation - Real SQL Server Lock Test")
class DangKyHocPhanConcurrencyIntegrationTest {

    private static final String TEST_PREFIX = "CF3_";
    private static final String ACTIVE = "ACTIVE";
    private static final String OPEN = "OPEN";
    private static final String REGISTERED = "REGISTERED";
    private static final String CANCELLED = "CANCELLED";
    private static final int TEST_ITERATIONS = 10;

    @Autowired
    private DangKyHocPhanService dangKyHocPhanService;

    @Autowired
    private SinhVienRepository sinhVienRepository;

    @Autowired
    private LopHocPhanRepository lopHocPhanRepository;

    @Autowired
    private DangKyHocPhanRepository dangKyHocPhanRepository;

    @Autowired
    private KhoaRepository khoaRepository;

    @Autowired
    private MonHocRepository monHocRepository;

    @Autowired
    private HocKyRepository hocKyRepository;

    @Autowired
    private GiangVienRepository giangVienRepository;

    private String testSinhVienId;
    private String testLopHocPhanId;
    private String testMonHocId;
    private String testHocKyId;
    private String testKhoaId;
    private String testGiangVienId;

    private int successCount;
    private int rejectedCount;
    private List<Exception> exceptions;

    @BeforeEach
    void setUp() {
        successCount = 0;
        rejectedCount = 0;
        exceptions = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        // Cleanup test data
        cleanupTestData();
    }

    @Test
    @DisplayName("Concurrent cancel should serialize and allow only one success")
    void testConcurrentCancellation() throws InterruptedException {
        for (int iteration = 0; iteration < TEST_ITERATIONS; iteration++) {
            // Setup fresh test data for each iteration
            setupTestData(iteration);

            // Run concurrent cancel operations
            runConcurrentCancelOperations(iteration);

            // Verify results
            verifyIteration(iteration);

            // Cleanup for next iteration
            cleanupTestData();
        }

        // Final summary
        System.out.println("\n=== F-03 Concurrency Test Summary ===");
        System.out.println("Iterations: " + TEST_ITERATIONS);
        System.out.println("Successful iterations: " + successCount);
        System.out.println("Failed iterations: " + (TEST_ITERATIONS - successCount));
        System.out.println("Total exceptions: " + rejectedCount);

        assertEquals(TEST_ITERATIONS, successCount,
                "All iterations should show exactly one successful cancel per iteration");
    }

    private void setupTestData(int iteration) {
        String iterNum = String.format("%03d", iteration);  // 000-009

        // Create Khoa (Department)
        testKhoaId = TEST_PREFIX + "K" + iterNum;
        Khoa khoa = new Khoa();
        khoa.setMaKhoa(testKhoaId);
        khoa.setTenKhoa("Test Department " + iteration);
        khoa.setTrangThai(ACTIVE);
        khoa = khoaRepository.save(khoa);

        // Create MonHoc (Subject)
        testMonHocId = TEST_PREFIX + "M" + iterNum;
        MonHoc monHoc = new MonHoc();
        monHoc.setMaMonHoc(testMonHocId);
        monHoc.setTenMonHoc("Test Subject " + iteration);
        monHoc.setSoTinChi(3);
        monHoc.setKhoa(khoa);
        monHoc.setTrangThai(ACTIVE);
        monHoc = monHocRepository.save(monHoc);

        // Create HocKy (Semester)
        testHocKyId = TEST_PREFIX + "H" + iterNum;
        HocKy hocKy = new HocKy();
        hocKy.setMaHocKy(testHocKyId);
        hocKy.setTenHocKy("Test HK " + iteration);
        hocKy.setNamHoc("2026");
        LocalDateTime now = LocalDateTime.now();
        hocKy.setNgayMoDangKy(now.minusHours(1));
        hocKy.setNgayDongDangKy(now.plusHours(1));
        hocKy.setTinChiToiDa(12);
        hocKy = hocKyRepository.save(hocKy);

        // Create GiangVien (Instructor)
        testGiangVienId = TEST_PREFIX + "G" + iterNum;
        GiangVien giangVien = new GiangVien();
        giangVien.setMaGV(testGiangVienId);
        giangVien.setHoTen("Test GV " + iteration);
        giangVien.setKhoa(khoa);
        giangVien.setTrangThai(ACTIVE);
        giangVien = giangVienRepository.save(giangVien);

        // Create SinhVien (Student)
        testSinhVienId = TEST_PREFIX + "S" + iterNum;
        SinhVien sinhVien = new SinhVien();
        sinhVien.setMaSV(testSinhVienId);
        sinhVien.setHoTen("Test SV " + iteration);
        sinhVien.setKhoa(khoa);
        sinhVien.setEmail("test" + testSinhVienId + "@test.com");
        sinhVien.setTrangThai(ACTIVE);
        sinhVien = sinhVienRepository.save(sinhVien);

        // Create LopHocPhan (Course Section)
        testLopHocPhanId = TEST_PREFIX + "L" + iterNum;
        LopHocPhan lopHocPhan = new LopHocPhan();
        lopHocPhan.setMaLopHocPhan(testLopHocPhanId);
        lopHocPhan.setMonHoc(monHoc);
        lopHocPhan.setHocKy(hocKy);
        lopHocPhan.setGiangVien(giangVien);
        lopHocPhan.setTrangThai(OPEN);
        lopHocPhan.setSiSoToiDa(2);
        lopHocPhan.setSiSoHienTai(0);
        lopHocPhan = lopHocPhanRepository.save(lopHocPhan);

        // Create REGISTERED enrollment
        DangKyHocPhan dangKy = new DangKyHocPhan();
        dangKy.setSinhVien(sinhVien);
        dangKy.setLopHocPhan(lopHocPhan);
        dangKy.setNgayDangKy(now.minusMinutes(5));
        dangKy.setNgayHuy(null);
        dangKy.setTrangThai(REGISTERED);
        dangKyHocPhanRepository.save(dangKy);

        // Update SiSoHienTai to reflect the enrollment
        lopHocPhan.setSiSoHienTai(1);
        lopHocPhanRepository.save(lopHocPhan);
    }

    private void runConcurrentCancelOperations(int iteration) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(2);
        AtomicInteger thread1Success = new AtomicInteger(0);
        AtomicInteger thread2Success = new AtomicInteger(0);

        List<Exception> threadExceptions = new ArrayList<>();

        // Task 1: First cancel request
        Future<?> future1 = executor.submit(() -> {
            try {
                startLatch.countDown();
                startLatch.await(); // Wait for both threads to start
                dangKyHocPhanService.cancel(testSinhVienId, testLopHocPhanId);
                thread1Success.incrementAndGet();
                System.out.println("[Iteration " + iteration + "] Thread 1: Cancel succeeded");
            } catch (BusinessException e) {
                threadExceptions.add(e);
                System.out.println("[Iteration " + iteration + "] Thread 1: Cancel rejected - " + e.getMessage());
            } catch (Exception e) {
                threadExceptions.add(e);
                System.out.println("[Iteration " + iteration + "] Thread 1: Unexpected error - " + e.getMessage());
            }
        });

        // Task 2: Second cancel request (simultaneous)
        Future<?> future2 = executor.submit(() -> {
            try {
                startLatch.countDown();
                startLatch.await(); // Wait for both threads to start
                dangKyHocPhanService.cancel(testSinhVienId, testLopHocPhanId);
                thread2Success.incrementAndGet();
                System.out.println("[Iteration " + iteration + "] Thread 2: Cancel succeeded");
            } catch (BusinessException e) {
                threadExceptions.add(e);
                System.out.println("[Iteration " + iteration + "] Thread 2: Cancel rejected - " + e.getMessage());
            } catch (Exception e) {
                threadExceptions.add(e);
                System.out.println("[Iteration " + iteration + "] Thread 2: Unexpected error - " + e.getMessage());
            }
        });

        // Wait for both tasks to complete
        executor.shutdown();
        boolean completed = executor.awaitTermination(30, TimeUnit.SECONDS);
        assertTrue(completed, "Concurrent operations did not complete within 30 seconds");

        // Record results for this iteration
        int totalSuccess = thread1Success.get() + thread2Success.get();
        int totalRejected = threadExceptions.size();

        System.out.println("[Iteration " + iteration + "] Successes: " + totalSuccess + ", Rejections: " + totalRejected);

        if (totalSuccess == 1 && totalRejected == 1) {
            successCount++;
        } else {
            System.err.println("[Iteration " + iteration + "] MISMATCH: Expected 1 success and 1 rejection, but got "
                    + totalSuccess + " successes and " + totalRejected + " rejections");
        }

        exceptions.addAll(threadExceptions);
    }

    private void verifyIteration(int iteration) {
        // Query final database state
        Optional<LopHocPhan> finalSection = lopHocPhanRepository.findById(testLopHocPhanId);
        assertTrue(finalSection.isPresent(), "Test section should still exist");

        LopHocPhan section = finalSection.get();
        System.out.println("[Iteration " + iteration + "] Final SiSoHienTai: " + section.getSiSoHienTai());

        // Verify SiSoHienTai was decremented exactly once
        assertEquals(0, section.getSiSoHienTai(),
                "After one successful cancel, SiSoHienTai should be 0 (was 1)");

        // Verify enrollment state
        List<DangKyHocPhan> registeredEnrollments = dangKyHocPhanRepository
                .findByLopHocPhan_MaLopHocPhanAndTrangThai(testLopHocPhanId, REGISTERED);
        assertEquals(0, registeredEnrollments.size(),
                "There should be no REGISTERED enrollments after successful cancel");

        List<DangKyHocPhan> allEnrollments = dangKyHocPhanRepository
                .findByLopHocPhan_MaLopHocPhanAndTrangThai(testLopHocPhanId, CANCELLED);
        assertEquals(1, allEnrollments.size(),
                "There should be exactly one CANCELLED enrollment");

        DangKyHocPhan cancelledEnrollment = allEnrollments.get(0);
        assertNotNull(cancelledEnrollment.getNgayHuy(),
                "Cancelled enrollment must have NgayHuy set");

        System.out.println("[Iteration " + iteration + "] Enrollment state verified ✓");
    }

    private void cleanupTestData() {
        if (testSinhVienId != null) {
            // Delete enrollments first (foreign key constraint)
            List<DangKyHocPhan> allEnrollments = dangKyHocPhanRepository
                    .findBySinhVien_MaSVOrderByNgayDangKyDesc(testSinhVienId);
            for (DangKyHocPhan dangKy : allEnrollments) {
                dangKyHocPhanRepository.delete(dangKy);
            }

            // Delete student
            sinhVienRepository.deleteById(testSinhVienId);
        }

        if (testLopHocPhanId != null) {
            lopHocPhanRepository.deleteById(testLopHocPhanId);
        }

        if (testGiangVienId != null) {
            giangVienRepository.deleteById(testGiangVienId);
        }

        if (testMonHocId != null) {
            monHocRepository.deleteById(testMonHocId);
        }

        if (testHocKyId != null) {
            hocKyRepository.deleteById(testHocKyId);
        }

        if (testKhoaId != null) {
            khoaRepository.deleteById(testKhoaId);
        }
    }
}
