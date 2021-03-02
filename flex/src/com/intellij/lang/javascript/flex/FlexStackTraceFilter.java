// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlexStackTraceFilter implements Filter {

  public static final String GLOBAL_PREFIX = "global/";
  private final Project myProject;

  private static final String AT = "at ";

  public FlexStackTraceFilter(Project project) {
    myProject = project;
  }

  @Override
  @Nullable
  public Result applyFilter(@NotNull final String line, final int entireLength) {
    //    [trace]    at org.flexunit::Assert$/fail()[E:\hudson\jobs\FlexUnit4-Flex4.1\workspace\FlexUnit4\src\org\flexunit\Assert.as:294]
    //    at org.flexunit::Assert$/fail()[E:\hudson\jobs\FlexUnit4-Flex4.1\workspace\FlexUnit4\src\org\flexunit\Assert.as:294]
    //    at global/org.flexunit.asserts::fail()[E:\hudson\jobs\FlexUnit4-Flex4.1\workspace\FlexUnit4\src\org\flexunit\asserts\fail.as:39]
    //    at foo::SomeTest/testSmth()[/untitled5/app/testSrc/foo/SomeTest.as:19]
    //    at Function/http://adobe.com/AS3/2006/builtin::apply()
    //    at mx.core::UIComponent/set initialized()[C:\autobuild\3.3.0\frameworks\projects\framework\src\mx\core\UIComponent.as:1169]

    final String trimmed = StringUtil.trimStart(line, "[trace]").trim();
    final int bracketOpenIndex = line.lastIndexOf('[');

    if (!trimmed.startsWith(AT) || bracketOpenIndex < 0 || !trimmed.endsWith("]")) return null;

    final int bracketCloseIndex = line.lastIndexOf(']');
    final String pathAndLineNumber = FileUtil.toSystemIndependentName(line.substring(bracketOpenIndex + 1, bracketCloseIndex));

    String filePath = pathAndLineNumber; // "E:\hudson\jobs\FlexUnit4-Flex4.1\workspace\FlexUnit4\src\org\flexunit\Assert.as:294"
    int lineNumber = -1;

    final int colonIndex = pathAndLineNumber.lastIndexOf(':');
    if (colonIndex > 0) {
      try {
        lineNumber = Integer.parseInt(pathAndLineNumber.substring(colonIndex + 1)) - 1; // 294 - 1 = 293
        filePath =
          pathAndLineNumber.substring(0, colonIndex);  // "E:\hudson\jobs\FlexUnit4-Flex4.1\workspace\FlexUnit4\src\org\flexunit\Assert.as"
      }
      catch (NumberFormatException ignore) {/*unlucky*/}
    }

    if (filePath.startsWith("//")) return null;  // UNC path

    final String fileName = PathUtil.getFileName(filePath); // Assert.as
    if (!FlexCommonUtils.isSourceFile(fileName)) return null; // Flex filter cares only about *.as and *.mxml

    final int atIndex = line.indexOf(AT);
    String fqnInfo = line.substring(atIndex + AT.length(), bracketOpenIndex).trim(); // "org.flexunit::Assert$/fail()"
    fqnInfo = StringUtil.trimStart(fqnInfo, GLOBAL_PREFIX); // for case like "global/org.flexunit.asserts::fail()"

    final int slashOrParenIndex = StringUtil.indexOfAny(fqnInfo, "/(");

    if (slashOrParenIndex > 0) {
      String somethingLikeFqn = fqnInfo.substring(0, slashOrParenIndex);  // "org.flexunit::Assert$"

      somethingLikeFqn = StringUtil.trimEnd(somethingLikeFqn, "$");

      final int dotIndex = fileName.lastIndexOf('.');
      final String dotExtension = dotIndex < 0 ? "" : fileName.substring(dotIndex);
      final String fileNameWithoutExtension = FileUtilRt.getNameWithoutExtension(fileName);

      if (somethingLikeFqn.equals(fileNameWithoutExtension) || somethingLikeFqn.endsWith("::" + fileNameWithoutExtension)) {
        final String relativePath =
          StringUtil.replace(somethingLikeFqn, "::", "/").replace('.', '/') + dotExtension; // "org/flexunit/Assert.as"

        if (filePath.endsWith(relativePath)) {
          final int textStartOffset = entireLength - line.length();
          final int highlightEndOffset = textStartOffset + bracketCloseIndex;

          return applyFlexStackTraceFilter(filePath, relativePath, lineNumber, highlightEndOffset);
        }
      }
    }

    return null;
  }

  @Nullable
  private Result applyFlexStackTraceFilter(final String filePath,
                                           final String matchingRelativePath,
                                           final int lineNumber,
                                           final int highlightEndOffset) {
    final Collection<VirtualFile> result = new ArrayList<>();

    final Collection<VirtualFile> files =
      FilenameIndex.getVirtualFilesByName(myProject, PathUtil.getFileName(filePath), GlobalSearchScope.allScope(myProject));

    for (final VirtualFile file : files) {
      if (file.getPath().endsWith(matchingRelativePath)) {
        result.add(file);
      }
    }

    if (!result.isEmpty()) {
      final int colonLineNumberLength = lineNumber > 0 ? (":".length() + String.valueOf(lineNumber).length()) : 0;
      int highlightingLength = matchingRelativePath.length() + colonLineNumberLength;

      if (result.size() == 1 && result.iterator().next().getPath().equals(filePath)) {
        highlightingLength = filePath.length() + colonLineNumberLength;
      }

      final int highlightStartOffset = highlightEndOffset - highlightingLength;
      return new Result(highlightStartOffset, highlightEndOffset, new OpenOneOfSeveralFilesHyperlinkInfo(result, lineNumber));
    }

    return null;
  }

  private static class OpenOneOfSeveralFilesHyperlinkInfo implements HyperlinkInfo {

    private final Collection<VirtualFile> myFiles;
    private final int myLine;

    OpenOneOfSeveralFilesHyperlinkInfo(@NotNull final Collection<VirtualFile> files, final int line) {
      myFiles = files;
      myLine = line;
    }

    @Override
    public void navigate(@NotNull Project project) {
      final List<VirtualFile> validFiles = new ArrayList<>(myFiles.size());
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
        final Collection<PsiFile> psiFiles = new ArrayList<>(validFiles.size());
        for (final VirtualFile file : validFiles) {
          final PsiFile psiFile = psiManager.findFile(file);
          if (psiFile != null) {
            psiFiles.add(psiFile);
          }
        }

        final JList list = new JBList(PsiUtilCore.toPsiFileArray(psiFiles));
        list.setCellRenderer(new DefaultPsiElementCellRenderer());

        JBPopup popup = JBPopupFactory.getInstance()
          .createPopupChooserBuilder(ContainerUtil.newArrayList(PsiUtilCore.toPsiFileArray(psiFiles)))
          .setItemChosenCallback((selectedElement) -> {
            final VirtualFile file = selectedElement.getVirtualFile();
            if (file != null) {
              FileEditorManager.getInstance(project)
                .openTextEditor(new OpenFileDescriptor(project, file, myLine, 0), true);
            }
          }).createPopup();
        final JFrame frame = WindowManager.getInstance().getFrame(project);
        final Point mousePosition = frame.getMousePosition();
        if (mousePosition != null) {
          popup.show(new RelativePoint(frame, mousePosition));
        }
      }
    }
  }
}
