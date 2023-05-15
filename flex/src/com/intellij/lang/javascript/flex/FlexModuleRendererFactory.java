// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex;

import com.intellij.ide.util.DefaultModuleRendererFactory;
import com.intellij.lang.javascript.flex.projectStructure.FlexCompositeSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleJdkOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public final class FlexModuleRendererFactory extends DefaultModuleRendererFactory {

  @Override
  protected boolean handles(final Object element) {
    return super.handles(element);
  }

  @Override
  protected @Nls @NotNull String getPresentableName(OrderEntry order, VirtualFile vFile) {
    if (order instanceof ModuleJdkOrderEntry) {
      Sdk sdk = ((ModuleJdkOrderEntry)order).getJdk();
      if (sdk instanceof FlexCompositeSdk) {
        return "< " + ((FlexCompositeSdk)sdk).getName(vFile) + " >";
      }
    }
    return super.getPresentableName(order, vFile);
  }
}
