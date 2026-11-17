-- =============================================================================
-- File: script/02_seed.sql
-- Project: Chat System (Version 1 — Deterministic Seed Data)
-- Target Engine: PostgreSQL 15+ (UTF-8)
-- Prerequisite: Execute script/01_schema.sql first on a clean database.
--
-- IMPORTANT NOTES ON FIXTURES & CREDENTIALS:
-- 1. All records in this script are deterministic synthetic test fixtures.
-- 2. Password hashes use standard PBKDF2-HMAC-SHA256 (310,000 iterations,
--    256-bit derived key, salt 'chatsystem-seed-2026') generated via Python's
--    standard hashlib.pbkdf2_hmac / Java 21 PBKDF2WithHmacSHA256:
--      - Admin synthetic test password:   AdminDemo#2026
--      - Regular synthetic test password: UserDemo#2026
--    These are synthetic local test credentials only and must never be used
--    in production.
-- 3. Rows in user_public_keys, group_key_versions, group_key_envelopes, and
--    encrypted group_messages are synthetic storage fixtures demonstrating
--    schema constraints, not evidence of live end-to-end cryptography.
--    No private keys or plaintext group keys are stored.
-- 4. All identity sequences are synchronized at the end of the transaction
--    using setval(pg_get_serial_sequence(...), MAX(id), true).
-- =============================================================================

BEGIN;

SET client_encoding = 'UTF8';
SET TIME ZONE 'Asia/Ho_Chi_Minh';

