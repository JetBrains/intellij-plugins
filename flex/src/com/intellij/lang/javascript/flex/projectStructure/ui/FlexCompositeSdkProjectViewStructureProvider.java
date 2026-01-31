// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
import com.intellij.openapi.roots.JdkOrderEntry;
import com.intellij.openapi.roots.ModuleJdkOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootPolicy;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FlexCompositeSdkProjectViewStructureProvider implements TreeStructureProvider, DumbAware {

  @Override
  public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent,
                                                         final @NotNull Collection<AbstractTreeNode<?>> children,
                                                         final ViewSettings settings) {
    if (!(parent instanceof ExternalLibrariesNode)) {
      return children;
    }

    Set<Sdk> processedSdks = new HashSet<>();
    Collection<AbstractTreeNode<?>> result = new ArrayList<>();

    for (AbstractTreeNode child : children) {
      Object value = child.getValue();
      if (!(value instanceof NamedLibraryElement libraryElement)) {
        result.add(child);
        continue;
      }

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

    private final @NotNull Sdk mySdk;

    private final @NotNull Module myModule;

    private IndividualSdkOrderEntry(final @NotNull Sdk sdk, final @NotNull Module module) {
      mySdk = sdk;
      myModule = module;
    }

    @Override
    public @NotNull Sdk getJdk() {
      return mySdk;
    }

    @Override
    public @NotNull String getJdkName() {
      return mySdk.getName();
    }

    @Override
    public @Nullable String getJdkTypeName() {
      return mySdk.getSdkType().getName();
    }

    @Override
    public VirtualFile @NotNull [] getRootFiles(final @NotNull OrderRootType type) {
      List<VirtualFile> directories =
        ContainerUtil.filter(mySdk.getRootProvider().getFiles(type), VirtualFile::isDirectory);
      return VfsUtilCore.toVirtualFileArray(directories);
    }

    @Override
    public String @NotNull [] getRootUrls(final @NotNull OrderRootType type) {
      return mySdk.getRootProvider().getUrls(type);
    }

    @Override
    public VirtualFile @NotNull [] getFiles(final @NotNull OrderRootType type) {
      return mySdk.getRootProvider().getFiles(type);
    }

    @Override
    public @NotNull String getPresentableName() {
      return "<" + getJdkName() + ">";
    }

    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public @NotNull Module getOwnerModule() {
      return myModule;
    }

    @Override
    public <R> R accept(final @NotNull RootPolicy<R> policy, final @Nullable R initialValue) {
      return policy.visitModuleJdkOrderEntry(this, initialValue);
    }

    @Override
    public int compareTo(final @NotNull OrderEntry o) {
      return 0;
    }

    @Override
    public boolean isSynthetic() {
      return true;
    }
  }
}
