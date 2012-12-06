package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartCatchPart;
import com.jetbrains.lang.dart.psi.DartFormalParameterList;
import com.jetbrains.lang.dart.psi.DartTryStatement;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartWithTryCatchSurrounder extends DartBlockStatementSurrounderBase {
  @Override
  public String getTemplateDescription() {
    return "try / catch";
  }

  @Override
  protected String getTemplateText() {
    return "try {\n} catch (Error err) {\n\n}";
  }

  @Override
  protected PsiElement findElementToDelete(PsiElement surrounder) {
    assert surrounder instanceof DartTryStatement;
    final List<DartCatchPart> catchPartList = ((DartTryStatement)surrounder).getCatchPartList();
    assert !catchPartList.isEmpty();
    DartFormalParameterList parameterList = catchPartList.iterator().next().getFormalParameterList();
    assert parameterList != null;
    return parameterList.getNormalFormalParameterList().iterator().next();
  }
}
