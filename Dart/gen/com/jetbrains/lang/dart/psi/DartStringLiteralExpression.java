// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;

public interface DartStringLiteralExpression extends DartExpression, DartReference, PsiLanguageInjectionHost {

  @NotNull
  List<DartLongTemplateEntry> getLongTemplateEntryList();

  @NotNull
  List<DartShortTemplateEntry> getShortTemplateEntryList();

}
