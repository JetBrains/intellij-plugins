package com.intellij.lang.javascript.flex;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlexStackTraceFilter implements Filter {

  // taken from RStackTraceFilter

  // Disk name for Windows
  private static final String DISK_PATTERN = "\\p{Alpha}:";

  private static final String FILENAME_PATTERN = "[^:/\\\\]+";

  // Extension pattern
  public static final String EXT_PATTERN = "\\.[\\p{Graph}^:/\\\\\\?]+";

  private static final String LINE_NUM_PATTERN = ":(\\d+)";
  // Path use both type separators(for Unix and Windows )
  private static final String PATTERN = "(?:/|\\\\)[^:]*" + FILENAME_PATTERN + "(?:" + EXT_PATTERN + ")?";

  private static final Pattern WIN_CPATTERN = Pattern.compile("\\[((" + DISK_PATTERN + PATTERN + ")" + LINE_NUM_PATTERN + ")\\]");

  private static final Pattern UNIX_CPATTERN = Pattern.compile("\\[((" + PATTERN + ")" + LINE_NUM_PATTERN + ")\\]");

  private final Project myProject;

  public FlexStackTraceFilter(Project project) {
    myProject = project;
  }

  @Nullable
  public Result applyFilter(final String line, final int entireLength) {
    if (line.indexOf('[') < 0) {
      return null;
    }

    Pattern p = SystemInfo.isWindows ? WIN_CPATTERN : UNIX_CPATTERN;
    Matcher m = p.matcher(line);
    if (!m.find()) {
      p = SystemInfo.isWindows ? UNIX_CPATTERN : WIN_CPATTERN;
      m = p.matcher(line);
      if (!m.find()) {
        return null;
      }
    }

    final int textStartOffset = entireLength - line.length();
    final int highlightStartOffset = textStartOffset + m.start(1);
    final int highlightEndOffset = textStartOffset + m.end(1);
    int lineNumber = 0;
    try {
      lineNumber = StringUtil.isEmpty(m.group(3)) ? 0 : Integer.parseInt(m.group(3)) - 1;
    }
    catch (NumberFormatException ignore) {/*ignore*/}

    final String filePath = m.group(2);

    final File file = new File(filePath);
    if (!file.isFile()) {
      return applyFlexStackTraceFilter(line, filePath, highlightEndOffset, lineNumber);
    }

    final Ref<VirtualFile> vFile = new Ref<VirtualFile>();

    try {
      GuiUtils.runOrInvokeAndWait(new Runnable() {
        public void run() {
          vFile.set(LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file));
        }
      });
    }
    catch (InvocationTargetException e) {
      // skip
    }
    catch (InterruptedException e) {
      // skip
    }

    if (vFile.isNull()) {
      return null;
    }

    final OpenFileHyperlinkInfo info = new OpenFileHyperlinkInfo(myProject, vFile.get(), lineNumber);
    return new Result(highlightStartOffset, highlightEndOffset, info);
  }

  @Nullable
  private Result applyFlexStackTraceFilter(final String line,
                                           final String _filePath,
                                           final int highlightEndOffset,
                                           final int lineNumber) {
    // 	at mx.core::UIComponent/set initialized()[C:\autobuild\3.3.0\frameworks\projects\framework\src\mx\core\UIComponent.as:1169]
    // 	at Main/launchTask()[C:\path\Main.mxml:144]

    final String filePath = FileUtil.toSystemIndependentName(_filePath);
    final String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
    final String fileNameWithoutExtension = FileUtil.getNameWithoutExtension(fileName);

    final String AT = "at ";
    final int atIndex = line.indexOf(AT);
    final int slashIndex = line.indexOf("/", atIndex);

    if (atIndex >= 0 && slashIndex > atIndex) {
      final String somethingLikeFqn = line.substring(atIndex + AT.length(), slashIndex);

      if (somethingLikeFqn.equals(fileNameWithoutExtension) || somethingLikeFqn.endsWith("::" + fileNameWithoutExtension)) {
        final StringTokenizer tokenizer = new StringTokenizer(somethingLikeFqn, ".:", false);
        final StringBuilder relativePathBuffer = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
          if (relativePathBuffer.length() > 0) {
            relativePathBuffer.append('/');
          }
          relativePathBuffer.append(tokenizer.nextToken());
        }
        relativePathBuffer.append('.');
        relativePathBuffer.append(FileUtil.getExtension(fileName));

        final String relativePathToLookFor = relativePathBuffer.toString();
        if (filePath.endsWith(relativePathToLookFor)) {
          final Collection<VirtualFile> result = new ArrayList<VirtualFile>();

          final Collection<VirtualFile> files =
            FilenameIndex.getVirtualFilesByName(myProject, fileName, GlobalSearchScope.allScope(myProject));

          for (final VirtualFile file : files) {
            if (file.getPath().endsWith(relativePathToLookFor)) {
              result.add(file);
            }
          }

          if (!result.isEmpty()) {
            final int highlightStartOffset =
              highlightEndOffset - relativePathToLookFor.length() - (lineNumber > 0 ? (String.valueOf(lineNumber).length() + 1) : 0);
            return new Result(highlightStartOffset, highlightEndOffset, new OpenOneOfSeveralFilesHyperlinkInfo(result, lineNumber));
          }
        }
      }
    }

    return null;
  }

  private static class OpenOneOfSeveralFilesHyperlinkInfo implements HyperlinkInfo {

    private final Collection<VirtualFile> myFiles;
    private final int myLine;

    public OpenOneOfSeveralFilesHyperlinkInfo(@NotNull final Collection<VirtualFile> files, final int line) {
      myFiles = files;
      myLine = line;
    }

    public void navigate(final Project project) {
      final List<VirtualFile> validFiles = new ArrayList<VirtualFile>(myFiles.size());
      for (final VirtualFile file : myFiles) {
        if (file.isValid()) {
          validFiles.add(file);
        }
      }

      if (validFiles.isEmpty()) {
        return;
      }

      if (validFiles.size() == 1) {
        FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, validFiles.get(0), myLine, 0), true);
      }
      else {
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Collection<PsiFile> psiFiles = new ArrayList<PsiFile>(validFiles.size());
        for (final VirtualFile file : validFiles) {
          final PsiFile psiFile = psiManager.findFile(file);
          if (psiFile != null) {
            psiFiles.add(psiFile);
          }
        }

        final JList list = new JBList(PsiUtilCore.toPsiFileArray(psiFiles));
        list.setCellRenderer(new DefaultPsiElementCellRenderer());

        final PopupChooserBuilder builder = new PopupChooserBuilder(list);

        final JBPopup popup = builder
          .setItemChoosenCallback(new Runnable() {
            public void run() {
              final Object[] selectedElements = list.getSelectedValues();
              if (selectedElements != null && selectedElements.length == 1 && selectedElements[0] instanceof PsiFile) {
                final VirtualFile file = ((PsiFile)selectedElements[0]).getVirtualFile();
                if (file != null) {
                  FileEditorManager.getInstance(project)
                    .openTextEditor(new OpenFileDescriptor(project, file, myLine, 0), true);
                }
              }
            }
          })
          .createPopup();

        final JFrame frame = WindowManager.getInstance().getFrame(project);
        final Point mousePosition = frame.getMousePosition();
        if (mousePosition != null) {
          popup.show(new RelativePoint(frame, mousePosition));
        }
      }
    }
  }
}
