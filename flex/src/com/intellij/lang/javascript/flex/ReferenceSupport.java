// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.OSAgnosticPathUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Maxim.Mossienko
 */
public final class ReferenceSupport {

  enum RelativeToWhat {
    Absolute, CurrentFile, ProjectRoot, SourceRoot, Other
  }

  public static PsiReference[] getFileRefs(final PsiElement elt,
                                           final PsiElement valueNode,
                                           final int offset,
                                           final LookupOptions lookupOptions) {
    String str = StringUtil.stripQuotesAroundValue(valueNode.getText());
    return getFileRefs(elt, offset, str, lookupOptions);
  }

  public static PsiReference[] getFileRefs(@NotNull PsiElement elt, final int offset, String str, final LookupOptions lookupOptions) {
    if (lookupOptions.IGNORE_TEXT_AFTER_HASH) {
      int hashIndex = str.indexOf('#');
      if (hashIndex != -1) str = str.substring(0, hashIndex);
    }

    final RelativeToWhat relativeToWhat = relativeToWhat(str, elt, lookupOptions);
    final boolean startsWithSlash = str.startsWith("/");

    final FileReferenceSet base = new FileReferenceSet(str, elt, offset, null, elt.getContainingFile().getViewProvider().getVirtualFile().isCaseSensitive()) {
      @Override
      public boolean isAbsolutePathReference() {
        return relativeToWhat == RelativeToWhat.Absolute;
      }

      @Override
      public boolean couldBeConvertedTo(final boolean relative) {
        return ((relative && lookupOptions.RELATIVE_TO_FILE) || (!relative && lookupOptions.ABSOLUTE)) &&
               super.couldBeConvertedTo(relative);
      }

      @Override
      public FileReference createFileReference(final TextRange range, final int index, final String text) {
        return new JSFlexFileReference(this, range, index, text, relativeToWhat);
      }

      @NotNull
      @Override
      public Collection<PsiFileSystemItem> computeDefaultContexts() {
        PsiFile psiFile = getContainingFile();
        if (psiFile == null) return Collections.emptyList();

        PsiElement context = psiFile.getContext();
        if (context instanceof PsiLanguageInjectionHost) {
          psiFile = context.getContainingFile();
        }
        psiFile = psiFile.getOriginalFile();

        final List<VirtualFile> dirs = new ArrayList<>();

        // paths relative to file should not start with slash
        if (lookupOptions.RELATIVE_TO_FILE && !startsWithSlash) {
          appendFileLocation(dirs, psiFile);
        }

        if ((lookupOptions.RELATIVE_TO_SOURCE_ROOTS_START_WITH_SLASH && startsWithSlash) ||
            (lookupOptions.RELATIVE_TO_SOURCE_ROOTS_START_WITHOUT_SLASH && !startsWithSlash)) {
          appendSourceRoots(dirs, psiFile);
        }

        if (lookupOptions.ABSOLUTE) {
          appendFileSystemRoots(dirs);
        }

        if (lookupOptions.RELATIVE_TO_PROJECT_BASE_DIR) {
          dirs.add(psiFile.getProject().getBaseDir());
        }

        if (lookupOptions.IN_ROOTS_OF_MODULE_DEPENDENCIES) {
          appendRootsOfModuleDependencies(dirs, ModuleUtilCore.findModuleForPsiElement(psiFile));
        }

        return toFileSystemItems(dirs);
      }
    };

    return base.getAllReferences();
  }

  // prevent certain tests from failing VirtualDirectoryImpl.assertAccessInTests() check
  public static boolean ALLOW_ABSOLUTE_REFERENCES_IN_TESTS = true;

  private static RelativeToWhat relativeToWhat(final String path, final PsiElement psiElement, final LookupOptions lookupOptions) {
    if (lookupOptions.ABSOLUTE && (ALLOW_ABSOLUTE_REFERENCES_IN_TESTS || !ApplicationManager.getApplication().isUnitTestMode())) {
      if (SystemInfo.isWindows) {
        if (path.length() > 2 && OSAgnosticPathUtil.startsWithWindowsDrive(path)) {
          return RelativeToWhat.Absolute;
        }
      }
      else if (path.startsWith("/") && LocalFileSystem.getInstance().findFileByPath(path) != null) {
        return RelativeToWhat.Absolute;
      }
    }

    if (lookupOptions.RELATIVE_TO_FILE) {
      PsiFile psiFile = psiElement.getContainingFile();
      final PsiElement context = psiFile.getContext();
      if (context != null) psiFile = context.getContainingFile();
      final VirtualFile vFile = psiFile.getVirtualFile();
      if (vFile != null && VfsUtilCore.findRelativeFile(path, vFile) != null) {
        return RelativeToWhat.CurrentFile;
      }
    }

    if (lookupOptions.RELATIVE_TO_PROJECT_BASE_DIR && VfsUtilCore.findRelativeFile(path, psiElement.getProject().getBaseDir()) != null) {
      return RelativeToWhat.ProjectRoot;
    }

    if ((lookupOptions.RELATIVE_TO_SOURCE_ROOTS_START_WITH_SLASH && path.startsWith("/"))
        || lookupOptions.RELATIVE_TO_SOURCE_ROOTS_START_WITHOUT_SLASH && !path.startsWith("/")) {
      final Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
      if (module != null) {
        for (final VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots()) {
          if (VfsUtilCore.findRelativeFile(path, sourceRoot) != null) {
            return RelativeToWhat.SourceRoot;
          }
        }
      }
    }

    // consider unresolved paths as relative to file if possible, it is needed for correct refactoring
    return lookupOptions.RELATIVE_TO_FILE ? RelativeToWhat.CurrentFile : RelativeToWhat.Other;
  }

