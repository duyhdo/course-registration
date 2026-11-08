package com.ptit.courseregistration.web;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaginationSupportTest {

    @Test
    void shouldUseDefaultPageAndNormalizeInvalidPageSize() {
        assertEquals(PageRequest.of(0, 10), PaginationSupport.pageable(null, null, Sort.unsorted()));
        assertEquals(PageRequest.of(0, 10), PaginationSupport.pageable(-1, 15, Sort.unsorted()));
        assertEquals(PageRequest.of(2, 20), PaginationSupport.pageable(2, 20, Sort.unsorted()));
    }

    @Test
    void shouldFetchRequestedPageWithStableSort() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"));
        AtomicReference<Pageable> requested = new AtomicReference<>();
        Page<String> result = PaginationSupport.fetchPage(1, 20, sort, pageable -> {
            requested.set(pageable);
            return new PageImpl<>(List.of("item"), pageable, 21);
        });

        assertEquals(List.of("item"), result.getContent());
        assertEquals(PageRequest.of(1, 20, sort), requested.get());
    }

    @Test
    void shouldPreserveFiltersAndBuildPaginationRange() {
        Page<Integer> page = new PageImpl<>(
                List.of(11, 12, 13), PageRequest.of(1, 10), 23);
        PaginationSupport.PaginationView view = PaginationSupport.view(page, "/admin/enrollments",
                PaginationSupport.filters("keyword", "java", "maHocKy", "HK1", "trangThai", ""));

        assertEquals(11, view.getFirstItem());
        assertEquals(13, view.getLastItem());
        assertEquals(23, view.getTotalElements());
        assertEquals(3, view.getTotalPages());
        assertEquals("/admin/enrollments?keyword=java&maHocKy=HK1&page=2&size=10",
                view.getPages().get(2).getUrl());
        assertEquals("/admin/enrollments?keyword=java&maHocKy=HK1&page=0&size=10",
                view.getPreviousUrl());
        assertEquals("/admin/enrollments?keyword=java&maHocKy=HK1&page=2&size=10",
                view.getNextUrl());
    }

    @Test
    void shouldRenderZeroRangeForEmptyResults() {
        Page<Integer> page = Page.empty(PageRequest.of(0, 10));
        PaginationSupport.PaginationView view = PaginationSupport.view(page, "/student/enrollment-history", Map.of());

        assertEquals(0, view.getTotalElements());
        assertEquals(0, view.getFirstItem());
        assertEquals(0, view.getLastItem());
        assertEquals(0, view.getTotalPages());
        assertNull(view.getPreviousUrl());
        assertNull(view.getNextUrl());
    }
}
