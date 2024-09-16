
CREATE SEQUENCE hotels_seq;

CREATE TABLE hotels (
  id bigint check (id > 0) NOT NULL DEFAULT NEXTVAL ('hotels_seq'),
  name text NOT NULL,
  address_line_1 text DEFAULT '',
  address_line_2 text DEFAULT NULL,
  rooms int NOT NULL,
  currency text DEFAULT NULL,
  timezone text DEFAULT NULL,
  star_rating smallint NOT NULL DEFAULT '5',
  phone_number text DEFAULT NULL,
  email text DEFAULT NULL,
  vat decimal(8,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (id)
);


INSERT INTO hotels (name, address_line_1, address_line_2, rooms, currency, timezone, star_rating, email, vat) VALUES
    ('Katanox Amsterdam Hotel', 'Amsterdam', null, 20, 'EUR', 'CET', 4, 'katanox+amsterdam@example.com', '20.00'),
    ('Katanox Berlin Hotel', 'Berlin', null, 30, 'EUR', 'CET', 5, 'katanox+berlin@example.com', '30.00');