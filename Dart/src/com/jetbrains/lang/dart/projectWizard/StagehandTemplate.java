// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class StagehandTemplate extends DartProjectTemplate {
  @NotNull private final Stagehand myStagehand;
  @NotNull private final Stagehand.StagehandDescriptor myTemplate;

  StagehandTemplate(@NotNull final Stagehand stagehand, @NotNull final Stagehand.StagehandDescriptor template) {
    super(template.myLabel, template.myDescription);
    myStagehand = stagehand;
    myTemplate = template;
  }

  @Override
  public Collection<VirtualFile> generateProject(@NotNull final String sdkRoot,
                                                 @NotNull final Module module,
                                                 @NotNull final VirtualFile baseDir) throws IOException {
    try {
      myStagehand.generateInto(sdkRoot, baseDir, myTemplate.myId);
    }
    catch (Stagehand.StagehandException e) {
      throw new IOException(e);
    }

    final List<VirtualFile> files = new ArrayList<>();

    final VirtualFile pubspec =
      LocalFileSystem.getInstance().refreshAndFindFileByPath(baseDir.getPath() + "/" + PubspecYamlUtil.PUBSPEC_YAML);
    ContainerUtil.addIfNotNull(files, pubspec);

    final VirtualFile mainFile = myTemplate.myEntrypoint.isEmpty()
                                 ? null
                                 : LocalFileSystem.getInstance()
                                   .refreshAndFindFileByPath(baseDir.getPath() + "/" + myTemplate.myEntrypoint);

    if (mainFile != null && mainFile.getName().equals("index.html")) {
      ContainerUtil.addIfNotNull(files, mainFile.getParent().findChild("main.dart"));
    }

    ContainerUtil.addIfNotNull(files, mainFile);

    if (!myTemplate.myEntrypoint.isEmpty() && mainFile != null) {
      if (myTemplate.myEntrypoint.startsWith("bin/") && FileTypeRegistry.getInstance().isFileOfType(mainFile, DartFileType.INSTANCE)) {
        createCmdLineRunConfiguration(module, mainFile);
      }
      if (myTemplate.myEntrypoint.startsWith("web/") && FileTypeRegistry.getInstance().isFileOfType(mainFile, HtmlFileType.INSTANCE)) {
        createWebRunConfiguration(module, mainFile);
      }
    }

    return files;
  }
}
