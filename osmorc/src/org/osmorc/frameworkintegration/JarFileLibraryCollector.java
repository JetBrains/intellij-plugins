package org.osmorc.frameworkintegration;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.util.OsgiFileUtil;

import java.util.Collection;
import java.util.Set;

/**
 * Convenience implementation of {@link FrameworkLibraryCollector} that extracts jars  from the given
 * jar paths and calls {@link #collectFrameworkJars} for the jar files.
 *
 * @author janthomae@janthomae.de
 */
public abstract class JarFileLibraryCollector implements FrameworkLibraryCollector {
  @Override
  public final void collectFrameworkLibraries(@NotNull FrameworkInstanceLibrarySourceFinder sourceFinder,
                                              @NotNull Collection<VirtualFile> directoriesWithJars) {
    Set<VirtualFile> classRoots = new HashSet<VirtualFile>();
    for (VirtualFile directoryWithJars : directoriesWithJars) {
      for (VirtualFile child : directoryWithJars.getChildren()) {
        VirtualFile dir = OsgiFileUtil.getFolder(child);
        if (dir != null) {
          VirtualFile manifest = dir.findFileByRelativePath("META-INF/MANIFEST.MF"); // it's a bundle
          if (manifest != null && !sourceFinder.containsOnlySources(child)) {
            classRoots.add(child);
          }
        }
      }
    }
    collectFrameworkJars(classRoots, sourceFinder);
  }

  /**
   * Called with the jar files from the given jar paths. Override this in your subclass to collect the jar files
   *
   * @param jarFiles     the jar files that have been found in the jar paths
   * @param sourceFinder a source finder which can obtain sources for each of the given jar files, if this is required.
   */
  protected void collectFrameworkJars(@NotNull Collection<VirtualFile> jarFiles,
                                      @NotNull FrameworkInstanceLibrarySourceFinder sourceFinder) {

  }
}
