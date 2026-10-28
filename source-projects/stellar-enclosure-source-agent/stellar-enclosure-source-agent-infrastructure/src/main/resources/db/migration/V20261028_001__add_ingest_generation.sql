-- A generation identifies the currently queued ingest across worker retries.
-- Existing rows stay nullable until their legacy messages are reconciled.
ALTER TABLE knowledge_document ADD COLUMN ingest_generation varchar(36);
