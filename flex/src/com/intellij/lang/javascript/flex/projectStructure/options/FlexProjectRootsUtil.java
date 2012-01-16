package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkProperties;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableImplUtil;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ksafonov
 */
public class FlexProjectRootsUtil {

  public static boolean dependsOnLibrary(@NotNull FlexIdeBuildConfiguration bc, @NotNull final Library library, final boolean transitive) {
    final String libraryLevel = library.getTable() != null ? library.getTable().getTableLevel() : null;
    if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(libraryLevel) || LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryLevel)) {
      return !ContainerUtil.process(bc.getDependencies().getEntries(), new Processor<DependencyEntry>() {
        @Override
        public boolean process(DependencyEntry dependencyEntry) {
          if (transitive && dependencyEntry.getDependencyType().getLinkageType() != LinkageType.Include) {
            return true;
          }
          if (!(dependencyEntry instanceof SharedLibraryEntry)) {
            return true;
          }
          return !((SharedLibraryEntry)dependencyEntry).getLibraryName().equals(library.getName()) ||
                 !((SharedLibraryEntry)dependencyEntry).getLibraryLevel().equals(libraryLevel);
        }
      });
    }
    else {
      final String libraryId = getLibraryId(library);
      return libraryId != null && !ContainerUtil.process(bc.getDependencies().getEntries(), new Processor<DependencyEntry>() {
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
  }

  public static boolean dependOnLibrary(Iterable<FlexIdeBuildConfiguration> bcs,
                                        @NotNull final Library library,
                                        final boolean transitive) {
    return !ContainerUtil.process(bcs, new Processor<FlexIdeBuildConfiguration>() {
      @Override
      public boolean process(FlexIdeBuildConfiguration configuration) {
        return !dependsOnLibrary(configuration, library, transitive);
      }
    });
  }

  public static boolean dependsOnSdk(@NotNull FlexIdeBuildConfiguration bc, @NotNull Library library) {
    SdkEntry sdkEntry = bc.getDependencies().getSdkEntry();
    if (sdkEntry == null) {
      return false;
    }
    return false;
    //return id != null && id.equals(sdkEntry.getLibraryId());
  }

  public static boolean dependOnSdk(Iterable<FlexIdeBuildConfiguration> bcs, @NotNull final Library sdk) {
    return !ContainerUtil.process(bcs, new Processor<FlexIdeBuildConfiguration>() {
      @Override
      public boolean process(FlexIdeBuildConfiguration configuration) {
        return !dependsOnSdk(configuration, sdk);
      }
    });
  }

  public static String getLibraryId(Library library) {
    return ((FlexLibraryProperties)((LibraryEx)library).getProperties()).getId();
  }

  public static String getSdkLibraryId(@NotNull Library library) {
    return ((FlexSdkProperties)((LibraryEx)library).getProperties()).getId();
  }

  @Nullable
  public static LibraryOrderEntry findOrderEntry(ModuleLibraryEntry entry, ModuleRootModel rootModel) {
    for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
      if (orderEntry instanceof LibraryOrderEntry) {
        if (!LibraryTableImplUtil.MODULE_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel())) {
          continue;
        }
        LibraryEx library = (LibraryEx)((LibraryOrderEntry)orderEntry).getLibrary();
        if (library == null || !(library.getType() instanceof FlexLibraryType)) {
          continue;
        }
        if (entry.getLibraryId().equals(getLibraryId(library))) {
          return (LibraryOrderEntry)orderEntry;
        }
      }
    }
    return null;
  }

  public static boolean isFlexLibrary(final Library library) {
    return ((LibraryEx)library).getType() instanceof FlexLibraryType;
  }

}
