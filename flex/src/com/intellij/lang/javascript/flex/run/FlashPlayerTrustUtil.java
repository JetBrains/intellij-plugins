package com.intellij.lang.javascript.flex.run;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SystemProperties;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.intellij.openapi.util.SystemInfo.*;

public class FlashPlayerTrustUtil {

  private final static String WINDOWS_VISTA_AND_7_TRUST_DIR_REL_PATH =
    "\\AppData\\Roaming\\Macromedia\\Flash Player\\#Security\\FlashPlayerTrust";
  private final static String WINDOWS_XP_TRUST_DIR_REL_PATH = "\\Application Data\\Macromedia\\Flash Player\\#Security\\FlashPlayerTrust";
  private final static String MAC_TRUST_DIR_REL_PATH = "/Library/Preferences/Macromedia/Flash Player/#Security/FlashPlayerTrust";
  private final static String LINUX_TRUST_DIR_REL_PATH = "/.macromedia/Flash_Player/#Security/FlashPlayerTrust";

  private final static String INTELLIJ_IDEA_CFG = "intellij_idea.cfg";

  private final static String OBJECT_TAG_NAME = "object";
  private final static String PARAM_TAG_NAME = "param";
  private final static String EMBED_TAG_NAME = "embed";
  private final static String NAME_ATTR_NAME = "name";
  private final static String MOVIE_ATTR_VALUE = "movie";
  private final static String VALUE_ATTR_NAME = "value";
  private final static String SRC_ATTR_NAME = "src";
  private final static String TYPE_ATTR_NAME = "type";
  private final static String TYPE_ATTR_VALUE = "application/x-shockwave-flash";
  private final static String DATA_ATTR_NAME = "data";

  private FlashPlayerTrustUtil() {
  }

  public static void updateTrustedStatus(final @NotNull Project project,
                                         final boolean isDebug,
                                         final @NotNull FlexRunnerParameters runnerParameters) {
    if (FlexBaseRunner.isRunAsAir(runnerParameters)) {
      return;
    }

    final FlexRunnerParameters.RunMode mode = runnerParameters.getRunMode();
    if (mode != FlexRunnerParameters.RunMode.HtmlOrSwfFile && mode != FlexRunnerParameters.RunMode.MainClass) {
      return;
    }

    final String[] trustedSwfPaths = getSwfFilesCanonicalPaths(project, runnerParameters);
    if (trustedSwfPaths.length == 0) {
      showWarningBalloonIfNeeded(project, isDebug, runnerParameters.isRunTrusted(), FlexBundle.message("could.not.find.swf.to.trust"));
      return;
    }

    updateTrustedStatus(project, runnerParameters.isRunTrusted(), isDebug, trustedSwfPaths);
  }

  public static void updateTrustedStatus(final Project project,
                                         final boolean trustedStatus,
                                         final boolean isDebug,
                                         final String... swfPaths) {
    final File ideaCfgFile = getIdeaUserTrustConfigFile(project, isDebug, trustedStatus);
    if (ideaCfgFile == null) {
      return;
    }

    try {
      fixIdeaCfgFileContentIfNeeded(ideaCfgFile, swfPaths, trustedStatus);
    }
    catch (IOException e) {
      // always show
      final ToolWindowManager manager = ToolWindowManager.getInstance(project);
      manager.notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.WARNING,
                              FlexBundle.message("failed.to.update.idea.trust.cfg.file", INTELLIJ_IDEA_CFG, e.getMessage()));
    }
  }

  private static void fixIdeaCfgFileContentIfNeeded(final @NotNull File ideaCfgFile,
                                                    final @NotNull String[] trustedSwfPaths,
                                                    final boolean runTrusted) throws IOException {

    final String content = FileUtil.loadFile(ideaCfgFile);

    for (final String trustedSwfPath : trustedSwfPaths) {
      int startIndex = content.indexOf(trustedSwfPath);
      int endIndex = startIndex + trustedSwfPath.length();
      if (startIndex != -1 &&
          (startIndex == 0 || content.charAt(startIndex - 1) == '\n' || content.charAt(startIndex - 1) == '\r') &&
          (endIndex == content.length() || content.charAt(endIndex) == '\n' || content.charAt(endIndex) == '\r')) {
        // already contains
        if (!runTrusted) {
          // remove this line
          final StringBuilder newContent = new StringBuilder();
          newContent.append(content, 0, startIndex);

          while (endIndex < content.length() && (content.charAt(endIndex) == '\n' || content.charAt(endIndex) == '\r')) {
            endIndex++;
          }

          if (endIndex < content.length()) {
            newContent.append(content, endIndex, content.length());
          }

          FileUtil.writeToFile(ideaCfgFile, newContent.toString());
        }
      }
      else {
        // doesn't contain yet
        if (runTrusted) {
          final StringBuilder newContent = new StringBuilder(content);
          if (content.length() > 0 && content.charAt(content.length() - 1) != '\n') {
            newContent.append('\n');
          }
          newContent.append(trustedSwfPath);
          newContent.append('\n');
          FileUtil.writeToFile(ideaCfgFile, newContent.toString());
        }
      }
    }
  }

  static String[] getSwfFilesCanonicalPaths(final Project project, final FlexRunnerParameters runnerParameters) {
    final FlexRunnerParameters.RunMode mode = runnerParameters.getRunMode();

    try {
      final String htmlOrSwfFilePath = runnerParameters.getHtmlOrSwfFilePath();
      final String extension = FileUtil.getExtension(htmlOrSwfFilePath);
      if (FlexUtils.isSwfExtension(extension)) {
        return new String[]{new File(htmlOrSwfFilePath).getCanonicalPath()};
      }
      else if (mode == FlexRunnerParameters.RunMode.HtmlOrSwfFile && FlexUtils.isHtmlExtension(extension)) {
        final VirtualFile htmlFile = LocalFileSystem.getInstance().findFileByPath(htmlOrSwfFilePath);
        if (htmlFile != null) {
          PsiFile psiFile = PsiManager.getInstance(project).findFile(htmlFile);
          if (!(psiFile instanceof XmlFile)) {
            final PsiFileFactory factory = PsiFileFactory.getInstance(project);
            psiFile = factory.createFileFromText("dummy.html", VfsUtil.loadText(htmlFile));
          }

          if (psiFile instanceof XmlFile) {
            return getSwfFilePathsFromHtmlWrapper((XmlFile)psiFile, htmlFile.getParent().getPath());
          }
        }
      }
    }
    catch (IOException e) {/**/}

    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  private static String[] getSwfFilePathsFromHtmlWrapper(final @NotNull XmlFile xmlFile, final String baseDirPath) throws IOException {
    final XmlDocument document = xmlFile.getDocument();
    final XmlTag rootTag = document == null ? null : document.getRootTag();
    if (rootTag != null) {
      final Set<String> result = new THashSet<String>();
      appendSwfFilesPathsRecursively(result, baseDirPath, rootTag);
      return ArrayUtil.toStringArray(result);
    }
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  private static void appendSwfFilesPathsRecursively(final Set<String> swfFilePaths, final String baseDirPath, final XmlTag tag)
    throws IOException {
    String swfRelPath = null;

    if (OBJECT_TAG_NAME.equals(tag.getName())) {
      final String typeAttrValue = tag.getAttributeValue(TYPE_ATTR_NAME);
      final String dataAttrValue = tag.getAttributeValue(DATA_ATTR_NAME);
      if (TYPE_ATTR_VALUE.equals(typeAttrValue) && !StringUtil.isEmptyOrSpaces(dataAttrValue)) {
        swfRelPath = dataAttrValue;
      }
    }
    else if (PARAM_TAG_NAME.equals(tag.getName())) {
      final XmlTag parentTag = tag.getParentTag();
      final String nameAttrValue = tag.getAttributeValue(NAME_ATTR_NAME);
      final String valueAttrValue = tag.getAttributeValue(VALUE_ATTR_NAME);
      if (parentTag != null &&
          OBJECT_TAG_NAME.equals(parentTag.getName()) &&
          MOVIE_ATTR_VALUE.equals(nameAttrValue) &&
          !StringUtil.isEmptyOrSpaces(valueAttrValue)) {
        swfRelPath = valueAttrValue;
      }
    }
    else if (EMBED_TAG_NAME.equals(tag.getName())) {
      final XmlTag parentTag = tag.getParentTag();
      final String typeAttrValue = tag.getAttributeValue(TYPE_ATTR_NAME);
      final String srcAttrValue = tag.getAttributeValue(SRC_ATTR_NAME);
      if (parentTag != null &&
          OBJECT_TAG_NAME.equals(parentTag.getName()) &&
          TYPE_ATTR_VALUE.equals(typeAttrValue) &&
          !StringUtil.isEmptyOrSpaces(srcAttrValue)) {
        swfRelPath = srcAttrValue;
      }
    }

    if (swfRelPath != null) {
      final File absFile = new File(swfRelPath);
      if (absFile.isAbsolute()) {
        swfFilePaths.add(absFile.getCanonicalPath());
      }
      else {
        swfFilePaths.add(new File(baseDirPath, swfRelPath).getCanonicalPath());
      }
    }

    for (final XmlTag subTag : tag.getSubTags()) {
      appendSwfFilesPathsRecursively(swfFilePaths, baseDirPath, subTag);
    }
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
