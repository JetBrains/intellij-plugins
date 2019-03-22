// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.highlighting;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class SwfHighlightingTest extends LightPlatformCodeInsightFixtureTestCase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(FlexHighlightingTest.BASE_PATH);
  }

  public void testLineMarkersInSwf() {
    final String testName = getTestName(false);
    FlexTestUtils.addLibrary(myModule, "flex lib", getTestDataPath() + getBasePath(), testName + ".swc", null, null);
    try {
      VirtualFile swcFile = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + ".swc");
      VirtualFile swfFile = JarFileSystem.getInstance().getJarRootForLocalFile(swcFile).findChild("library.swf");
      myFixture.openFileInEditor(swfFile);
      JSDaemonAnalyzerLightTestCase.checkHighlightByFile(myFixture, getTestDataPath() + getBasePath() + "/" + getTestName(false) + ".as");
    }
    finally {
      FlexTestUtils.removeLibrary(myModule, "flex lib");
    }
  }

  public void testProtectSwf() {
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + getTestName(false) + ".swf");
    myFixture.openFileInEditor(vFile);
    assertFalse(FileDocumentManager.getInstance().requestWriting(myFixture.getEditor().getDocument(), getProject()));
  }
}
