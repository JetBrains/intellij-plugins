package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
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

  public StagehandTemplate(@NotNull final Stagehand stagehand, @NotNull final Stagehand.StagehandDescriptor template) {
    super(getLabel(template), template.myDescription);
    myStagehand = stagehand;
    myTemplate = template;
  }

  private static String getLabel(final Stagehand.StagehandDescriptor descriptor) {
    return descriptor.myLabel != "" ? descriptor.myLabel : prettify(descriptor.myId);
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

    final List<VirtualFile> files = new ArrayList<VirtualFile>();

    final VirtualFile pubspec =
      LocalFileSystem.getInstance().refreshAndFindFileByPath(baseDir.getPath() + "/" + PubspecYamlUtil.PUBSPEC_YAML);
    ContainerUtil.addIfNotNull(files, pubspec);

    final VirtualFile mainFile = myTemplate.myEntrypoint.isEmpty()
                                 ? null
                                 : LocalFileSystem.getInstance()
                                   .refreshAndFindFileByPath(baseDir.getPath() + "/" + myTemplate.myEntrypoint);
    ContainerUtil.addIfNotNull(files, mainFile);

    if (!myTemplate.myEntrypoint.isEmpty() && mainFile != null) {
      if (myTemplate.myEntrypoint.startsWith("bin/") && mainFile.getFileType() == DartFileType.INSTANCE) {
        createCmdLineRunConfiguration(module, mainFile);
      }
      if (myTemplate.myEntrypoint.startsWith("web/") && mainFile.getFileType() == HtmlFileType.INSTANCE) {
        createWebRunConfiguration(module, mainFile);
      }
    }

    return files;
  }

  private static String prettify(@NotNull String name) {
    // consoleapp -> Console App
    name = StringUtil.capitalize(name);
    if (name.endsWith("app")) {
      name = name.substring(0, name.length() - "app".length()) + " App";
    }
    return name;
  }
}
