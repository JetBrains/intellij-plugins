package org.osmorc.frameworkintegration;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * Base class for all {@link FrameworkInstanceManager}s.
 */
public abstract class AbstractFrameworkInstanceManager implements FrameworkInstanceManager {


  private LocalFileSystem myLocalFileSystem;

  /**
   * Ctor.
   *
   * @param localFileSystem in instance of {@link LocalFileSystem}
   */
  public AbstractFrameworkInstanceManager(LocalFileSystem localFileSystem) {
    myLocalFileSystem = localFileSystem;
  }

  /**
   * PAX runner can download framework instances from the net. These have a distinct directory structure, which differs from the
   * structure of the regular downloads available from the framework vendors. Since PAX's structure is equal for all frameworks
   * it can be checked in the base class, so not every {@link FrameworkInstanceManager} has to duplicate this check. Call this
   * from derived classes if {@link FrameworkInstanceDefinition#isDownloadedByPaxRunner()} returns true.
   *
   * @return null if the given definition has {@link FrameworkInstanceDefinition#isDownloadedByPaxRunner()} set to true and the downloaded file structure is ok Otherwise returns an error message.
   */
  @Nullable
  protected String checkDownloadedFolderStructure(@NotNull FrameworkInstanceDefinition frameworkInstanceDefinition) {
    if (!frameworkInstanceDefinition.isDownloadedByPaxRunner()) {
      return "The framework is not downloaded by Pax Runner.";
    }

    VirtualFile installFolder = myLocalFileSystem.findFileByPath(frameworkInstanceDefinition.getBaseFolder());
    if (installFolder == null || !installFolder.isDirectory()) {
      return MessageFormat
        .format("The download location {0} does not exist or is not a directory.", frameworkInstanceDefinition.getBaseFolder());
    }

    VirtualFile bundlesFolder = installFolder.findChild("bundles");
    if (bundlesFolder == null || !bundlesFolder.isDirectory()) {
      return MessageFormat.format("The download location {0} does not contain a bundles folder. Did you already download the framework?",
                                  frameworkInstanceDefinition.getBaseFolder());
    }
    return null;
  }


  protected LocalFileSystem getLocalFileSystem() {
    return myLocalFileSystem;
  }
}
