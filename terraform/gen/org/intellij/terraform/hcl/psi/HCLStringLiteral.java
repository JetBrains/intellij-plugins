// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;

public interface HCLStringLiteral extends HCLLiteral, PsiLanguageInjectionHost, NavigatablePsiElement {

  @NotNull List<Pair<TextRange, String>> getTextFragments();

  @NotNull String getValue();

  char getQuoteSymbol();

}
