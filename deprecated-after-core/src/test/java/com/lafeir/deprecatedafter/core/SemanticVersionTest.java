package com.lafeir.deprecatedafter.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticVersionTest {

    private static int cmp(String a, String b) {
        return Integer.signum(SemanticVersion.parse(a).compareTo(SemanticVersion.parse(b)));
    }

    @ParameterizedTest
    @CsvSource({
            "1.0.0, 1.0.0, 0",
            "1.0.1, 1.0.0, 1",
            "1.0.0, 1.0.1, -1",
            "2.0.0, 1.9.9, 1",
            "1.10.0, 1.9.0, 1",
            "1.0, 1.0.0, 0",
            "1, 1.0.0, 0",
            "1.2, 1.2.1, -1",
    })
    void comparesNumericCore(String a, String b, int expected) {
        assertEquals(expected, cmp(a, b));
    }

    @ParameterizedTest
    @CsvSource({
            "2.0.0-SNAPSHOT, 2.0.0, -1",
            "2.0.0, 2.0.0-SNAPSHOT, 1",
            "2.0.0-alpha, 2.0.0, -1",
            "2.0.1-SNAPSHOT, 2.0.0, 1",
            "2.0.0-alpha.1, 2.0.0-alpha.2, -1",
            "2.0.0-alpha, 2.0.0-alpha.1, -1",
            "1.0.0+build.5, 1.0.0, 0",
    })
    void comparesPreReleaseAndBuildMetadata(String a, String b, int expected) {
        assertEquals(expected, cmp(a, b));
    }

    @Test
    void preReleaseSortsBelowItsRelease() {
        assertTrue(cmp("2.0.0-SNAPSHOT", "2.0.0") < 0);
    }
}
