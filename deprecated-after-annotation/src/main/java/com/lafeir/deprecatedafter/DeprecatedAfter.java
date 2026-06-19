package com.lafeir.deprecatedafter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a program element that must be removed once the project's version moves
 * strictly past {@link #value()}.
 *
 * <p>The {@code deprecated-after} build-tool integrations (Gradle plugin and Maven
 * plugin) scan compiled classes for this annotation and fail the build when the
 * current project version is strictly greater than the version recorded here. This
 * turns a deferred "remove this later" into an enforced, version-gated contract so
 * dead code cannot silently outlive its deprecation window.
 *
 * <p>Retention is {@link RetentionPolicy#CLASS}: the annotation is written to the
 * bytecode (where the scanner reads it) but is not loaded at runtime, so consumers
 * can depend on this artifact with {@code compileOnly} / {@code provided} scope and
 * carry no runtime footprint.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface DeprecatedAfter {

    /**
     * The version after which the annotated element must no longer exist, e.g. {@code "2.0.0"}.
     * The build fails once the project version is strictly greater than this value.
     */
    String value();

    /**
     * Optional human-readable reason for the deprecation, surfaced in the build failure message.
     */
    String reason() default "";

    /**
     * Optional pointer to the replacement API, surfaced in the build failure message.
     */
    String replacement() default "";
}
