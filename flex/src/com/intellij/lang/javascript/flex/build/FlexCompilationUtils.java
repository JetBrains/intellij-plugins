// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.Dependencies;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.ModuleLibraryEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.SharedLibraryEntry;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.ZipUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public final class FlexCompilationUtils {

  private FlexCompilationUtils() {
  }

  public static Collection<VirtualFile> getANEFiles(final ModuleRootModel moduleRootModel, final Dependencies dependencies) {
    final Collection<VirtualFile> result = new ArrayList<>();

    for (DependencyEntry entry : dependencies.getEntries()) {
      if (entry instanceof ModuleLibraryEntry) {
        final LibraryOrderEntry orderEntry =
          FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, moduleRootModel);
        if (orderEntry != null) {
          for (VirtualFile libFile : orderEntry.getRootFiles(OrderRootType.CLASSES)) {
            addIfANE(result, libFile);
          }
        }
      }
      else if (entry instanceof SharedLibraryEntry) {
        final Library library = FlexProjectRootsUtil.findOrderEntry(moduleRootModel.getModule().getProject(), (SharedLibraryEntry)entry);
        if (library != null) {
          for (VirtualFile libFile : library.getFiles((OrderRootType.CLASSES))) {
            addIfANE(result, libFile);
          }
        }
      }
    }
    return result;
  }

  private static void addIfANE(final Collection<? super VirtualFile> result, final VirtualFile libFile) {
    final VirtualFile realFile = getRealFile(libFile);
    if (realFile != null && !realFile.isDirectory() && "ane".equalsIgnoreCase(realFile.getExtension())) {
      result.add(realFile);
    }
  }

  public static String[] getAirExtensionIDs(final ModuleRootModel moduleRootModel, final Dependencies dependencies) {
    final Collection<VirtualFile> aneFiles = getANEFiles(moduleRootModel, dependencies);
    final Collection<String> extensionIDs = new ArrayList<>();
    for (VirtualFile aneFile : aneFiles) {
      final String extensionId = getExtensionId(aneFile);
      ContainerUtil.addIfNotNull(extensionIDs, extensionId);
    }
    return ArrayUtilRt.toStringArray(extensionIDs);
  }

  @Nullable
  private static String getExtensionId(final VirtualFile aneFile) {
    final VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(aneFile);
    if (jarRoot == null) return null;
    final VirtualFile xmlFile = VfsUtilCore.findRelativeFile("META-INF/ANE/extension.xml", jarRoot);
    if (xmlFile == null) return null;
    try {
      return FlexUtils.findXMLElement(xmlFile.getInputStream(), "<extension><id>");
    }
    catch (IOException e) {
      return null;
    }
  }

  public static void unzipANEFiles(final Collection<? extends VirtualFile> aneFiles, final ProgressIndicator indicator) {
    final File baseDir = new File(getPathToUnzipANE());
    if (!baseDir.exists()) {
      if (!baseDir.mkdir()) {
        Logger.getLogger(FlexCompilationUtils.class.getName()).warning("Failed to create " + baseDir.getPath());
        return;
      }
    }

    for (VirtualFile file : aneFiles) {
      if (indicator != null && indicator.isCanceled()) return;

      final File subDir = new File(baseDir, file.getName());
      if (!subDir.exists()) {
        if (!subDir.mkdir()) {
          Logger.getLogger(FlexCompilationUtils.class.getName()).warning("Failed to create " + baseDir.getPath());
          continue;
        }
      }

      try {
        ZipUtil.extract(new File(file.getPath()), subDir, null, true);
      }
      catch (IOException e) {
        Logger.getLogger(FlexCompilationUtils.class.getName()).warning("Failed to unzip " + file.getPath() + " to " + baseDir.getPath());
      }
    }
  }

  public static void deleteUnzippedANEFiles() {
    FileUtil.delete(new File(getPathToUnzipANE()));
  }

  public static String getPathToUnzipANE() {
    return FileUtil.getTempDirectory() + File.separator + "IntelliJ_ANE_unzipped";
  }

  static VirtualFile refreshAndFindFileInWriteAction(final String outputFilePath, final String... possibleBaseDirs) {
    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
    final Ref<VirtualFile> outputFileRef = new Ref<>();

    final Application app = ApplicationManager.getApplication();
    app.invokeAndWait(() -> outputFileRef.set(app.runWriteAction(new NullableComputable<VirtualFile>() {
      @Override
      public VirtualFile compute() {
        VirtualFile outputFile = localFileSystem.refreshAndFindFileByPath(outputFilePath);
        //if (outputFile == null) {
        //  outputFile =
        //    localFileSystem.refreshAndFindFileByPath(FlexUtils.getFlexCompilerWorkDirPath(project, null) + "/" + outputFilePath);
        //}
        if (outputFile == null) {
          for (final String baseDir : possibleBaseDirs) {
            outputFile = localFileSystem.refreshAndFindFileByPath(baseDir + "/" + outputFilePath);
            if (outputFile != null) {
              break;
            }
          }
        }
        if (outputFile == null) return null;

        // it's important because this file has just been created
        outputFile.refresh(false, false);
        return outputFile;
      }
    })));

    return outputFileRef.get();
  }

  @Nullable
  public static VirtualFile getRealFile(final VirtualFile libFile) {
    if (libFile.getFileSystem() instanceof JarFileSystem) {
      return JarFileSystem.getInstance().getVirtualFileForJar(libFile);
    }
    return libFile;
  }
}
