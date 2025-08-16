package org.danlafeir

/**
 * Indicates that the annotated element is deprecated and should be removed after a specified version.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeprecatedAfter(
    /**
     * The version after which this element should be removed (e.g., "1.0.0", "2.1.0").
     */
    val value: String,
    
    /**
     * The reason for deprecation (optional).
     */
    val reason: String = "",
    
    /**
     * The replacement to use instead (optional).
     */
    val replacement: String = ""
)