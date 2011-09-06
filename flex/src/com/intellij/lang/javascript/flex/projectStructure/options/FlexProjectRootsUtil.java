package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkProperties;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public class FlexProjectRootsUtil {

  public static boolean dependsOnModuleLibrary(@NotNull FlexIdeBuildConfiguration bc, @NotNull Library library, final boolean transitive) {
    final String libraryId = getLibraryId(library);
    return !ContainerUtil.process(bc.DEPENDENCIES.getEntries(), new Processor<DependencyEntry>() {
      @Override
      public boolean process(DependencyEntry dependencyEntry) {
        if (transitive && dependencyEntry.getDependencyType().getLinkageType() != LinkageType.Include) {
          return true;
        }
        if (!(dependencyEntry instanceof ModuleLibraryEntry)) {
          return true;
        }
        return !((ModuleLibraryEntry)dependencyEntry).getLibraryId().equals(libraryId);
      }
    });
  }

  public static boolean dependOnModuleLibrary(Iterable<FlexIdeBuildConfiguration> bcs,
                                              @NotNull final Library library,
                                              final boolean transitive) {
    return !ContainerUtil.process(bcs, new Processor<FlexIdeBuildConfiguration>() {
      @Override
      public boolean process(FlexIdeBuildConfiguration configuration) {
        return !dependsOnModuleLibrary(configuration, library, transitive);
      }
    });
  }

  public static boolean dependsOnSdk(@NotNull FlexIdeBuildConfiguration bc, @NotNull Library library) {
    SdkEntry sdkEntry = bc.DEPENDENCIES.getSdkEntry();
    if (sdkEntry == null) {
      return false;
    }

    return getSdkId(library).equals(sdkEntry.getLibraryId());
  }

  public static boolean dependOnSdk(Iterable<FlexIdeBuildConfiguration> bcs, @NotNull final Library sdk) {
    return !ContainerUtil.process(bcs, new Processor<FlexIdeBuildConfiguration>() {
      @Override
      public boolean process(FlexIdeBuildConfiguration configuration) {
        return !dependsOnSdk(configuration, sdk);
      }
    });
  }

  private static String getLibraryId(Library library) {
    return ((FlexLibraryProperties)((LibraryEx)library).getProperties()).getId();
  }

  private static String getSdkId(Library library) {
    return ((FlexSdkProperties)((LibraryEx)library).getProperties()).getId();
  }
}
