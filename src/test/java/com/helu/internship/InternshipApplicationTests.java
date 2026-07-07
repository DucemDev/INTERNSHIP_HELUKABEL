package com.helu.internship;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class InternshipApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void testRlsAndLossReasons() {
        System.out.println("=== TEST LEAD DATES AND HISTORY DATES ===");
        
        String sql = "SELECT l.lead_id, l.created_date, l.status, h.changed_at " +
                     "FROM lead l " +
                     "INNER JOIN lead_item li ON l.lead_id = li.lead_id " +
                     "INNER JOIN product p ON li.product_id = p.product_id " +
                     "LEFT JOIN lead_status_history h ON l.lead_id = h.lead_id AND h.new_status = 'Lost' " +
                     "WHERE p.product_name = 'Control Cable' AND l.status = 'Lost' " +
                     "ORDER BY l.created_date";

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            System.out.println(String.format("Lead: %s | Created: %s | Status: %s | Lost At: %s",
                rs.getString("lead_id"),
                rs.getDate("created_date"),
                rs.getString("status"),
                rs.getTimestamp("changed_at")
            ));
            return null;
        });
    }

}



