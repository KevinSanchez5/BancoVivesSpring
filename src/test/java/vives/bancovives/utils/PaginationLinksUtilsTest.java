package vives.bancovives.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PaginationLinksUtilsTest {

    private final PaginationLinksUtils paginationLinksUtils = new PaginationLinksUtils();

    @Test
    void createLinkHeader_withNextAndPreviousPages() {
        Page<?> mockPage = mock(Page.class);
        when(mockPage.hasNext()).thenReturn(true);
        when(mockPage.hasPrevious()).thenReturn(true);
        when(mockPage.getNumber()).thenReturn(1);
        when(mockPage.getSize()).thenReturn(10);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://example.com/items");

        String linkHeader = paginationLinksUtils.createLinkHeader(mockPage, uriBuilder);

        assertEquals(
       "<http://example.com/items?page=2&size=10>; rel=\"next\", " +
                "<http://example.com/items?page=0&size=10>; rel=\"prev\", " +
                "<http://example.com/items?page=0&size=10>; rel=\"first\", " +
                "<http://example.com/items?page=-1&size=10>; rel=\"last\"",
                linkHeader);
    }

    @Test
    void createLinkHeader_withFirstAndLastPages() {
        Page<?> mockPage = mock(Page.class);
        when(mockPage.hasNext()).thenReturn(false);
        when(mockPage.hasPrevious()).thenReturn(false);
        when(mockPage.isFirst()).thenReturn(false);
        when(mockPage.isLast()).thenReturn(false);
        when(mockPage.getNumber()).thenReturn(5);
        when(mockPage.getSize()).thenReturn(10);
        when(mockPage.getTotalPages()).thenReturn(10);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://example.com/items");

        String linkHeader = paginationLinksUtils.createLinkHeader(mockPage, uriBuilder);

        assertEquals("<http://example.com/items?page=0&size=10>; rel=\"first\", <http://example.com/items?page=9&size=10>; rel=\"last\"", linkHeader);
    }

    @Test
    void createLinkHeader_withAllLinks() {
        Page<?> mockPage = mock(Page.class);
        when(mockPage.hasNext()).thenReturn(true);
        when(mockPage.hasPrevious()).thenReturn(true);
        when(mockPage.isFirst()).thenReturn(false);
        when(mockPage.isLast()).thenReturn(false);
        when(mockPage.getNumber()).thenReturn(2);
        when(mockPage.getSize()).thenReturn(10);
        when(mockPage.getTotalPages()).thenReturn(5);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://example.com/items");

        String linkHeader = paginationLinksUtils.createLinkHeader(mockPage, uriBuilder);

        assertEquals("<http://example.com/items?page=3&size=10>; rel=\"next\", " +
                "<http://example.com/items?page=1&size=10>; rel=\"prev\", " +
                "<http://example.com/items?page=0&size=10>; rel=\"first\", " +
                "<http://example.com/items?page=4&size=10>; rel=\"last\"", linkHeader);
    }

    @Test
    void createLinkHeader_withOnlyFirstPage() {
        Page<?> mockPage = mock(Page.class);
        when(mockPage.hasNext()).thenReturn(true);
        when(mockPage.hasPrevious()).thenReturn(false);
        when(mockPage.isFirst()).thenReturn(true);
        when(mockPage.isLast()).thenReturn(false);
        when(mockPage.getNumber()).thenReturn(0);
        when(mockPage.getSize()).thenReturn(10);
        when(mockPage.getTotalPages()).thenReturn(5);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://example.com/items");

        String linkHeader = paginationLinksUtils.createLinkHeader(mockPage, uriBuilder);

        assertEquals("<http://example.com/items?page=1&size=10>; rel=\"next\", " +
                "<http://example.com/items?page=4&size=10>; rel=\"last\"", linkHeader);
    }

    @Test
    void createLinkHeader_withOnlyLastPage() {
        Page<?> mockPage = mock(Page.class);
        when(mockPage.hasNext()).thenReturn(false);
        when(mockPage.hasPrevious()).thenReturn(true);
        when(mockPage.isFirst()).thenReturn(false);
        when(mockPage.isLast()).thenReturn(true);
        when(mockPage.getNumber()).thenReturn(4);
        when(mockPage.getSize()).thenReturn(10);
        when(mockPage.getTotalPages()).thenReturn(5);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://example.com/items");

        String linkHeader = paginationLinksUtils.createLinkHeader(mockPage, uriBuilder);

        assertEquals("<http://example.com/items?page=3&size=10>; rel=\"prev\", " +
                "<http://example.com/items?page=0&size=10>; rel=\"first\"", linkHeader);
    }
}
