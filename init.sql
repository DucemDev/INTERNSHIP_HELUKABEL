-- =================================================================================
-- INTERNSHIP HELUKABEL 2026 - FINAL DATABASE SCRIPT & DATA MOCKUP (SQL SERVER 2022)
-- =================================================================================
USE
master;
GO

IF DB_ID('Internship_2026') IS NOT NULL
BEGIN
    ALTER DATABASE [Internship_2026] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [Internship_2026];
END
GO

CREATE DATABASE [Internship_2026];
GO

USE [Internship_2026];
GO

-- =================================================================================
-- 1. CREATE TABLES
-- =================================================================================
CREATE TABLE role
(role_id     INT IDENTITY(1,1) NOT NULL,
 role_name   NVARCHAR(50) NOT NULL,
 description NVARCHAR(255) NULL,
 CONSTRAINT PK_role PRIMARY KEY (role_id));
GO

CREATE TABLE [user]
(user_id
 UNIQUEIDENTIFIER
 DEFAULT NEWSEQUENTIALID() NOT NULL,
    user_code VARCHAR (50) NOT NULL UNIQUE,
    password VARCHAR (255) NOT NULL,
    full_name NVARCHAR (100) NOT NULL,
    email VARCHAR (100) NOT NULL UNIQUE,
    role_id INT NOT NULL,
    is_active BIT DEFAULT 1 NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT PK_user PRIMARY KEY (user_id),
    CONSTRAINT FK_user_role FOREIGN KEY (role_id) REFERENCES role (role_id));
GO

CREATE TABLE lead_source
(source_id   VARCHAR(50) NOT NULL,
 source_name NVARCHAR(100) NOT NULL,
 source_type VARCHAR(20) NOT NULL, -- Thuộc tính mới thêm vào

 CONSTRAINT PK_lead_source PRIMARY KEY (source_id),
    -- Ràng buộc CHECK để đảm bảo dữ liệu luôn chuẩn hóa, chỉ nhận 1 trong 2 giá trị này
 CONSTRAINT CHK_source_type CHECK (source_type IN ('IN BOUND', 'OUT BOUND')));
GO

CREATE TABLE product
(product_id   VARCHAR(50) NOT NULL,
 product_name NVARCHAR(100) NOT NULL,
 CONSTRAINT PK_product PRIMARY KEY (product_id));
GO

CREATE TABLE lead
(lead_id         VARCHAR(50)      NOT NULL,
 created_date    DATE             NOT NULL,
 full_name       NVARCHAR(100) NOT NULL,
 phone_number    VARCHAR(20) NULL,
 email           VARCHAR(100) NULL,
 account         NVARCHAR(150) NOT NULL,
 industry_type   NVARCHAR(100) NOT NULL,
 customer_group  NVARCHAR(50) NOT NULL,
 customer_role   NVARCHAR(50) NOT NULL,
 location        NVARCHAR(100) NOT NULL,
 region          NVARCHAR(50) NOT NULL,
 status          NVARCHAR(50) NOT NULL,
 cost            DECIMAL(18, 2)   NOT NULL,
 loss_reason     NVARCHAR(100) NULL,
 business_result DECIMAL(18, 2) NULL,
 product_name    NVARCHAR(100) NULL,
 source_name     NVARCHAR(100) NULL,
 source_id       VARCHAR(50)      NOT NULL,
 user_id         UNIQUEIDENTIFIER NOT NULL,
 CONSTRAINT PK_lead PRIMARY KEY (lead_id),
 CONSTRAINT FK_lead_source FOREIGN KEY (source_id) REFERENCES lead_source (source_id),
 CONSTRAINT FK_lead_user FOREIGN KEY (user_id) REFERENCES [user](user_id),
 CONSTRAINT CHK_lead_status CHECK (status IN ('New', 'Contacted', 'Qualified', 'Proposal Sent', 'In Negotiation', 'Won', 'Lost', 'Disqualified')),
 CONSTRAINT CHK_loss_reason CHECK (loss_reason IN ('Price', 'Timing', 'No Budget', 'Product Fit', 'Competitor', 'No Decision')));
GO

CREATE TABLE lead_item
(item_id          BIGINT IDENTITY(1,1) NOT NULL,
 lead_id          VARCHAR(50)    NOT NULL,
 product_id       VARCHAR(50)    NOT NULL,
 quantity         INT            NOT NULL DEFAULT 1,
 expected_revenue DECIMAL(18, 2) NOT NULL,
 CONSTRAINT PK_lead_item PRIMARY KEY (item_id),
 CONSTRAINT FK_lead_item_lead FOREIGN KEY (lead_id) REFERENCES lead (lead_id) ON DELETE CASCADE,
 CONSTRAINT FK_lead_item_product FOREIGN KEY (product_id) REFERENCES product (product_id));
GO

CREATE TABLE lead_status_history
(history_id         BIGINT IDENTITY(1,1) NOT NULL,
 lead_id            VARCHAR(50)                     NOT NULL,
 old_status         NVARCHAR(50) NULL,
 new_status         NVARCHAR(50) NOT NULL,
 changed_at         DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
 changed_by_user_id UNIQUEIDENTIFIER NULL,
 note               NVARCHAR(255) NULL,
 CONSTRAINT PK_lead_status_history PRIMARY KEY (history_id),
 CONSTRAINT FK_history_lead FOREIGN KEY (lead_id) REFERENCES lead (lead_id) ON DELETE CASCADE,
 CONSTRAINT FK_history_user FOREIGN KEY (changed_by_user_id) REFERENCES [user](user_id));
GO

CREATE TABLE activity_log
(log_id      BIGINT IDENTITY(1,1) NOT NULL,
 user_id     UNIQUEIDENTIFIER NULL,
 action_type VARCHAR(50)                     NOT NULL,
 entity_type VARCHAR(50)                     NOT NULL,
 entity_id   VARCHAR(50) NULL,
 description NVARCHAR(MAX) NULL,
 created_at  DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
 CONSTRAINT PK_activity_log PRIMARY KEY (log_id),
 CONSTRAINT FK_activity_log_user FOREIGN KEY (user_id) REFERENCES [user](user_id));
GO

CREATE TABLE notification
(
    id          UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    user_id     UNIQUEIDENTIFIER NOT NULL,
    title       NVARCHAR(200) NOT NULL,
    message     NVARCHAR(1000) NOT NULL,
    is_read     BIT DEFAULT 0 NOT NULL,
    type        VARCHAR(50) NOT NULL,
    link        VARCHAR(255) NULL,
    created_at  DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    CONSTRAINT PK_notification PRIMARY KEY (id),
    CONSTRAINT FK_notification_user FOREIGN KEY (user_id) REFERENCES [user](user_id) ON DELETE CASCADE
);
GO

CREATE TABLE sales_target
(target_id      BIGINT IDENTITY(1,1) NOT NULL,
 user_id        UNIQUEIDENTIFIER                NOT NULL, -- Seller được giao chỉ tiêu
 period_month   INT                             NOT NULL, -- Tháng (1-12)
 period_year    INT                             NOT NULL, -- Năm (VD: 2026)
 revenue_target DECIMAL(18, 2)                  NOT NULL, -- Chỉ tiêu doanh thu (VD: 5,000,000,000)
 created_by     UNIQUEIDENTIFIER                NOT NULL, -- Admin giao chỉ tiêu
 created_at     DATETIME2 DEFAULT SYSDATETIME() NOT NULL,

 CONSTRAINT PK_sales_target PRIMARY KEY (target_id),
 CONSTRAINT FK_target_user FOREIGN KEY (user_id) REFERENCES [user](user_id),
 CONSTRAINT FK_target_creator FOREIGN KEY (created_by) REFERENCES [user](user_id),
    -- Đảm bảo 1 nhân viên chỉ có 1 target trong 1 tháng của 1 năm
 CONSTRAINT UQ_sales_target_period UNIQUE (user_id, period_month, period_year),
    -- Đảm bảo dữ liệu tháng hợp lệ
 CONSTRAINT CHK_target_month CHECK (period_month >= 1 AND period_month <= 12));
GO

-- =================================================================================
-- CREATE TABLE: lead_bant_point (Mô hình chấm điểm B-A-N-T)
-- =================================================================================
CREATE TABLE lead_bant_point
(lead_id     VARCHAR(50) NOT NULL,
 budget      INT NOT NULL DEFAULT 0,
 authority   INT NOT NULL DEFAULT 0,
 need        INT NOT NULL DEFAULT 0,
 timeline    INT NOT NULL DEFAULT 0,

    -- Computed column: Tự động tính tổng điểm và lưu vật lý xuống đĩa (PERSISTED)
 total_score AS (budget + authority + need + timeline) PERSISTED,

 CONSTRAINT PK_lead_bant_point PRIMARY KEY (lead_id),
 CONSTRAINT FK_bant_lead FOREIGN KEY (lead_id) REFERENCES lead (lead_id) ON DELETE CASCADE,

    -- Ràng buộc dữ liệu: Đảm bảo điểm nhập vào nằm trong khoảng 0-100
 CONSTRAINT CHK_bant_score CHECK (budget >= 0 AND budget <= 25 AND
                                  authority >= 0 AND authority <= 25 AND
                                  need >= 0 AND need <= 25 AND
                                  timeline >= 0 AND timeline <= 25));
GO




-- =================================================================================
-- COMPANY TARGETS
-- =================================================================================
CREATE TABLE company_target
(
    target_id      BIGINT IDENTITY(1,1) NOT NULL,
    target_type    VARCHAR(20) NOT NULL,
    period_quarter INT NULL,
    period_year    INT NOT NULL,
    revenue_target DECIMAL(18, 2) NOT NULL,
    created_by     UNIQUEIDENTIFIER NOT NULL,
    created_at     DATETIME2 DEFAULT SYSDATETIME() NOT NULL,

    CONSTRAINT PK_company_target PRIMARY KEY (target_id),
    CONSTRAINT FK_company_target_creator FOREIGN KEY (created_by) REFERENCES [user] (user_id),
    CONSTRAINT CHK_company_target_type CHECK (target_type IN ('YEARLY', 'QUARTERLY')),
    CONSTRAINT CHK_company_target_logic CHECK
        (
        (target_type = 'YEARLY' AND period_quarter IS NULL)
            OR (target_type = 'QUARTERLY' AND period_quarter BETWEEN 1 AND 4)
        )
);
GO

-- Thêm Index để Power BI dễ dàng truy vấn theo tháng/năm
CREATE
NONCLUSTERED INDEX IX_sales_target_period ON sales_target (period_year, period_month);
GO

-- 2. Chèn dữ liệu mẫu (Mock Data)
-- Tran Quoc Minh

-- =================================================================================
-- 2. CREATE INDEXES
-- =================================================================================
CREATE
NONCLUSTERED INDEX IX_lead_source_id ON lead (source_id);
CREATE
NONCLUSTERED INDEX IX_lead_user_id ON lead (user_id);
CREATE
NONCLUSTERED INDEX IX_lead_created_date ON lead (created_date);
CREATE
NONCLUSTERED INDEX IX_lead_status ON lead (status);
CREATE
NONCLUSTERED INDEX IX_lead_analytics_covering ON lead (source_id, region, status) INCLUDE (cost, business_result);

CREATE
NONCLUSTERED INDEX IX_lead_item_lead_id ON lead_item (lead_id);
CREATE
NONCLUSTERED INDEX IX_lead_item_product_id ON lead_item (product_id);

CREATE
NONCLUSTERED INDEX IX_status_history_lead_id ON lead_status_history (lead_id);
CREATE
NONCLUSTERED INDEX IX_status_history_changed_at ON lead_status_history (changed_at);
CREATE
NONCLUSTERED INDEX IX_notification_unread ON notification (user_id, is_read);
CREATE
NONCLUSTERED INDEX IX_activity_log_user_date ON activity_log (user_id, created_at);
CREATE UNIQUE NONCLUSTERED INDEX IX_user_email ON [user] (email);
GO

-- =================================================================================
-- 3. INSERT MOCK DATA
-- =================================================================================

-- 3.1 INSERT ROLES



CREATE UNIQUE NONCLUSTERED INDEX UQ_company_target_period
    ON company_target (period_year, target_type, period_quarter);
GO
INSERT INTO role (role_name, description) VALUES
('Admin', N'Quản trị viên hệ thống'),
('Seller', N'Nhân viên kinh doanh');
GO

-- 3.2 INSERT USERS (ADMIN & SELLERS)
INSERT INTO [user] (user_id, user_code, password, full_name, email, role_id, is_active) VALUES
('21cf3ed1-c2eb-410c-8098-bf3020e06991', 'AD001', '$2a$10$KYb.bgu6TVIbiSgxsmGPa.8cwvySo1R0GX/F.sICYF0PwqjPuULGa', N'Administrator', 'admin@helukabel.vn', 1, 1),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 'NV001', '$2a$10$KYb.bgu6TVIbiSgxsmGPa.8cwvySo1R0GX/F.sICYF0PwqjPuULGa', N'Vo Duc Thanh', 'thanh0@helukabel.vn', 2, 1),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 'NV002', '$2a$10$KYb.bgu6TVIbiSgxsmGPa.8cwvySo1R0GX/F.sICYF0PwqjPuULGa', N'Nguyen Thi Thu Huong', 'seller_huong@helukabel.vn', 2, 1),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 'NV003', '$2a$10$KYb.bgu6TVIbiSgxsmGPa.8cwvySo1R0GX/F.sICYF0PwqjPuULGa', N'Tran Quoc Minh', 'seller_minh@helukabel.vn', 2, 1),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 'NV004', '$2a$10$KYb.bgu6TVIbiSgxsmGPa.8cwvySo1R0GX/F.sICYF0PwqjPuULGa', N'Pham Thi Ngoc Lan', 'seller_lan@helukabel.vn', 2, 1),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 'NV005', '$2a$10$KYb.bgu6TVIbiSgxsmGPa.8cwvySo1R0GX/F.sICYF0PwqjPuULGa', N'Le Van Hung', 'seller_hung@helukabel.vn', 2, 1),
('aa512e88-565a-44bb-84f6-612ecc49e949', 'NV006', '$2a$10$KYb.bgu6TVIbiSgxsmGPa.8cwvySo1R0GX/F.sICYF0PwqjPuULGa', N'Hoang Minh Tuan', 'seller_tuan@helukabel.vn', 2, 1);
GO


