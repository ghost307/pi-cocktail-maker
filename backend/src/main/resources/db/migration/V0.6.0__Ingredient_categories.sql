ALTER TABLE ingredients
    add column parent_group_id bigint NULL REFERENCES ingredients ON DELETE SET NULL;


-- forbid update of and to dtype-value ingredientGroup
CREATE FUNCTION check_illegal_ingredient_dtype_change_function()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF (OLD.dtype != NEW.dtype AND (OLD.dtype = 'IngredientGroup' OR NEW.dtype = 'IngredientGroup')) THEN
        RAISE EXCEPTION 'IngredientGroup dtype may not be changed!';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER check_illegal_ingredient_dtype_change
    BEFORE UPDATE
    ON ingredients
    FOR EACH ROW
EXECUTE PROCEDURE check_illegal_ingredient_dtype_change_function();


-- check for illegal parent (only groups can be parents)
CREATE FUNCTION check_illegal_ingredient_parent_function()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    is_illegal bool;
BEGIN
    IF (NEW.parent_group_id IS NULL) THEN
        RETURN NEW;
    END IF;
    SELECT count(*) != 0
    INTO is_illegal
    FROM ingredients
    WHERE id = NEW.parent_group_id
      AND dtype != 'IngredientGroup';

    if (NOT is_illegal) THEN
        RETURN NEW;
    END IF;

    RAISE EXCEPTION 'parent_group_id must reference a group node' USING ERRCODE = '20808';
END;
$$;

CREATE TRIGGER check_illegal_ingredient_parent
    BEFORE INSERT OR UPDATE
    ON ingredients
    FOR EACH ROW
EXECUTE PROCEDURE check_illegal_ingredient_parent_function();


-- Check for cycles in ingredient hierarchy
CREATE OR REPLACE FUNCTION check_illegal_ingredient_cycle_function()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    IF EXISTS(
            WITH RECURSIVE list_parents(parent) AS (
                SELECT i.id AS child
                FROM ingredients AS i
                WHERE i.parent_group_id = NEW.id
                UNION ALL
                SELECT i.id AS child
                FROM ingredients AS i
                         join list_parents lp on i.parent_group_id = lp.parent
            )
            SELECT *
            FROM list_parents
            WHERE list_parents.parent = NEW.id
            LIMIT 1
        )
    THEN
        RAISE EXCEPTION 'Illegal cycle detected';
    ELSE
        RETURN NEW;
    END IF;
END
$$;

CREATE TRIGGER check_illegal_ingredient_cycle
    AFTER INSERT OR UPDATE
    ON ingredients
    FOR EACH ROW
EXECUTE PROCEDURE check_illegal_ingredient_cycle_function();