// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;

public interface HCLHeredocContent extends HCLElement {

  @NotNull List<String> getLines();

  @NotNull List<CharSequence> getLinesRaw();

  int getLinesCount();

  @NotNull String getValue();

  @Nullable Integer getMinimalIndentation();

  @NotNull List<Pair<TextRange, String>> getTextFragments();

}
