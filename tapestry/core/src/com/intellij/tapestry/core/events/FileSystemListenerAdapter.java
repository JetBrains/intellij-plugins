package com.intellij.tapestry.core.events;

import com.intellij.tapestry.core.resource.IResource;

/**
 * A helper class for creating Tapestry listeners.
 */
public abstract class FileSystemListenerAdapter implements FileSystemListener {

    /**
     * {@inheritDoc}
     */
    public void fileCreated(String path) {
    }

    /**
     * {@inheritDoc}
     */
    public void fileDeleted(String path) {
    }

    /**
     * {@inheritDoc}
     */
    public void classCreated(String classFqn) {
    }

    /**
     * {@inheritDoc}
     */
    public void classDeleted(String classFqn) {
    }

    /**
     * {@inheritDoc}
     */
    public void fileContentsChanged(IResource changedFile) {
    }
}
