// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.util;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.flex.FlexTestOption;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.inspections.actionscript.JSUntypedDeclarationInspection;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public abstract class ActionScriptDaemonAnalyzerTestCase extends JSDaemonAnalyzerTestCase {

  protected void runUntypedDeclarationInspectionTestWithFix(final String fileName, final String[] files, String ext) throws Exception {
    enableInspectionTool(new JSUntypedDeclarationInspection());
    Collection<HighlightInfo> infos = doTestFor(true, files);

    findAndInvokeIntentionAction(infos, "Add Type to Declaration", myEditor, myFile);

    checkResultByFile(getBasePath() + "/" + fileName + "_after." + ext);
  }

  @Override
  protected void addLibraries(VirtualFile[] files) {
    for (VirtualFile file : files) {
      if (file != null && "swc".equals(file.getExtension())) {
        FlexTestUtils.addFlexLibrary(false, myModule, file.getName(), true, file.getParent().getPath(), file.getName(), null, null);
      }
    }
  }

  @Override
  protected LocalInspectionTool[] configureLocalInspectionTools() {
    ArrayList<LocalInspectionTool> tools = new ArrayList<>(Arrays.asList(super.configureLocalInspectionTools()));
    if (FlexTestUtils.testMethodHasOption(getClass(), getTestName(false), FlexTestOption.WithUnusedImports)) {
      tools.add(new JSUnusedLocalSymbolsInspection());
    }
    return tools.toArray(LocalInspectionTool.EMPTY_ARRAY);
  }
}
