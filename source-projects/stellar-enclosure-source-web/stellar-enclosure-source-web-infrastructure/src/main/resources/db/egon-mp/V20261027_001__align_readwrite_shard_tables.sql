-- Align empty legacy shard tables with the starter STANDARD_TENANT_ID physical names.
-- Existing populated shards require an explicit data redistribution plan.
DO $egon$
BEGIN
    IF current_setting('egon_migration.role') = 'SHARD' THEN
        LOCK TABLE school_classes_0, school_classes_1 IN ACCESS EXCLUSIVE MODE;
        LOCK TABLE school_class_users_0, school_class_users_1 IN ACCESS EXCLUSIVE MODE;
        IF EXISTS (SELECT 1 FROM school_classes_0 LIMIT 1) OR EXISTS (SELECT 1 FROM school_classes_1 LIMIT 1) THEN
            RAISE EXCEPTION 'Populated legacy shards require explicit redistribution before read/write routing';
        END IF;
        IF EXISTS (SELECT 1 FROM school_class_users_0 LIMIT 1) OR EXISTS (SELECT 1 FROM school_class_users_1 LIMIT 1) THEN
            RAISE EXCEPTION 'Populated legacy shards require explicit redistribution before read/write routing';
        END IF;
        ALTER TABLE school_classes_0 RENAME TO school_classes_t0;
        ALTER TABLE school_classes_1 RENAME TO school_classes_t1;
        ALTER TABLE school_class_users_0 RENAME TO school_class_users_t0;
        ALTER TABLE school_class_users_1 RENAME TO school_class_users_t1;
    END IF;
END
$egon$;
