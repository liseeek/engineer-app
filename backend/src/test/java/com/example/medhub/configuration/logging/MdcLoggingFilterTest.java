package com.example.medhub.configuration.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MdcLoggingFilterTest {

    private final MdcLoggingFilter filter = new MdcLoggingFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain filterChain = mock(FilterChain.class);

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should add X-Request-Id header to response")
    void shouldAddRequestIdHeaderToResponse() throws ServletException, IOException {
        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        String requestId = response.getHeader(MdcLoggingFilter.REQUEST_ID_HEADER);
        assertThat(requestId).isNotNull();
        assertThat(requestId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("Should set MDC requestId during filter execution")
    void shouldSetMdcRequestIdDuringExecution() throws ServletException, IOException {
        // Given
        doAnswer(invocation -> {
            // Verify MDC is set during filter chain execution
            String mdcValue = MDC.get(MdcLoggingFilter.REQUEST_ID_KEY);
            assertThat(mdcValue).isNotNull();
            assertThat(mdcValue).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should clear MDC after filter completes")
    void shouldClearMdcAfterFilterCompletes() throws ServletException, IOException {
        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - MDC should be cleared
        assertThat(MDC.get(MdcLoggingFilter.REQUEST_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should match X-Request-Id header with MDC value")
    void shouldMatchHeaderWithMdcValue() throws ServletException, IOException {
        // Given
        final String[] capturedMdcValue = new String[1];
        doAnswer(invocation -> {
            capturedMdcValue[0] = MDC.get(MdcLoggingFilter.REQUEST_ID_KEY);
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        String headerValue = response.getHeader(MdcLoggingFilter.REQUEST_ID_HEADER);
        assertThat(headerValue).isEqualTo(capturedMdcValue[0]);
    }
}
