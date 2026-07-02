package com.helu.internship.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DateFilterInterceptor implements HandlerInterceptor {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DateFilterInterceptor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String yearStr = request.getParameter("year");
        String quarterStr = request.getParameter("quarter");

        Integer year = null;
        Integer quarter = null;

        if (yearStr != null && !yearStr.trim().isEmpty() && !"all".equalsIgnoreCase(yearStr)) {
            try {
                year = Integer.parseInt(yearStr.trim());
            } catch (NumberFormatException e) {
                // Ignore invalid numbers
            }
        }
        if (quarterStr != null && !quarterStr.trim().isEmpty() && !"all".equalsIgnoreCase(quarterStr)) {
            try {
                quarter = Integer.parseInt(quarterStr.trim());
            } catch (NumberFormatException e) {
                // Ignore invalid numbers
            }
        }

        // Set session context variables in SQL Server
        if (year != null) {
            jdbcTemplate.execute("EXEC sp_set_session_context 'selected_year', " + year + ";");
        } else {
            jdbcTemplate.execute("EXEC sp_set_session_context 'selected_year', NULL;");
        }

        if (quarter != null) {
            jdbcTemplate.execute("EXEC sp_set_session_context 'selected_quarter', " + quarter + ";");
        } else {
            jdbcTemplate.execute("EXEC sp_set_session_context 'selected_quarter', NULL;");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Reset session context to prevent connection leakage in the Hikari connection pool
        jdbcTemplate.execute("EXEC sp_set_session_context 'selected_year', NULL;");
        jdbcTemplate.execute("EXEC sp_set_session_context 'selected_quarter', NULL;");
    }
}
