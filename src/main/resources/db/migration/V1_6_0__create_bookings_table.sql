
CREATE SEQUENCE bookings_seq;

CREATE TYPE booking_status AS ENUM ('pending','booked','cancelled', 'failed');

CREATE TABLE bookings (
  id bigint check (id > 0) NOT NULL DEFAULT NEXTVAL ('bookings_seq'),
  hotel_id bigint check (hotel_id > 0) NOT NULL,
  guest_name varchar(50) NOT NULL,
  currency varchar(50) NOT NULL,
  guest_surname varchar(50) NOT NULL,
  guest_date_of_birth date NOT NULL,
  reservation_date date NOT NULL DEFAULT NOW(),
  checkin_date DATE NOT NULL,
  checkout_date DATE NOT NULL,
  status booking_status NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  PRIMARY KEY (id),
  CONSTRAINT reservation_hotel_id_foreign FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE);