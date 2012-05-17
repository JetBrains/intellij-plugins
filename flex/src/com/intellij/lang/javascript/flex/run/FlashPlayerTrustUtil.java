package com.intellij.lang.javascript.flex.run;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.intellij.openapi.util.SystemInfo.*;

public class FlashPlayerTrustUtil {

  private final static String WINDOWS_VISTA_AND_7_TRUST_DIR_REL_PATH =
    "\\AppData\\Roaming\\Macromedia\\Flash Player\\#Security\\FlashPlayerTrust";
  private final static String WINDOWS_XP_TRUST_DIR_REL_PATH = "\\Application Data\\Macromedia\\Flash Player\\#Security\\FlashPlayerTrust";
  private final static String MAC_TRUST_DIR_REL_PATH = "/Library/Preferences/Macromedia/Flash Player/#Security/FlashPlayerTrust";
  private final static String LINUX_TRUST_DIR_REL_PATH = "/.macromedia/Flash_Player/#Security/FlashPlayerTrust";

  private final static String INTELLIJ_IDEA_CFG = "intellij_idea.cfg";

  private FlashPlayerTrustUtil() {
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
                                                    final @NotNull String[] trustedPaths,
                                                    final boolean runTrusted) throws IOException {
    final StringBuilder buf = new StringBuilder();
    final List<String> lines = StringUtil.split(FileUtil.loadFile(ideaCfgFile, "UTF-8"), "\n");

    for (String line : lines) {
      boolean appendLine = true;

      for (String path : trustedPaths) {
        appendLine &= !line.equals(path) && !line.startsWith(path + File.separatorChar) && !path.startsWith(line + File.separatorChar);
      }

      if (appendLine) {
        buf.append(line).append('\n');
      }
    }

    if (runTrusted) {
      for (String path : trustedPaths) {
        buf.append(path).append('\n');
      }
    }

    FileUtil.writeToFile(ideaCfgFile, buf.toString().getBytes("UTF-8"));
  }

  @Nullable
  private static File getIdeaUserTrustConfigFile(final Project project, final boolean isDebug, final boolean runTrusted) {
    final File flashPlayerTrustDir = getFlashPlayerTrustDir(project, isDebug, runTrusted);
    if (flashPlayerTrustDir == null) {
      return null;
    }

    final File ideaTrustedCfgFile = new File(flashPlayerTrustDir, INTELLIJ_IDEA_CFG);
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
      isWindows ? (isWindowsVista || isWindows7 ? WINDOWS_VISTA_AND_7_TRUST_DIR_REL_PATH : WINDOWS_XP_TRUST_DIR_REL_PATH)
                : isLinux ? LINUX_TRUST_DIR_REL_PATH : MAC_TRUST_DIR_REL_PATH;
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
