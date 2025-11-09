CREATE TABLE equipment
(
    equipment_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    category           VARCHAR(50)  NOT NULL,
    condition_status   VARCHAR(50) DEFAULT 'Good',
    total_quantity     INT          NOT NULL,
    available_quantity INT          NOT NULL,
    description        TEXT,
    created_by         BIGINT,
    created_at         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipment_created_by
        FOREIGN KEY (created_by)
            REFERENCES user (id)
            ON DELETE SET NULL
);