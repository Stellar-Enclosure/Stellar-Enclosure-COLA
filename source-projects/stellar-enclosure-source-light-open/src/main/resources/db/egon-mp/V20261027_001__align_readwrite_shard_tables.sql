-- Align empty legacy shard tables with the starter STANDARD_TENANT_ID physical names.
-- Existing populated shards require an explicit data redistribution plan.
DO $egon$
BEGIN
    IF current_setting('egon_migration.role') = 'SHARD' THEN
        LOCK TABLE light_school_classes_0, light_school_classes_1 IN ACCESS EXCLUSIVE MODE;
        LOCK TABLE light_class_course_schedules_0, light_class_course_schedules_1 IN ACCESS EXCLUSIVE MODE;
        IF EXISTS (SELECT 1 FROM light_school_classes_0 LIMIT 1) OR EXISTS (SELECT 1 FROM light_school_classes_1 LIMIT 1) THEN
            RAISE EXCEPTION 'Populated legacy shards require explicit redistribution before read/write routing';
        END IF;
        IF EXISTS (SELECT 1 FROM light_class_course_schedules_0 LIMIT 1) OR EXISTS (SELECT 1 FROM light_class_course_schedules_1 LIMIT 1) THEN
            RAISE EXCEPTION 'Populated legacy shards require explicit redistribution before read/write routing';
        END IF;
        ALTER TABLE light_school_classes_0 RENAME TO light_school_classes_t0;
        ALTER TABLE light_school_classes_1 RENAME TO light_school_classes_t1;
        ALTER TABLE light_class_course_schedules_0 RENAME TO light_class_course_schedules_t0;
        ALTER TABLE light_class_course_schedules_1 RENAME TO light_class_course_schedules_t1;
    END IF;
END
$egon$;
