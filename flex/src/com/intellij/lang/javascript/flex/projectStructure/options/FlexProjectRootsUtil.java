// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.flex.model.bc.LinkageType;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModuleLibraryEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.SharedLibraryEntry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableImplUtil;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FlexProjectRootsUtil {

  public static boolean dependsOnLibrary(@NotNull FlexBuildConfiguration bc, @NotNull final Library library, final boolean transitive,
                                         final boolean productionOnly) {
    final String libraryLevel = library.getTable() != null ? library.getTable().getTableLevel() : null;
    if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(libraryLevel) || LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryLevel)) {
      return !ContainerUtil.process(bc.getDependencies().getEntries(), dependencyEntry -> {
        if (!(dependencyEntry instanceof SharedLibraryEntry)) {
          return true;
        }
        if (!canDependOn(dependencyEntry, transitive, productionOnly)) {
          return true;
        }
        return !((SharedLibraryEntry)dependencyEntry).getLibraryName().equals(library.getName()) ||
               !((SharedLibraryEntry)dependencyEntry).getLibraryLevel().equals(libraryLevel);
      });
    }
    else {
      final String libraryId = getLibraryId(library);
      return libraryId != null && !ContainerUtil.process(bc.getDependencies().getEntries(), dependencyEntry -> {
        if (!(dependencyEntry instanceof ModuleLibraryEntry)) {
          return true;
        }
        if (!canDependOn(dependencyEntry, transitive, productionOnly)) {
          return true;
        }
        return !((ModuleLibraryEntry)dependencyEntry).getLibraryId().equals(libraryId);
      });
    }
  }

  private static boolean canDependOn(final DependencyEntry dependencyEntry, final boolean transitive, final boolean productionOnly) {
    if (productionOnly && dependencyEntry.getDependencyType().getLinkageType() == LinkageType.Test) {
      return false;
    }
    return !transitive || dependencyEntry.getDependencyType().getLinkageType() == LinkageType.Include;
  }

  public static boolean dependOnLibrary(Iterable<? extends FlexBuildConfiguration> bcs,
                                        @NotNull final Library library,
                                        final boolean transitive, final boolean productionOnly) {
    return !ContainerUtil.process(bcs, configuration -> !dependsOnLibrary(configuration, library, transitive, productionOnly));
  }

  public static String getLibraryId(Library library) {
    return ((FlexLibraryProperties)((LibraryEx)library).getProperties()).getId();
  }

  @Nullable
  public static LibraryOrderEntry findOrderEntry(ModuleLibraryEntry entry, ModuleRootModel rootModel) {
    for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
      if (orderEntry instanceof LibraryOrderEntry) {
        if (!LibraryTableImplUtil.MODULE_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel())) {
          continue;
        }
        LibraryEx library = (LibraryEx)((LibraryOrderEntry)orderEntry).getLibrary();
        if (library == null || !(library.getKind() == FlexLibraryType.FLEX_LIBRARY)) {
          continue;
        }
        if (entry.getLibraryId().equals(getLibraryId(library))) {
          return (LibraryOrderEntry)orderEntry;
        }
      }
    }
    return null;
  }

  @Nullable
  public static Library findOrderEntry(final Project project, final SharedLibraryEntry entry) {
    final LibraryTable libraryTable = LibraryTablesRegistrar.APPLICATION_LEVEL.equals(entry.getLibraryLevel())
                                      ? LibraryTablesRegistrar.getInstance().getLibraryTable()
                                      : LibraryTablesRegistrar.getInstance().getLibraryTable(project);
    return libraryTable.getLibraryByName((entry).getLibraryName());
  }

  public static boolean isFlexLibrary(final Library library) {
    return ((LibraryEx)library).getKind() == FlexLibraryType.FLEX_LIBRARY;
  }

}
