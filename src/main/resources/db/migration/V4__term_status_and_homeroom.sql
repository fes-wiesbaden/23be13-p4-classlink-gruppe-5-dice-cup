ALTER TABLE terms
    ALTER COLUMN status TYPE VARCHAR(10)
        USING (
        CASE status
            WHEN 0 THEN 'OPEN'
            WHEN 1 THEN 'CLOSED'
            ELSE 'OPEN'
            END
        );
