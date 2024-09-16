
CREATE SEQUENCE extra_charges_percentage_seq;

CREATE TYPE applied_on AS ENUM ('first_night','total_amount');

CREATE TABLE extra_charges_percentage (
  id bigint check (id > 0) NOT NULL DEFAULT NEXTVAL ('extra_charges_percentage_seq'),
  description varchar(255) DEFAULT NULL,
  applied_on applied_on NOT NULL,
  percentage double precision NOT NULL,
  hotel_id bigint check (hotel_id > 0) NOT NULL,
  PRIMARY KEY (id)
 ,
  CONSTRAINT extra_charges_percentage_hotel_id_foreign FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
)   ;


CREATE INDEX extra_charges_percentage_hotel_id_foreign ON extra_charges_percentage (hotel_id);


INSERT INTO public.extra_charges_percentage
(description, "applied_on", percentage, hotel_id) VALUES
('Staying fee', 'first_night', 10, 1),
('Safety fee', 'total_amount', 15, 1),
('Staying fee', 'first_night', 15, 2),
('Safety fee', 'total_amount', 5, 2);
