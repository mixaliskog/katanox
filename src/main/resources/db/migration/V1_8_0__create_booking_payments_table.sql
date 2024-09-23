CREATE
SEQUENCE booking_payments_seq;

CREATE TABLE booking_payments
(
    id           bigint check (id > 0)         NOT NULL DEFAULT NEXTVAL('booking_payments_seq'),
    booking_id   bigint check (booking_id > 0) NOT NULL,
    card_holder  varchar(50)                   NOT NULL,
    card_number  varchar(50)                   NOT NULL,
    cvv          varchar(3)                   NOT NULL,
    expiry_month varchar(2)                   NOT NULL,
    expiry_year  varchar(4)                   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT booking_payment_booking_id_foreign FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
);