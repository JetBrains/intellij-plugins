// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.typescript.refactoring.ES6MoveFileHandler;
import com.intellij.psi.PsiFile;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.codeInsight.refs.AngularJSTemplateReferencesProvider.Angular2SoftFileReferenceSet;
import org.angularjs.codeInsight.refs.AngularJSTemplateReferencesProvider.Angular2TemplateReferenceData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Angular2MoveFileHandler extends ES6MoveFileHandler {

  @Override
  public boolean canProcessElement(PsiFile element) {
    return element instanceof JSFile
           && DialectDetector.isTypeScript(element)
           && Angular2LangUtil.isAngular2Context(element);
  }

  @Override
  protected @NotNull List<UsageInfo> doFindUsages(@NotNull PsiFile psiFile) {
    // In addition to hack from ES6MoveFileHandler for preventing broken file reference,
    // we need to workaround broken contract of `prepareMovedFile` when moving directories.
    Map<String, Angular2TemplateReferenceData> map = Angular2SoftFileReferenceSet.encodeTemplateReferenceData(psiFile);
    if (map.isEmpty()) return super.doFindUsages(psiFile);
    return ContainerUtil.append(super.doFindUsages(psiFile), new MyRestoreReferencesUsage(psiFile, map));
  }

  @Override
  public void updateMovedFile(PsiFile file) throws IncorrectOperationException {
    super.updateMovedFile(file);
    Angular2SoftFileReferenceSet.decodeTemplateReferenceData(file);
  }

  private static final class MyRestoreReferencesUsage extends RestoreReferencesUsage<Map<String, Angular2TemplateReferenceData>> {

    MyRestoreReferencesUsage(@NotNull PsiFile element, @NotNull Map<String, Angular2TemplateReferenceData> refs) {
      super(element, refs);
    }

    @Override
    protected void restore(@NotNull PsiFile file) {
      Angular2SoftFileReferenceSet.decodeTemplateReferenceData(file, myRefs);
    }
  }
}
