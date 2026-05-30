-- 迁移脚本：让单人预约上限 person_limit 真正生效
-- 适用于已经用旧版 schema.sql 初始化过的数据库。
-- 变更点：
--   1. 删除旧唯一索引 (id_card, visit_date)（它把每人每天硬限为 1 次，导致 person_limit 无法 > 1）。
--   2. 新增唯一索引 (id_card, slot_id)，仍禁止同一身份证重复预约同一个时段。
--   3. 新增普通索引 (id_card, visit_date)，支撑「当天已约次数」统计查询。
-- 执行前请确认不存在违反新约束的历史数据（同一身份证重复预约同一时段）。

USE museum_reservation;

ALTER TABLE reservation_record
  DROP INDEX uk_reservation_record_id_card_visit_date;

ALTER TABLE reservation_record
  ADD UNIQUE KEY uk_reservation_record_id_card_slot (id_card, slot_id);

ALTER TABLE reservation_record
  ADD KEY idx_reservation_record_id_card_visit_date (id_card, visit_date);
