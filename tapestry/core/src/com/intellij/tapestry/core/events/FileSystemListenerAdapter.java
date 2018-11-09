package com.intellij.tapestry.core.events;

import com.intellij.tapestry.core.resource.IResource;

/**
 * A helper class for creating Tapestry listeners.
 */
public abstract class FileSystemListenerAdapter implements FileSystemListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileCreated(String path) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileDeleted(String path) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void classCreated(String classFqn) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void classDeleted(String classFqn) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileContentsChanged(IResource changedFile) {
    }
}
