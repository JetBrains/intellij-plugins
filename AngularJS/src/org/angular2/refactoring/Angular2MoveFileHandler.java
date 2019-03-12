// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.typescript.refactoring.ES6MoveFileHandler;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.codeInsight.refs.AngularJSTemplateReferencesProvider;

import java.util.Map;

public class Angular2MoveFileHandler extends ES6MoveFileHandler {
  @Override
  public boolean canProcessElement(PsiFile element) {
    return element instanceof JSFile
           && DialectDetector.isTypeScript(element)
           && Angular2LangUtil.isAngular2Context(element);
  }

  @Override
  public void prepareMovedFile(PsiFile file, PsiDirectory moveDestination, Map<PsiElement, PsiElement> oldToNewMap) {
    super.prepareMovedFile(file, moveDestination, oldToNewMap);
    AngularJSTemplateReferencesProvider.Angular2SoftFileReferenceSet.encodeAmbiguousRelativePathData(file);
  }

  @Override
  public void updateMovedFile(PsiFile file) throws IncorrectOperationException {
    super.updateMovedFile(file);
    AngularJSTemplateReferencesProvider.Angular2SoftFileReferenceSet.decodeAmbiguousRelativePathData(file);
  }
}
