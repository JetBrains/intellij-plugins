package com.google.jstestdriver.idea.assertFramework;

import com.google.jstestdriver.idea.icons.JstdIcons;
import com.intellij.ide.IconProvider;
import com.intellij.lang.javascript.index.JSIndexEntry;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: ksafonov
 */
public class JsTestDriverIconProvider extends IconProvider {

  @Override
  public Icon getIcon(@NotNull final PsiElement element, @Iconable.IconFlags final int flags) {
    if (element instanceof JSFile && isTestFile((JSFile)element)) {
      return JstdIcons.TEST_FILE_ICON;
    }
    return null;
  }

  private static boolean isTestFile(final JSFile file) {
    JSIndexEntry entry = JavaScriptIndex.getInstance(file.getProject()).getEntryForFile(file);
    return entry != null && entry.isTestFile();
  }
}