-- -----------------------------------------------------------------------------
-- 1. Seed users (IDs 1..26)
--   - IDs 1..24: 24 deterministic Vietnamese user accounts matching the Swing
--     UI mock dataset (active, locked, and zero-activity/zero-friend users).
--   - ID 25: 'admin' (role = 'ADMIN', created on 2025-12-31 23:00:00+07).
--   - ID 26: 'deleteduser01' (soft-deleted account with deleted_at set,
--     created on 2026-02-10+07 to test historical inclusion vs. current
--     friend-graph exclusion).
--   - Note on ID 1 ('nguyenvanan'): created_at is '2025-12-31 23:30:00+00',
--     which is '2026-01-01 06:30:00+07' in Asia/Ho_Chi_Minh (January 2026).
-- -----------------------------------------------------------------------------
INSERT INTO users (
    id, username, password_hash, full_name, address, date_of_birth,
    gender, email, role, is_locked, created_at, updated_at, deleted_at
) VALUES
    (1,  'nguyenvanan',    '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Nguyễn Văn An',         '12 Nguyễn Huệ, Quận 1, TP.HCM',                  DATE '2002-03-14', 'MALE',   'nguyenvanan@chatsystem.vn',     'USER',  FALSE, TIMESTAMPTZ '2025-12-31 23:30:00+00', TIMESTAMPTZ '2026-01-01 06:30:00+07', NULL),
    (2,  'tranthibich',    '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Trần Thị Bích Ngọc',    '45 Lê Lợi, Quận Hải Châu, Đà Nẵng',              DATE '2003-07-22', 'FEMALE', 'bichngoc.tran@chatsystem.vn',   'USER',  FALSE, TIMESTAMPTZ '2026-01-15 09:00:00+07', TIMESTAMPTZ '2026-01-15 09:00:00+07', NULL),
    (3,  'lehoangnam',     '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Lê Hoàng Nam',          '88 Cầu Giấy, Quận Cầu Giấy, Hà Nội',             DATE '2001-11-05', 'MALE',   'hoangnam.le@chatsystem.vn',     'USER',  FALSE, TIMESTAMPTZ '2026-01-18 10:15:00+07', TIMESTAMPTZ '2026-01-18 10:15:00+07', NULL),
    (4,  'phamminhchau',   '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Phạm Minh Châu',        '210 Điện Biên Phủ, Quận Bình Thạnh, TP.HCM',     DATE '2003-05-30', 'FEMALE', 'minhchau.pham@chatsystem.vn',   'USER',  TRUE,  TIMESTAMPTZ '2026-01-24 14:20:00+07', TIMESTAMPTZ '2026-03-10 11:00:00+07', NULL),
    (5,  'hoangquocviet',  '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Hoàng Quốc Việt',       '19 Trần Phú, TP. Nha Trang, Khánh Hòa',          DATE '2000-09-12', 'MALE',   'quocviet.hoang@chatsystem.vn',  'USER',  FALSE, TIMESTAMPTZ '2026-02-02 08:30:00+07', TIMESTAMPTZ '2026-02-02 08:30:00+07', NULL),
    (6,  'vuthithao',      '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Vũ Thị Phương Thảo',    '63 Lạch Tray, Quận Ngô Quyền, Hải Phòng',        DATE '2004-02-18', 'FEMALE', 'phuongthao.vu@chatsystem.vn',   'USER',  FALSE, TIMESTAMPTZ '2026-02-08 11:00:00+07', TIMESTAMPTZ '2026-02-08 11:00:00+07', NULL),
    (7,  'dangquanghuy',   '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Đặng Quang Huy',        '105 Nguyễn Văn Cừ, Quận 5, TP.HCM',              DATE '2002-12-01', 'MALE',   'quanghuy.dang@chatsystem.vn',   'USER',  FALSE, TIMESTAMPTZ '2026-02-14 16:45:00+07', TIMESTAMPTZ '2026-02-14 16:45:00+07', NULL),
    (8,  'buithanhha',     '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Bùi Thanh Hà',          '34 Kim Mã, Quận Ba Đình, Hà Nội',                DATE '2003-04-09', 'FEMALE', 'thanhha.bui@chatsystem.vn',     'USER',  TRUE,  TIMESTAMPTZ '2026-02-20 19:10:00+07', TIMESTAMPTZ '2026-03-15 15:00:00+07', NULL),
    (9,  'dinhgiabao',     '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Đinh Gia Bảo',          '77 Nguyễn Trãi, Quận Ninh Kiều, Cần Thơ',        DATE '2001-08-25', 'MALE',   'giabao.dinh@chatsystem.vn',     'USER',  FALSE, TIMESTAMPTZ '2026-03-01 07:50:00+07', TIMESTAMPTZ '2026-03-01 07:50:00+07', NULL),
    (10, 'ngothuyduong',   '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Ngô Thùy Dương',        '152 Võ Văn Ngân, TP. Thủ Đức, TP.HCM',           DATE '2004-06-17', 'FEMALE', 'thuyduong.ngo@chatsystem.vn',   'USER',  FALSE, TIMESTAMPTZ '2026-03-06 13:15:00+07', TIMESTAMPTZ '2026-03-06 13:15:00+07', NULL),
    (11, 'duongtuananh',   '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Dương Tuấn Anh',        '28 Hùng Vương, TP. Huế, Thừa Thiên Huế',         DATE '2002-01-29', 'MALE',   'tuananh.duong@chatsystem.vn',   'USER',  FALSE, TIMESTAMPTZ '2026-03-12 15:40:00+07', TIMESTAMPTZ '2026-03-12 15:40:00+07', NULL),
    (12, 'lykhanhlinh',    '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Lý Khánh Linh',         '91 Nguyễn Thị Minh Khai, Quận 3, TP.HCM',        DATE '2003-10-11', 'FEMALE', 'khanhlinh.ly@chatsystem.vn',    'USER',  FALSE, TIMESTAMPTZ '2026-03-19 20:05:00+07', TIMESTAMPTZ '2026-03-19 20:05:00+07', NULL),
    (13, 'truongducthang', '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Trương Đức Thắng',      '56 Giải Phóng, Quận Hai Bà Trưng, Hà Nội',       DATE '2000-04-03', 'MALE',   'ducthang.truong@chatsystem.vn', 'USER',  TRUE,  TIMESTAMPTZ '2026-03-25 10:00:00+07', TIMESTAMPTZ '2026-04-01 09:00:00+07', NULL),
    (14, 'haquynhnhu',     '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Hà Quỳnh Như',          '18 Lê Duẩn, TP. Vinh, Nghệ An',                  DATE '2004-08-07', 'FEMALE', 'quynhnhu.ha@chatsystem.vn',     'USER',  FALSE, TIMESTAMPTZ '2026-04-02 09:25:00+07', TIMESTAMPTZ '2026-04-02 09:25:00+07', NULL),
    (15, 'phanminhkhoi',   '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Phan Minh Khôi',        '134 Cộng Hòa, Quận Tân Bình, TP.HCM',            DATE '2002-05-19', 'MALE',   'minhkhoi.phan@chatsystem.vn',   'USER',  FALSE, TIMESTAMPTZ '2026-04-09 11:30:00+07', TIMESTAMPTZ '2026-04-09 11:30:00+07', NULL),
    (16, 'vothuytrang',    '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Võ Thùy Trang',         '40 Nguyễn Văn Linh, Quận Thanh Khê, Đà Nẵng',    DATE '2003-09-23', 'FEMALE', 'thuytrang.vo@chatsystem.vn',    'USER',  FALSE, TIMESTAMPTZ '2026-04-15 14:50:00+07', TIMESTAMPTZ '2026-04-15 14:50:00+07', NULL),
    (17, 'doanduckien',    '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Đoàn Trung Kiên',       '72 Trần Hưng Đạo, TP. Nam Định, Nam Định',       DATE '2001-02-14', 'MALE',   'trungkien.doan@chatsystem.vn',  'USER',  FALSE, TIMESTAMPTZ '2026-04-21 17:10:00+07', TIMESTAMPTZ '2026-04-21 17:10:00+07', NULL),
    (18, 'lamngocmai',     '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Lâm Ngọc Mai',          '205 Cách Mạng Tháng 8, Quận 10, TP.HCM',         DATE '2004-11-28', 'FEMALE', 'ngocmai.lam@chatsystem.vn',     'USER',  FALSE, TIMESTAMPTZ '2026-05-03 08:45:00+07', TIMESTAMPTZ '2026-05-03 08:45:00+07', NULL),
    (19, 'trinhhoanglong', '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Trịnh Hoàng Long',      '89 Xuân Thủy, Quận Cầu Giấy, Hà Nội',            DATE '2002-07-06', 'MALE',   'hoanglong.trinh@chatsystem.vn', 'USER',  FALSE, TIMESTAMPTZ '2026-05-11 12:00:00+07', TIMESTAMPTZ '2026-05-11 12:00:00+07', NULL),
    (20, 'maiphuongvy',    '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Mai Phương Vy',         '15 Đại lộ Bình Dương, TP. Thủ Dầu Một',          DATE '2003-03-16', 'FEMALE', 'phuongvy.mai@chatsystem.vn',    'USER',  FALSE, TIMESTAMPTZ '2026-05-18 15:20:00+07', TIMESTAMPTZ '2026-05-18 15:20:00+07', NULL),
    -- Users 21, 23, 24 are active accounts with ZERO activity and ZERO friends
    (21, 'caothevinh',     '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Cao Thế Vinh',          '64 Quang Trung, Quận Gò Vấp, TP.HCM',            DATE '2001-10-20', 'MALE',   'thevinh.cao@chatsystem.vn',     'USER',  FALSE, TIMESTAMPTZ '2026-05-25 18:30:00+07', TIMESTAMPTZ '2026-05-25 18:30:00+07', NULL),
    (22, 'chuthanhhuyen',  '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Chu Thanh Huyền',       '112 Hoàng Quốc Việt, Quận Bắc Từ Liêm, Hà Nội',  DATE '2004-01-08', 'FEMALE', 'thanhhuyen.chu@chatsystem.vn',  'USER',  FALSE, TIMESTAMPTZ '2026-06-01 09:10:00+07', TIMESTAMPTZ '2026-06-01 09:10:00+07', NULL),
    (23, 'luonggiahan',    '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Lương Gia Hân',         '29 Phạm Văn Đồng, TP. Thủ Đức, TP.HCM',          DATE '2003-06-12', 'FEMALE', 'giahan.luong@chatsystem.vn',    'USER',  FALSE, TIMESTAMPTZ '2026-06-07 10:40:00+07', TIMESTAMPTZ '2026-06-07 10:40:00+07', NULL),
    (24, 'taquocdat',      '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Tạ Quốc Đạt',           '51 Lê Hồng Phong, TP. Vũng Tàu',                 DATE '2002-09-04', 'MALE',   'quocdat.ta@chatsystem.vn',      'USER',  FALSE, TIMESTAMPTZ '2026-06-14 16:00:00+07', TIMESTAMPTZ '2026-06-14 16:00:00+07', NULL),
    -- Administrator account (registered in December 2025 in Asia/Ho_Chi_Minh)
    (25, 'admin',          '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$Jo+AWDZ/vFsq865Ss8+JsNs1IFlijhr1LqvivDVwQFk=}', 'Quản Trị Hệ Thống',     'Khu phố 6, Phường Linh Trung, TP. Thủ Đức',      DATE '1998-01-01', 'OTHER',  'admin@chatsystem.vn',           'ADMIN', FALSE, TIMESTAMPTZ '2025-12-31 23:00:00+07', TIMESTAMPTZ '2025-12-31 23:00:00+07', NULL),
    -- Soft-deleted account (registered Feb 2026, soft-deleted May 2026)
    (26, 'deleteduser01',  '{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}', 'Đặng Văn Cựu (Đã xóa)', '99 Lý Thường Kiệt, Quận 10, TP.HCM',             DATE '2001-06-15', 'MALE',   'deleted01@chatsystem.vn',       'USER',  FALSE, TIMESTAMPTZ '2026-02-10 09:00:00+07', TIMESTAMPTZ '2026-05-20 10:00:00+07', TIMESTAMPTZ '2026-05-20 10:00:00+07');


