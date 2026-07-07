package com.helu.internship.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Configuration
public class DataSourceWrapperConfig {

    @Bean
    public static BeanPostProcessor dataSourceBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSource && !(bean instanceof FilteredDataSource)) {
                    return new FilteredDataSource((DataSource) bean);
                }
                return bean;
            }
        };
    }

    private static class FilteredDataSource extends DelegatingDataSource {
        public FilteredDataSource(DataSource delegate) {
            super(delegate);
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection conn = super.getConnection();
            applyFilterContext(conn);
            return conn;
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            Connection conn = super.getConnection(username, password);
            applyFilterContext(conn);
            return conn;
        }

        private void applyFilterContext(Connection conn) {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String yearStr = request.getParameter("year");
                    String quarterStr = request.getParameter("quarter");

                    Integer year = null;
                    Integer quarter = null;

                    if (yearStr != null && !yearStr.trim().isEmpty() && !"all".equalsIgnoreCase(yearStr)) {
                        try {
                            year = Integer.parseInt(yearStr.trim());
                        } catch (NumberFormatException ignored) {}
                    }
                    if (quarterStr != null && !quarterStr.trim().isEmpty() && !"all".equalsIgnoreCase(quarterStr)) {
                        try {
                            quarter = Integer.parseInt(quarterStr.trim());
                        } catch (NumberFormatException ignored) {}
                    }

                    // Set session context variables in SQL Server on this connection
                    try (PreparedStatement stmt = conn.prepareStatement("EXEC sp_set_session_context 'selected_year', ?")) {
                        if (year != null) {
                            stmt.setInt(1, year);
                        } else {
                            stmt.setNull(1, java.sql.Types.INTEGER);
                        }
                        stmt.execute();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("EXEC sp_set_session_context 'selected_quarter', ?")) {
                        if (quarter != null) {
                            stmt.setInt(1, quarter);
                        } else {
                            stmt.setNull(1, java.sql.Types.INTEGER);
                        }
                        stmt.execute();
                    }
                }
            } catch (Exception e) {
                // Print to standard error but do not throw to avoid crashing the connection pool checkout
                System.err.println("Warning: failed to set SQL Server session context on connection: " + e.getMessage());
            }
        }
    }
}
