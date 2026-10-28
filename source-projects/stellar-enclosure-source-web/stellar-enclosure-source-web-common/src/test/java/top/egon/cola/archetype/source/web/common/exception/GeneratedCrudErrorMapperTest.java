package top.egon.cola.archetype.source.web.common.exception;

import org.junit.jupiter.api.Test;
import top.egon.cola.archetype.source.web.common.enums.OrganizationFailureType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneratedCrudErrorMapperTest {
    @Test
    void classifiesMissingAndConcurrentWrites() {
        assertEquals(OrganizationFailureType.NOT_FOUND, GeneratedCrudErrorMapper.missing(12L).failureType());
        assertEquals(OrganizationFailureType.CONFLICT, GeneratedCrudErrorMapper.zeroRows("update", 12L, 1L).failureType());
    }
}
