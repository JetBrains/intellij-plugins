// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesNode;
import com.intellij.ide.projectView.impl.nodes.NamedLibraryElement;
import com.intellij.ide.projectView.impl.nodes.NamedLibraryElementNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.flex.projectStructure.FlexCompositeSdk;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FlexCompositeSdkProjectViewStructureProvider implements TreeStructureProvider, DumbAware {

  @NotNull
  @Override
  public Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent,
                                             @NotNull final Collection<AbstractTreeNode<?>> children,
                                             final ViewSettings settings) {
    if (!(parent instanceof ExternalLibrariesNode)) {
      return children;
    }

    Set<Sdk> processedSdks = new HashSet<>();
    Collection<AbstractTreeNode<?>> result = new ArrayList<>();

    for (AbstractTreeNode child : children) {
      Object value = child.getValue();
      if (!(value instanceof NamedLibraryElement)) {
        result.add(child);
        continue;
      }

      NamedLibraryElement libraryElement = (NamedLibraryElement)value;
      OrderEntry orderEntry = libraryElement.getOrderEntry();
      if (!(orderEntry instanceof JdkOrderEntry)) {
        result.add(child);
        continue;
      }

      Sdk sdk = ((JdkOrderEntry)orderEntry).getJdk();
      if (!(sdk instanceof FlexCompositeSdk)) {
        result.add(child);
        continue;
      }

      Sdk[] sdks = ((FlexCompositeSdk)sdk).getSdks();
      for (Sdk individualSdk : sdks) {
        if (processedSdks.add(individualSdk)) {
          IndividualSdkOrderEntry entry = new IndividualSdkOrderEntry(individualSdk, orderEntry.getOwnerModule());
          result.add(new NamedLibraryElementNode(parent.getProject(), new NamedLibraryElement(null, entry),
                                                 ((ExternalLibrariesNode)parent).getSettings()));
        }
      }
    }
    return result;
  }

  private static final class IndividualSdkOrderEntry implements ModuleJdkOrderEntry {

    @NotNull
    private final Sdk mySdk;

    @NotNull
    private final Module myModule;

    private IndividualSdkOrderEntry(@NotNull final Sdk sdk, @NotNull final Module module) {
      mySdk = sdk;
      myModule = module;
    }

    @Override
    @NotNull
    public Sdk getJdk() {
      return mySdk;
    }

    @Override
    @NotNull
    public String getJdkName() {
      return mySdk.getName();
    }

    @Nullable
    @Override
    public String getJdkTypeName() {
      return mySdk.getSdkType().getName();
    }

    @Override
    public VirtualFile @NotNull [] getRootFiles(@NotNull final OrderRootType type) {
      List<VirtualFile> directories =
        ContainerUtil.filter(mySdk.getRootProvider().getFiles(type), VirtualFile::isDirectory);
      return VfsUtilCore.toVirtualFileArray(directories);
    }

    @Override
    public String @NotNull [] getRootUrls(@NotNull final OrderRootType type) {
      return mySdk.getRootProvider().getUrls(type);
    }

    @Override
    public VirtualFile @NotNull [] getFiles(@NotNull final OrderRootType type) {
      return mySdk.getRootProvider().getFiles(type);
    }

    @NotNull
    @Override
    public String getPresentableName() {
      return "<" + getJdkName() + ">";
    }

    @Override
    public boolean isValid() {
      return true;
    }

    @NotNull
    @Override
    public Module getOwnerModule() {
      return myModule;
    }

    @Override
    public <R> R accept(@NotNull final RootPolicy<R> policy, @Nullable final R initialValue) {
      return policy.visitModuleJdkOrderEntry(this, initialValue);
    }

    @Override
    public int compareTo(@NotNull final OrderEntry o) {
      return 0;
    }

    @Override
    public boolean isSynthetic() {
      return true;
    }
  }
}