-- Giao chỉ tiêu Target cho các năm 2024, 2025, 2026 với tỷ trọng Quý phân bổ thực tế (Q1: 15%, Q2: 25%, Q3: 20%, Q4: 40%)
INSERT INTO sales_target (user_id, period_month, period_year, revenue_target, created_by)
VALUES
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 3, 2024, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 6, 2024, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 9, 2024, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 12, 2024, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 3, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 6, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 9, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 12, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 3, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 6, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 9, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 12, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 3, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 6, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 9, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 12, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 3, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 6, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 9, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 12, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 3, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 6, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 9, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 12, 2024, 3000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 3, 2025, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 6, 2025, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 9, 2025, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 12, 2025, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 3, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 6, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 9, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 12, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 3, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 6, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 9, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 12, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 3, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 6, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 9, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 12, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 3, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 6, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 9, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 12, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 3, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 6, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 9, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 12, 2025, 4000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 3, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 6, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 9, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('d3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', 12, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 3, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 6, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 9, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f3f1ab23-29da-4ea3-8377-befc37b314f1', 12, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 3, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 6, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 9, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('45f0d7fa-7580-4d9e-98e2-495aa86f75c9', 12, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 3, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 6, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 9, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('655612cb-fc04-4fa8-a2de-87a6a55d30c7', 12, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 3, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 6, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 9, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('f1460724-4d7f-4796-bca7-50bf1ab09811', 12, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 3, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 6, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 9, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
('aa512e88-565a-44bb-84f6-612ecc49e949', 12, 2026, 5000000000, '21cf3ed1-c2eb-410c-8098-bf3020e06991');
GO


-- 3.3 INSERT LEAD SOURCES
INSERT INTO lead_source (source_id, source_name, source_type) VALUES
('SRC01', N'Industry Publication', 'OUT BOUND'), -- Quảng cáo báo chí ngành
('SRC02', N'Cold Call',            'OUT BOUND'), -- Gọi điện tiếp cận trực tiếp
('SRC03', N'Exhibition',           'OUT BOUND'), -- Gian hàng triển lãm
('SRC04', N'Referral',             'IN BOUND'),  -- Khách hàng tự giới thiệu
('SRC05', N'Partner Introduction', 'IN BOUND'),  -- Đối tác giới thiệu sang
('SRC06', N'LinkedIn',             'OUT BOUND'), -- Sales tự đi săn trên mạng xã hội
('SRC07', N'Website Inquiry',      'IN BOUND'),  -- Khách tự để lại thông tin trên web
('SRC08', N'Trade Show',           'OUT BOUND'), -- Hội chợ thương mại
('SRC09', N'Email Campaign',       'OUT BOUND'), -- Gửi mail hàng loạt tiếp cận
('SRC10', N'Webinar',              'IN BOUND');  -- Khách đăng ký xem thảo luận trực tuyến
GO

-- 3.4 INSERT PRODUCTS
INSERT INTO product (product_id, product_name) VALUES
('PRD01', N'Control Cable'),
('PRD02', N'Drag-chain Cable'),
('PRD03', N'Data/Signal Cable'),
('PRD04', N'Power Cable'),
('PRD05', N'Special/Custom'),
('PRD06', N'Fiber Optic');
GO

-- 3.5 INSERT LEADS (WITHOUT product_id)
INSERT INTO lead (lead_id, created_date, full_name, account, industry_type, customer_group, customer_role, location, region, status, cost, loss_reason, business_result, source_id, user_id) VALUES

('L-2026-0001', '2026-04-09', N'Dang Quoc Viet', N'VinFast Automotive', N'Automotive & EV', N'Industry', N'Manufacturer', N'Can Tho', N'South', N'New', 1917000, NULL, NULL, 'SRC01', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0002', '2025-04-17', N'Le Quoc Viet', N'VinFast Automotive', N'Automotive & EV', N'Industry', N'Manufacturer', N'Quang Ninh', N'North', N'Won', 3051000, NULL, 9450000000.00, 'SRC01', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0003', '2026-01-05', N'Huynh Hong Huong', N'Samsung Electronics VN', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Tay Ninh', N'South', N'Won', 486000, NULL, 216000000.00, 'SRC02', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0004', '2024-10-30', N'Phung Huu Cuong', N'ABB Vietnam', N'Industrial Machinery', N'Energy', N'Manufacturer', N'Quang Nam', N'Central', N'In Negotiation', 4455000, NULL, 9716665527.83, 'SRC03', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0005', '2026-02-16', N'Phan Thi Cuong', N'Festo Vietnam', N'Industrial Machinery', N'Industry', N'Trading', N'Bac Ninh', N'North', N'Contacted', 216000, NULL, NULL, 'SRC04', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0006', '2024-11-09', N'Huynh Minh Lan', N'Eco Constructions', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Vung Tau', N'South', N'Lost', 3591000, N'Price', 21330936052.11, 'SRC03', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0007', '2026-04-12', N'Ly Hong Kiet', N'Pacific Corporation', N'Renewable Energy', N'Penetration', N'Contractor', N'Quy Nhon', N'Central', N'New', 405000, NULL, NULL, 'SRC04', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0008', '2026-03-21', N'Tran Minh Thuy', N'Piaggio Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Ho Chi Minh City', N'South', N'Qualified', 567000, NULL, 3570677554.27, 'SRC02', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0009', '2026-04-03', N'Hoang Huu Nam', N'CC1 Corporation', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Nam Dinh', N'North', N'New', 621000, NULL, NULL, 'SRC05', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0010', '2025-02-03', N'Vo Duc Thu', N'Eco Constructions', N'Construction & Infrastructure', N'Energy', N'Contractor', N'Hai Duong', N'North', N'Won', 1890000, NULL, 2025000000.00, 'SRC06', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0011', '2026-04-18', N'Ngo Ngoc Ha', N'Minth Automotive', N'Automotive & EV', N'Industry', N'Manufacturer', N'Hanoi', N'North', N'New', 4266000, NULL, NULL, 'SRC03', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0012', '2026-02-22', N'Bui Hong Quang', N'Foxconn Vietnam', N'Industrial Machinery', N'Industry', N'Trading', N'Can Tho', N'South', N'Contacted', 1404000, NULL, NULL, 'SRC07', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0013', '2026-03-04', N'Dinh Thanh', N'Suzuki Vietnam', N'Automotive & EV', N'Industry', N'Trading', N'Quy Nhon', N'Central', N'Proposal Sent', 1620000, NULL, 16569228011.58, 'SRC01', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0014', '2026-02-01', N'Pham Minh Tai', N'Coteccons Construction', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Ho Chi Minh City', N'South', N'Proposal Sent', 2997000, NULL, 31543957654.77, 'SRC03', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0015', '2024-09-29', N'Hoang Duc Hoa', N'Panasonic Automotive VN', N'Automotive & EV', N'Penetration', N'Manufacturer', N'Khanh Hoa', N'Central', N'Lost', 621000, N'Price', 5556299853.83, 'SRC05', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0016', '2026-02-16', N'Ngo Minh Yen', N'Phu My Wind Power', N'Renewable Energy', N'Penetration', N'Contractor', N'Hung Yen', N'North', N'Proposal Sent', 189000, NULL, 1371256706.45, 'SRC04', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0017', '2026-02-04', N'Vo Van Hung', N'PC1 Renewable', N'Renewable Energy', N'Energy', N'Manufacturer', N'Bac Ninh', N'North', N'Disqualified', 5130000, NULL, NULL, 'SRC08', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0018', '2025-03-28', N'Cao Ngoc Tuan', N'Gelex Energy', N'Renewable Energy', N'Energy', N'Contractor', N'Ho Chi Minh City', N'South', N'Won', 675000, NULL, 5332500000.00, 'SRC05', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0019', '2024-08-23', N'Pham Hong Lan', N'Super Energy Corp', N'Renewable Energy', N'Energy', N'Contractor', N'Can Tho', N'South', N'Lost', 4482000, N'Price', 39620618251.91, 'SRC08', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0020', '2025-12-22', N'Ngo Minh Long', N'Thaco Auto Group', N'Automotive & EV', N'Industry', N'Manufacturer', N'Nha Trang', N'Central', N'Won', 1539000, NULL, 3240000000.00, 'SRC06', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0021', '2024-06-14', N'Ly Long', N'Bamboo Capital Energy', N'Renewable Energy', N'Industry', N'Trading', N'Tien Giang', N'South', N'Lost', 2457000, N'Price', 16868318730.03, 'SRC08', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0022', '2025-06-26', N'Duong Ngoc Hoa', N'Pacific Corporation', N'Renewable Energy', N'Energy', N'Contractor', N'Hai Phong', N'North', N'Won', 1458000, NULL, 2025000000.00, 'SRC06', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0023', '2026-03-23', N'Bui Hong Hien', N'Yaskawa Vietnam', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Quang Ninh', N'North', N'New', 891000, NULL, NULL, 'SRC07', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0024', '2024-12-02', N'Tran Van Thao', N'Bosch Vietnam Automotive', N'Automotive & EV', N'Industry', N'Trading', N'Khanh Hoa', N'Central', N'Lost', 891000, N'Timing', 7729165760.77, 'SRC06', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0025', '2026-04-09', N'Le Thi Dung', N'Hyundai Thanh Cong', N'Automotive & EV', N'Industry', N'Trading', N'Ho Chi Minh City', N'South', N'New', 621000, NULL, NULL, 'SRC04', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0026', '2024-11-29', N'Cao Linh', N'Lear Corporation VN', N'Automotive & EV', N'Penetration', N'Manufacturer', N'Long An', N'South', N'Won', 594000, NULL, 324000000.00, 'SRC02', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0027', '2024-11-21', N'Do Thi Anh', N'Yaskawa Vietnam', N'Industrial Machinery', N'Industry', N'Trading', N'Hai Duong', N'North', N'In Negotiation', 3888000, NULL, 3886666211.13, 'SRC03', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0028', '2026-02-06', N'Le Quoc Ngoc', N'Licogi 16 Energy', N'Renewable Energy', N'Energy', N'Contractor', N'Quy Nhon', N'Central', N'Qualified', 1593000, NULL, 2683358831.37, 'SRC07', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0029', '2025-04-15', N'Bui Thi Nam', N'Panasonic Automotive VN', N'Automotive & EV', N'Industry', N'Trading', N'Can Tho', N'South', N'Won', 2133000, NULL, 9450000000.00, 'SRC01', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0030', '2026-01-30', N'Phan Huu Thu', N'Bac Phuong Solar', N'Renewable Energy', N'Energy', N'Trading', N'Hai Phong', N'North', N'Proposal Sent', 5346000, NULL, 23366356962.30, 'SRC03', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0031', '2025-12-24', N'Do Duc Hai', N'VinFast Automotive', N'Automotive & EV', N'Industry', N'Manufacturer', N'Long An', N'South', N'Won', 135000, NULL, 324000000.00, 'SRC09', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0032', '2024-12-17', N'Luu Phuc', N'Denso Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Hue', N'Central', N'In Negotiation', 5265000, NULL, 30056751527.72, 'SRC08', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0033', '2026-03-20', N'Ho Trang', N'Super Energy Corp', N'Renewable Energy', N'Penetration', N'Contractor', N'Hung Yen', N'North', N'Qualified', 1107000, NULL, 6605694023.56, 'SRC06', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0034', '2024-09-08', N'Do Hong Dung', N'Hyundai Thanh Cong', N'Automotive & EV', N'Industry', N'Trading', N'Hai Duong', N'North', N'Lost', 162000, N'No Budget', 392146418.69, 'SRC09', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0035', '2025-01-02', N'Ly Minh Tai', N'Xuan Thien Group', N'Renewable Energy', N'Energy', N'Trading', N'Hung Yen', N'North', N'Won', 4428000, NULL, 1647000000.00, 'SRC08', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0036', '2024-09-02', N'Ho Thuy', N'Yamaha Motor Vietnam', N'Automotive & EV', N'Energy', N'Trading', N'Da Nang', N'Central', N'Lost', 2538000, N'Product Fit', 26293885807.13, 'SRC08', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0037', '2024-09-10', N'Luu Huu Cuong', N'Goertek Vina', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Can Tho', N'South', N'Lost', 756000, N'Competitor', 4372053359.63, 'SRC02', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0038', '2025-12-18', N'Luu Quoc Hai', N'Minth Automotive', N'Automotive & EV', N'Industry', N'Manufacturer', N'Khanh Hoa', N'Central', N'Disqualified', 3969000, NULL, NULL, 'SRC01', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0039', '2026-02-12', N'Duong Thi Huong', N'Licogi 16 Energy', N'Renewable Energy', N'Energy', N'Contractor', N'Hai Duong', N'North', N'Lost', 1404000, N'Price', 18858123879.71, 'SRC07', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0040', '2026-02-10', N'Ngo Huu Hien', N'Masterise Homes', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Quang Nam', N'Central', N'Contacted', 1863000, NULL, NULL, 'SRC06', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0041', '2024-05-12', N'Nguyen Huu Huy', N'CotecLand', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Binh Duong', N'South', N'Lost', 783000, N'Competitor', 6378290498.23, 'SRC02', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0042', '2026-03-23', N'Vu Duc Khoa', N'Vietnam Sun Energy', N'Renewable Energy', N'Energy', N'Contractor', N'Binh Duong', N'South', N'New', 459000, NULL, NULL, 'SRC02', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0043', '2025-08-03', N'Vo Quoc Phuc', N'Phu My Wind Power', N'Renewable Energy', N'Industry', N'Contractor', N'Binh Dinh', N'Central', N'Won', 918000, NULL, 3456000000.00, 'SRC05', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0044', '2025-04-28', N'Vu Hong Dung', N'Truong Thanh Solar', N'Renewable Energy', N'Energy', N'Contractor', N'Tay Ninh', N'South', N'Won', 5346000, NULL, 7411500000.00, 'SRC03', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0045', '2024-09-16', N'Ly Duc Hong', N'Super Energy Corp', N'Renewable Energy', N'Energy', N'Trading', N'Binh Duong', N'South', N'Lost', 1215000, N'Timing', 1565908907.38, 'SRC02', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0046', '2026-03-23', N'Mai Hong Hien', N'REE Mechanical', N'Construction & Infrastructure', N'Energy', N'Contractor', N'Binh Dinh', N'Central', N'New', 1512000, NULL, NULL, 'SRC10', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0047', '2026-03-11', N'Vu Quoc Kiet', N'SMC Manufacturing VN', N'Industrial Machinery', N'Industry', N'Trading', N'Hung Yen', N'North', N'Contacted', 1107000, NULL, NULL, 'SRC07', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0048', '2026-02-02', N'Hoang Quoc Phuong', N'Ford Vietnam', N'Automotive & EV', N'Energy', N'Contractor', N'Bac Ninh', N'North', N'In Negotiation', 486000, NULL, 4617624469.54, 'SRC09', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0049', '2026-02-08', N'Tran Quoc Trang', N'Fecon Corporation', N'Construction & Infrastructure', N'Energy', N'Contractor', N'Long An', N'South', N'Proposal Sent', 5103000, NULL, 51462702373.11, 'SRC08', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0050', '2025-10-31', N'Nguyen Minh Tai', N'An Phong Construction', N'Construction & Infrastructure', N'Penetration', N'Trading', N'Tien Giang', N'South', N'Disqualified', 324000, NULL, NULL, 'SRC09', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0051', '2024-10-24', N'Vu Van Phuong', N'Fanuc Vietnam', N'Industrial Machinery', N'Industry', N'Contractor', N'Quang Nam', N'Central', N'In Negotiation', 1593000, NULL, 3382318627.15, 'SRC06', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0052', '2025-07-29', N'Luu Van Khoa', N'Minth Automotive', N'Automotive & EV', N'Industry', N'Manufacturer', N'Nha Trang', N'Central', N'Won', 891000, NULL, 1350000000.00, 'SRC06', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0053', '2026-01-18', N'Do Minh Phuong', N'Green Yellow Vietnam', N'Renewable Energy', N'Energy', N'Trading', N'Quang Ngai', N'Central', N'Contacted', 675000, NULL, NULL, 'SRC07', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0054', '2025-08-07', N'Vo Thi Cuong', N'Denso Vietnam', N'Automotive & EV', N'Energy', N'Trading', N'Quang Ninh', N'North', N'Won', 4293000, NULL, 445500000.00, 'SRC08', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0055', '2026-02-04', N'Phan Ngoc Hai', N'Continental Tires VN', N'Automotive & EV', N'Energy', N'Manufacturer', N'Binh Duong', N'South', N'Contacted', 3537000, NULL, NULL, 'SRC03', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0056', '2026-01-11', N'Nguyen Huu Quyen', N'Continental Tires VN', N'Automotive & EV', N'Energy', N'Trading', N'Quang Nam', N'Central', N'Lost', 189000, N'Timing', 911396718.40, 'SRC04', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0057', '2026-01-24', N'Chu Huu Minh', N'Super Energy Corp', N'Renewable Energy', N'Energy', N'Contractor', N'Hai Phong', N'North', N'Disqualified', 864000, NULL, NULL, 'SRC02', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0058', '2024-11-24', N'Mai Minh Anh', N'Intel Products VN', N'Industrial Machinery', N'Penetration', N'Trading', N'Hai Duong', N'North', N'Proposal Sent', 432000, NULL, 2237777515.50, 'SRC09', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0059', '2026-01-14', N'Duong Duc Ha', N'Central Cons Group', N'Construction & Infrastructure', N'Penetration', N'Contractor', N'Ba Ria', N'South', N'Proposal Sent', 3267000, NULL, 17372719639.05, 'SRC01', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0060', '2026-03-28', N'Vo Thi Kiet', N'Siemens Vietnam', N'Industrial Machinery', N'Penetration', N'Manufacturer', N'Nha Trang', N'Central', N'New', 783000, NULL, NULL, 'SRC05', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0061', '2026-01-23', N'Vu Minh Son', N'Samsung Electronics VN', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Nam Dinh', N'North', N'Proposal Sent', 621000, NULL, 3972274738.41, 'SRC02', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0062', '2024-07-24', N'Nguyen Thanh', N'Bosch Rexroth VN', N'Industrial Machinery', N'Industry', N'Trading', N'Binh Dinh', N'Central', N'Lost', 2430000, N'Price', 22003027724.18, 'SRC01', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0063', '2024-10-20', N'Nguyen Thi Huy', N'CC1 Corporation', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Bac Giang', N'North', N'Lost', 567000, N'Competitor', 4412650997.97, 'SRC05', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0064', '2026-03-23', N'Truong Minh Lan', N'Fanuc Vietnam', N'Industrial Machinery', N'Energy', N'Manufacturer', N'Nam Dinh', N'North', N'Contacted', 216000, NULL, NULL, 'SRC04', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0065', '2024-11-12', N'Truong Duc Thanh', N'Foxconn Vietnam', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Can Tho', N'South', N'Lost', 918000, N'Competitor', 5354426981.87, 'SRC05', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0066', '2025-12-10', N'Phan Huu Viet', N'Foxconn Vietnam', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Long An', N'South', N'Won', 2052000, NULL, 135000000.00, 'SRC01', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0067', '2026-02-04', N'Dinh Ngoc Long', N'Luxshare-ICT Vietnam', N'Industrial Machinery', N'Penetration', N'Manufacturer', N'Binh Dinh', N'Central', N'Disqualified', 1836000, NULL, NULL, 'SRC10', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0068', '2025-08-24', N'Vu Thi Hung', N'Yamaha Motor Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Dong Nai', N'South', N'Won', 2673000, NULL, 16632000000.00, 'SRC08', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0069', '2026-02-09', N'Pham Trang', N'Piaggio Vietnam', N'Automotive & EV', N'Industry', N'Trading', N'Hung Yen', N'North', N'Disqualified', 2916000, NULL, NULL, 'SRC01', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0070', '2026-01-25', N'Luu Hong Tai', N'Hung Thinh Corp', N'Construction & Infrastructure', N'Penetration', N'Contractor', N'Can Tho', N'South', N'Contacted', 3861000, NULL, NULL, 'SRC03', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0071', '2024-03-20', N'Huynh Quoc Son', N'Intel Products VN', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Quang Ngai', N'Central', N'Lost', 2268000, N'Timing', 15383382035.38, 'SRC10', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0072', '2024-05-15', N'Vo Huu Huy', N'LG Display Vietnam', N'Industrial Machinery', N'Energy', N'Manufacturer', N'Vung Tau', N'South', N'Lost', 351000, N'No Decision', 3279709211.56, 'SRC04', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0073', '2025-09-02', N'Pham Viet', N'Ecotech Vietnam', N'Renewable Energy', N'Energy', N'Trading', N'Tien Giang', N'South', N'Won', 918000, NULL, 945000000.00, 'SRC06', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0074', '2026-03-29', N'Pham Ngoc Tai', N'Yaskawa Vietnam', N'Industrial Machinery', N'Industry', N'Trading', N'Khanh Hoa', N'Central', N'New', 3321000, NULL, NULL, 'SRC01', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0075', '2025-05-02', N'Do Huu Binh', N'Lear Corporation VN', N'Automotive & EV', N'Industry', N'Trading', N'Tien Giang', N'South', N'Lost', 756000, N'Timing', 2115146042.49, 'SRC05', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0076', '2025-12-01', N'Tran Duc Hoa', N'Festo Vietnam', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Nam Dinh', N'North', N'In Negotiation', 1809000, NULL, 5727703613.28, 'SRC06', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0077', '2026-03-19', N'Vu Minh Trang', N'Delta Construction', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Vinh Phuc', N'North', N'Contacted', 756000, NULL, NULL, 'SRC02', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0078', '2025-08-16', N'Truong Van Hong', N'Schneider Electric VN', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Hai Duong', N'North', N'Lost', 135000, N'No Budget', 656658275.87, 'SRC09', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0079', '2024-12-23', N'Tran Quoc Ha', N'Fecon Corporation', N'Construction & Infrastructure', N'Energy', N'Contractor', N'Dong Nai', N'South', N'Won', 2376000, NULL, 945000000.00, 'SRC10', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0080', '2026-01-23', N'Ly Ngoc Minh', N'Phu My Wind Power', N'Renewable Energy', N'Industry', N'Contractor', N'Ba Ria', N'South', N'In Negotiation', 270000, NULL, 2297813644.23, 'SRC04', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0081', '2025-04-24', N'Duong Thi Viet', N'Panasonic Automotive VN', N'Automotive & EV', N'Penetration', N'Trading', N'Nha Trang', N'Central', N'Lost', 3483000, N'Competitor', 8759143838.30, 'SRC03', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0082', '2025-11-12', N'Chu Huu Trang', N'SMC Manufacturing VN', N'Industrial Machinery', N'Penetration', N'Trading', N'Hung Yen', N'North', N'Won', 2025000, NULL, 540000000.00, 'SRC10', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0083', '2025-11-22', N'Dang Hong Huong', N'Bamboo Capital Energy', N'Renewable Energy', N'Penetration', N'Contractor', N'Tien Giang', N'South', N'In Negotiation', 3132000, NULL, 17007088939.77, 'SRC08', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0084', '2026-03-09', N'Ly Hoa', N'Central Cons Group', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Quy Nhon', N'Central', N'Contacted', 513000, NULL, NULL, 'SRC09', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0085', '2025-11-21', N'Luu Hong Viet', N'TTC Energy', N'Renewable Energy', N'Penetration', N'Contractor', N'Da Nang', N'Central', N'Lost', 2295000, N'Competitor', 7879178500.01, 'SRC10', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0086', '2025-02-19', N'Cao Duc Nhung', N'Cofico', N'Construction & Infrastructure', N'Penetration', N'Contractor', N'Ho Chi Minh City', N'South', N'Won', 1134000, NULL, 3240000000.00, 'SRC02', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0087', '2026-02-02', N'Ly Van Phuc', N'Nidec Tosok', N'Automotive & EV', N'Industry', N'Manufacturer', N'Thai Nguyen', N'North', N'Lost', 5670000, N'Price', 26030988463.84, 'SRC03', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0088', '2025-12-16', N'Phung Hong Khoa', N'Siemens Vietnam', N'Industrial Machinery', N'Penetration', N'Trading', N'Quy Nhon', N'Central', N'Lost', 405000, N'Price', 1405580283.14, 'SRC09', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0089', '2026-03-06', N'Tran Thi Dung', N'CotecLand', N'Construction & Infrastructure', N'Penetration', N'Trading', N'Hung Yen', N'North', N'Contacted', 5427000, NULL, NULL, 'SRC03', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0090', '2025-11-21', N'Huynh Huu Binh', N'Schaeffler Vietnam', N'Automotive & EV', N'Industry', N'Trading', N'Bac Giang', N'North', N'Proposal Sent', 702000, NULL, 3860660511.03, 'SRC02', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0091', '2026-01-03', N'Duong Duc Quang', N'Vietnam Sun Energy', N'Renewable Energy', N'Energy', N'Contractor', N'Quang Nam', N'Central', N'Won', 837000, NULL, 10530000000.00, 'SRC05', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0092', '2025-03-11', N'Ly Huu Anh', N'Licogi Group', N'Construction & Infrastructure', N'Penetration', N'Contractor', N'Bac Giang', N'North', N'Won', 2160000, NULL, 1350000000.00, 'SRC01', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0093', '2025-08-31', N'Chu Huu Hong', N'Fanuc Vietnam', N'Industrial Machinery', N'Penetration', N'Manufacturer', N'Quang Nam', N'Central', N'Won', 1242000, NULL, 9450000000.00, 'SRC10', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0094', '2026-02-05', N'Bui Huu Binh', N'Festo Vietnam', N'Industrial Machinery', N'Penetration', N'Contractor', N'Nam Dinh', N'North', N'Proposal Sent', 4104000, NULL, 20965097115.19, 'SRC08', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0095', '2025-07-12', N'Vu Huu Viet', N'Valeo Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Dong Nai', N'South', N'Won', 648000, NULL, 486000000.00, 'SRC04', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0096', '2026-02-04', N'Ly Van Thao', N'Suzuki Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Bac Giang', N'North', N'Contacted', 1026000, NULL, NULL, 'SRC02', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0097', '2026-03-15', N'Truong Minh Ha', N'AC Energy Vietnam', N'Renewable Energy', N'Energy', N'Contractor', N'Vinh Phuc', N'North', N'Qualified', 1350000, NULL, 12187627349.75, 'SRC07', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0098', '2024-11-09', N'Pham Ngoc Hung', N'Siemens Vietnam', N'Industrial Machinery', N'Industry', N'Trading', N'Vung Tau', N'South', N'Won', 432000, NULL, 2025000000.00, 'SRC09', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0099', '2026-04-17', N'Truong Thi Hong', N'Yamaha Motor Vietnam', N'Automotive & EV', N'Industry', N'Contractor', N'Nam Dinh', N'North', N'New', 594000, NULL, NULL, 'SRC05', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0100', '2026-01-29', N'Cao Quoc Hoa', N'Foxconn Vietnam', N'Industrial Machinery', N'Industry', N'Trading', N'Quy Nhon', N'Central', N'Qualified', 1188000, NULL, 8580089654.22, 'SRC02', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0101', '2024-11-30', N'Chu Huu Trang', N'Schaeffler Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Hai Phong', N'North', N'Won', 1377000, NULL, 540000000.00, 'SRC07', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0102', '2026-04-06', N'Ly Minh Khanh', N'Truong Thanh Solar', N'Renewable Energy', N'Energy', N'Contractor', N'Thai Nguyen', N'North', N'Contacted', 216000, NULL, NULL, 'SRC09', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0103', '2025-06-19', N'Tran Minh Tuan', N'Licogi Group', N'Construction & Infrastructure', N'Energy', N'Trading', N'Hue', N'Central', N'Lost', 4968000, N'Price', 20371831894.49, 'SRC03', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0104', '2026-01-18', N'Ngo Huu Thanh', N'LG Display Vietnam', N'Industrial Machinery', N'Penetration', N'Trading', N'Tien Giang', N'South', N'Qualified', 1215000, NULL, 7357165290.40, 'SRC10', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0105', '2025-06-26', N'Do Quoc Nam', N'Thaco Auto Group', N'Automotive & EV', N'Energy', N'Manufacturer', N'Tien Giang', N'South', N'Lost', 2322000, N'Price', 10885390360.97, 'SRC01', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0106', '2025-12-21', N'Hoang Van Kiet', N'Bac Phuong Solar', N'Renewable Energy', N'Penetration', N'Manufacturer', N'Thai Nguyen', N'North', N'Qualified', 2484000, NULL, 4801173923.05, 'SRC01', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0107', '2025-04-20', N'Hoang Van Lan', N'VinFast Automotive', N'Automotive & EV', N'Energy', N'Manufacturer', N'Da Nang', N'Central', N'Won', 2754000, NULL, 513000000.00, 'SRC08', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0108', '2025-11-17', N'Vu Ngoc Ngoc', N'Honda Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Khanh Hoa', N'Central', N'Won', 648000, NULL, 6709500000.00, 'SRC04', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0109', '2025-12-22', N'Vu Hong Van', N'Thaco Auto Group', N'Automotive & EV', N'Energy', N'Manufacturer', N'Nha Trang', N'Central', N'Qualified', 2970000, NULL, 9292687697.57, 'SRC03', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0110', '2026-03-10', N'Hoang Huu Thao', N'Bamboo Capital Energy', N'Renewable Energy', N'Energy', N'Contractor', N'Binh Duong', N'South', N'Qualified', 2619000, NULL, 9111589110.35, 'SRC01', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0111', '2026-02-20', N'Chu Thi Hien', N'Ricons Group', N'Construction & Infrastructure', N'Penetration', N'Contractor', N'Nha Trang', N'Central', N'Qualified', 486000, NULL, 4216324544.60, 'SRC04', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0112', '2025-08-06', N'Duong Thi Linh', N'Cofico', N'Construction & Infrastructure', N'Energy', N'Contractor', N'Hue', N'Central', N'Lost', 1971000, N'No Budget', 6472156596.07, 'SRC06', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0113', '2024-12-06', N'Le Thi Van', N'Honda Vietnam', N'Automotive & EV', N'Industry', N'Contractor', N'Ba Ria', N'South', N'Won', 1755000, NULL, 5400000000.00, 'SRC06', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0114', '2025-06-06', N'Duong Minh Van', N'Foxconn Vietnam', N'Industrial Machinery', N'Penetration', N'Trading', N'Bac Giang', N'North', N'Lost', 513000, N'No Budget', 1312019093.01, 'SRC05', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0115', '2025-09-01', N'Cao Ngoc Viet', N'AC Energy Vietnam', N'Renewable Energy', N'Energy', N'Contractor', N'Quy Nhon', N'Central', N'Lost', 486000, N'No Budget', 765500646.51, 'SRC02', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0116', '2024-12-10', N'Truong Binh', N'Ecotech Vietnam', N'Renewable Energy', N'Energy', N'Contractor', N'Long An', N'South', N'Won', 567000, NULL, 945000000.00, 'SRC07', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0117', '2026-01-31', N'Nguyen Duc Phuc', N'ABB Vietnam', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Thai Nguyen', N'North', N'Contacted', 2457000, NULL, NULL, 'SRC08', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0118', '2025-07-15', N'Duong Ngoc Huong', N'Truong Thanh Solar', N'Renewable Energy', N'Energy', N'Contractor', N'Da Nang', N'Central', N'Won', 783000, NULL, 148500000.00, 'SRC05', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0119', '2025-10-09', N'Dang Ngoc Van', N'Honda Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Nam Dinh', N'North', N'Won', 567000, NULL, 2200500000.00, 'SRC04', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0120', '2025-10-07', N'Cao Huu Cuong', N'Unicons', N'Construction & Infrastructure', N'Industry', N'Trading', N'Hai Phong', N'North', N'Lost', 2862000, N'No Decision', 13157096422.69, 'SRC08', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0121', '2026-01-20', N'Dang Huu Linh', N'AC Energy Vietnam', N'Renewable Energy', N'Energy', N'Contractor', N'Quang Ngai', N'Central', N'Qualified', 1026000, NULL, 8121715962.19, 'SRC06', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0122', '2025-04-02', N'Truong Minh Hung', N'Vietnam Sun Energy', N'Renewable Energy', N'Energy', N'Contractor', N'Quang Ngai', N'Central', N'Lost', 3726000, N'Competitor', 18322712084.79, 'SRC08', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0123', '2025-09-30', N'Duong Hong Trang', N'Festo Vietnam', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Da Nang', N'Central', N'Disqualified', 1863000, NULL, NULL, 'SRC06', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0124', '2025-10-15', N'Duong Thi Dung', N'Intel Products VN', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Bac Ninh', N'North', N'Disqualified', 378000, NULL, NULL, 'SRC09', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0125', '2026-01-25', N'Huynh Thi Thu', N'Bac Phuong Solar', N'Renewable Energy', N'Energy', N'Contractor', N'Quang Ninh', N'North', N'Contacted', 702000, NULL, NULL, 'SRC02', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0126', '2025-12-09', N'Pham Duc Hai', N'TTC Energy', N'Renewable Energy', N'Energy', N'Contractor', N'Tay Ninh', N'South', N'Lost', 3645000, N'Competitor', 13973630445.63, 'SRC03', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0127', '2026-03-04', N'Luu Ngoc Quyen', N'CC1 Corporation', N'Construction & Infrastructure', N'Penetration', N'Trading', N'Da Nang', N'Central', N'Proposal Sent', 513000, NULL, 2648876763.75, 'SRC02', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0128', '2026-03-17', N'Dinh Huu Kiet', N'CC1 Corporation', N'Construction & Infrastructure', N'Penetration', N'Contractor', N'Bac Ninh', N'North', N'Qualified', 1323000, NULL, 6641959646.41, 'SRC07', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0129', '2026-01-11', N'Bui Thi Khoa', N'Denso Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Nha Trang', N'Central', N'Qualified', 729000, NULL, 5915160893.48, 'SRC05', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0130', '2025-10-27', N'Luu Quoc Linh', N'Schaeffler Vietnam', N'Automotive & EV', N'Industry', N'Manufacturer', N'Hai Phong', N'North', N'Lost', 1242000, N'Product Fit', 4376760758.58, 'SRC10', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0131', '2026-01-31', N'Mai Duc Cuong', N'Intel Products VN', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Nha Trang', N'Central', N'Qualified', 3402000, NULL, 18540056531.80, 'SRC01', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0132', '2025-10-13', N'Bui Hong Trang', N'ABB Vietnam', N'Industrial Machinery', N'Energy', N'Contractor', N'Hai Phong', N'North', N'Lost', 459000, N'Price', 1485157751.48, 'SRC02', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0133', '2025-09-09', N'Dang Duc Huong', N'Hung Thinh Corp', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Hue', N'Central', N'Won', 1215000, NULL, 216000000.00, 'SRC07', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0134', '2025-12-22', N'Ngo Minh Nam', N'Bosch Rexroth VN', N'Industrial Machinery', N'Industry', N'Trading', N'Can Tho', N'South', N'In Negotiation', 1917000, NULL, 2630525479.64, 'SRC01', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0135', '2025-10-28', N'Ho Van Ngoc', N'Unicons', N'Construction & Infrastructure', N'Industry', N'Trading', N'Da Nang', N'Central', N'Won', 1404000, NULL, 324000000.00, 'SRC10', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0136', '2025-08-18', N'Bui Thi Thu', N'Bamboo Capital Energy', N'Renewable Energy', N'Energy', N'Contractor', N'Khanh Hoa', N'Central', N'Lost', 4428000, N'Price', 13759982242.59, 'SRC08', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0137', '2026-01-25', N'Ngo Duc Quang', N'Ha Do Green Power', N'Renewable Energy', N'Energy', N'Contractor', N'Binh Duong', N'South', N'Disqualified', 2052000, NULL, NULL, 'SRC06', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0138', '2026-03-30', N'Cao Quoc Yen', N'Eco Constructions', N'Construction & Infrastructure', N'Penetration', N'Contractor', N'Ba Ria', N'South', N'New', 1458000, NULL, NULL, 'SRC10', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0139', '2026-02-09', N'Phan Minh Quyen', N'Omron Industrial VN', N'Industrial Machinery', N'Penetration', N'Manufacturer', N'Bac Giang', N'North', N'Contacted', 486000, NULL, NULL, 'SRC09', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0140', '2025-04-16', N'Hoang Huu Duc', N'Ha Do Green Power', N'Renewable Energy', N'Energy', N'Trading', N'Quy Nhon', N'Central', N'Won', 3267000, NULL, 391500000.00, 'SRC03', 'f1460724-4d7f-4796-bca7-50bf1ab09811'),
('L-2026-0141', '2026-04-18', N'Le Quoc Yen', N'Ecotech Vietnam', N'Renewable Energy', N'Penetration', N'Trading', N'Quang Ngai', N'Central', N'New', 3510000, NULL, NULL, 'SRC08', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0142', '2025-08-26', N'Le Minh Viet', N'Truong Thanh Solar', N'Renewable Energy', N'Industry', N'Trading', N'Tien Giang', N'South', N'Won', 1080000, NULL, 135000000.00, 'SRC02', '655612cb-fc04-4fa8-a2de-87a6a55d30c7'),
('L-2026-0143', '2025-02-18', N'Chu Duc Hoa', N'Omron Industrial VN', N'Industrial Machinery', N'Industry', N'Manufacturer', N'Bac Ninh', N'North', N'Won', 3024000, NULL, 472500000.00, 'SRC08', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0144', '2025-10-15', N'Bui Quoc Tai', N'Ricons Group', N'Construction & Infrastructure', N'Industry', N'Contractor', N'Dong Nai', N'South', N'Won', 648000, NULL, 4792500000.00, 'SRC05', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0145', '2025-08-27', N'Luu Quoc Phuc', N'Nidec Tosok', N'Automotive & EV', N'Industry', N'Manufacturer', N'Hue', N'Central', N'Lost', 621000, N'No Decision', 2155223100.82, 'SRC04', 'aa512e88-565a-44bb-84f6-612ecc49e949'),
('L-2026-0146', '2025-11-04', N'Ho Thi Ha', N'Nidec Tosok', N'Automotive & EV', N'Industry', N'Trading', N'Khanh Hoa', N'Central', N'In Negotiation', 1674000, NULL, 4326015706.31, 'SRC01', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0147', '2025-01-22', N'Dinh Hong Ngoc', N'Hoa Binh Construction', N'Construction & Infrastructure', N'Industry', N'Manufacturer', N'Ba Ria', N'South', N'Won', 4806000, NULL, 351000000.00, 'SRC03', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5'),
('L-2026-0148', '2026-01-24', N'Tran Minh Khanh', N'Luxshare-ICT Vietnam', N'Industrial Machinery', N'Industry', N'Contractor', N'Tien Giang', N'South', N'Proposal Sent', 2268000, NULL, 33734163467.27, 'SRC10', 'f3f1ab23-29da-4ea3-8377-befc37b314f1'),
('L-2026-0149', '2025-05-23', N'Le Ngoc Cuong', N'AC Energy Vietnam', N'Renewable Energy', N'Energy', N'Manufacturer', N'Quang Ninh', N'North', N'Lost', 3240000, N'No Budget', 7975046283.42, 'SRC08', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9'),
('L-2026-0150', '2025-09-15', N'Vu Huu Cuong', N'CotecLand', N'Construction & Infrastructure', N'Penetration', N'Contractor', N'Hue', N'Central', N'Lost', 5130000, N'No Budget', 21337429508.49, 'SRC08', 'aa512e88-565a-44bb-84f6-612ecc49e949');
;
GO
INSERT INTO lead_item (lead_id, product_id, quantity, expected_revenue) VALUES

('L-2026-0001', 'PRD01', 3471, 0.00),
('L-2026-0001', 'PRD05', 148, 0.00),
('L-2026-0002', 'PRD02', 1761, 4464742084.81),
('L-2026-0002', 'PRD05', 2904, 4985257915.19),
('L-2026-0003', 'PRD02', 4705, 20078021.46),
('L-2026-0003', 'PRD04', 1604, 99892388.92),
('L-2026-0003', 'PRD06', 624, 96029589.62),
('L-2026-0004', 'PRD01', 4408, 9716665527.83),
('L-2026-0005', 'PRD03', 3497, 0.00),
('L-2026-0005', 'PRD06', 4457, 0.00),
('L-2026-0006', 'PRD04', 1130, 5814830968.31),
('L-2026-0006', 'PRD05', 991, 2076725345.82),
('L-2026-0006', 'PRD01', 1644, 13439379737.98),
('L-2026-0007', 'PRD01', 4549, 0.00),
('L-2026-0007', 'PRD03', 3333, 0.00),
('L-2026-0007', 'PRD02', 4634, 0.00),
('L-2026-0008', 'PRD02', 845, 3570677554.27),
('L-2026-0009', 'PRD04', 2811, 0.00),
('L-2026-0009', 'PRD06', 3658, 0.00),
('L-2026-0010', 'PRD03', 3717, 2025000000.00),
('L-2026-0011', 'PRD04', 392, 0.00),
('L-2026-0011', 'PRD06', 4476, 0.00),
('L-2026-0011', 'PRD01', 4116, 0.00),
('L-2026-0012', 'PRD03', 599, 0.00),
('L-2026-0012', 'PRD01', 1276, 0.00),
('L-2026-0013', 'PRD02', 2026, 16569228011.58),
('L-2026-0014', 'PRD01', 2775, 15870966142.20),
('L-2026-0014', 'PRD03', 1444, 5510293858.10),
('L-2026-0014', 'PRD05', 116, 10162697654.47),
('L-2026-0015', 'PRD01', 2620, 1672533474.01),
('L-2026-0015', 'PRD02', 2494, 1934187483.74),
('L-2026-0015', 'PRD05', 3252, 1949578896.08),
('L-2026-0016', 'PRD01', 2566, 726204234.52),
('L-2026-0016', 'PRD04', 1182, 645052471.93),
('L-2026-0017', 'PRD04', 4708, 0.00),
('L-2026-0017', 'PRD05', 3079, 0.00),
('L-2026-0017', 'PRD02', 3055, 0.00),
('L-2026-0018', 'PRD04', 3040, 2098842091.21),
('L-2026-0018', 'PRD02', 381, 3233657908.79),
('L-2026-0019', 'PRD01', 3345, 15811218685.58),
('L-2026-0019', 'PRD05', 3526, 9886640255.38),
('L-2026-0019', 'PRD02', 3994, 13922759310.95),
('L-2026-0020', 'PRD04', 1701, 985959785.86),
('L-2026-0020', 'PRD03', 4513, 2254040214.14),
('L-2026-0021', 'PRD01', 2296, 9276560360.56),
('L-2026-0021', 'PRD06', 3706, 5764864644.20),
('L-2026-0021', 'PRD02', 4881, 1826893725.27),
('L-2026-0022', 'PRD04', 322, 2025000000.00),
('L-2026-0023', 'PRD03', 1000, 0.00),
('L-2026-0023', 'PRD02', 4835, 0.00),
('L-2026-0023', 'PRD01', 3858, 0.00),
('L-2026-0024', 'PRD01', 743, 2649999689.41),
('L-2026-0024', 'PRD05', 4043, 1994860877.30),
('L-2026-0024', 'PRD04', 2671, 3084305194.06),
('L-2026-0025', 'PRD05', 1432, 0.00),
('L-2026-0025', 'PRD06', 323, 0.00),
('L-2026-0025', 'PRD04', 3247, 0.00),
('L-2026-0026', 'PRD04', 478, 324000000.00),
('L-2026-0027', 'PRD01', 3251, 3886666211.13),
('L-2026-0028', 'PRD01', 324, 2683358831.37),
('L-2026-0029', 'PRD01', 925, 9450000000.00),
('L-2026-0030', 'PRD01', 4604, 18657771176.45),
('L-2026-0030', 'PRD06', 557, 4708585785.85),
('L-2026-0031', 'PRD01', 4005, 145188009.26),
('L-2026-0031', 'PRD03', 4933, 83325376.55),
('L-2026-0031', 'PRD06', 208, 95486614.19),
('L-2026-0032', 'PRD04', 398, 19356373993.97),
('L-2026-0032', 'PRD06', 3811, 10700377533.75),
('L-2026-0033', 'PRD04', 648, 6605694023.56),
('L-2026-0034', 'PRD05', 1343, 392146418.69),
('L-2026-0035', 'PRD03', 2074, 1647000000.00),
('L-2026-0036', 'PRD01', 1140, 8638820536.31),
('L-2026-0036', 'PRD03', 2749, 8429140426.21),
('L-2026-0036', 'PRD06', 812, 9225924844.61),
('L-2026-0037', 'PRD01', 4384, 1879982944.64),
('L-2026-0037', 'PRD05', 1425, 2492070414.99),
('L-2026-0038', 'PRD05', 2366, 0.00),
('L-2026-0038', 'PRD04', 95, 0.00),
('L-2026-0039', 'PRD04', 3782, 7033152758.42),
('L-2026-0039', 'PRD03', 4332, 4374466440.95),
('L-2026-0039', 'PRD05', 475, 7450504680.34),
('L-2026-0040', 'PRD03', 4061, 0.00),
('L-2026-0040', 'PRD01', 844, 0.00),
('L-2026-0040', 'PRD04', 577, 0.00),
('L-2026-0041', 'PRD03', 4763, 1636620178.55),
('L-2026-0041', 'PRD01', 1185, 2445226986.14),
('L-2026-0041', 'PRD04', 4428, 2296443333.54),
('L-2026-0042', 'PRD04', 1884, 0.00),
('L-2026-0043', 'PRD05', 721, 3456000000.00),
('L-2026-0044', 'PRD04', 4741, 2927665045.35),
('L-2026-0044', 'PRD01', 523, 3368321768.61),
('L-2026-0044', 'PRD05', 555, 1115513186.04),
('L-2026-0045', 'PRD01', 593, 1565908907.38),
('L-2026-0046', 'PRD01', 2176, 0.00),
('L-2026-0046', 'PRD06', 4190, 0.00),
('L-2026-0047', 'PRD06', 1493, 0.00),
('L-2026-0047', 'PRD03', 1290, 0.00),
('L-2026-0047', 'PRD04', 1247, 0.00),
('L-2026-0048', 'PRD05', 4509, 444105250.26),
('L-2026-0048', 'PRD03', 3553, 1958343633.66),
('L-2026-0048', 'PRD04', 2180, 2215175585.62),
('L-2026-0049', 'PRD03', 4307, 21686247943.25),
('L-2026-0049', 'PRD04', 1724, 12753311614.30),
('L-2026-0049', 'PRD01', 1782, 17023142815.56),
('L-2026-0050', 'PRD01', 963, 0.00),
('L-2026-0050', 'PRD02', 768, 0.00),
('L-2026-0051', 'PRD03', 1780, 3382318627.15),
('L-2026-0052', 'PRD02', 2747, 1350000000.00),
('L-2026-0053', 'PRD04', 2629, 0.00),
('L-2026-0053', 'PRD03', 507, 0.00),
('L-2026-0054', 'PRD04', 3057, 387856995.92),
('L-2026-0054', 'PRD03', 1130, 57643004.08),
('L-2026-0055', 'PRD02', 3228, 0.00),
('L-2026-0055', 'PRD01', 4837, 0.00),
('L-2026-0055', 'PRD03', 1734, 0.00),
('L-2026-0056', 'PRD04', 4555, 765739708.61),
('L-2026-0056', 'PRD02', 4693, 145657009.79),
('L-2026-0057', 'PRD04', 4844, 0.00),
('L-2026-0057', 'PRD05', 1566, 0.00),
('L-2026-0058', 'PRD03', 3652, 992188435.90),
('L-2026-0058', 'PRD04', 2178, 981481366.45),
('L-2026-0058', 'PRD05', 3106, 264107713.15),
('L-2026-0059', 'PRD04', 109, 2122133454.87),
('L-2026-0059', 'PRD05', 3316, 15250586184.18),
('L-2026-0060', 'PRD02', 3986, 0.00),
('L-2026-0060', 'PRD04', 4214, 0.00),
('L-2026-0060', 'PRD05', 373, 0.00),
('L-2026-0061', 'PRD03', 1262, 1845979654.68),
('L-2026-0061', 'PRD01', 4826, 2126295083.73),
('L-2026-0062', 'PRD03', 4637, 7528408208.55),
('L-2026-0062', 'PRD02', 727, 7066665838.42),
('L-2026-0062', 'PRD06', 734, 7407953677.21),
('L-2026-0063', 'PRD04', 649, 1761312924.88),
('L-2026-0063', 'PRD06', 1575, 1302247322.12),
('L-2026-0063', 'PRD01', 298, 1349090750.97),
('L-2026-0064', 'PRD01', 1054, 0.00),
('L-2026-0064', 'PRD03', 1674, 0.00),
('L-2026-0065', 'PRD01', 927, 5354426981.87),
('L-2026-0066', 'PRD02', 2738, 135000000.00),
('L-2026-0067', 'PRD03', 1760, 0.00),
('L-2026-0067', 'PRD05', 1782, 0.00),
('L-2026-0068', 'PRD02', 302, 7683889387.21),
('L-2026-0068', 'PRD05', 4282, 1609777810.49),
('L-2026-0068', 'PRD01', 4890, 7338332802.30),
('L-2026-0069', 'PRD02', 2694, 0.00),
('L-2026-0070', 'PRD06', 4979, 0.00),
('L-2026-0071', 'PRD03', 3371, 15383382035.38),
('L-2026-0072', 'PRD06', 4071, 1441216161.05),
('L-2026-0072', 'PRD01', 2696, 414675877.32),
('L-2026-0072', 'PRD03', 426, 1423817173.19),
('L-2026-0073', 'PRD05', 4657, 945000000.00),
('L-2026-0074', 'PRD06', 1213, 0.00),
('L-2026-0075', 'PRD02', 3436, 226046141.95),
('L-2026-0075', 'PRD01', 4741, 234119218.44),
('L-2026-0075', 'PRD04', 583, 1654980682.10),
('L-2026-0076', 'PRD03', 228, 5727703613.28),
('L-2026-0077', 'PRD04', 4034, 0.00),
('L-2026-0078', 'PRD02', 3617, 101634266.63),
('L-2026-0078', 'PRD01', 4847, 218405551.69),
('L-2026-0078', 'PRD04', 4142, 336618457.55),
('L-2026-0079', 'PRD06', 2624, 945000000.00),
('L-2026-0080', 'PRD01', 4222, 2297813644.23),
('L-2026-0081', 'PRD01', 2108, 8759143838.30),
('L-2026-0082', 'PRD03', 1248, 540000000.00),
('L-2026-0083', 'PRD04', 2475, 7324586977.01),
('L-2026-0083', 'PRD03', 3052, 5250959613.66),
('L-2026-0083', 'PRD06', 1371, 4431542349.10),
('L-2026-0084', 'PRD03', 4583, 0.00),
('L-2026-0085', 'PRD04', 2375, 7879178500.01),
('L-2026-0086', 'PRD03', 1037, 2765992147.17),
('L-2026-0086', 'PRD05', 1735, 474007852.83),
('L-2026-0087', 'PRD01', 2390, 15044288296.85),
('L-2026-0087', 'PRD02', 4249, 10986700166.99),
('L-2026-0088', 'PRD01', 3857, 229217707.71),
('L-2026-0088', 'PRD02', 3042, 116771285.06),
('L-2026-0088', 'PRD05', 4185, 1059591290.37),
('L-2026-0089', 'PRD04', 4884, 0.00),
('L-2026-0089', 'PRD01', 3671, 0.00),
('L-2026-0089', 'PRD06', 2052, 0.00),
('L-2026-0090', 'PRD01', 2179, 483519617.40),
('L-2026-0090', 'PRD02', 4796, 1874107044.19),
('L-2026-0090', 'PRD04', 121, 1503033849.44),
('L-2026-0091', 'PRD04', 1604, 10530000000.00),
('L-2026-0092', 'PRD06', 4445, 424759269.21),
('L-2026-0092', 'PRD02', 3028, 145217137.64),
('L-2026-0092', 'PRD03', 1044, 780023593.15),
('L-2026-0093', 'PRD06', 1283, 531521884.56),
('L-2026-0093', 'PRD04', 4727, 5458328746.66),
('L-2026-0093', 'PRD02', 228, 3460149368.78),
('L-2026-0094', 'PRD01', 4184, 20965097115.19),
('L-2026-0095', 'PRD04', 4820, 486000000.00),
('L-2026-0096', 'PRD01', 289, 0.00),
('L-2026-0096', 'PRD02', 2865, 0.00),
('L-2026-0096', 'PRD06', 1764, 0.00),
('L-2026-0097', 'PRD04', 1974, 6688332082.18),
('L-2026-0097', 'PRD02', 2691, 5499295267.57),
('L-2026-0098', 'PRD01', 4194, 193803351.42),
('L-2026-0098', 'PRD04', 2906, 1480854627.86),
('L-2026-0098', 'PRD02', 4661, 350342020.72),
('L-2026-0099', 'PRD01', 4322, 0.00),
('L-2026-0099', 'PRD04', 2274, 0.00),
('L-2026-0100', 'PRD02', 1439, 758605487.72),
('L-2026-0100', 'PRD06', 4587, 4957094480.11),
('L-2026-0100', 'PRD01', 1216, 2864389686.39),
('L-2026-0101', 'PRD02', 1694, 540000000.00),
('L-2026-0102', 'PRD04', 3853, 0.00),
('L-2026-0103', 'PRD01', 4454, 20371831894.49),
('L-2026-0104', 'PRD02', 3701, 7357165290.40),
('L-2026-0105', 'PRD02', 445, 10885390360.97),
('L-2026-0106', 'PRD04', 3169, 2374061138.75),
('L-2026-0106', 'PRD06', 2959, 2427112784.30),
('L-2026-0107', 'PRD02', 3505, 487341318.87),
('L-2026-0107', 'PRD06', 946, 25658681.13),
('L-2026-0108', 'PRD01', 1440, 1355343161.36),
('L-2026-0108', 'PRD04', 2699, 5354156838.64),
('L-2026-0109', 'PRD04', 3984, 4265755956.73),
('L-2026-0109', 'PRD02', 4817, 1094190189.65),
('L-2026-0109', 'PRD06', 1504, 3932741551.19),
('L-2026-0110', 'PRD03', 537, 9111589110.35),
('L-2026-0111', 'PRD06', 1255, 1455381061.08),
('L-2026-0111', 'PRD02', 349, 331741271.28),
('L-2026-0111', 'PRD05', 808, 2429202212.24),
('L-2026-0112', 'PRD06', 4716, 1578574779.53),
('L-2026-0112', 'PRD04', 4888, 4893581816.54),
('L-2026-0113', 'PRD01', 2409, 2894321571.37),
('L-2026-0113', 'PRD05', 4438, 2505678428.63),
('L-2026-0114', 'PRD02', 4038, 651900927.22),
('L-2026-0114', 'PRD05', 4951, 493034314.70),
('L-2026-0114', 'PRD01', 3955, 167083851.09),
('L-2026-0115', 'PRD01', 1759, 765500646.51),
('L-2026-0116', 'PRD01', 4719, 569768264.91),
('L-2026-0116', 'PRD05', 2245, 375231735.09),
('L-2026-0117', 'PRD06', 1612, 0.00),
('L-2026-0117', 'PRD04', 2963, 0.00),
('L-2026-0117', 'PRD01', 4610, 0.00),
('L-2026-0118', 'PRD04', 2173, 53574874.09),
('L-2026-0118', 'PRD05', 3929, 14051644.16),
('L-2026-0118', 'PRD03', 1285, 80873481.75),
('L-2026-0119', 'PRD04', 2002, 533982067.06),
('L-2026-0119', 'PRD03', 1052, 838748984.93),
('L-2026-0119', 'PRD01', 1782, 827768948.01),
('L-2026-0120', 'PRD01', 4399, 5409537902.01),
('L-2026-0120', 'PRD03', 4645, 1696211037.07),
('L-2026-0120', 'PRD05', 3845, 6051347483.61),
('L-2026-0121', 'PRD04', 4798, 2055844652.46),
('L-2026-0121', 'PRD05', 2085, 2439903543.58),
('L-2026-0121', 'PRD03', 1100, 3625967766.15),
('L-2026-0122', 'PRD04', 2247, 18322712084.79),
('L-2026-0123', 'PRD02', 387, 0.00),
('L-2026-0123', 'PRD06', 2144, 0.00),
('L-2026-0124', 'PRD06', 3706, 0.00),
('L-2026-0125', 'PRD01', 1498, 0.00),
('L-2026-0125', 'PRD04', 4425, 0.00),
('L-2026-0125', 'PRD05', 2195, 0.00),
('L-2026-0126', 'PRD04', 2967, 3464214790.14),
('L-2026-0126', 'PRD03', 2795, 3717219241.11),
('L-2026-0126', 'PRD06', 4598, 6792196414.38),
('L-2026-0127', 'PRD04', 3263, 2005013328.64),
('L-2026-0127', 'PRD01', 901, 643863435.11),
('L-2026-0128', 'PRD04', 4381, 3787082254.53),
('L-2026-0128', 'PRD06', 456, 2854877391.88),
('L-2026-0129', 'PRD02', 637, 1950317635.16),
('L-2026-0129', 'PRD01', 1513, 3057905427.97),
('L-2026-0129', 'PRD04', 456, 906937830.35),
('L-2026-0130', 'PRD05', 3231, 4376760758.58),
('L-2026-0131', 'PRD02', 533, 18540056531.80),
('L-2026-0132', 'PRD03', 1773, 259780068.74),
('L-2026-0132', 'PRD04', 3782, 431332944.32),
('L-2026-0132', 'PRD01', 1302, 794044738.42),
('L-2026-0133', 'PRD04', 4865, 155351577.57),
('L-2026-0133', 'PRD03', 2902, 60648422.43),
('L-2026-0134', 'PRD01', 1992, 1699094278.68),
('L-2026-0134', 'PRD02', 1524, 931431200.96),
('L-2026-0135', 'PRD06', 2804, 324000000.00),
('L-2026-0136', 'PRD03', 4629, 7802051787.04),
('L-2026-0136', 'PRD05', 2819, 5957930455.55),
('L-2026-0137', 'PRD04', 2251, 0.00),
('L-2026-0137', 'PRD01', 1188, 0.00),
('L-2026-0138', 'PRD03', 723, 0.00),
('L-2026-0139', 'PRD02', 2813, 0.00),
('L-2026-0139', 'PRD01', 4473, 0.00),
('L-2026-0139', 'PRD03', 2897, 0.00),
('L-2026-0140', 'PRD04', 2162, 391500000.00),
('L-2026-0141', 'PRD04', 3868, 0.00),
('L-2026-0141', 'PRD02', 838, 0.00),
('L-2026-0142', 'PRD04', 63, 135000000.00),
('L-2026-0143', 'PRD02', 4112, 472500000.00),
('L-2026-0144', 'PRD04', 2025, 2605549741.77),
('L-2026-0144', 'PRD06', 610, 2186950258.23),
('L-2026-0145', 'PRD04', 1167, 736091582.13),
('L-2026-0145', 'PRD02', 4690, 1419131518.69),
('L-2026-0146', 'PRD02', 2607, 2824423477.67),
('L-2026-0146', 'PRD04', 2927, 1501592228.64),
('L-2026-0147', 'PRD04', 108, 299900510.52),
('L-2026-0147', 'PRD05', 1650, 51099489.48),
('L-2026-0148', 'PRD03', 4996, 11635914267.76),
('L-2026-0148', 'PRD06', 761, 11211428124.95),
('L-2026-0148', 'PRD04', 4251, 10886821074.56),
('L-2026-0149', 'PRD04', 4064, 6608389761.97),
('L-2026-0149', 'PRD06', 88, 1366656521.45),
('L-2026-0150', 'PRD04', 2115, 12380639458.07),
('L-2026-0150', 'PRD03', 627, 2739079526.12),
('L-2026-0150', 'PRD05', 4705, 6217710524.30);
;
GO

-- 3.7 GENERATE LEAD STATUS HISTORY (SIMULATED FUNNEL)
INSERT INTO lead_status_history (lead_id, old_status, new_status, changed_at, changed_by_user_id, note) VALUES

('L-2026-0001', NULL, N'New', '2026-04-09 11:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0002', NULL, N'New', '2025-04-17 16:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0002', N'New', N'Contacted', '2025-04-25 18:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0002', N'Contacted', N'Qualified', '2025-05-07 23:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0002', N'Qualified', N'Proposal Sent', '2025-05-10 01:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0002', N'Proposal Sent', N'In Negotiation', '2025-06-04 04:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0002', N'In Negotiation', N'Won', '2025-06-12 07:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0003', NULL, N'New', '2026-01-05 08:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0003', N'New', N'Proposal Sent', '2026-02-04 11:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0003', N'Proposal Sent', N'In Negotiation', '2026-03-02 14:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0003', N'In Negotiation', N'Won', '2026-03-21 15:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0004', NULL, N'New', '2024-10-30 12:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0004', N'New', N'Contacted', '2024-11-10 21:36:39', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0004', N'Contacted', N'Proposal Sent', '2024-12-21 07:03:19', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0004', N'Proposal Sent', N'In Negotiation', '2024-12-25 17:59:59', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0005', NULL, N'New', '2026-02-16 08:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0005', N'New', N'Contacted', '2026-02-23 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0006', NULL, N'New', '2024-11-09 12:52:15', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0006', N'New', N'Qualified', '2024-11-12 04:27:05', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0006', N'Qualified', N'In Negotiation', '2024-12-08 10:09:40', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0006', N'In Negotiation', N'Lost', '2024-12-26 17:59:59', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', N'Lost-Price'),
('L-2026-0007', NULL, N'New', '2026-04-12 11:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0008', NULL, N'New', '2026-03-21 10:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0008', N'New', N'Contacted', '2026-04-12 15:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0008', N'Contacted', N'Qualified', '2026-05-07 17:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0009', NULL, N'New', '2026-04-03 14:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0010', NULL, N'New', '2025-02-03 12:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0010', N'New', N'Proposal Sent', '2025-02-20 16:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0010', N'Proposal Sent', N'In Negotiation', '2025-03-16 20:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0010', N'In Negotiation', N'Won', '2025-04-13 22:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0011', NULL, N'New', '2026-04-18 15:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0012', NULL, N'New', '2026-02-22 10:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0012', N'New', N'Contacted', '2026-03-20 15:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0013', NULL, N'New', '2026-03-04 16:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0013', N'New', N'Contacted', '2026-03-11 19:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0013', N'Contacted', N'Qualified', '2026-04-06 23:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0013', N'Qualified', N'Proposal Sent', '2026-04-16 01:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0014', NULL, N'New', '2026-02-01 12:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0014', N'New', N'Contacted', '2026-02-07 17:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0014', N'Contacted', N'Proposal Sent', '2026-03-07 19:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0015', NULL, N'New', '2024-09-29 15:04:34', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0015', N'New', N'Contacted', '2024-11-04 04:45:11', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0015', N'Contacted', N'Proposal Sent', '2024-11-12 03:29:04', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0015', N'Proposal Sent', N'In Negotiation', '2024-12-16 04:14:45', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0015', N'In Negotiation', N'Lost', '2024-12-22 15:35:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', N'Lost-Price'),
('L-2026-0016', NULL, N'New', '2026-02-16 15:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0016', N'New', N'Contacted', '2026-03-17 20:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0016', N'Contacted', N'Qualified', '2026-04-13 00:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0016', N'Qualified', N'Proposal Sent', '2026-04-29 02:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0017', NULL, N'New', '2026-02-04 10:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0017', N'New', N'Contacted', '2026-03-05 11:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0017', N'Contacted', N'Proposal Sent', '2026-03-10 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0017', N'Proposal Sent', N'In Negotiation', '2026-03-24 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0017', N'In Negotiation', N'Disqualified', '2026-04-11 17:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0018', NULL, N'New', '2025-03-28 15:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0018', N'New', N'Proposal Sent', '2025-04-20 17:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0018', N'Proposal Sent', N'In Negotiation', '2025-05-14 21:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0018', N'In Negotiation', N'Won', '2025-05-20 00:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0019', NULL, N'New', '2024-08-24 12:03:04', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0019', N'New', N'Qualified', '2024-09-08 12:49:08', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0019', N'Qualified', N'In Negotiation', '2024-09-18 08:31:14', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0019', N'In Negotiation', N'Lost', '2024-12-18 22:47:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', N'Lost-Price'),
('L-2026-0020', NULL, N'New', '2025-12-22 12:41:25', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0020', N'New', N'Proposal Sent', '2025-12-25 03:30:36', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0020', N'Proposal Sent', N'In Negotiation', '2025-12-26 16:57:37', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0020', N'In Negotiation', N'Won', '2025-12-31 01:11:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0021', NULL, N'New', '2024-06-14 13:48:40', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0021', N'New', N'Contacted', '2024-06-27 15:09:53', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0021', N'Contacted', N'Qualified', '2024-07-19 12:26:22', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0021', N'Qualified', N'Proposal Sent', '2024-09-04 21:55:52', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0021', N'Proposal Sent', N'In Negotiation', '2024-10-25 16:55:50', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0021', N'In Negotiation', N'Lost', '2024-12-11 22:47:59', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-Price'),
('L-2026-0022', NULL, N'New', '2025-06-26 12:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0022', N'New', N'Qualified', '2025-07-07 13:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0022', N'Qualified', N'Proposal Sent', '2025-07-14 16:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0022', N'Proposal Sent', N'In Negotiation', '2025-08-10 17:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0022', N'In Negotiation', N'Won', '2025-08-27 20:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0023', NULL, N'New', '2026-03-23 15:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0024', NULL, N'New', '2024-12-02 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0024', N'New', N'Contacted', '2024-12-11 07:15:21', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0024', N'Contacted', N'Proposal Sent', '2024-12-19 08:35:21', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0024', N'Proposal Sent', N'In Negotiation', '2024-12-20 23:06:12', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0024', N'In Negotiation', N'Lost', '2024-12-29 01:11:59', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-Timing'),
('L-2026-0025', NULL, N'New', '2026-04-09 10:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0026', NULL, N'New', '2024-11-29 12:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0026', N'New', N'Qualified', '2024-12-02 06:34:08', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0026', N'Qualified', N'Proposal Sent', '2024-12-07 18:21:43', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0026', N'Proposal Sent', N'In Negotiation', '2024-12-13 06:09:18', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0026', N'In Negotiation', N'Won', '2024-12-28 17:59:59', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0027', NULL, N'New', '2024-11-21 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0027', N'New', N'Contacted', '2024-11-28 01:03:39', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0027', N'Contacted', N'Qualified', '2024-12-06 11:09:56', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0027', N'Qualified', N'Proposal Sent', '2024-12-21 21:35:32', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0027', N'Proposal Sent', N'In Negotiation', '2024-12-27 22:47:59', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0028', NULL, N'New', '2026-02-06 13:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0028', N'New', N'Contacted', '2026-02-10 17:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0028', N'Contacted', N'Qualified', '2026-02-21 20:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0029', NULL, N'New', '2025-04-15 15:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0029', N'New', N'Contacted', '2025-04-19 19:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0029', N'Contacted', N'Proposal Sent', '2025-05-08 20:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0029', N'Proposal Sent', N'In Negotiation', '2025-05-23 23:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0029', N'In Negotiation', N'Won', '2025-06-17 02:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0030', NULL, N'New', '2026-01-30 10:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0030', N'New', N'Contacted', '2026-02-02 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0030', N'Contacted', N'Qualified', '2026-02-12 18:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0030', N'Qualified', N'Proposal Sent', '2026-02-15 23:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0031', NULL, N'New', '2025-12-24 12:09:17', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0031', N'New', N'Qualified', '2025-12-25 07:30:51', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0031', N'Qualified', N'Proposal Sent', '2025-12-26 02:33:50', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0031', N'Proposal Sent', N'In Negotiation', '2025-12-27 16:02:37', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0031', N'In Negotiation', N'Won', '2025-12-31 05:59:59', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0032', NULL, N'New', '2024-12-17 12:26:09', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0032', N'New', N'Contacted', '2024-12-21 21:48:17', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0032', N'Contacted', N'Qualified', '2024-12-26 07:01:41', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0032', N'Qualified', N'Proposal Sent', '2024-12-26 21:33:43', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0032', N'Proposal Sent', N'In Negotiation', '2024-12-30 13:11:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0033', NULL, N'New', '2026-03-20 09:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0033', N'New', N'Qualified', '2026-04-05 14:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0034', NULL, N'New', '2024-09-08 15:31:05', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0034', N'New', N'Qualified', '2024-10-15 21:06:23', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0034', N'Qualified', N'In Negotiation', '2024-11-02 16:33:05', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0034', N'In Negotiation', N'Lost', '2024-12-20 13:11:59', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', N'Lost-No Budget'),
('L-2026-0035', NULL, N'New', '2025-01-02 12:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0035', N'New', N'Contacted', '2025-01-19 15:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0035', N'Contacted', N'Proposal Sent', '2025-01-31 17:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0035', N'Proposal Sent', N'In Negotiation', '2025-02-19 18:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0035', N'In Negotiation', N'Won', '2025-02-25 20:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0036', NULL, N'New', '2024-09-02 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0036', N'New', N'Qualified', '2024-10-13 02:36:29', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0036', N'Qualified', N'Proposal Sent', '2024-11-24 11:30:25', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0036', N'Proposal Sent', N'In Negotiation', '2024-12-04 09:52:21', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0036', N'In Negotiation', N'Lost', '2024-12-19 22:47:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', N'Lost-Product Fit'),
('L-2026-0037', NULL, N'New', '2024-09-10 12:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0037', N'New', N'Contacted', '2024-09-20 18:00:44', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0037', N'Contacted', N'Qualified', '2024-11-20 04:58:59', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0037', N'Qualified', N'In Negotiation', '2024-12-10 11:59:14', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0037', N'In Negotiation', N'Lost', '2024-12-20 17:59:59', 'aa512e88-565a-44bb-84f6-612ecc49e949', N'Lost-Competitor'),
('L-2026-0038', NULL, N'New', '2025-12-18 12:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0038', N'New', N'Contacted', '2025-12-21 00:53:35', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0038', N'Contacted', N'Qualified', '2025-12-23 17:44:08', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0038', N'Qualified', N'Proposal Sent', '2025-12-25 03:29:40', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0038', N'Proposal Sent', N'In Negotiation', '2025-12-27 04:40:03', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0038', N'In Negotiation', N'Disqualified', '2025-12-30 15:35:59', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0039', NULL, N'New', '2026-02-12 09:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0039', N'New', N'Qualified', '2026-03-14 11:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0039', N'Qualified', N'In Negotiation', '2026-04-04 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0039', N'In Negotiation', N'Lost', '2026-04-09 18:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-Price'),
('L-2026-0040', NULL, N'New', '2026-02-10 11:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0040', N'New', N'Contacted', '2026-02-18 14:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0041', NULL, N'New', '2024-05-12 12:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0041', N'New', N'Qualified', '2024-09-30 19:55:30', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0041', N'Qualified', N'In Negotiation', '2024-11-27 19:04:59', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0041', N'In Negotiation', N'Lost', '2024-12-08 15:35:59', 'aa512e88-565a-44bb-84f6-612ecc49e949', N'Lost-Competitor'),
('L-2026-0042', NULL, N'New', '2026-03-23 15:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0043', NULL, N'New', '2025-08-03 16:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0043', N'New', N'Contacted', '2025-08-30 18:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0043', N'Contacted', N'Qualified', '2025-09-29 20:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0043', N'Qualified', N'Proposal Sent', '2025-10-26 22:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0043', N'Proposal Sent', N'In Negotiation', '2025-11-14 02:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0043', N'In Negotiation', N'Won', '2025-11-18 04:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0044', NULL, N'New', '2025-04-28 10:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0044', N'New', N'Proposal Sent', '2025-05-16 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0044', N'Proposal Sent', N'In Negotiation', '2025-05-31 13:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0044', N'In Negotiation', N'Won', '2025-06-27 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0045', NULL, N'New', '2024-09-16 12:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0045', N'New', N'Qualified', '2024-11-09 00:54:28', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0045', N'Qualified', N'In Negotiation', '2024-11-17 20:17:45', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0045', N'In Negotiation', N'Lost', '2024-12-21 08:23:59', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', N'Lost-Timing'),
('L-2026-0046', NULL, N'New', '2026-03-23 14:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0047', NULL, N'New', '2026-03-11 09:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0047', N'New', N'Contacted', '2026-03-22 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0048', NULL, N'New', '2026-02-02 16:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0048', N'New', N'Qualified', '2026-02-25 21:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0048', N'Qualified', N'In Negotiation', '2026-03-02 02:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0049', NULL, N'New', '2026-02-08 11:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0049', N'New', N'Contacted', '2026-02-18 16:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0049', N'Contacted', N'Proposal Sent', '2026-03-11 17:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0050', NULL, N'New', '2025-10-31 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0050', N'New', N'Contacted', '2025-11-03 19:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0050', N'Contacted', N'Proposal Sent', '2025-11-08 22:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0050', N'Proposal Sent', N'In Negotiation', '2025-12-06 02:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0050', N'In Negotiation', N'Disqualified', '2025-12-10 04:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0051', NULL, N'New', '2024-10-24 12:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0051', N'New', N'Qualified', '2024-11-02 11:44:54', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0051', N'Qualified', N'Proposal Sent', '2024-11-19 07:09:34', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0051', N'Proposal Sent', N'In Negotiation', '2024-12-25 03:35:59', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0052', NULL, N'New', '2025-07-29 11:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0052', N'New', N'Contacted', '2025-08-06 16:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0052', N'Contacted', N'Proposal Sent', '2025-08-31 21:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0052', N'Proposal Sent', N'In Negotiation', '2025-09-13 02:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0052', N'In Negotiation', N'Won', '2025-10-03 03:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0053', NULL, N'New', '2026-01-18 09:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0053', N'New', N'Contacted', '2026-01-24 14:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0054', NULL, N'New', '2025-08-07 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0054', N'New', N'Contacted', '2025-08-19 15:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0054', N'Contacted', N'Proposal Sent', '2025-09-11 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0054', N'Proposal Sent', N'In Negotiation', '2025-09-28 19:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0054', N'In Negotiation', N'Won', '2025-10-23 23:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0055', NULL, N'New', '2026-02-04 08:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0055', N'New', N'Contacted', '2026-02-10 09:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0056', NULL, N'New', '2026-01-11 08:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0056', N'New', N'Contacted', '2026-01-17 09:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0056', N'Contacted', N'Qualified', '2026-02-08 13:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0056', N'Qualified', N'In Negotiation', '2026-02-13 18:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0056', N'In Negotiation', N'Lost', '2026-02-16 23:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', N'Lost-Timing'),
('L-2026-0057', NULL, N'New', '2026-01-24 13:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0057', N'New', N'Qualified', '2026-02-23 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0057', N'Qualified', N'Proposal Sent', '2026-02-25 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0057', N'Proposal Sent', N'In Negotiation', '2026-03-16 21:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0057', N'In Negotiation', N'Disqualified', '2026-03-19 01:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0058', NULL, N'New', '2024-11-24 12:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0058', N'New', N'Contacted', '2024-12-17 05:13:20', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0058', N'Contacted', N'Proposal Sent', '2024-12-28 05:59:59', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0059', NULL, N'New', '2026-01-14 15:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0059', N'New', N'Contacted', '2026-01-22 17:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0059', N'Contacted', N'Qualified', '2026-02-08 20:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0059', N'Qualified', N'Proposal Sent', '2026-02-18 01:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0060', NULL, N'New', '2026-03-28 13:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0061', NULL, N'New', '2026-01-23 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0061', N'New', N'Contacted', '2026-02-15 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0061', N'Contacted', N'Qualified', '2026-03-09 20:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0061', N'Qualified', N'Proposal Sent', '2026-03-17 22:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0062', NULL, N'New', '2024-07-24 18:45:28', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0062', N'New', N'Contacted', '2024-08-31 20:25:30', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0062', N'Contacted', N'Qualified', '2024-10-06 02:30:48', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0062', N'Qualified', N'In Negotiation', '2024-11-11 21:05:40', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0062', N'In Negotiation', N'Lost', '2024-12-15 22:47:59', 'aa512e88-565a-44bb-84f6-612ecc49e949', N'Lost-Price'),
('L-2026-0063', NULL, N'New', '2024-10-20 13:14:27', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0063', N'New', N'Qualified', '2024-11-18 07:10:27', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0063', N'Qualified', N'In Negotiation', '2024-12-19 13:54:39', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0063', N'In Negotiation', N'Lost', '2024-12-24 17:59:59', 'aa512e88-565a-44bb-84f6-612ecc49e949', N'Lost-Competitor'),
('L-2026-0064', NULL, N'New', '2026-03-23 14:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0064', N'New', N'Contacted', '2026-04-15 18:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0065', NULL, N'New', '2024-11-12 14:06:20', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0065', N'New', N'Contacted', '2024-11-28 11:46:29', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0065', N'Contacted', N'Proposal Sent', '2024-12-09 13:37:37', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0065', N'Proposal Sent', N'In Negotiation', '2024-12-20 04:25:26', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0065', N'In Negotiation', N'Lost', '2024-12-27 01:11:59', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', N'Lost-Competitor'),
('L-2026-0066', NULL, N'New', '2025-12-10 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0066', N'New', N'Contacted', '2025-12-15 23:41:08', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0066', N'Contacted', N'Proposal Sent', '2025-12-21 01:31:39', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0066', N'Proposal Sent', N'In Negotiation', '2025-12-24 14:10:57', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0066', N'In Negotiation', N'Won', '2025-12-29 20:23:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0067', NULL, N'New', '2026-02-04 08:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0067', N'New', N'Contacted', '2026-03-06 12:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0067', N'Contacted', N'Qualified', '2026-03-14 17:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0067', N'Qualified', N'In Negotiation', '2026-04-01 22:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0067', N'In Negotiation', N'Disqualified', '2026-04-08 03:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0068', NULL, N'New', '2025-08-24 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0068', N'New', N'Proposal Sent', '2025-09-07 21:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0068', N'Proposal Sent', N'In Negotiation', '2025-10-06 02:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0068', N'In Negotiation', N'Won', '2025-10-15 07:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0069', NULL, N'New', '2026-02-09 09:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0069', N'New', N'Contacted', '2026-02-19 13:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0069', N'Contacted', N'Qualified', '2026-03-16 14:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0069', N'Qualified', N'Proposal Sent', '2026-04-08 17:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0069', N'Proposal Sent', N'In Negotiation', '2026-04-28 21:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0069', N'In Negotiation', N'Disqualified', '2026-05-03 00:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0070', NULL, N'New', '2026-01-25 15:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0070', N'New', N'Contacted', '2026-02-11 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0071', NULL, N'New', '2024-03-20 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0071', N'New', N'Contacted', '2024-06-03 12:23:10', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0071', N'Contacted', N'Proposal Sent', '2024-09-14 18:29:03', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0071', N'Proposal Sent', N'In Negotiation', '2024-11-05 19:48:06', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0071', N'In Negotiation', N'Lost', '2024-12-03 08:23:59', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-Timing'),
('L-2026-0072', NULL, N'New', '2024-05-15 22:40:42', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0072', N'New', N'Qualified', '2024-07-22 05:47:06', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0072', N'Qualified', N'In Negotiation', '2024-09-19 10:00:39', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0072', N'In Negotiation', N'Lost', '2024-12-08 22:47:59', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', N'Lost-No Decision'),
('L-2026-0073', NULL, N'New', '2025-09-02 14:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0073', N'New', N'Qualified', '2025-09-13 16:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0073', N'Qualified', N'Proposal Sent', '2025-09-26 21:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0073', N'Proposal Sent', N'In Negotiation', '2025-10-08 00:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0073', N'In Negotiation', N'Won', '2025-10-17 05:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0074', NULL, N'New', '2026-03-29 13:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0075', NULL, N'New', '2025-05-02 09:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0075', N'New', N'Contacted', '2025-05-09 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0075', N'Contacted', N'Qualified', '2025-05-26 17:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0075', N'Qualified', N'Proposal Sent', '2025-06-04 22:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0075', N'Proposal Sent', N'In Negotiation', '2025-06-30 03:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0075', N'In Negotiation', N'Lost', '2025-07-19 07:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-Timing'),
('L-2026-0076', NULL, N'New', '2025-12-01 12:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0076', N'New', N'Contacted', '2025-12-14 17:43:08', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0076', N'Contacted', N'Proposal Sent', '2025-12-27 08:38:32', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0076', N'Proposal Sent', N'In Negotiation', '2025-12-28 22:47:59', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0077', NULL, N'New', '2026-03-19 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0077', N'New', N'Contacted', '2026-04-06 19:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0078', NULL, N'New', '2025-08-16 11:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0078', N'New', N'Contacted', '2025-09-08 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0078', N'Contacted', N'Qualified', '2025-09-15 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0078', N'Qualified', N'Proposal Sent', '2025-10-14 21:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0078', N'Proposal Sent', N'In Negotiation', '2025-11-13 01:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0078', N'In Negotiation', N'Lost', '2025-11-30 03:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-No Budget'),
('L-2026-0079', NULL, N'New', '2024-12-23 12:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0079', N'New', N'Qualified', '2024-12-25 08:09:48', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0079', N'Qualified', N'Proposal Sent', '2024-12-26 12:59:39', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0079', N'Proposal Sent', N'In Negotiation', '2024-12-27 08:15:30', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0079', N'In Negotiation', N'Won', '2024-12-31 03:35:59', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0080', NULL, N'New', '2026-01-23 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0080', N'New', N'Contacted', '2026-01-31 17:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0080', N'Contacted', N'Qualified', '2026-02-28 21:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0080', N'Qualified', N'Proposal Sent', '2026-03-24 01:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0080', N'Proposal Sent', N'In Negotiation', '2026-04-12 05:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0081', NULL, N'New', '2025-04-24 08:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0081', N'New', N'Contacted', '2025-05-12 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0081', N'Contacted', N'Qualified', '2025-05-20 14:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0081', N'Qualified', N'In Negotiation', '2025-05-30 18:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0081', N'In Negotiation', N'Lost', '2025-06-17 23:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', N'Lost-Competitor'),
('L-2026-0082', NULL, N'New', '2025-11-12 12:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0082', N'New', N'Proposal Sent', '2025-12-02 16:40:56', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0082', N'Proposal Sent', N'In Negotiation', '2025-12-05 03:27:22', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0082', N'In Negotiation', N'Won', '2025-12-27 01:11:59', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0083', NULL, N'New', '2025-11-22 08:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0083', N'New', N'Qualified', '2025-11-28 12:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0083', N'Qualified', N'In Negotiation', '2025-12-17 17:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0084', NULL, N'New', '2026-03-09 11:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0084', N'New', N'Contacted', '2026-04-07 13:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0085', NULL, N'New', '2025-11-21 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0085', N'New', N'Qualified', '2025-12-14 06:05:13', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0085', N'Qualified', N'In Negotiation', '2025-12-20 08:15:56', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0085', N'In Negotiation', N'Lost', '2025-12-27 22:47:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', N'Lost-Competitor'),
('L-2026-0086', NULL, N'New', '2025-02-19 10:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0086', N'New', N'Qualified', '2025-02-28 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0086', N'Qualified', N'Proposal Sent', '2025-03-11 17:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0086', N'Proposal Sent', N'In Negotiation', '2025-04-04 21:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0086', N'In Negotiation', N'Won', '2025-04-13 23:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0087', NULL, N'New', '2026-02-02 16:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0087', N'New', N'Qualified', '2026-02-07 18:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0087', N'Qualified', N'In Negotiation', '2026-02-13 20:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0087', N'In Negotiation', N'Lost', '2026-02-18 21:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', N'Lost-Price'),
('L-2026-0088', NULL, N'New', '2025-12-16 13:01:55', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0088', N'New', N'Qualified', '2025-12-22 13:18:49', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0088', N'Qualified', N'In Negotiation', '2025-12-28 04:06:02', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0088', N'In Negotiation', N'Lost', '2025-12-30 10:47:59', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-Price'),
('L-2026-0089', NULL, N'New', '2026-03-06 12:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0089', N'New', N'Contacted', '2026-03-29 17:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0090', NULL, N'New', '2025-11-21 12:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0090', N'New', N'Contacted', '2025-12-11 09:39:50', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0090', N'Contacted', N'Proposal Sent', '2025-12-27 22:47:59', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0091', NULL, N'New', '2026-01-03 13:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0091', N'New', N'Contacted', '2026-01-11 16:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0091', N'Contacted', N'Qualified', '2026-01-16 20:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0091', N'Qualified', N'Proposal Sent', '2026-01-25 22:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0091', N'Proposal Sent', N'In Negotiation', '2026-02-21 23:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0091', N'In Negotiation', N'Won', '2026-03-01 04:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0092', NULL, N'New', '2025-03-11 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0092', N'New', N'Contacted', '2025-03-15 17:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0092', N'Contacted', N'Qualified', '2025-04-07 22:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0092', N'Qualified', N'Proposal Sent', '2025-04-18 00:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0092', N'Proposal Sent', N'In Negotiation', '2025-05-03 03:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0092', N'In Negotiation', N'Won', '2025-05-18 08:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0093', NULL, N'New', '2025-08-31 13:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0093', N'New', N'Qualified', '2025-09-15 17:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0093', N'Qualified', N'Proposal Sent', '2025-10-04 21:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0093', N'Proposal Sent', N'In Negotiation', '2025-10-06 23:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0093', N'In Negotiation', N'Won', '2025-10-26 01:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0094', NULL, N'New', '2026-02-05 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0094', N'New', N'Qualified', '2026-02-07 18:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0094', N'Qualified', N'Proposal Sent', '2026-03-03 19:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0095', NULL, N'New', '2025-07-12 13:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0095', N'New', N'Qualified', '2025-07-27 14:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0095', N'Qualified', N'Proposal Sent', '2025-08-04 16:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0095', N'Proposal Sent', N'In Negotiation', '2025-08-08 18:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0095', N'In Negotiation', N'Won', '2025-09-06 21:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0096', NULL, N'New', '2026-02-04 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0096', N'New', N'Contacted', '2026-03-03 16:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0097', NULL, N'New', '2026-03-15 15:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0097', N'New', N'Contacted', '2026-04-07 20:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0097', N'Contacted', N'Qualified', '2026-04-11 23:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0098', NULL, N'New', '2024-11-09 12:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0098', N'New', N'Qualified', '2024-11-10 22:48:11', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0098', N'Qualified', N'Proposal Sent', '2024-11-26 05:41:07', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0098', N'Proposal Sent', N'In Negotiation', '2024-12-18 00:27:22', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0098', N'In Negotiation', N'Won', '2024-12-26 17:59:59', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0099', NULL, N'New', '2026-04-17 17:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0100', NULL, N'New', '2026-01-29 15:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0100', N'New', N'Qualified', '2026-02-17 17:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0101', NULL, N'New', '2024-11-30 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0101', N'New', N'Proposal Sent', '2024-12-12 13:52:37', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0101', N'Proposal Sent', N'In Negotiation', '2024-12-15 09:07:43', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0101', N'In Negotiation', N'Won', '2024-12-28 20:23:59', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0102', NULL, N'New', '2026-04-06 11:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0102', N'New', N'Contacted', '2026-04-27 15:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0103', NULL, N'New', '2025-06-19 10:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0103', N'New', N'Qualified', '2025-07-14 11:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0103', N'Qualified', N'Proposal Sent', '2025-08-08 12:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0103', N'Proposal Sent', N'In Negotiation', '2025-08-21 15:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0103', N'In Negotiation', N'Lost', '2025-09-04 17:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', N'Lost-Price'),
('L-2026-0104', NULL, N'New', '2026-01-18 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0104', N'New', N'Qualified', '2026-02-10 20:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0105', NULL, N'New', '2025-06-26 11:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0105', N'New', N'Qualified', '2025-07-06 15:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0105', N'Qualified', N'Proposal Sent', '2025-07-18 19:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0105', N'Proposal Sent', N'In Negotiation', '2025-07-24 20:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0105', N'In Negotiation', N'Lost', '2025-08-19 23:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', N'Lost-Price'),
('L-2026-0106', NULL, N'New', '2025-12-21 12:58:14', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0106', N'New', N'Qualified', '2025-12-30 22:47:59', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0107', NULL, N'New', '2025-04-20 10:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0107', N'New', N'Qualified', '2025-05-03 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0107', N'Qualified', N'Proposal Sent', '2025-05-21 15:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0107', N'Proposal Sent', N'In Negotiation', '2025-06-08 19:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0107', N'In Negotiation', N'Won', '2025-06-20 23:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0108', NULL, N'New', '2025-11-17 13:54:53', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0108', N'New', N'Qualified', '2025-12-05 13:22:10', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0108', N'Qualified', N'Proposal Sent', '2025-12-11 09:08:44', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0108', N'Proposal Sent', N'In Negotiation', '2025-12-15 23:33:44', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0108', N'In Negotiation', N'Won', '2025-12-27 13:11:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0109', NULL, N'New', '2025-12-22 12:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0109', N'New', N'Qualified', '2025-12-31 01:11:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0110', NULL, N'New', '2026-03-10 17:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0110', N'New', N'Qualified', '2026-04-09 20:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0111', NULL, N'New', '2026-02-20 16:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0111', N'New', N'Contacted', '2026-03-11 21:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0111', N'Contacted', N'Qualified', '2026-04-10 22:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0112', NULL, N'New', '2025-08-06 14:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0112', N'New', N'Qualified', '2025-08-21 16:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0112', N'Qualified', N'In Negotiation', '2025-09-09 19:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0112', N'In Negotiation', N'Lost', '2025-09-15 22:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', N'Lost-No Budget'),
('L-2026-0113', NULL, N'New', '2024-12-06 14:06:19', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0113', N'New', N'Proposal Sent', '2024-12-14 14:58:10', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0113', N'Proposal Sent', N'In Negotiation', '2024-12-21 09:30:52', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0113', N'In Negotiation', N'Won', '2024-12-29 10:47:59', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0114', NULL, N'New', '2025-06-06 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0114', N'New', N'Contacted', '2025-07-02 17:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0114', N'Contacted', N'Proposal Sent', '2025-07-25 20:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0114', N'Proposal Sent', N'In Negotiation', '2025-07-31 22:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0114', N'In Negotiation', N'Lost', '2025-08-04 02:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-No Budget'),
('L-2026-0115', NULL, N'New', '2025-09-01 16:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0115', N'New', N'Contacted', '2025-09-24 17:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0115', N'Contacted', N'Proposal Sent', '2025-10-06 22:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0115', N'Proposal Sent', N'In Negotiation', '2025-10-25 02:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0115', N'In Negotiation', N'Lost', '2025-11-18 06:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', N'Lost-No Budget'),
('L-2026-0116', NULL, N'New', '2024-12-10 13:11:57', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0116', N'New', N'Qualified', '2024-12-15 14:20:12', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0116', N'Qualified', N'Proposal Sent', '2024-12-19 22:55:21', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0116', N'Proposal Sent', N'In Negotiation', '2024-12-27 04:35:33', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0116', N'In Negotiation', N'Won', '2024-12-29 20:23:59', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0117', NULL, N'New', '2026-01-31 16:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0117', N'New', N'Contacted', '2026-02-18 17:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0118', NULL, N'New', '2025-07-15 13:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0118', N'New', N'Contacted', '2025-07-24 15:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0118', N'Contacted', N'Qualified', '2025-08-18 18:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0118', N'Qualified', N'Proposal Sent', '2025-08-29 21:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0118', N'Proposal Sent', N'In Negotiation', '2025-09-02 00:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0118', N'In Negotiation', N'Won', '2025-09-19 02:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0119', NULL, N'New', '2025-10-09 13:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0119', N'New', N'Contacted', '2025-11-02 14:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0119', N'Contacted', N'Proposal Sent', '2025-11-29 18:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0119', N'Proposal Sent', N'In Negotiation', '2025-12-15 20:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0119', N'In Negotiation', N'Won', '2025-12-23 21:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0120', NULL, N'New', '2025-10-07 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0120', N'New', N'Contacted', '2025-10-20 15:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0120', N'Contacted', N'Proposal Sent', '2025-10-23 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0120', N'Proposal Sent', N'In Negotiation', '2025-11-03 21:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0120', N'In Negotiation', N'Lost', '2025-11-06 01:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-No Decision'),
('L-2026-0121', NULL, N'New', '2026-01-20 15:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0121', N'New', N'Qualified', '2026-01-24 19:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0122', NULL, N'New', '2025-04-02 09:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0122', N'New', N'Qualified', '2025-04-19 14:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0122', N'Qualified', N'In Negotiation', '2025-04-24 19:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0122', N'In Negotiation', N'Lost', '2025-05-06 21:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', N'Lost-Competitor'),
('L-2026-0123', NULL, N'New', '2025-09-30 09:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0123', N'New', N'Contacted', '2025-10-22 13:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0123', N'Contacted', N'Qualified', '2025-11-02 16:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0123', N'Qualified', N'In Negotiation', '2025-11-26 20:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0123', N'In Negotiation', N'Disqualified', '2025-12-05 01:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0124', NULL, N'New', '2025-10-15 15:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0124', N'New', N'Qualified', '2025-11-02 18:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0124', N'Qualified', N'In Negotiation', '2025-11-27 23:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0124', N'In Negotiation', N'Disqualified', '2025-12-22 01:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0125', NULL, N'New', '2026-01-25 13:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0125', N'New', N'Contacted', '2026-02-03 14:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0126', NULL, N'New', '2025-12-09 12:29:06', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0126', N'New', N'Qualified', '2025-12-17 08:40:35', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0126', N'Qualified', N'Proposal Sent', '2025-12-18 10:22:59', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0126', N'Proposal Sent', N'In Negotiation', '2025-12-25 07:46:41', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0126', N'In Negotiation', N'Lost', '2025-12-29 17:59:59', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', N'Lost-Competitor'),
('L-2026-0127', NULL, N'New', '2026-03-04 09:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0127', N'New', N'Contacted', '2026-03-24 14:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0127', N'Contacted', N'Proposal Sent', '2026-03-30 19:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0128', NULL, N'New', '2026-03-17 16:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0128', N'New', N'Contacted', '2026-04-15 18:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0128', N'Contacted', N'Qualified', '2026-04-27 23:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0129', NULL, N'New', '2026-01-11 10:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0129', N'New', N'Contacted', '2026-01-19 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0129', N'Contacted', N'Qualified', '2026-01-30 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0130', NULL, N'New', '2025-10-27 16:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0130', N'New', N'Contacted', '2025-10-30 18:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0130', N'Contacted', N'Qualified', '2025-11-26 22:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0130', N'Qualified', N'In Negotiation', '2025-12-12 03:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0130', N'In Negotiation', N'Lost', '2025-12-18 04:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', N'Lost-Product Fit'),
('L-2026-0131', NULL, N'New', '2026-01-31 11:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0131', N'New', N'Qualified', '2026-02-11 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0132', NULL, N'New', '2025-10-13 15:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0132', N'New', N'Qualified', '2025-10-16 18:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0132', N'Qualified', N'Proposal Sent', '2025-11-05 21:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0132', N'Proposal Sent', N'In Negotiation', '2025-12-04 22:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0132', N'In Negotiation', N'Lost', '2025-12-14 00:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-Price'),
('L-2026-0133', NULL, N'New', '2025-09-09 13:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0133', N'New', N'Proposal Sent', '2025-09-21 14:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0133', N'Proposal Sent', N'In Negotiation', '2025-09-27 18:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0133', N'In Negotiation', N'Won', '2025-10-05 21:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0134', NULL, N'New', '2025-12-22 12:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0134', N'New', N'Contacted', '2025-12-23 01:31:54', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0134', N'Contacted', N'Proposal Sent', '2025-12-26 18:41:33', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0134', N'Proposal Sent', N'In Negotiation', '2025-12-31 01:11:59', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0135', NULL, N'New', '2025-10-28 16:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0135', N'New', N'Qualified', '2025-11-10 19:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0135', N'Qualified', N'Proposal Sent', '2025-11-26 20:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0135', N'Proposal Sent', N'In Negotiation', '2025-12-06 21:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0135', N'In Negotiation', N'Won', '2025-12-20 02:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0136', NULL, N'New', '2025-08-18 09:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0136', N'New', N'Qualified', '2025-09-10 10:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0136', N'Qualified', N'In Negotiation', '2025-10-01 11:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0136', N'In Negotiation', N'Lost', '2025-10-08 12:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-Price'),
('L-2026-0137', NULL, N'New', '2026-01-25 13:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0137', N'New', N'Contacted', '2026-01-28 18:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0137', N'Contacted', N'Qualified', '2026-02-09 19:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0137', N'Qualified', N'Proposal Sent', '2026-02-13 23:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0137', N'Proposal Sent', N'In Negotiation', '2026-03-02 00:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0137', N'In Negotiation', N'Disqualified', '2026-03-26 04:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0138', NULL, N'New', '2026-03-30 17:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0139', NULL, N'New', '2026-02-09 10:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0139', N'New', N'Contacted', '2026-02-24 11:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0140', NULL, N'New', '2025-04-16 15:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0140', N'New', N'Qualified', '2025-04-29 17:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0140', N'Qualified', N'Proposal Sent', '2025-05-21 19:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0140', N'Proposal Sent', N'In Negotiation', '2025-06-05 21:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0140', N'In Negotiation', N'Won', '2025-06-22 22:00:00', 'f1460724-4d7f-4796-bca7-50bf1ab09811', NULL),
('L-2026-0141', NULL, N'New', '2026-04-18 09:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0142', NULL, N'New', '2025-08-26 10:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0142', N'New', N'Contacted', '2025-09-06 15:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0142', N'Contacted', N'Proposal Sent', '2025-09-14 19:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0142', N'Proposal Sent', N'In Negotiation', '2025-10-13 22:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0142', N'In Negotiation', N'Won', '2025-10-29 00:00:00', '655612cb-fc04-4fa8-a2de-87a6a55d30c7', NULL),
('L-2026-0143', NULL, N'New', '2025-02-18 16:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0143', N'New', N'Qualified', '2025-03-03 19:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0143', N'Qualified', N'Proposal Sent', '2025-03-28 20:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0143', N'Proposal Sent', N'In Negotiation', '2025-04-28 01:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0143', N'In Negotiation', N'Won', '2025-05-03 06:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0144', NULL, N'New', '2025-10-15 14:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0144', N'New', N'Qualified', '2025-10-23 15:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0144', N'Qualified', N'Proposal Sent', '2025-11-04 20:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0144', N'Proposal Sent', N'In Negotiation', '2025-11-28 23:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0144', N'In Negotiation', N'Won', '2025-12-02 04:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0145', NULL, N'New', '2025-08-27 16:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0145', N'New', N'Qualified', '2025-09-22 21:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0145', N'Qualified', N'Proposal Sent', '2025-09-25 01:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0145', N'Proposal Sent', N'In Negotiation', '2025-10-02 03:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0145', N'In Negotiation', N'Lost', '2025-10-23 06:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', N'Lost-No Decision'),
('L-2026-0146', NULL, N'New', '2025-11-04 13:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0146', N'New', N'Qualified', '2025-11-07 14:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0146', N'Qualified', N'In Negotiation', '2025-11-27 15:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0147', NULL, N'New', '2025-01-22 12:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0147', N'New', N'Qualified', '2025-02-10 16:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0147', N'Qualified', N'Proposal Sent', '2025-03-06 21:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0147', N'Proposal Sent', N'In Negotiation', '2025-03-17 02:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0147', N'In Negotiation', N'Won', '2025-03-27 06:00:00', 'd3e4851b-02e7-4bc3-86d8-cbd82d96c2e5', NULL),
('L-2026-0148', NULL, N'New', '2026-01-24 08:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0148', N'New', N'Contacted', '2026-01-28 09:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0148', N'Contacted', N'Qualified', '2026-02-16 12:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0148', N'Qualified', N'Proposal Sent', '2026-03-03 14:00:00', 'f3f1ab23-29da-4ea3-8377-befc37b314f1', NULL),
('L-2026-0149', NULL, N'New', '2025-05-23 15:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0149', N'New', N'Qualified', '2025-06-16 19:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0149', N'Qualified', N'In Negotiation', '2025-06-18 23:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', NULL),
('L-2026-0149', N'In Negotiation', N'Lost', '2025-06-23 01:00:00', '45f0d7fa-7580-4d9e-98e2-495aa86f75c9', N'Lost-No Budget'),
('L-2026-0150', NULL, N'New', '2025-09-15 11:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0150', N'New', N'Qualified', '2025-09-30 16:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0150', N'Qualified', N'Proposal Sent', '2025-10-28 17:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0150', N'Proposal Sent', N'In Negotiation', '2025-11-21 22:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', NULL),
('L-2026-0150', N'In Negotiation', N'Lost', '2025-12-01 02:00:00', 'aa512e88-565a-44bb-84f6-612ecc49e949', N'Lost-No Budget');
;
GO


INSERT INTO lead_bant_point (lead_id, budget, authority, need, timeline) VALUES
('L-2026-0001', 21, 19, 23, 20),
('L-2026-0002', 24, 20, 21, 23),
('L-2026-0003', 19, 24, 18, 21),
('L-2026-0004', 15, 19, 20, 16),
('L-2026-0005', 20, 21, 24, 18),
('L-2026-0006', 11, 13, 15, 10), -- Điểm thấp tương ứng Lead Lost
('L-2026-0007', 18, 16, 20, 21),
('L-2026-0008', 21, 23, 19, 20),
('L-2026-0009', 19, 18, 21, 19),
('L-2026-0010', 23, 24, 25, 21),
('L-2026-0011', 21, 20, 19, 23),
('L-2026-0012', 16, 19, 15, 18),
('L-2026-0013', 20, 21, 20, 21),
('L-2026-0014', 24, 18, 23, 20),
('L-2026-0015', 14, 11, 13, 16),
('L-2026-0016', 19, 20, 18, 19),
('L-2026-0017', 13, 16, 11, 14),
('L-2026-0018', 24, 25, 23, 24),
('L-2026-0019', 10, 14, 13, 11),
('L-2026-0020', 20, 23, 21, 20),
('L-2026-0021', 14, 15, 14, 16),
('L-2026-0022', 23, 21, 24, 20),
('L-2026-0023', 19, 18, 21, 19),
('L-2026-0024', 15, 14, 16, 13),
('L-2026-0025', 21, 19, 20, 23),
('L-2026-0026', 23, 24, 21, 20),
('L-2026-0027', 18, 21, 24, 19),
('L-2026-0028', 21, 18, 20, 21),
('L-2026-0029', 25, 23, 24, 25),
('L-2026-0030', 20, 21, 19, 20),
('L-2026-0031', 24, 20, 21, 23),
('L-2026-0032', 19, 24, 20, 19),
('L-2026-0033', 21, 19, 23, 21),
('L-2026-0034', 10, 9, 14, 11), -- No Budget
('L-2026-0035', 23, 24, 21, 20),
('L-2026-0036', 14, 15, 11, 14), -- Product Fit Issue
('L-2026-0037', 13, 14, 16, 13),
('L-2026-0038', 11, 16, 14, 10),
('L-2026-0039', 16, 14, 13, 16),
('L-2026-0040', 18, 21, 19, 18),
('L-2026-0041', 14, 10, 16, 14),
('L-2026-0042', 21, 18, 21, 20),
('L-2026-0043', 24, 21, 23, 24),
('L-2026-0044', 25, 24, 23, 21),
('L-2026-0045', 15, 16, 14, 13),
('L-2026-0046', 19, 20, 21, 18),
('L-2026-0047', 21, 19, 18, 21),
('L-2026-0048', 18, 24, 21, 19),
('L-2026-0049', 20, 21, 23, 21),
('L-2026-0050', 14, 13, 11, 15),
('L-2026-0051', 21, 19, 20, 21),
('L-2026-0052', 23, 24, 21, 23),
('L-2026-0053', 19, 20, 19, 21),
('L-2026-0054', 20, 21, 24, 23),
('L-2026-0055', 18, 19, 21, 18),
('L-2026-0056', 14, 16, 13, 11), -- Timing Issue
('L-2026-0057', 11, 13, 16, 14),
('L-2026-0058', 20, 21, 19, 20),
('L-2026-0059', 19, 24, 21, 20),
('L-2026-0060', 21, 19, 20, 18),
('L-2026-0061', 20, 21, 21, 24),
('L-2026-0062', 16, 14, 15, 16),
('L-2026-0063', 13, 16, 14, 13),
('L-2026-0064', 19, 20, 19, 21),
('L-2026-0065', 16, 14, 16, 13),
('L-2026-0066', 24, 20, 21, 23),
('L-2026-0067', 10, 16, 14, 13),
('L-2026-0068', 25, 24, 25, 23),
('L-2026-0069', 14, 13, 16, 11),
('L-2026-0070', 19, 21, 20, 19),
('L-2026-0071', 15, 14, 13, 16),
('L-2026-0072', 14, 11, 14, 13),
('L-2026-0073', 23, 21, 24, 20),
('L-2026-0074', 20, 19, 21, 21),
('L-2026-0075', 14, 16, 13, 11),
('L-2026-0076', 21, 24, 20, 19),
('L-2026-0077', 19, 20, 19, 21),
('L-2026-0078', 11, 14, 16, 13),
('L-2026-0079', 24, 20, 21, 23),
('L-2026-0080', 19, 18, 20, 19),
('L-2026-0081', 14, 16, 14, 15),
('L-2026-0082', 20, 24, 21, 20),
('L-2026-0083', 19, 20, 18, 21),
('L-2026-0084', 21, 19, 20, 24),
('L-2026-0085', 16, 14, 16, 13),
('L-2026-0086', 23, 21, 24, 20),
('L-2026-0087', 14, 16, 13, 14),
('L-2026-0088', 15, 14, 16, 15),
('L-2026-0089', 19, 21, 18, 19),
('L-2026-0090', 20, 19, 21, 20),
('L-2026-0091', 24, 23, 25, 21),
('L-2026-0092', 21, 20, 24, 23),
('L-2026-0093', 23, 21, 23, 21),
('L-2026-0094', 19, 20, 19, 21),
('L-2026-0095', 20, 24, 21, 20),
('L-2026-0096', 18, 19, 20, 19),
('L-2026-0097', 21, 20, 19, 21),
('L-2026-0098', 24, 21, 23, 24),
('L-2026-0099', 20, 19, 21, 20),
('L-2026-0100', 19, 21, 18, 19),
('L-2026-0101', 23, 21, 24, 20),
('L-2026-0102', 19, 18, 21, 20),
('L-2026-0103', 14, 16, 13, 14),
('L-2026-0104', 21, 19, 20, 21),
('L-2026-0105', 16, 13, 16, 15),
('L-2026-0106', 18, 21, 19, 20),
('L-2026-0107', 24, 20, 21, 23),
('L-2026-0108', 23, 24, 20, 21),
('L-2026-0109', 21, 20, 19, 24),
('L-2026-0110', 19, 21, 20, 18),
('L-2026-0111', 20, 19, 18, 21),
('L-2026-0112', 11, 13, 16, 14),
('L-2026-0113', 23, 25, 24, 20),
('L-2026-0114', 14, 15, 14, 11),
('L-2026-0115', 13, 11, 16, 14),
('L-2026-0116', 24, 21, 20, 23),
('L-2026-0117', 19, 20, 19, 21),
('L-2026-0118', 21, 23, 21, 20),
('L-2026-0119', 23, 21, 24, 20),
('L-2026-0120', 14, 16, 13, 14),
('L-2026-0121', 20, 19, 21, 20),
('L-2026-0122', 15, 14, 16, 15),
('L-2026-0123', 11, 16, 13, 14),
('L-2026-0124', 14, 13, 11, 16),
('L-2026-0125', 19, 21, 18, 20),
('L-2026-0126', 16, 14, 15, 13),
('L-2026-0127', 20, 21, 19, 21),
('L-2026-0128', 19, 18, 21, 20),
('L-2026-0129', 21, 20, 19, 18),
('L-2026-0130', 13, 16, 14, 13),
('L-2026-0131', 20, 24, 21, 19),
('L-2026-0132', 16, 14, 15, 16),
('L-2026-0133', 23, 21, 20, 24),
('L-2026-0134', 21, 19, 21, 20),
('L-2026-0135', 24, 21, 23, 21),
('L-2026-0136', 15, 14, 16, 13),
('L-2026-0137', 14, 15, 13, 11),
('L-2026-0138', 19, 21, 18, 20),
('L-2026-0139', 21, 18, 20, 19),
('L-2026-0140', 24, 20, 24, 21),
('L-2026-0141', 18, 21, 19, 20),
('L-2026-0142', 23, 21, 20, 24),
('L-2026-0143', 24, 23, 21, 20),
('L-2026-0144', 23, 21, 24, 23),
('L-2026-0145', 14, 16, 13, 15),
('L-2026-0146', 21, 18, 21, 20),
('L-2026-0147', 24, 21, 20, 23),
('L-2026-0148', 19, 20, 18, 21),
('L-2026-0149', 13, 14, 11, 16),
('L-2026-0150', 14, 10, 13, 14);
GO




-- =================================================================================
-- COMPANY TARGET SAMPLE DATA
-- =================================================================================
INSERT INTO company_target (target_type, period_quarter, period_year, revenue_target, created_by)
VALUES
    ('YEARLY', NULL, 2026, 250000000000.00, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
    ('QUARTERLY', 1, 2026, 50000000000.00, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
    ('QUARTERLY', 2, 2026, 65000000000.00, '21cf3ed1-c2eb-410c-8098-bf3020e06991'),
    ('QUARTERLY', 3, 2026, 80000000000.00, '21cf3ed1-c2eb-410c-8098-bf3020e06991');
GO

-- =================================================================================
-- 3.8 SYNCHRONIZE PRODUCT AND SOURCE NAMES IN LEAD TABLE
-- =================================================================================
UPDATE lead
SET source_name = (
    SELECT MAX(source_name)
    FROM lead_source
    WHERE lead_source.source_id = lead.source_id
)
WHERE source_name IS NULL;

UPDATE lead
SET product_name = (
    SELECT MAX(p.product_name)
    FROM lead_item li
    JOIN product p ON li.product_id = p.product_id
    WHERE li.lead_id = lead.lead_id
)
WHERE product_name IS NULL;
GO

-- =================================================================================
-- 4. ROW-LEVEL SECURITY & POWER BI VIEWS FOR DYNAMIC YEAR/QUARTER FILTERING
-- =================================================================================

-- Create schema for security objects if not exists
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'Security')
BEGIN
EXEC('CREATE SCHEMA Security');
END
GO

-- Create Predicate Function for Lead filtering
CREATE OR ALTER FUNCTION Security.fn_lead_securitypredicate(@created_date DATE)
    RETURNS TABLE
    WITH SCHEMABINDING
    AS
    RETURN SELECT 1 AS fn_securitypredicate_result
               WHERE (SESSION_CONTEXT(N'selected_year') IS NULL OR YEAR(@created_date) = CAST(SESSION_CONTEXT(N'selected_year') AS INT))
      AND (SESSION_CONTEXT(N'selected_quarter') IS NULL OR DATEPART(QUARTER, @created_date) = CAST(SESSION_CONTEXT(N'selected_quarter') AS INT));
GO

-- Create Security Policy for Lead table
IF EXISTS (SELECT * FROM sys.security_policies WHERE name = 'LeadFilter')
BEGIN
    DROP SECURITY POLICY Security.LeadFilter;
END
GO

CREATE SECURITY POLICY Security.LeadFilter
ADD FILTER PREDICATE Security.fn_lead_securitypredicate(created_date) ON dbo.lead
WITH (STATE = ON);
GO

-- Create Predicate Function for Sales Target filtering
CREATE OR ALTER FUNCTION Security.fn_target_securitypredicate(@period_year INT, @period_month INT)
    RETURNS TABLE
    WITH SCHEMABINDING
    AS
    RETURN SELECT 1 AS fn_target_securitypredicate_result
               WHERE (SESSION_CONTEXT(N'selected_year') IS NULL OR @period_year = CAST(SESSION_CONTEXT(N'selected_year') AS INT))
      AND (SESSION_CONTEXT(N'selected_quarter') IS NULL OR ((@period_month - 1) / 3 + 1) = CAST(SESSION_CONTEXT(N'selected_quarter') AS INT));
GO

-- Create Security Policy for Sales Target table
IF EXISTS (SELECT * FROM sys.security_policies WHERE name = 'TargetFilter')
BEGIN
    DROP SECURITY POLICY Security.TargetFilter;
END
GO

CREATE SECURITY POLICY Security.TargetFilter
ADD FILTER PREDICATE Security.fn_target_securitypredicate(period_year, period_month) ON dbo.sales_target
WITH (STATE = ON);
GO

-- Create Predicate Function for Company Target filtering
CREATE OR ALTER FUNCTION Security.fn_company_target_securitypredicate(@period_quarter INT, @period_year INT)
    RETURNS TABLE
    WITH SCHEMABINDING
    AS
    RETURN SELECT 1 AS fn_company_target_securitypredicate_result
               WHERE (SESSION_CONTEXT(N'selected_year') IS NULL OR @period_year = CAST(SESSION_CONTEXT(N'selected_year') AS INT))
      AND (SESSION_CONTEXT(N'selected_quarter') IS NULL OR @period_quarter IS NULL OR @period_quarter = CAST(SESSION_CONTEXT(N'selected_quarter') AS INT));
GO

-- Create Security Policy for Company Target table
IF EXISTS (SELECT * FROM sys.security_policies WHERE name = 'CompanyTargetFilter')
BEGIN
    DROP SECURITY POLICY Security.CompanyTargetFilter;
END
GO

CREATE SECURITY POLICY Security.CompanyTargetFilter
ADD FILTER PREDICATE Security.fn_company_target_securitypredicate(period_quarter, period_year) ON dbo.company_target
WITH (STATE = ON);
GO

-- View giúp Power BI dễ dàng phân tích theo năm/quý/tháng
CREATE OR ALTER VIEW v_lead_power_bi AS
SELECT
    l.*,
    YEAR(l.created_date) AS created_year,
    DATEPART(QUARTER, l.created_date) AS created_quarter,
    MONTH(l.created_date) AS created_month,
    DATENAME(MONTH, l.created_date) AS created_month_name
FROM lead l;
GO

SELECT
    COUNT(*) AS won_count,
    SUM(business_result) AS won_revenue
FROM lead
WHERE status = 'Won';


SELECT
    period_year,
    SUM(revenue_target) / 1000000000.0 AS Total_Billion
FROM sales_target
GROUP BY period_year
ORDER BY period_year;


SELECT
    period_year AS [Năm],
      CASE
          WHEN period_month = 3 THEN 'Quý 1'
          WHEN period_month = 6 THEN 'Quý 2'
          WHEN period_month = 9 THEN 'Quý 3'
          WHEN period_month = 12 THEN 'Quý 4'
END AS [Quý],
      revenue_target / 1000000000.0 AS [Target (Tỷ VNĐ)]
  FROM sales_target st
  JOIN [user] u ON st.user_id = u.user_id
  WHERE u.user_code = 'NV002'
  ORDER BY period_year, period_month;