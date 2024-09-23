CREATE INDEX idx_prices_room_date_currency_quantity
    ON prices (date, currency, quantity);