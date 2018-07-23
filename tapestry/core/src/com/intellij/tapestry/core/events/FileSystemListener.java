package com.intellij.tapestry.core.events;

import com.intellij.tapestry.core.resource.IResource;

/**
 * A file system listener.
 * Classes that want to be notified of filesystem event should implement this interface.
 */
public interface FileSystemListener {

    /**
     * Called when a file is created.
     *
     * @param path of the created file.
     */
    void fileCreated(String path);

    /**
     * Called when a file is deleted.
     *
     * @param path of the deleted file.
     */
    void fileDeleted(String path);

    /**
     * Called when a file is deleted.
     *
     * @param changedFile the changed file.
     */
    void fileContentsChanged(IResource changedFile);

    /**
     * Called when a class is deleted.
     *
     * @param classFqn the deleted class fully qualified name.
     */
    void classCreated(String classFqn);

    /**
     * Called when a class is deleted.
     *
     * @param classFqn the deleted class fully qualified name.
     */
    void classDeleted(String classFqn);
}
