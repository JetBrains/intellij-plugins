package com.intellij.flex.util;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.inspections.actionscript.JSUntypedDeclarationInspection;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collection;

public abstract class ActionScriptDaemonAnalyzerTestCase extends JSDaemonAnalyzerTestCase {

  protected void runUntypedDeclarationInspectionTestWithFix(final String fileName, final String[] files, String ext) throws Exception {
    try {
      enableInspectionTool(new JSUntypedDeclarationInspection());
      Collection<HighlightInfo> infos = doTestFor(true, files);

      findAndInvokeIntentionAction(infos, "Add Type to Declaration", myEditor, myFile);

      checkResultByFile(getBasePath() + "/" + fileName + "_after." + ext);
    }
    finally {
      LookupManager.getInstance(myProject).hideActiveLookup();
    }
  }

  @Override
  protected void addLibraries(VirtualFile[] files) {
    for (VirtualFile file : files) {
      if (file != null && "swc".equals(file.getExtension())) {
        FlexTestUtils.addFlexLibrary(false, myModule, file.getName(), true, file.getParent().getPath(), file.getName(), null, null);
      }
    }
  }
}
