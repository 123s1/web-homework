CREATE DATABASE IF NOT EXISTS museum_reservation
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE museum_reservation;

CREATE TABLE IF NOT EXISTS admin_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  role VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_admin_user_username (username),
  KEY idx_admin_user_status (status),
  CONSTRAINT chk_admin_user_status CHECK (status IN (0, 1)),
  CONSTRAINT chk_admin_user_role CHECK (role IN ('ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS visitor (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  id_card VARCHAR(18) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_visitor_id_card (id_card),
  KEY idx_visitor_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS museum_info (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NOT NULL,
  open_info TEXT NOT NULL,
  rules TEXT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_museum_info_status (status),
  CONSTRAINT chk_museum_info_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notice (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  type VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notice_enabled (enabled),
  KEY idx_notice_type (type),
  KEY idx_notice_created_at (created_at),
  CONSTRAINT chk_notice_enabled CHECK (enabled IN (0, 1)),
  CONSTRAINT chk_notice_type CHECK (type IN ('NORMAL', 'EXHIBITION', 'CLOSE', 'RULE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reservation_activity (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  activity_name VARCHAR(100) NOT NULL,
  visit_date DATE NOT NULL,
  daily_capacity INT UNSIGNED NOT NULL,
  person_limit INT UNSIGNED NOT NULL DEFAULT 1,
  booking_start DATETIME NOT NULL,
  booking_end DATETIME NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservation_activity_visit_date (visit_date),
  KEY idx_reservation_activity_status (status),
  KEY idx_reservation_activity_booking_time (booking_start, booking_end),
  CONSTRAINT chk_reservation_activity_capacity CHECK (daily_capacity > 0),
  CONSTRAINT chk_reservation_activity_person_limit CHECK (person_limit > 0),
  CONSTRAINT chk_reservation_activity_booking_time CHECK (booking_end > booking_start),
  CONSTRAINT chk_reservation_activity_status CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reservation_slot (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  activity_id BIGINT UNSIGNED NOT NULL,
  visit_date DATE NOT NULL,
  slot_name VARCHAR(50) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  total_capacity INT UNSIGNED NOT NULL,
  booked_count INT UNSIGNED NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  version INT UNSIGNED NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservation_slot_activity_time (activity_id, start_time, end_time),
  KEY idx_reservation_slot_activity_id (activity_id),
  KEY idx_reservation_slot_visit_date_enabled (visit_date, enabled),
  CONSTRAINT fk_reservation_slot_activity FOREIGN KEY (activity_id) REFERENCES reservation_activity (id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_reservation_slot_time CHECK (end_time > start_time),
  CONSTRAINT chk_reservation_slot_total_capacity CHECK (total_capacity > 0),
  CONSTRAINT chk_reservation_slot_booked_count CHECK (booked_count <= total_capacity),
  CONSTRAINT chk_reservation_slot_enabled CHECK (enabled IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reservation_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  reservation_no VARCHAR(50) NOT NULL,
  visitor_id BIGINT UNSIGNED NOT NULL,
  activity_id BIGINT UNSIGNED NOT NULL,
  slot_id BIGINT UNSIGNED NOT NULL,
  id_card VARCHAR(18) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  visit_date DATE NOT NULL,
  slot_name VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
  qr_content VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservation_record_no (reservation_no),
  UNIQUE KEY uk_reservation_record_id_card_slot (id_card, slot_id),
  KEY idx_reservation_record_slot_status (slot_id, status),
  KEY idx_reservation_record_visitor_id (visitor_id),
  KEY idx_reservation_record_visit_date_slot_id (visit_date, slot_id),
  KEY idx_reservation_record_id_card_visit_date (id_card, visit_date),
  KEY idx_reservation_record_created_at (created_at),
  CONSTRAINT fk_reservation_record_visitor FOREIGN KEY (visitor_id) REFERENCES visitor (id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_reservation_record_activity FOREIGN KEY (activity_id) REFERENCES reservation_activity (id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_reservation_record_slot FOREIGN KEY (slot_id) REFERENCES reservation_slot (id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_reservation_record_status CHECK (status IN ('SUCCESS', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS request_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  request_type VARCHAR(50) NOT NULL,
  success TINYINT NOT NULL,
  message VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_request_log_request_type (request_type),
  KEY idx_request_log_success (success),
  KEY idx_request_log_created_at (created_at),
  CONSTRAINT chk_request_log_success CHECK (success IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO admin_user (id, username, password, role, status)
VALUES (1, 'admin', 'admin123', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
  role = VALUES(role),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO museum_info (id, name, address, open_info, rules, status)
VALUES (
  1,
  '省博物馆',
  '省会城市文化中心博物馆路 1 号',
  '开放时间：周二至周日 09:00-17:00，16:30 停止入馆，周一闭馆，法定节假日以公告为准。',
  '参观须知：游客需实名预约，按预约日期和时段入馆；同一身份证同一天只能预约一次；请携带有效身份证件并配合现场管理。',
  1
)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  address = VALUES(address),
  open_info = VALUES(open_info),
  rules = VALUES(rules),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO notice (id, title, content, type, enabled)
VALUES
  (1, '参观预约须知', '本馆实行实名预约参观，请提前选择参观日期和入场时段。', 'RULE', 1),
  (2, '特殊展览开放通知', '近期将开放专题展览，热门时段名额有限，请合理安排参观时间。', 'EXHIBITION', 1),
  (3, '入馆提醒', '预约成功后请按预约时段入馆，并出示预约凭证或二维码。', 'NORMAL', 1)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content),
  type = VALUES(type),
  enabled = VALUES(enabled),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO reservation_activity (id, activity_name, visit_date, daily_capacity, person_limit, booking_start, booking_end, status)
VALUES
  (1, '省博物馆参观预约', CURDATE() + INTERVAL 1 DAY, 200, 1, NOW() - INTERVAL 1 HOUR, TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '23:59:59'), 'OPEN'),
  (2, '省博物馆参观预约', CURDATE() + INTERVAL 2 DAY, 200, 1, NOW() - INTERVAL 1 HOUR, TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '23:59:59'), 'OPEN'),
  (3, '省博物馆参观预约', CURDATE() + INTERVAL 3 DAY, 200, 1, NOW() - INTERVAL 1 HOUR, TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '23:59:59'), 'OPEN')
ON DUPLICATE KEY UPDATE
  activity_name = VALUES(activity_name),
  daily_capacity = VALUES(daily_capacity),
  person_limit = VALUES(person_limit),
  booking_start = VALUES(booking_start),
  booking_end = VALUES(booking_end),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO reservation_slot (id, activity_id, visit_date, slot_name, start_time, end_time, total_capacity, booked_count, enabled, version)
VALUES
  (1, 1, CURDATE() + INTERVAL 1 DAY, '上午场', '09:00:00', '11:00:00', 50, 0, 1, 0),
  (2, 1, CURDATE() + INTERVAL 1 DAY, '中午场', '11:00:00', '13:00:00', 50, 0, 1, 0),
  (3, 1, CURDATE() + INTERVAL 1 DAY, '下午场', '13:00:00', '15:00:00', 50, 0, 1, 0),
  (4, 1, CURDATE() + INTERVAL 1 DAY, '傍晚场', '15:00:00', '17:00:00', 50, 0, 1, 0),
  (5, 2, CURDATE() + INTERVAL 2 DAY, '上午场', '09:00:00', '11:00:00', 50, 0, 1, 0),
  (6, 2, CURDATE() + INTERVAL 2 DAY, '中午场', '11:00:00', '13:00:00', 50, 0, 1, 0),
  (7, 2, CURDATE() + INTERVAL 2 DAY, '下午场', '13:00:00', '15:00:00', 50, 0, 1, 0),
  (8, 2, CURDATE() + INTERVAL 2 DAY, '傍晚场', '15:00:00', '17:00:00', 50, 0, 1, 0),
  (9, 3, CURDATE() + INTERVAL 3 DAY, '上午场', '09:00:00', '11:00:00', 50, 0, 1, 0),
  (10, 3, CURDATE() + INTERVAL 3 DAY, '中午场', '11:00:00', '13:00:00', 50, 0, 1, 0),
  (11, 3, CURDATE() + INTERVAL 3 DAY, '下午场', '13:00:00', '15:00:00', 50, 0, 1, 0),
  (12, 3, CURDATE() + INTERVAL 3 DAY, '傍晚场', '15:00:00', '17:00:00', 50, 0, 1, 0)
ON DUPLICATE KEY UPDATE
  visit_date = VALUES(visit_date),
  slot_name = VALUES(slot_name),
  total_capacity = VALUES(total_capacity),
  enabled = VALUES(enabled),
  updated_at = CURRENT_TIMESTAMP;
