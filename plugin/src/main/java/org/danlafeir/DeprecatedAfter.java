package org.danlafeir;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated element is deprecated and should be removed after a specified date.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprecatedAfter {
    
    /**
     * The date after which this element should be removed, in ISO-8601 format (yyyy-MM-dd).
     * @return the removal date
     */
    String value();
    
    /**
     * The reason for deprecation (optional).
     * @return the deprecation reason
     */
    String reason() default "";
    
    /**
     * The replacement to use instead (optional).
     * @return the suggested replacement
     */
    String replacement() default "";
}