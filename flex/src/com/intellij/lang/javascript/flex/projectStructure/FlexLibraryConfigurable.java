package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibraryConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;

public class FlexLibraryConfigurable extends LibraryConfigurable {
  FlexLibraryConfigurable(final Library library, final StructureConfigurableContext context, final Runnable treeUpdater) {
    super(context.createModifiableModelProvider(library.getTable().getTableLevel()), library, context, treeUpdater);
  }
}