-- -----------------------------------------------------------------------------
-- 2. Seed login_history & app_open_events
-- Demonstrates:
--   - Multiple successful logins within a SINGLE app launch:
--     On 2026-03-05, user 1 ('nguyenvanan') launches the app once
--     (launch_id '11111111-1111-4111-8111-111111110301' -> 1 row in app_open_events)
--     but authenticates 3 times in that same launch (login_history IDs 5, 6, 7).
--   - Multiple app opens by the same user in one month:
--     User 1 has 3 distinct app launches in March 2026 (2026-03-05, 2026-03-12,
--     2026-03-25) -> 3 app_opens in Query 9, but 1 distinct user in Query 10.
--   - Events across months 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12 of 2026, with
--     August 2026 (Month 8) intentionally left at 0 app opens to test zero-count
--     months in the annual activity chart.
--   - Year boundary in Asia/Ho_Chi_Minh:
--     Event at '2025-12-31 23:45:00+00' = '2026-01-01 06:45:00+07' (Month 1 of 2026).
-- -----------------------------------------------------------------------------
INSERT INTO app_open_events (id, launch_id, user_id, opened_at) VALUES
    -- Dec 2025 in Asia/Ho_Chi_Minh (prior year boundary)
    (1,  UUID '11111111-1111-4111-8111-111111110001', 25, TIMESTAMPTZ '2025-12-31 23:10:00+07'),
    -- Jan 2026 (including UTC year-boundary timestamp 2025-12-31 23:45:00+00 = 2026-01-01 06:45:00+07)
    (2,  UUID '11111111-1111-4111-8111-111111110101', 1,  TIMESTAMPTZ '2025-12-31 23:45:00+00'),
    (3,  UUID '11111111-1111-4111-8111-111111110102', 2,  TIMESTAMPTZ '2026-01-16 08:30:00+07'),
    (4,  UUID '11111111-1111-4111-8111-111111110103', 3,  TIMESTAMPTZ '2026-01-20 19:00:00+07'),
    -- Feb 2026 (includes soft-deleted user 26 to test historical retention)
    (5,  UUID '11111111-1111-4111-8111-111111110201', 1,  TIMESTAMPTZ '2026-02-03 09:00:00+07'),
    (6,  UUID '11111111-1111-4111-8111-111111110202', 5,  TIMESTAMPTZ '2026-02-05 10:00:00+07'),
    (7,  UUID '11111111-1111-4111-8111-111111110203', 26, TIMESTAMPTZ '2026-02-12 14:00:00+07'),
    -- Mar 2026 (User 1 opens app 3 times; Users 2, 3, 5, 10 also open app)
    (8,  UUID '11111111-1111-4111-8111-111111110301', 1,  TIMESTAMPTZ '2026-03-05 08:00:05+07'),
    (9,  UUID '11111111-1111-4111-8111-111111110302', 1,  TIMESTAMPTZ '2026-03-12 18:30:00+07'),
    (10, UUID '11111111-1111-4111-8111-111111110303', 1,  TIMESTAMPTZ '2026-03-25 20:15:00+07'),
    (11, UUID '11111111-1111-4111-8111-111111110304', 2,  TIMESTAMPTZ '2026-03-06 09:10:00+07'),
    (12, UUID '11111111-1111-4111-8111-111111110305', 2,  TIMESTAMPTZ '2026-03-18 21:00:00+07'),
    (13, UUID '11111111-1111-4111-8111-111111110306', 3,  TIMESTAMPTZ '2026-03-08 10:00:00+07'),
    (14, UUID '11111111-1111-4111-8111-111111110307', 5,  TIMESTAMPTZ '2026-03-15 11:20:00+07'),
    (15, UUID '11111111-1111-4111-8111-111111110308', 10, TIMESTAMPTZ '2026-03-22 16:45:00+07'),
    -- Apr 2026
    (16, UUID '11111111-1111-4111-8111-111111110401', 14, TIMESTAMPTZ '2026-04-04 08:15:00+07'),
    (17, UUID '11111111-1111-4111-8111-111111110402', 15, TIMESTAMPTZ '2026-04-12 13:30:00+07'),
    -- May 2026
    (18, UUID '11111111-1111-4111-8111-111111110501', 18, TIMESTAMPTZ '2026-05-05 09:40:00+07'),
    (19, UUID '11111111-1111-4111-8111-111111110502', 20, TIMESTAMPTZ '2026-05-19 17:00:00+07'),
    -- Jun 2026
    (20, UUID '11111111-1111-4111-8111-111111110601', 22, TIMESTAMPTZ '2026-06-02 10:00:00+07'),
    -- Jul 2026
    (21, UUID '11111111-1111-4111-8111-111111110701', 1,  TIMESTAMPTZ '2026-07-10 09:00:00+07'),
    -- Aug 2026 (Month 8): INTENTIONALLY ZERO EVENTS
    -- Sep 2026
    (22, UUID '11111111-1111-4111-8111-111111110901', 2,  TIMESTAMPTZ '2026-09-05 14:00:00+07'),
    -- Oct 2026
    (23, UUID '11111111-1111-4111-8111-111111111001', 3,  TIMESTAMPTZ '2026-10-11 15:30:00+07'),
    -- Nov 2026
    (24, UUID '11111111-1111-4111-8111-111111111101', 1,  TIMESTAMPTZ '2026-11-01 08:00:00+07'),
    (25, UUID '11111111-1111-4111-8111-111111111102', 2,  TIMESTAMPTZ '2026-11-03 19:20:00+07'),
    -- Dec 2026
    (26, UUID '11111111-1111-4111-8111-111111111201', 1,  TIMESTAMPTZ '2026-12-15 20:00:00+07');

