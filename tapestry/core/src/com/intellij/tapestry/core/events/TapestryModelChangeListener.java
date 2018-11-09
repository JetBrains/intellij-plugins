package com.intellij.tapestry.core.events;

/**
 * A Tapestry model change listener.
 * Classes that want to be notified of any change in the Tapestry model should implement this interface.
 */
public interface TapestryModelChangeListener {

    /**
     * Called when something changed in the Tapestry application model.
     */
    void modelChanged();
}
