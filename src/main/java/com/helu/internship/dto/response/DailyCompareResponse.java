package com.helu.internship.dto.response;

import java.math.BigDecimal;

public class DailyCompareResponse {

    private long todayTotalLeads;
    private long yesterdayTotalLeads;
    private double totalLeadsChangePercent;

    private long todayNewLeads;
    private long yesterdayNewLeads;
    private double newLeadsChangePercent;

    private BigDecimal todayRevenueWon;
    private BigDecimal yesterdayRevenueWon;
    private double revenueWonChangePercent;

    public DailyCompareResponse(
            long todayTotalLeads, long yesterdayTotalLeads, double totalLeadsChangePercent,
            long todayNewLeads, long yesterdayNewLeads, double newLeadsChangePercent,
            BigDecimal todayRevenueWon, BigDecimal yesterdayRevenueWon, double revenueWonChangePercent) {
        this.todayTotalLeads = todayTotalLeads;
        this.yesterdayTotalLeads = yesterdayTotalLeads;
        this.totalLeadsChangePercent = totalLeadsChangePercent;
        this.todayNewLeads = todayNewLeads;
        this.yesterdayNewLeads = yesterdayNewLeads;
        this.newLeadsChangePercent = newLeadsChangePercent;
        this.todayRevenueWon = todayRevenueWon;
        this.yesterdayRevenueWon = yesterdayRevenueWon;
        this.revenueWonChangePercent = revenueWonChangePercent;
    }

    public long getTodayTotalLeads() { return todayTotalLeads; }
    public long getYesterdayTotalLeads() { return yesterdayTotalLeads; }
    public double getTotalLeadsChangePercent() { return totalLeadsChangePercent; }

    public long getTodayNewLeads() { return todayNewLeads; }
    public long getYesterdayNewLeads() { return yesterdayNewLeads; }
    public double getNewLeadsChangePercent() { return newLeadsChangePercent; }

    public BigDecimal getTodayRevenueWon() { return todayRevenueWon; }
    public BigDecimal getYesterdayRevenueWon() { return yesterdayRevenueWon; }
    public double getRevenueWonChangePercent() { return revenueWonChangePercent; }
}
