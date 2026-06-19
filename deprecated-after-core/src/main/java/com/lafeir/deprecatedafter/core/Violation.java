package com.lafeir.deprecatedafter.core;

/**
 * A program element annotated with {@code @DeprecatedAfter} whose recorded version has been
 * surpassed by the current project version, meaning it should have been removed already.
 */
public final class Violation {

    private final String elementName;
    private final String afterVersion;
    private final String reason;
    private final String replacement;

    public Violation(String elementName, String afterVersion, String reason, String replacement) {
        this.elementName = elementName;
        this.afterVersion = afterVersion;
        this.reason = reason == null ? "" : reason;
        this.replacement = replacement == null ? "" : replacement;
    }

    public String getElementName() {
        return elementName;
    }

    public String getAfterVersion() {
        return afterVersion;
    }

    public String getReason() {
        return reason;
    }

    public String getReplacement() {
        return replacement;
    }

    /** Renders this violation for a build-failure message. */
    public String describe() {
        StringBuilder sb = new StringBuilder(elementName)
                .append(" (deprecated after version ")
                .append(afterVersion)
                .append(")");
        if (!reason.isEmpty()) {
            sb.append(" - Reason: ").append(reason);
        }
        if (!replacement.isEmpty()) {
            sb.append(" - Use: ").append(replacement);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return describe();
    }
}
