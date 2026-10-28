package top.egon.cola.archetype.source.light.common.exception;

/** Default error boundary for generated CRUD persistence scaffolds. */
public final class GeneratedCrudErrorMapper {
    private GeneratedCrudErrorMapper() { }

    public static GeneratedCrudException missing(Long id) {
        return new GeneratedCrudException("GENERATED_RESOURCE_NOT_FOUND", "resource not found", true);
    }

    public static GeneratedCrudException zeroRows(String operation, Long id, Long version) {
        return new GeneratedCrudException("GENERATED_WRITE_CONFLICT", "resource changed or is unavailable", false);
    }
}
