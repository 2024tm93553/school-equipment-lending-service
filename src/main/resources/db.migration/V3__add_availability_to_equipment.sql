-- Add availability column to equipment table
ALTER TABLE equipment ADD COLUMN availability BOOLEAN NOT NULL DEFAULT TRUE;

-- Update existing records to set availability based on available quantity
UPDATE equipment
SET availability = CASE
    WHEN available_quantity > 0 THEN TRUE
    ELSE FALSE
END;
