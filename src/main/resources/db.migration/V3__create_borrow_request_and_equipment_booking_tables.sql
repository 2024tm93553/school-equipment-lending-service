CREATE TABLE borrow_request
(
    request_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id        BIGINT NOT NULL,
    requested_by        BIGINT NOT NULL,
    approved_by         BIGINT,
    quantity            INT    NOT NULL,
    from_date           DATE   NOT NULL,
    to_date             DATE   NOT NULL,
    return_date         DATE,
    reason              VARCHAR(255),
    status              VARCHAR(50) DEFAULT 'PENDING',
    remarks             VARCHAR(255),
    condition_after_use VARCHAR(100),
    created_at          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_borrow_request_equipment
        FOREIGN KEY (equipment_id)
            REFERENCES equipment (equipment_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_borrow_request_user
        FOREIGN KEY (requested_by)
            REFERENCES user (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_borrow_request_approved_by
        FOREIGN KEY (approved_by)
            REFERENCES user (id)
            ON DELETE SET NULL
);


CREATE TABLE equipment_booking
(
    booking_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id   BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    booking_date DATE   NOT NULL,
    quantity     INT    NOT NULL,
    status       VARCHAR(50) DEFAULT 'ACTIVE',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_request
        FOREIGN KEY (request_id)
            REFERENCES borrow_request (request_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_booking_equipment
        FOREIGN KEY (equipment_id)
            REFERENCES equipment (equipment_id)
            ON DELETE CASCADE
);