INSERT INTO login_history (id, user_id, login_at, logout_at, ip_address) VALUES
    (1,  25, TIMESTAMPTZ '2025-12-31 23:10:02+07', TIMESTAMPTZ '2025-12-31 23:55:00+07', '192.168.1.10'),
    (2,  1,  TIMESTAMPTZ '2026-01-01 06:45:02+07', TIMESTAMPTZ '2026-01-01 07:30:00+07', '10.0.0.11'),
    (3,  2,  TIMESTAMPTZ '2026-01-16 08:30:05+07', TIMESTAMPTZ '2026-01-16 10:00:00+07', '10.0.0.12'),
    (4,  26, TIMESTAMPTZ '2026-02-12 14:00:05+07', TIMESTAMPTZ '2026-02-12 15:10:00+07', '10.0.0.26'),
    -- 3 successful logins by user 1 within the SINGLE launch on 2026-03-05
    (5,  1,  TIMESTAMPTZ '2026-03-05 08:00:06+07', TIMESTAMPTZ '2026-03-05 09:15:00+07', '10.0.0.11'),
    (6,  1,  TIMESTAMPTZ '2026-03-05 09:30:00+07', TIMESTAMPTZ '2026-03-05 10:45:00+07', '10.0.0.11'),
    (7,  1,  TIMESTAMPTZ '2026-03-05 11:15:00+07', TIMESTAMPTZ '2026-03-05 12:30:00+07', '10.0.0.11'),
    -- Other logins in March 2026 and later
    (8,  2,  TIMESTAMPTZ '2026-03-06 09:10:03+07', TIMESTAMPTZ '2026-03-06 11:00:00+07', '10.0.0.12'),
    (9,  3,  TIMESTAMPTZ '2026-03-08 10:00:04+07', TIMESTAMPTZ '2026-03-08 12:00:00+07', '10.0.0.13'),
    (10, 1,  TIMESTAMPTZ '2026-03-12 18:30:02+07', TIMESTAMPTZ '2026-03-12 20:00:00+07', '10.0.0.11'),
    (11, 5,  TIMESTAMPTZ '2026-03-15 11:20:05+07', TIMESTAMPTZ '2026-03-15 13:00:00+07', '10.0.0.15'),
    (12, 10, TIMESTAMPTZ '2026-03-22 16:45:02+07', TIMESTAMPTZ '2026-03-22 18:00:00+07', '10.0.0.20'),
    (13, 1,  TIMESTAMPTZ '2026-03-25 20:15:03+07', NULL,                                 '10.0.0.11');