  private static void appendFileLocation(final List<VirtualFile> dirs, final PsiFile psiFile) {
    final VirtualFile file = psiFile.getVirtualFile();
    if (file != null) {
      dirs.add(file.getParent());
    }
  }

  private static void appendSourceRoots(final Collection<VirtualFile> dirs, final PsiFile psiFile) {
    final VirtualFile file = psiFile.getVirtualFile();
    if (file != null && ProjectRootManager.getInstance(psiFile.getProject()).getFileIndex().getSourceRootForFile(file) != null) {
      appendSourceRoots(dirs, ModuleUtilCore.findModuleForPsiElement(psiFile));
    }
  }

  private static void appendSourceRoots(final Collection<VirtualFile> dirs, final Module module) {
    if (module != null) {
      ContainerUtil.addAll(dirs, ModuleRootManager.getInstance(module).getSourceRoots());
    }
  }

  private static void appendFileSystemRoots(final Collection<VirtualFile> dirs) {
    ContainerUtil.addAll(dirs, ManagingFS.getInstance().getLocalRoots());
  }

  private static void appendRootsOfModuleDependencies(final List<VirtualFile> dirs, final Module module) {
    if (module != null) {
      final OrderEntry[] orderEntries = ModuleRootManager.getInstance(module).getOrderEntries();
      for (final OrderEntry orderEntry : orderEntries) {
        if (orderEntry instanceof LibraryOrderEntry || orderEntry instanceof JdkOrderEntry) {
          final VirtualFile[] files = orderEntry.getFiles(OrderRootType.CLASSES);
          for (final VirtualFile file : files) {
            if ("swc".equalsIgnoreCase(file.getExtension())) {
              dirs.add(file);
            }
          }
        } else if (orderEntry instanceof ModuleOrderEntry) {
          appendSourceRoots(dirs, ((ModuleOrderEntry)orderEntry).getModule());
        }
      }
    }
  }

  public static class LookupOptions {
    // default is absolute or relative to current file
    public static final LookupOptions SCRIPT_SOURCE = new LookupOptions(false, true, true, false, false, false, false);
    public static final LookupOptions STYLE_SOURCE = new LookupOptions(false, true, true, true, true, false, true);
    public static final LookupOptions XML_AND_MODEL_SOURCE = new LookupOptions(false, true, true, true, false, false, false);
    public static final LookupOptions EMBEDDED_ASSET = new LookupOptions(true, true, true, true, false, false, true);
    public static final LookupOptions NON_EMBEDDED_ASSET = new LookupOptions(false, true, false, false, true, false, false);
    public static final LookupOptions FLEX_COMPILER_CONFIG_PATH_ELEMENT = new LookupOptions(false, true, true, false, false, true, false);

    public final boolean IGNORE_TEXT_AFTER_HASH;
    public final boolean ABSOLUTE;
    public final boolean RELATIVE_TO_FILE;
    public final boolean RELATIVE_TO_SOURCE_ROOTS_START_WITH_SLASH;
    public final boolean RELATIVE_TO_SOURCE_ROOTS_START_WITHOUT_SLASH;
    public final boolean RELATIVE_TO_PROJECT_BASE_DIR; // better name would be RELATIVE_TO_COMPILER_START_DIR but FlexUtils.getFlexCompilerStartDirectory() is not accessible from this class
    public final boolean IN_ROOTS_OF_MODULE_DEPENDENCIES;

    public LookupOptions(final boolean ignoreTextAfterHash,
                         final boolean absolute,
                         final boolean relativeToFile,
                         final boolean relativeToSourceRootsStartWithSlash,
                         final boolean relativeToSourceRootsStartWithoutSlash,
                         final boolean relativeToProjectBaseDir,
                         final boolean inRootsOfModuleDependencies) {
      this.IGNORE_TEXT_AFTER_HASH = ignoreTextAfterHash;
      this.ABSOLUTE = absolute;
      this.RELATIVE_TO_FILE = relativeToFile;
      this.RELATIVE_TO_SOURCE_ROOTS_START_WITH_SLASH = relativeToSourceRootsStartWithSlash;
      this.RELATIVE_TO_SOURCE_ROOTS_START_WITHOUT_SLASH = relativeToSourceRootsStartWithoutSlash;
      this.RELATIVE_TO_PROJECT_BASE_DIR = relativeToProjectBaseDir;
      this.IN_ROOTS_OF_MODULE_DEPENDENCIES = inRootsOfModuleDependencies;
    }
  }
}
