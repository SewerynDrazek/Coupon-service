CREATE TABLE coupons
(
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(50)  NOT NULL,
    created_date DATE         NOT NULL,
    volume       BIGINT       NOT NULL,
    spent        BIGINT       NOT NULL DEFAULT 0,
    country      VARCHAR(10)  NOT NULL,

    CONSTRAINT coupons_code_unique          UNIQUE (code),
    CONSTRAINT coupons_volume_positive      CHECK (volume > 0),
    CONSTRAINT coupons_spent_non_negative   CHECK (spent >= 0),
    CONSTRAINT coupons_spent_le_volume      CHECK (spent <= volume)
);