-- -----------------------------------------------------------------------------
-- 3. Seed friend_requests, friendships, and user_blocks
-- Social graph highlights for User 1 ('nguyenvanan'):
--   - Accepted friendships in `friendships`:
--       (1, 2) active, (1, 3) active, (1, 4) locked-but-not-deleted,
--       (1, 26) soft-deleted.
--     -> Active direct friends of User 1 = {2, 3, 4} (count = 3).
--   - Overlapping 2-hop friendships:
--       (2, 3) triangle overlap with 1's direct friend 3;
--       (2, 5) and (3, 5) diamond overlap reaching user 5 via both 2 and 3;
--       (3, 6) reaching user 6 via 3;
--       (5, 7) is 3 hops from user 1 (excluded);
--       (9, 26) is 2 hops from user 1 ONLY via soft-deleted user 26 (excluded).
--     -> Active friends within 1 or 2 edges of User 1 = {2, 3, 4, 5, 6} (count = 5).
-- -----------------------------------------------------------------------------
INSERT INTO friend_requests (
    id, sender_id, receiver_id, status, created_at, responded_at
) VALUES
    -- Accepted requests corresponding to canonical rows in friendships
    (1,  1,  2,  'ACCEPTED',  TIMESTAMPTZ '2026-01-16 09:00:00+07', TIMESTAMPTZ '2026-01-16 09:05:00+07'),
    (2,  1,  3,  'ACCEPTED',  TIMESTAMPTZ '2026-01-19 10:00:00+07', TIMESTAMPTZ '2026-01-19 10:30:00+07'),
    (3,  4,  1,  'ACCEPTED',  TIMESTAMPTZ '2026-01-25 11:00:00+07', TIMESTAMPTZ '2026-01-25 11:15:00+07'),
    (4,  2,  3,  'ACCEPTED',  TIMESTAMPTZ '2026-01-20 14:00:00+07', TIMESTAMPTZ '2026-01-20 14:10:00+07'),
    (5,  2,  5,  'ACCEPTED',  TIMESTAMPTZ '2026-02-03 10:00:00+07', TIMESTAMPTZ '2026-02-03 10:20:00+07'),
    (6,  3,  5,  'ACCEPTED',  TIMESTAMPTZ '2026-02-04 11:00:00+07', TIMESTAMPTZ '2026-02-04 11:30:00+07'),
    (7,  3,  6,  'ACCEPTED',  TIMESTAMPTZ '2026-02-09 15:00:00+07', TIMESTAMPTZ '2026-02-09 15:45:00+07'),
    (8,  5,  7,  'ACCEPTED',  TIMESTAMPTZ '2026-02-15 09:00:00+07', TIMESTAMPTZ '2026-02-15 09:30:00+07'),
    (9,  10, 12, 'ACCEPTED',  TIMESTAMPTZ '2026-03-20 10:00:00+07', TIMESTAMPTZ '2026-03-20 10:15:00+07'),
    (10, 14, 15, 'ACCEPTED',  TIMESTAMPTZ '2026-04-10 08:00:00+07', TIMESTAMPTZ '2026-04-10 08:20:00+07'),
    (11, 1,  26, 'ACCEPTED',  TIMESTAMPTZ '2026-02-11 10:00:00+07', TIMESTAMPTZ '2026-02-11 10:10:00+07'),
    (12, 26, 9,  'ACCEPTED',  TIMESTAMPTZ '2026-03-02 09:00:00+07', TIMESTAMPTZ '2026-03-02 09:20:00+07'),
    -- Pending requests (do NOT count as friends)
    (13, 1,  10, 'PENDING',   TIMESTAMPTZ '2026-03-25 09:00:00+07', NULL),
    (14, 11, 1,  'PENDING',   TIMESTAMPTZ '2026-03-26 14:00:00+07', NULL),
    (15, 15, 16, 'PENDING',   TIMESTAMPTZ '2026-04-16 10:00:00+07', NULL),
    -- Rejected requests
    (16, 7,  1,  'REJECTED',  TIMESTAMPTZ '2026-02-16 10:00:00+07', TIMESTAMPTZ '2026-02-16 12:00:00+07'),
    (17, 18, 19, 'REJECTED',  TIMESTAMPTZ '2026-05-12 09:00:00+07', TIMESTAMPTZ '2026-05-12 11:00:00+07'),
    -- Cancelled requests (including request cancelled when user 2 blocked user 8)
    (18, 8,  2,  'CANCELLED', TIMESTAMPTZ '2026-02-21 08:00:00+07', TIMESTAMPTZ '2026-02-22 09:00:00+07'),
    (19, 12, 14, 'CANCELLED', TIMESTAMPTZ '2026-04-03 15:00:00+07', TIMESTAMPTZ '2026-04-03 16:00:00+07');

