CREATE SEQUENCE extra_charges_flat_seq;

CREATE TYPE charge_type AS ENUM ('per_night','once');
CREATE TABLE extra_charges_flat (
  id bigint check (id > 0) NOT NULL DEFAULT NEXTVAL ('extra_charges_flat_seq'),
  description varchar(255) DEFAULT NULL,
  charge_type charge_type NOT NULL,
  price double precision NOT NULL,
  currency varchar(255) NOT NULL,
  hotel_id bigint check (hotel_id > 0) NOT NULL,
  PRIMARY KEY (id)
 ,
  CONSTRAINT extra_charges_flat_hotel_id_foreign FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
)   ;


CREATE INDEX extra_charges_flat_hotel_id_foreign ON extra_charges_flat (hotel_id);


INSERT INTO public.extra_charges_flat
(description, "charge_type", price, currency, hotel_id) VALUES
('Cleaning fee', 'once', 25, 'EUR', 1),
('Wifi', 'per_night', 5, 'EUR', 1),
('Cleaning fee', 'once', 20, 'EUR', 2),
('Wifi', 'once', 3, 'EUR', 2);
