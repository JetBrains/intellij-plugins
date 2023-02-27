// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HCLHeredocLiteral extends HCLLiteral {

  @NotNull String getValue();

  @NotNull
  HCLHeredocContent getContent();

  @NotNull
  HCLHeredocMarker getMarkerStart();

  @Nullable
  HCLHeredocMarker getMarkerEnd();

  boolean isIndented();

  @Nullable Integer getIndentation();

}
