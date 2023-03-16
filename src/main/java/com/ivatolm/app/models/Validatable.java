package com.ivatolm.app.models;

/**
 * Interface for validating objects.
 *
 * @author ivatolm
 */
public interface Validatable {

    /**
     * Check object for validity.
     *
     * @return true if object is valid, else false
     */
    boolean validate();

}
