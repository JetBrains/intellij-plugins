// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.run;

import com.intellij.flex.model.bc.LinkageType;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.BuildConfigurationEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.PathUtil;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intellij.openapi.util.SystemInfo.isWinVistaOrNewer;

public final class FlashPlayerTrustUtil {

  private final static String WINDOWS_VISTA_AND_7_TRUST_DIR_REL_PATH =
    "\\AppData\\Roaming\\Macromedia\\Flash Player\\#Security\\FlashPlayerTrust";
  private final static String WINDOWS_XP_TRUST_DIR_REL_PATH = "\\Application Data\\Macromedia\\Flash Player\\#Security\\FlashPlayerTrust";
  private final static String MAC_TRUST_DIR_REL_PATH = "/Library/Preferences/Macromedia/Flash Player/#Security/FlashPlayerTrust";
  private final static String UNIX_TRUST_DIR_REL_PATH = "/.macromedia/Flash_Player/#Security/FlashPlayerTrust";

  private final static String INTELLIJ_IDEA_CFG = "intellij_idea.cfg";

  private FlashPlayerTrustUtil() {
  }

  public static void updateTrustedStatus(final Module module, final FlexBuildConfiguration bc,
                                         final boolean isDebug, final boolean isTrusted) {
    final Collection<String> paths = new ArrayList<>();

    try {
      paths.add(new File(PathUtil.getParentPath(bc.getActualOutputFilePath())).getCanonicalPath());
    }
    catch (IOException e) {/**/}

    for (DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry && entry.getDependencyType().getLinkageType() == LinkageType.LoadInRuntime) {
        final FlexBuildConfiguration dependencyBC = ((BuildConfigurationEntry)entry).findBuildConfiguration();
        if (dependencyBC != null) {
          try {
            paths.add(new File(PathUtil.getParentPath(dependencyBC.getActualOutputFilePath())).getCanonicalPath());
          }
          catch (IOException e) {/**/}
        }
      }
    }

    updateTrustedStatus(module.getProject(), isTrusted, isDebug, ArrayUtilRt.toStringArray(paths));
  }

  public static void updateTrustedStatus(final Project project,
                                         final boolean trustedStatus,
                                         final boolean isDebug,
                                         final String... paths) {
    final File ideaCfgFile = getIdeaUserTrustConfigFile(project, isDebug, trustedStatus);
    if (ideaCfgFile == null) {
      return;
    }

    try {
      fixIdeaCfgFileContentIfNeeded(ideaCfgFile, paths, trustedStatus);
    }
    catch (IOException e) {
      // always show
      final ToolWindowManager manager = ToolWindowManager.getInstance(project);
      manager.notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.WARNING,
                              FlexBundle.message("failed.to.update.idea.trust.cfg.file", INTELLIJ_IDEA_CFG, e.getMessage()));
    }
  }

  private static void fixIdeaCfgFileContentIfNeeded(final @NotNull File ideaCfgFile,
                                                    final String @NotNull [] trustedPaths,
                                                    final boolean runTrusted) throws IOException {
    final String[] trustedPathsFiltered = removeNestedPaths(trustedPaths);

    final StringBuilder buf = new StringBuilder();
    final List<String> lines = StringUtil.split(FileUtil.loadFile(ideaCfgFile, "UTF-8"), "\n");

    for (String line : lines) {
      boolean appendLine = true;

      for (String path : trustedPathsFiltered) {
        appendLine &= !line.equals(path) && !line.startsWith(path + File.separatorChar) && !path.startsWith(line + File.separatorChar);
      }

      if (appendLine) {
        buf.append(line).append('\n');
      }
    }

    if (runTrusted) {
      for (String path : trustedPathsFiltered) {
        buf.append(path).append('\n');
      }
    }

    FileUtil.writeToFile(ideaCfgFile, buf.toString().getBytes(StandardCharsets.UTF_8));
  }

  private static String[] removeNestedPaths(final String[] paths) {
    if (paths.length < 2) return paths;

    final Collection<String> result = new ArrayList<>(paths.length);

    for (int i = 0; i < paths.length; i++) {
      final String path = paths[i];
      boolean include = true;

      for (int j = 0; j < paths.length; j++) {
        final String otherPath = paths[j];

        if (i < j && path.equals(otherPath)) {
          include = false;
          break;
        }

        if (i != j && path.startsWith(otherPath + File.separatorChar)) {
          include = false;
          break;
        }
      }

      if (include) {
        result.add(path);
      }
    }

    return ArrayUtilRt.toStringArray(result);
  }

  @Nullable
  private static File getIdeaUserTrustConfigFile(final Project project, final boolean isDebug, final boolean runTrusted) {
    final File flashPlayerTrustDir = getFlashPlayerTrustDir(project, isDebug, runTrusted);
    if (flashPlayerTrustDir == null) {
      return null;
    }

    final File ideaTrustedCfgFile = new File(flashPlayerTrustDir, INTELLIJ_IDEA_CFG);

    if (ideaTrustedCfgFile.isFile() && ideaTrustedCfgFile.length() > 102400) {
      // this file may become such big only because of a bug like IDEA-86188
      FileUtil.delete(ideaTrustedCfgFile);
    }

    if (!ideaTrustedCfgFile.exists() && runTrusted) {
      try {
        final boolean ok = ideaTrustedCfgFile.createNewFile();
        if (!ok) {
          showWarningBalloonIfNeeded(project, isDebug, runTrusted, FlexBundle
            .message("error.creating.idea.trust.cfg.file", INTELLIJ_IDEA_CFG, flashPlayerTrustDir.getPath()));
        }
      }
      catch (IOException e) {
        showWarningBalloonIfNeeded(project, isDebug, runTrusted,
                                   FlexBundle.message("error.creating.idea.trust.cfg.file", INTELLIJ_IDEA_CFG, e.getMessage()));
        return null;
      }
    }

    return ideaTrustedCfgFile;
  }

  @Nullable
  private static File getFlashPlayerTrustDir(final Project project, final boolean isDebug, final boolean runTrusted) {
    final String flashPlayerTrustDirRelPath =
      SystemInfo.isWindows ? (isWinVistaOrNewer ? WINDOWS_VISTA_AND_7_TRUST_DIR_REL_PATH : WINDOWS_XP_TRUST_DIR_REL_PATH) :
      SystemInfo.isMac ? MAC_TRUST_DIR_REL_PATH :
      UNIX_TRUST_DIR_REL_PATH;
    final File flashPlayerTrustDir = new File(SystemProperties.getUserHome() + flashPlayerTrustDirRelPath);

    if (!flashPlayerTrustDir.isDirectory()) {
      if (flashPlayerTrustDir.isFile()) {
        showWarningBalloonIfNeeded(project, isDebug, runTrusted, FlexBundle.message("flash.player.trust.folder.does.not.exist"));
        return null;
      }

      if (!flashPlayerTrustDir.mkdirs()) {
        showWarningBalloonIfNeeded(project, isDebug, runTrusted,
                                   FlexBundle.message("error.creating.flash.player.trust.folder", flashPlayerTrustDir));
        return null;
      }
    }

    return flashPlayerTrustDir;
  }

  private static void showWarningBalloonIfNeeded(final Project project,
                                                 final boolean isDebug,
                                                 final boolean runTrusted,
                                                 final String message) {
    if (runTrusted) {
      final ToolWindowManager manager = ToolWindowManager.getInstance(project);
      manager.notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.WARNING, message);
    }
  }
}
