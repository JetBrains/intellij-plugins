package org.osmorc.frameworkintegration;

import com.intellij.openapi.vfs.LocalFileSystem;

/**
 * Base class for all {@link FrameworkInstanceManager}s.
 */
public abstract class AbstractFrameworkInstanceManager implements FrameworkInstanceManager {
  private LocalFileSystem myLocalFileSystem;

  public AbstractFrameworkInstanceManager(LocalFileSystem localFileSystem) {
    myLocalFileSystem = localFileSystem;
  }

  protected LocalFileSystem getLocalFileSystem() {
    return myLocalFileSystem;
  }
}
