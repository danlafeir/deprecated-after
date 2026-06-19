package com.lafeir.deprecatedafter.core;

/**
 * A lenient Semantic Versioning 2.0.0 comparator.
 *
 * <p>Compares the dotted numeric core ({@code major.minor.patch}, with missing trailing
 * parts treated as zero) and then the optional pre-release identifiers. A version with a
 * pre-release (e.g. {@code 2.0.0-SNAPSHOT}, {@code 2.0.0-alpha}) sorts <em>before</em> the
 * same version without one, matching semver precedence. Build metadata ({@code +sha}) is
 * ignored. This replaces naive {@code Integer.parseInt} comparison, which throws on
 * pre-release versions such as {@code 1.0.0-SNAPSHOT}.
 */
public final class SemanticVersion implements Comparable<SemanticVersion> {

    private final int[] core;
    private final String[] preRelease;

    private SemanticVersion(int[] core, String[] preRelease) {
        this.core = core;
        this.preRelease = preRelease;
    }

    public static SemanticVersion parse(String raw) {
        String v = raw.trim();
        int plus = v.indexOf('+');
        if (plus >= 0) {
            v = v.substring(0, plus);
        }

        String corePart;
        String prePart;
        int dash = v.indexOf('-');
        if (dash >= 0) {
            corePart = v.substring(0, dash);
            prePart = v.substring(dash + 1);
        } else {
            corePart = v;
            prePart = "";
        }

        String[] coreTokens = corePart.isEmpty() ? new String[0] : corePart.split("\\.");
        int[] core = new int[coreTokens.length];
        for (int i = 0; i < coreTokens.length; i++) {
            core[i] = isNumeric(coreTokens[i]) ? Integer.parseInt(coreTokens[i]) : 0;
        }

        String[] pre = prePart.isEmpty() ? new String[0] : prePart.split("\\.");
        return new SemanticVersion(core, pre);
    }

    @Override
    public int compareTo(SemanticVersion other) {
        int len = Math.max(core.length, other.core.length);
        for (int i = 0; i < len; i++) {
            int a = i < core.length ? core[i] : 0;
            int b = i < other.core.length ? other.core[i] : 0;
            if (a != b) {
                return Integer.compare(a, b);
            }
        }

        boolean hasPre = preRelease.length > 0;
        boolean otherHasPre = other.preRelease.length > 0;
        if (!hasPre && !otherHasPre) {
            return 0;
        }
        if (!hasPre) {
            return 1;
        }
        if (!otherHasPre) {
            return -1;
        }
        return comparePreRelease(preRelease, other.preRelease);
    }

    private static int comparePreRelease(String[] a, String[] b) {
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            boolean aNum = isNumeric(a[i]);
            boolean bNum = isNumeric(b[i]);
            int cmp;
            if (aNum && bNum) {
                cmp = Long.compare(Long.parseLong(a[i]), Long.parseLong(b[i]));
            } else if (aNum) {
                cmp = -1;
            } else if (bNum) {
                cmp = 1;
            } else {
                cmp = a[i].compareTo(b[i]);
            }
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(a.length, b.length);
    }

    private static boolean isNumeric(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
