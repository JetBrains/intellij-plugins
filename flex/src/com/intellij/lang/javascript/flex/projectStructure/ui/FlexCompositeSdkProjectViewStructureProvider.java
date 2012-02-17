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
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: ksafonov
 */
public class FlexCompositeSdkProjectViewStructureProvider implements TreeStructureProvider, DumbAware {

  @Override
  public Collection<AbstractTreeNode> modify(final AbstractTreeNode parent,
                                             final Collection<AbstractTreeNode> children,
                                             final ViewSettings settings) {
    if (!(parent instanceof ExternalLibrariesNode)) {
      return children;
    }

    for (AbstractTreeNode child : children) {
      Object value = child.getValue();
      if (!(value instanceof NamedLibraryElement)) {
        continue;
      }

      NamedLibraryElement libraryElement = (NamedLibraryElement)value;
      OrderEntry orderEntry = libraryElement.getOrderEntry();
      if (!(orderEntry instanceof JdkOrderEntry)) {
        continue;
      }

      Sdk sdk = ((JdkOrderEntry)orderEntry).getJdk();
      if (sdk instanceof FlexCompositeSdk) {
        Collection<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>(children);
        result.remove(child);
        Sdk[] sdks = ((FlexCompositeSdk)sdk).getSdks();
        for (Sdk individualSdk : sdks) {
          IndividualSdkOrderEntry entry = new IndividualSdkOrderEntry(individualSdk, orderEntry.getOwnerModule());
          result.add(new NamedLibraryElementNode(parent.getProject(), new NamedLibraryElement(null, entry),
                                                 ((ExternalLibrariesNode)parent).getSettings()));
        }
        return result;
      }
    }
    return children;
  }

  @Override
  public Object getData(final Collection<AbstractTreeNode> selected, final String dataName) {
    return null;
  }

  private static class IndividualSdkOrderEntry implements ModuleJdkOrderEntry {

    @NotNull
    private final Sdk mySdk;

    @NotNull
    private final Module myModule;

    private IndividualSdkOrderEntry(final Sdk sdk, final Module module) {
      mySdk = sdk;
      myModule = module;
    }

    @Override
    public Sdk getJdk() {
      return mySdk;
    }

    @Override
    public String getJdkName() {
      return mySdk.getName();
    }

    @Override
    public VirtualFile[] getRootFiles(final OrderRootType type) {
      List<VirtualFile> directories =
        ContainerUtil.filter(mySdk.getRootProvider().getFiles(type), new Condition<VirtualFile>() {
          @Override
          public boolean value(final VirtualFile virtualFile) {
            return virtualFile.isDirectory();
          }
        });
      return VfsUtil.toVirtualFileArray(directories);
    }

    @Override
    public String[] getRootUrls(final OrderRootType type) {
      return mySdk.getRootProvider().getUrls(type);
    }

    @NotNull
    @Override
    public VirtualFile[] getFiles(final OrderRootType type) {
      return mySdk.getRootProvider().getFiles(type);
    }

    @NotNull
    @Override
    public String[] getUrls(final OrderRootType rootType) {
      return getRootUrls(rootType);
    }

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
    public <R> R accept(final RootPolicy<R> policy, @Nullable final R initialValue) {
      return policy.visitModuleJdkOrderEntry(this, initialValue);
    }

    @Override
    public int compareTo(final OrderEntry o) {
      return 0;
    }

    @Override
    public boolean isSynthetic() {
      return true;
    }
  }
}
