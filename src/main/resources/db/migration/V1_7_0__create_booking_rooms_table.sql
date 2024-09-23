
CREATE SEQUENCE booking_rooms_seq;

CREATE TABLE booking_rooms (
  id bigint check (id > 0) NOT NULL DEFAULT NEXTVAL ('booking_rooms_seq'),
  hotel_id bigint check (hotel_id > 0) NOT NULL,
  booking_id bigint check (booking_id > 0) NOT NULL,
  room_id bigint check (room_id > 0) NOT NULL,
  currency varchar(50) NOT NULL,
  checkin_date DATE NOT NULL,
  checkout_date DATE NOT NULL,
  total_price_before_tax decimal NOT NULL,
  total_price_after_tax decimal NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT booking_room_hotel_id_foreign FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE,
  CONSTRAINT booking_room_booking_id_foreign FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
  CONSTRAINT booking_room_room_id_foreign FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
);