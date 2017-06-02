package com.wcs.vaadin.userinactivity;

/**
 * Registry to provide last user action time.
 */
public interface LastActionRegistry {
    /**
     * Stores last user action time.
     */
    void registerLastActionTime();

    /**
     * Returns remaining seconds until timeout event.
     * Before firing event this method called again.
     *
     * @return remaining time until timeout event in seconds
     * @param timeoutSeconds
     */
    int getRemainingSeconds(int timeoutSeconds);

}