INSERT INTO friendships (user_low_id, user_high_id, created_at) VALUES
    (1,  2,  TIMESTAMPTZ '2026-01-16 09:05:00+07'),
    (1,  3,  TIMESTAMPTZ '2026-01-19 10:30:00+07'),
    (1,  4,  TIMESTAMPTZ '2026-01-25 11:15:00+07'),
    (2,  3,  TIMESTAMPTZ '2026-01-20 14:10:00+07'),
    (2,  5,  TIMESTAMPTZ '2026-02-03 10:20:00+07'),
    (3,  5,  TIMESTAMPTZ '2026-02-04 11:30:00+07'),
    (3,  6,  TIMESTAMPTZ '2026-02-09 15:45:00+07'),
    (5,  7,  TIMESTAMPTZ '2026-02-15 09:30:00+07'),
    (10, 12, TIMESTAMPTZ '2026-03-20 10:15:00+07'),
    (14, 15, TIMESTAMPTZ '2026-04-10 08:20:00+07'),
    (1,  26, TIMESTAMPTZ '2026-02-11 10:10:00+07'),
    (9,  26, TIMESTAMPTZ '2026-03-02 09:20:00+07');

-- Directional blocks (none of these pairs have active friendships or PENDING requests)
INSERT INTO user_blocks (blocker_id, blocked_id, created_at) VALUES
    (2,  8,  TIMESTAMPTZ '2026-02-22 09:00:00+07'),
    (6,  13, TIMESTAMPTZ '2026-03-28 16:00:00+07'),
    (10, 17, TIMESTAMPTZ '2026-04-25 11:30:00+07');


-- -----------------------------------------------------------------------------
-- 4. Seed direct_messages & direct_message_hidden
-- Demonstrates:
--   - In March 2026, user 1 ('nguyenvanan') sends 4 direct messages to
--     2 distinct recipients (3 messages to user 2, 1 message to user 3).
--   - Hiding messages in direct_message_hidden does not reduce activity counts.
--   - Direct messages that trigger spam reports (IDs 7, 8, 9, 10, 11).
-- -----------------------------------------------------------------------------
INSERT INTO direct_messages (id, sender_id, receiver_id, content, sent_at) VALUES
    -- March 2026 messages sent by User 1 (3 to User 2, 1 to User 3 -> 2 distinct recipients)
    (1,  1,  2,  'Chào Bích Ngọc, cậu đã xem đề cương đồ án Java chưa?',              TIMESTAMPTZ '2026-03-05 08:10:00+07'),
    (2,  2,  1,  'Mình xem rồi An ơi, chiều nay nhóm mình họp nhé!',                 TIMESTAMPTZ '2026-03-05 08:12:00+07'),
    (3,  1,  2,  'Ok cậu, 14h chiều trên thư viện nhé.',                             TIMESTAMPTZ '2026-03-05 08:15:00+07'),
    (4,  1,  2,  'Mình vừa gửi thêm tài liệu thiết kế CSDL cho cậu đó.',             TIMESTAMPTZ '2026-03-12 18:40:00+07'),
    (5,  1,  3,  'Nam ơi, tối nay kiểm tra giúp mình phần giao diện Swing nhé.',     TIMESTAMPTZ '2026-03-12 19:00:00+07'),
    -- Feb 2026 message from soft-deleted User 26
    (6,  26, 1,  'Chào An, tài khoản này mình dùng thử nghiệm hồi tháng 2.',         TIMESTAMPTZ '2026-02-12 14:15:00+07'),
    -- Direct messages referenced by spam_reports
    (7,  4,  1,  'Nhận ngay khuyến mãi trúng thưởng 50 triệu tại link lạ!',          TIMESTAMPTZ '2026-03-09 22:00:00+07'),
    (8,  8,  2,  'Cơ hội đầu tư lợi nhuận 300% mỗi tuần, tham gia ngay!',            TIMESTAMPTZ '2026-02-21 20:00:00+07'),
    (9,  13, 6,  'Tin nhắn quảng cáo bán hàng tự động gửi hàng loạt.',               TIMESTAMPTZ '2026-03-27 11:00:00+07'),
    (10, 17, 10, 'Click vào đường dẫn nhận quà tặng miễn phí ngay hôm nay!',         TIMESTAMPTZ '2026-04-24 19:30:00+07'),
    (11, 19, 3,  ' Tuyển cộng tác viên việc nhẹ lương cao không cần kinh nghiệm.',   TIMESTAMPTZ '2026-05-14 08:20:00+07');

INSERT INTO direct_message_hidden (message_id, user_id, hidden_at) VALUES
    (1, 1, TIMESTAMPTZ '2026-03-06 10:00:00+07'),
    (3, 2, TIMESTAMPTZ '2026-03-07 11:00:00+07');


-- -----------------------------------------------------------------------------
-- 5. Seed chat_groups, group_members, encryption fixtures, group_messages,
--    and group_message_hidden
-- Demonstrates:
--   - Group 1 ('Cộng đồng Java Việt Nam', unencrypted):
--       Active Admins: users 1, 2 (2 admins)
--       Active Members: users 3, 5 (4 total active members)
--       Departed Member: user 6 (left_at = '2026-03-20 18:00:00+07')
--   - Group 2 ('Nhóm Đồ Án Hệ Thống Chat', encrypted = TRUE):
--       Active Admin: user 1; Active Members: users 2, 7.
--       Uses synthetic key versions 1 and 2 in group_key_versions.
--   - Group 3 ('CLB Thuật Toán & AI', unencrypted):
--       Active Admins: users 3, 10; Active Members: users 1, 12, 14.
--   - In March 2026, user 1 sends 3 messages to Group 1 and 2 messages to
--     Group 2 -> 2 distinct groups messaged (Query 9).
-- -----------------------------------------------------------------------------
INSERT INTO chat_groups (id, name, created_by, is_encrypted, created_at) VALUES
    (1, 'Cộng đồng Java Việt Nam',    1, FALSE, TIMESTAMPTZ '2026-01-20 09:00:00+07'),
    (2, 'Nhóm Đồ Án Hệ Thống Chat',   1, TRUE,  TIMESTAMPTZ '2026-02-10 14:00:00+07'),
    (3, 'CLB Thuật Toán & AI',        3, FALSE, TIMESTAMPTZ '2026-03-05 10:00:00+07'),
    (4, 'Nhóm Học Tập KHTN',          5, FALSE, TIMESTAMPTZ '2026-04-12 08:30:00+07');

