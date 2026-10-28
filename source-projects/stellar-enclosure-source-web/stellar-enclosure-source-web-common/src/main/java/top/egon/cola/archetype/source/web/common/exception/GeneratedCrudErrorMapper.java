package top.egon.cola.archetype.source.web.common.exception;

import top.egon.cola.archetype.source.web.common.enums.OrganizationFailureType;

/** Default error boundary for generated CRUD persistence scaffolds. */
public final class GeneratedCrudErrorMapper {
    private GeneratedCrudErrorMapper() { }

    public static OrganizationApplicationException missing(Long id) {
        return new OrganizationApplicationException(OrganizationFailureType.NOT_FOUND,
                "GENERATED_RESOURCE_NOT_FOUND", "resource not found");
    }

    public static OrganizationApplicationException zeroRows(String operation, Long id, Long version) {
        return new OrganizationApplicationException(OrganizationFailureType.CONFLICT,
                "GENERATED_WRITE_CONFLICT", "resource changed or is unavailable");
    }
}
