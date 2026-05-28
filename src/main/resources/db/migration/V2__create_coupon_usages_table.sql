CREATE TABLE coupon_usages
(
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    coupon_code VARCHAR(50)  NOT NULL,
    user_id     VARCHAR(255) NOT NULL,
    used_at     TIMESTAMP    NOT NULL,

    CONSTRAINT coupon_usages_unique UNIQUE (coupon_code, user_id)
);
