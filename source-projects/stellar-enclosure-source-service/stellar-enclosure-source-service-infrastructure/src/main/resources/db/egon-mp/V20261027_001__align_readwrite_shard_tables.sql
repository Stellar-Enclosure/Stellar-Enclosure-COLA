-- Align empty legacy shard tables with the starter STANDARD_TENANT_ID physical names.
-- Existing populated shards require an explicit data redistribution plan.
DO $egon$
BEGIN
    IF current_setting('egon_migration.role') = 'SHARD' THEN
        LOCK TABLE evaluation_course_schedule_0, evaluation_course_schedule_1 IN ACCESS EXCLUSIVE MODE;
        LOCK TABLE evaluation_exam_0, evaluation_exam_1 IN ACCESS EXCLUSIVE MODE;
        LOCK TABLE evaluation_exam_paper_0, evaluation_exam_paper_1 IN ACCESS EXCLUSIVE MODE;
        LOCK TABLE evaluation_score_0, evaluation_score_1 IN ACCESS EXCLUSIVE MODE;
        IF EXISTS (SELECT 1 FROM evaluation_course_schedule_0 LIMIT 1) OR EXISTS (SELECT 1 FROM evaluation_course_schedule_1 LIMIT 1) THEN
            RAISE EXCEPTION 'Populated legacy shards require explicit redistribution before read/write routing';
        END IF;
        IF EXISTS (SELECT 1 FROM evaluation_exam_0 LIMIT 1) OR EXISTS (SELECT 1 FROM evaluation_exam_1 LIMIT 1) THEN
            RAISE EXCEPTION 'Populated legacy shards require explicit redistribution before read/write routing';
        END IF;
        IF EXISTS (SELECT 1 FROM evaluation_exam_paper_0 LIMIT 1) OR EXISTS (SELECT 1 FROM evaluation_exam_paper_1 LIMIT 1) THEN
            RAISE EXCEPTION 'Populated legacy shards require explicit redistribution before read/write routing';
        END IF;
        IF EXISTS (SELECT 1 FROM evaluation_score_0 LIMIT 1) OR EXISTS (SELECT 1 FROM evaluation_score_1 LIMIT 1) THEN
            RAISE EXCEPTION 'Populated legacy shards require explicit redistribution before read/write routing';
        END IF;
        ALTER TABLE evaluation_course_schedule_0 RENAME TO evaluation_course_schedule_t0;
        ALTER TABLE evaluation_course_schedule_1 RENAME TO evaluation_course_schedule_t1;
        ALTER TABLE evaluation_exam_0 RENAME TO evaluation_exam_t0;
        ALTER TABLE evaluation_exam_1 RENAME TO evaluation_exam_t1;
        ALTER TABLE evaluation_exam_paper_0 RENAME TO evaluation_exam_paper_t0;
        ALTER TABLE evaluation_exam_paper_1 RENAME TO evaluation_exam_paper_t1;
        ALTER TABLE evaluation_score_0 RENAME TO evaluation_score_t0;
        ALTER TABLE evaluation_score_1 RENAME TO evaluation_score_t1;
    END IF;
END
$egon$;
