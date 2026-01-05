-- 고정 확장자 초기 데이터 (최초 실행 시에만 삽입)
MERGE INTO blocked_extension (extension, is_fixed, is_active, created_at) KEY(extension) VALUES ('bat', true, false, CURRENT_TIMESTAMP);
MERGE INTO blocked_extension (extension, is_fixed, is_active, created_at) KEY(extension) VALUES ('cmd', true, false, CURRENT_TIMESTAMP);
MERGE INTO blocked_extension (extension, is_fixed, is_active, created_at) KEY(extension) VALUES ('com', true, false, CURRENT_TIMESTAMP);
MERGE INTO blocked_extension (extension, is_fixed, is_active, created_at) KEY(extension) VALUES ('cpl', true, false, CURRENT_TIMESTAMP);
MERGE INTO blocked_extension (extension, is_fixed, is_active, created_at) KEY(extension) VALUES ('exe', true, false, CURRENT_TIMESTAMP);
MERGE INTO blocked_extension (extension, is_fixed, is_active, created_at) KEY(extension) VALUES ('scr', true, false, CURRENT_TIMESTAMP);
MERGE INTO blocked_extension (extension, is_fixed, is_active, created_at) KEY(extension) VALUES ('js', true, false, CURRENT_TIMESTAMP);
