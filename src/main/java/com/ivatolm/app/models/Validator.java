package com.ivatolm.app.models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Annotation for validation fields of the models.
 *
 * @author ivatolm
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validator {

    /**
     * Returns class that implements {@code ArgCheck}.
     * Class is required to be passed as java doesn't support
     * lambda being passed to annotation.
     *
     * @return class with implementation of the {@code ArgCheck}
     */
    Class<? extends ArgCheck> validator();

}
