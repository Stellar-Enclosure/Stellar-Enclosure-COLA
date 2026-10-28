package top.egon.cola.archetype.source.light.common.exception;

import java.io.Serial;

/** Rejection raised by generated persistence operations. */
public class GeneratedCrudException extends BaseBusinessException {
    @Serial
    private static final long serialVersionUID = 1L;
    private final boolean missing;

    public GeneratedCrudException(String code, String message, boolean missing) {
        super(code, message);
        this.missing = missing;
    }

    public boolean missing() {
        return missing;
    }
}