INSERT INTO group_members (group_id, user_id, role, joined_at, left_at) VALUES
    -- Group 1: 2 active admins (1, 2), 2 active members (3, 5), 1 departed member (6)
    (1, 1,  'ADMIN',  TIMESTAMPTZ '2026-01-20 09:00:00+07', NULL),
    (1, 2,  'ADMIN',  TIMESTAMPTZ '2026-01-20 09:05:00+07', NULL),
    (1, 3,  'MEMBER', TIMESTAMPTZ '2026-01-20 09:10:00+07', NULL),
    (1, 5,  'MEMBER', TIMESTAMPTZ '2026-02-03 11:00:00+07', NULL),
    (1, 6,  'MEMBER', TIMESTAMPTZ '2026-02-10 08:00:00+07', TIMESTAMPTZ '2026-03-20 18:00:00+07'),
    -- Group 2 (Encrypted): 1 active admin (1), 2 active members (2, 7)
    (2, 1,  'ADMIN',  TIMESTAMPTZ '2026-02-10 14:00:00+07', NULL),
    (2, 2,  'MEMBER', TIMESTAMPTZ '2026-02-10 14:02:00+07', NULL),
    (2, 7,  'MEMBER', TIMESTAMPTZ '2026-02-15 10:00:00+07', NULL),
    -- Group 3: 2 active admins (3, 10), 3 active members (1, 12, 14)
    (3, 3,  'ADMIN',  TIMESTAMPTZ '2026-03-05 10:00:00+07', NULL),
    (3, 10, 'ADMIN',  TIMESTAMPTZ '2026-03-06 14:00:00+07', NULL),
    (3, 1,  'MEMBER', TIMESTAMPTZ '2026-03-06 14:05:00+07', NULL),
    (3, 12, 'MEMBER', TIMESTAMPTZ '2026-03-20 11:00:00+07', NULL),
    (3, 14, 'MEMBER', TIMESTAMPTZ '2026-04-03 09:00:00+07', NULL),
    -- Group 4: 1 active admin (5), 3 active members (9, 11, 15)
    (4, 5,  'ADMIN',  TIMESTAMPTZ '2026-04-12 08:30:00+07', NULL),
    (4, 9,  'MEMBER', TIMESTAMPTZ '2026-04-12 08:35:00+07', NULL),
    (4, 11, 'MEMBER', TIMESTAMPTZ '2026-04-12 08:40:00+07', NULL),
    (4, 15, 'MEMBER', TIMESTAMPTZ '2026-04-12 08:45:00+07', NULL);

-- Synthetic storage fixtures for Group 2 encryption (NOT live cryptographic keys)
INSERT INTO user_public_keys (id, user_id, public_key, created_at, revoked_at) VALUES
    (1, 1, 'SYNTHETIC_PUBKEY_USER1_V1_BASE64', TIMESTAMPTZ '2026-02-10 13:50:00+07', NULL),
    (2, 2, 'SYNTHETIC_PUBKEY_USER2_V1_BASE64', TIMESTAMPTZ '2026-02-10 13:55:00+07', NULL),
    (3, 7, 'SYNTHETIC_PUBKEY_USER7_V1_BASE64', TIMESTAMPTZ '2026-02-15 09:50:00+07', NULL);

INSERT INTO group_key_versions (group_id, key_version, created_at) VALUES
    (2, 1, TIMESTAMPTZ '2026-02-10 14:00:00+07'),
    (2, 2, TIMESTAMPTZ '2026-02-15 10:00:00+07');

INSERT INTO group_key_envelopes (group_id, key_version, user_public_key_id, encrypted_group_key) VALUES
    (2, 1, 1, decode('53594e5448455449435f575241505045445f4b45595f47325f56315f5531', 'hex')),
    (2, 1, 2, decode('53594e5448455449435f575241505045445f4b45595f47325f56315f5532', 'hex')),
    (2, 2, 1, decode('53594e5448455449435f575241505045445f4b45595f47325f56325f5531', 'hex')),
    (2, 2, 2, decode('53594e5448455449435f575241505045445f4b45595f47325f56325f5532', 'hex')),
    (2, 2, 3, decode('53594e5448455449435f575241505045445f4b45595f47325f56325f5537', 'hex'));

