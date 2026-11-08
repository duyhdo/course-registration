package com.ptit.courseregistration.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class PaginationSupport {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final List<Integer> PAGE_SIZES = List.of(10, 20, 50);

    private PaginationSupport() {
    }

    public static Pageable pageable(Integer page, Integer size, Sort sort) {
        int pageNumber = page == null || page < 0 ? DEFAULT_PAGE : page;
        int pageSize = size == null || !PAGE_SIZES.contains(size) ? DEFAULT_SIZE : size;
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    public static <T> Page<T> fetchPage(Integer page, Integer size, Sort sort,
                                        Function<Pageable, Page<T>> fetcher) {
        Pageable pageable = pageable(page, size, sort);
        Page<T> result = fetcher.apply(pageable);
        if (result.getTotalPages() > 0 && pageable.getPageNumber() >= result.getTotalPages()) {
            return fetcher.apply(PageRequest.of(result.getTotalPages() - 1, result.getSize(), sort));
        }
        return result;
    }

    public static PaginationView view(Page<?> page, String path, Map<String, String> filters) {
        return new PaginationView(page, path, filters);
    }

    public static Map<String, String> filters(String... parameters) {
        if (parameters.length % 2 != 0) {
            throw new IllegalArgumentException("Pagination filters must be name/value pairs.");
        }
        Map<String, String> filters = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i += 2) {
            filters.put(parameters[i], parameters[i + 1]);
        }
        return filters;
    }

    public static final class PaginationView {

        private final String path;
        private final Map<String, String> filters;
        private final int number;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final int firstItem;
        private final long lastItem;
        private final List<PageLink> pages;
        private final String previousUrl;
        private final String nextUrl;

        private PaginationView(Page<?> page, String path, Map<String, String> filters) {
            this.path = path;
            this.filters = new LinkedHashMap<>();
            filters.forEach((key, value) -> {
                if (value != null && !value.isBlank()) {
                    this.filters.put(key, value);
                }
            });
            this.number = page.getNumber();
            this.size = page.getSize();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.firstItem = page.getNumberOfElements() == 0 ? 0 : page.getNumber() * page.getSize() + 1;
            this.lastItem = page.getNumberOfElements() == 0
                    ? 0 : Math.min(page.getTotalElements(), firstItem + page.getNumberOfElements() - 1L);
            this.pages = buildPageLinks();
            this.previousUrl = page.hasPrevious()
                    ? pageUrl(path, this.filters, page.getNumber() - 1, size) : null;
            this.nextUrl = page.hasNext()
                    ? pageUrl(path, this.filters, page.getNumber() + 1, size) : null;
        }

        private List<PageLink> buildPageLinks() {
            if (totalPages == 0) {
                return List.of();
            }
            int start = Math.max(0, number - 2);
            int end = Math.min(totalPages - 1, start + 4);
            start = Math.max(0, end - 4);
            List<PageLink> links = new ArrayList<>();
            for (int pageNumber = start; pageNumber <= end; pageNumber++) {
                links.add(new PageLink(pageNumber, pageUrl(path, filters, pageNumber, size),
                        pageNumber == number));
            }
            return List.copyOf(links);
        }

        private static String pageUrl(String path, Map<String, String> filters, int page, int size) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
            filters.forEach(builder::queryParam);
            return builder.queryParam("page", page)
                    .queryParam("size", size)
                    .build()
                    .encode()
                    .toUriString();
        }

        public String getPath() {
            return path;
        }

        public Map<String, String> getFilters() {
            return filters;
        }

        public int getNumber() {
            return number;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getFirstItem() {
            return firstItem;
        }

        public long getLastItem() {
            return lastItem;
        }

        public List<PageLink> getPages() {
            return pages;
        }

        public String getPreviousUrl() {
            return previousUrl;
        }

        public String getNextUrl() {
            return nextUrl;
        }

        public List<Integer> getPageSizes() {
            return PAGE_SIZES;
        }
    }

    public static final class PageLink {

        private final int number;
        private final String url;
        private final boolean active;

        private PageLink(int number, String url, boolean active) {
            this.number = number;
            this.url = url;
            this.active = active;
        }

        public int getNumber() {
            return number;
        }

        public String getUrl() {
            return url;
        }

        public boolean isActive() {
            return active;
        }
    }
}
