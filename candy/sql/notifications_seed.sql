CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500) NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'INFO',
    link_url VARCHAR(255),
    active BIT NOT NULL DEFAULT b'1',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO notifications (title, content, type, link_url, active, created_at)
VALUES
('신상품 입고 안내', '오늘 들어온 신선 식품과 추천 상품을 캔디앱에서 확인해보세요.', 'PRODUCT', '/product/new', b'1', NOW()),
('알뜰쇼핑 특가 진행 중', '할인율 높은 상품을 모아 알뜰쇼핑 코너에 준비했습니다.', 'SALE', '/product/sale', b'1', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('카카오페이 결제 안내', '앱 결제는 카카오톡 또는 카카오페이 앱이 설치된 실제 기기에서 이용해 주세요.', 'PAYMENT', '/checkout', b'1', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('배송 안내', '주문 상품은 출고 후 배송안내 메뉴에서 배송 정책을 확인할 수 있습니다.', 'DELIVERY', '/delivery', b'1', DATE_SUB(NOW(), INTERVAL 1 DAY));
