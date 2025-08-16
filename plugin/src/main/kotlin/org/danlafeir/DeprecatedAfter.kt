package org.danlafeir

/**
 * Indicates that the annotated element is deprecated and should be removed after a specified date.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeprecatedAfter(
    /**
     * The date after which this element should be removed, in ISO-8601 format (yyyy-MM-dd).
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