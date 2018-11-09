package com.intellij.tapestry.core.events;

import com.intellij.tapestry.core.resource.IResource;
import com.intellij.util.containers.ContainerUtil;

import java.util.List;

/**
 * Manages the events from the file system.
 * Each IDE implementation must also call register this class as a filesystem listener and call the appropriate method on each event.
 */
public class TapestryEventsManager implements FileSystemListener, TapestryModelChangeListener {

    private final List<FileSystemListener> _fileSystemListeners = ContainerUtil.createLockFreeCopyOnWriteList();
    private final List<TapestryModelChangeListener> _tapestryModelChangeListeners = ContainerUtil.createLockFreeCopyOnWriteList();

    /**
     * Adds a Tapestry model listener.
     *
     * @param listener the listener to add.
     */
    public synchronized void addTapestryModelListener(TapestryModelChangeListener listener) {
        _tapestryModelChangeListeners.add(listener);
    }

    /**
     * Removes a Tapestry model listener.
     *
     * @param listener the listener to remove.
     * @return {@code true} if the listener was successfully removed, {@code false} otherwise.
     */
    public synchronized boolean removeTapestryModelListener(TapestryModelChangeListener listener) {
        return _tapestryModelChangeListeners.remove(listener);
    }

    /**
     * Adds a file system listener.
     *
     * @param listener the listener to add.
     */
    public synchronized void addFileSystemListener(FileSystemListener listener) {
        _fileSystemListeners.add(listener);
    }

    /**
     * Removes a file system listener.
     *
     * @param listener the listener to remove.
     * @return {@code true} if the listener was successfully removed, {@code false} otherwise.
     */
    public synchronized boolean removeFileSystemListener(FileSystemListener listener) {
        return _fileSystemListeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void fileCreated(String path) {
        for (FileSystemListener listener : _fileSystemListeners)
            listener.fileCreated(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void classDeleted(String classFqn) {
        for (FileSystemListener listener : _fileSystemListeners)
            listener.classDeleted(classFqn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void fileDeleted(String path) {
        for (FileSystemListener listener : _fileSystemListeners)
            listener.fileDeleted(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void classCreated(String classFqn) {
        for (FileSystemListener listener : _fileSystemListeners)
            listener.classCreated(classFqn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void fileContentsChanged(IResource changedFile) {
        for (FileSystemListener listener : _fileSystemListeners)
            listener.fileContentsChanged(changedFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void modelChanged() {
        for (TapestryModelChangeListener listener : _tapestryModelChangeListeners)
            listener.modelChanged();
    }
}
