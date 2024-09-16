CREATE TABLE prices (
  date date NOT NULL,
  quantity int NOT NULL,
  room_id bigint check (room_id > 0) NOT NULL,
  price_after_tax decimal NOT NULL,
  currency character(3) NOT NULL,
  PRIMARY KEY (date,room_id)
 ,
  CONSTRAINT prices_room_id_foreign FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
)  ;

CREATE INDEX prices_room_id_foreign ON prices (room_id);

INSERT INTO public.prices
("date", quantity, room_id, price_after_tax, currency) VALUES
('2022-03-01', 2, 1, 100, 'EUR'),
('2022-03-02', 2, 1, 110, 'EUR'),
('2022-03-03', 2, 1, 110, 'EUR'),
('2022-03-04', 2, 1, 100, 'EUR'),
('2022-03-05', 2, 1, 130, 'EUR'),
('2022-03-01', 2, 2, 100, 'EUR'),
('2022-03-02', 2, 2, 150, 'EUR'),
('2022-03-03', 2, 2, 200, 'EUR'),
('2022-03-04', 3, 2, 100, 'EUR'),
('2022-03-05', 3, 2, 110, 'EUR'),
('2022-03-01', 2, 3, 110, 'EUR'),
('2022-03-02', 2, 3, 130, 'EUR'),
('2022-03-03', 2, 3, 160, 'EUR'),
('2022-03-04', 2, 3, 250, 'EUR'),
('2022-03-05', 2, 3, 100, 'EUR'),
('2022-03-01', 6, 4, 1000, 'EUR'),
('2022-03-02', 6, 4, 100, 'EUR'),
('2022-03-03', 6, 4, 110, 'EUR'),
('2022-03-04', 6, 4, 110, 'EUR'),
('2022-03-05', 6, 4, 110, 'EUR'),
('2022-03-06', 6, 4, 140, 'EUR');
