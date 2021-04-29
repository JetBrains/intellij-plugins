// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAirPackagingOptions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class FilesToPackageUtil {
  private static final Logger LOG = Logger.getInstance(FilesToPackageUtil.class.getName());

  static void setupFilesToPackage(final ModifiableAirPackagingOptions packagingOptions,
                                  final Collection<String> pathsExcludedFromPackaging,
                                  final ModuleRootModel rootModel) {
    final List<AirPackagingOptions.FilePathAndPathInPackage> filesToPackage = new ArrayList<>();

    for (VirtualFile srcRoot : rootModel.getSourceRoots()) {
      final FolderNode rootNode = new FolderNode(null, srcRoot.getPath(), ".");
      initNodes(srcRoot, rootNode, pathsExcludedFromPackaging);
      appendFilesToPackage(filesToPackage, rootNode);
    }

    packagingOptions.setFilesToPackage(filesToPackage);
  }

  private static void initNodes(final VirtualFile rootFolder,
                                final FolderNode rootFolderNode,
                                final Collection<String> pathsExcludedFromPackaging) {
    final Map<VirtualFile, Node> map = new HashMap<>();
    map.put(rootFolder, rootFolderNode);

    VfsUtilCore.visitChildrenRecursively(rootFolder, new VirtualFileVisitor<Void>() {
      @Override
      @NotNull
      public Result visitFileEx(@NotNull final VirtualFile file) {
        if (file.equals(rootFolder)) return CONTINUE;

        final VirtualFile parentFile = file.getParent();
        final Node parentNode = map.get(parentFile);

        LOG.assertTrue(parentNode instanceof FolderNode, file.getPath());

        if (pathsExcludedFromPackaging.contains(((FolderNode)parentNode).getChildRelativePath(file.getName())) ||
            file.getPath().endsWith("-app.xml") ||
            !canBeAddedToPackage(file)) {

          ((FolderNode)parentNode).setHasExcludedChildren();
          return SKIP_CHILDREN;
        }
        else {
          if (file.isDirectory()) {
            final FolderNode childFolderNode = ((FolderNode)parentNode).addChildFolderNode(file.getPath());
            map.put(file, childFolderNode);
          }
          else {
            ((FolderNode)parentNode).addChildFileNode(file.getPath());
          }
          return CONTINUE;
        }
      }
    });
  }

  private static void appendFilesToPackage(final List<AirPackagingOptions.FilePathAndPathInPackage> filesToPackage,
                                           final FolderNode node) {
    if (node.hasExcludedChildren()) {
      for (Node childNode : node.getChildNodes()) {
        if (childNode instanceof FolderNode) {
          appendFilesToPackage(filesToPackage, (FolderNode)childNode);
        }
        else {
          filesToPackage.add(new AirPackagingOptions.FilePathAndPathInPackage(childNode.path, childNode.relativePath));
        }
      }
    }
    else {
      filesToPackage.add(new AirPackagingOptions.FilePathAndPathInPackage(node.path, node.relativePath));
    }
  }

  private static boolean canBeAddedToPackage(final VirtualFile file) {
    if (FileTypeManager.getInstance().isFileIgnored(file.getName())) {
      return false;
    }

    if (!file.isDirectory()) {
      if (FlexCommonUtils.isSourceFile(file.getName()) || FileUtilRt.getExtension(file.getName()).equalsIgnoreCase("properties")) {
        return false;
      }
    }

    return true;
  }

  private static class Node {
    final @Nullable FolderNode parentNode;
    final String path;
    final String relativePath;

    protected Node(@Nullable final FolderNode parentNode, final String path, final String relativePath) {
      this.parentNode = parentNode;
      this.path = path;
      this.relativePath = relativePath;
    }
  }

  private static class FolderNode extends Node {
    private final Collection<Node> childNodes = new ArrayList<>();
    private boolean hasExcludedChildren;

    protected FolderNode(@Nullable final FolderNode parent, final String path, final String relativePath) {
      super(parent, path, relativePath);
    }

    String getChildRelativePath(final String childName) {
      return relativePath.equals(".") ? childName : relativePath + "/" + childName;
    }

    void setHasExcludedChildren() {
      if (hasExcludedChildren) return;

      FolderNode currentNode = this;
      do {
        currentNode.hasExcludedChildren = true;
        currentNode = currentNode.parentNode;
      }
      while (currentNode != null);
    }

    private boolean hasExcludedChildren() {
      return hasExcludedChildren;
    }

    FolderNode addChildFolderNode(final String childFolderPath) {
      final int lastSlashIndex = childFolderPath.lastIndexOf("/");
      LOG.assertTrue(lastSlashIndex > 0 && path.equals(childFolderPath.substring(0, lastSlashIndex)), path + ", " + childFolderPath);

      final FolderNode childFolderNode = new FolderNode(this, childFolderPath, getChildRelativePath(PathUtil.getFileName(childFolderPath)));
      childNodes.add(childFolderNode);
      return childFolderNode;
    }

    Node addChildFileNode(final String childFilePath) {
      final int lastSlashIndex = childFilePath.lastIndexOf("/");
      LOG.assertTrue(lastSlashIndex > 0 && path.equals(childFilePath.substring(0, lastSlashIndex)), path + ", " + childFilePath);

      final Node childFileNode = new Node(this, childFilePath, getChildRelativePath(PathUtil.getFileName(childFilePath)));
      childNodes.add(childFileNode);
      return childFileNode;
    }

    Collection<Node> getChildNodes() {
      return childNodes;
    }
  }
}