INSERT INTO group_messages (
    id, group_id, sender_id, content, ciphertext, nonce, key_version, sent_at
) VALUES
    -- 3 plaintext messages by User 1 in Group 1 during March 2026
    (1, 1, 1, 'Chào mọi người trong Cộng đồng Java Việt Nam!', NULL, NULL, NULL, TIMESTAMPTZ '2026-03-05 08:20:00+07'),
    (2, 1, 2, 'Chào An, tuần này mình thảo luận về Java 21 và FlatLaf nhé.', NULL, NULL, NULL, TIMESTAMPTZ '2026-03-05 08:25:00+07'),
    (3, 1, 1, 'Mình đã đẩy cấu hình Maven lên kho lưu trữ chung.', NULL, NULL, NULL, TIMESTAMPTZ '2026-03-12 18:45:00+07'),
    (4, 1, 1, 'Mọi người kiểm tra lại schema PostgreSQL giúp mình nhé.', NULL, NULL, NULL, TIMESTAMPTZ '2026-03-25 20:20:00+07'),
    (5, 1, 3, 'Mình đã chạy thử nghiệm, các bảng đều tạo thành công!', NULL, NULL, NULL, TIMESTAMPTZ '2026-03-25 20:30:00+07'),
    -- 2 encrypted synthetic fixture messages by User 1 in Group 2 during March 2026
    (6, 2, 1, NULL, decode('454e435f5041594c4f41445f3031', 'hex'), decode('4e4f4e43455f30303031', 'hex'), 2, TIMESTAMPTZ '2026-03-05 08:30:00+07'),
    (7, 2, 1, NULL, decode('454e435f5041594c4f41445f3032', 'hex'), decode('4e4f4e43455f30303032', 'hex'), 2, TIMESTAMPTZ '2026-03-12 18:50:00+07'),
    -- 1 plaintext message by User 10 in Group 3 during March 2026
    (8, 3, 10, 'Tài liệu ôn tập thuật toán đồ thị cho tuần này.', NULL, NULL, NULL, TIMESTAMPTZ '2026-03-22 17:00:00+07');

INSERT INTO group_message_hidden (message_id, user_id, hidden_at) VALUES
    (1, 1, TIMESTAMPTZ '2026-03-06 10:05:00+07'),
    (2, 3, TIMESTAMPTZ '2026-03-06 11:00:00+07');


-- -----------------------------------------------------------------------------
-- 6. Seed spam_reports
-- Demonstrates:
--   - 3 RESOLVED reports (against locked accounts 4, 8, 13; resolved by admin 25).
--   - 2 PENDING reports (against accounts 17, 19; resolved_by/resolved_at NULL).
--   - Every direct_message_id matches a message sent by reported_user_id to
--     reporter_id.
-- -----------------------------------------------------------------------------
INSERT INTO spam_reports (
    id, reporter_id, reported_user_id, direct_message_id, reason,
    message_snapshot, status, created_at, resolved_by, resolved_at
) VALUES
    (1, 1,  4,  7,  'Phát tán đường dẫn lừa đảo trúng thưởng',
        'Nhận ngay khuyến mãi trúng thưởng 50 triệu tại link lạ!',
        'RESOLVED', TIMESTAMPTZ '2026-03-10 08:00:00+07', 25, TIMESTAMPTZ '2026-03-10 11:00:00+07'),
    (2, 2,  8,  8,  'Gửi tin nhắn rác mời gọi đầu tư tài chính trái phép',
        'Cơ hội đầu tư lợi nhuận 300% mỗi tuần, tham gia ngay!',
        'RESOLVED', TIMESTAMPTZ '2026-03-15 09:30:00+07', 25, TIMESTAMPTZ '2026-03-15 15:00:00+07'),
    (3, 6,  13, 9,  'Spam quảng cáo bán hàng liên tục',
        'Tin nhắn quảng cáo bán hàng tự động gửi hàng loạt.',
        'RESOLVED', TIMESTAMPTZ '2026-03-28 14:00:00+07', 25, TIMESTAMPTZ '2026-04-01 09:00:00+07'),
    (4, 10, 17, 10, 'Gửi liên kết lạ nghi ngờ mã độc',
        'Click vào đường dẫn nhận quà tặng miễn phí ngay hôm nay!',
        'PENDING',  TIMESTAMPTZ '2026-04-25 08:15:00+07', NULL, NULL),
    (5, 3,  19, 11, 'Tin nhắn rác tuyển cộng tác viên không rõ nguồn gốc',
        'Tuyển cộng tác viên việc nhẹ lương cao không cần kinh nghiệm.',
        'PENDING',  TIMESTAMPTZ '2026-05-15 10:00:00+07', NULL, NULL);


-- -----------------------------------------------------------------------------
-- 7. Synchronize all BIGINT IDENTITY sequences to avoid future ID collisions
-- -----------------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('users',             'id'), COALESCE((SELECT MAX(id) FROM users),             1), true);
SELECT setval(pg_get_serial_sequence('login_history',     'id'), COALESCE((SELECT MAX(id) FROM login_history),     1), true);
SELECT setval(pg_get_serial_sequence('app_open_events',   'id'), COALESCE((SELECT MAX(id) FROM app_open_events),   1), true);
SELECT setval(pg_get_serial_sequence('friend_requests',   'id'), COALESCE((SELECT MAX(id) FROM friend_requests),   1), true);
SELECT setval(pg_get_serial_sequence('direct_messages',   'id'), COALESCE((SELECT MAX(id) FROM direct_messages),   1), true);
SELECT setval(pg_get_serial_sequence('chat_groups',       'id'), COALESCE((SELECT MAX(id) FROM chat_groups),       1), true);
SELECT setval(pg_get_serial_sequence('user_public_keys',  'id'), COALESCE((SELECT MAX(id) FROM user_public_keys),  1), true);
SELECT setval(pg_get_serial_sequence('group_messages',    'id'), COALESCE((SELECT MAX(id) FROM group_messages),    1), true);
SELECT setval(pg_get_serial_sequence('spam_reports',      'id'), COALESCE((SELECT MAX(id) FROM spam_reports),      1), true);

COMMIT;
