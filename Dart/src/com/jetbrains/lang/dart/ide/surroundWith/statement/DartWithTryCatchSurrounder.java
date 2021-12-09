// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartTryStatement;
import org.jetbrains.annotations.Nullable;

public class DartWithTryCatchSurrounder extends DartBlockStatementSurrounderBase {
  @Override
  public String getTemplateDescription() {
    @NlsSafe String description = "try / catch";
    return description;
  }

  @Override
  protected String getTemplateText() {
    return "try {\n} catch (e) {\ncaret_here: print(e);\n}";
  }

  @Override
  @Nullable
  protected PsiElement findElementToDelete(PsiElement surrounder) {
    //noinspection ConstantConditions
    return ((DartTryStatement)surrounder).getOnPartList().get(0).getBlock().getStatements().getFirstChild(); // todo preselect print(e);
  }
}
