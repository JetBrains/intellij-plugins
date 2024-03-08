// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ILTemplateBlockBody extends ILExpression {

  @NotNull
  List<BadTag> getBadTagList();

  @NotNull
  List<ILExpression> getILExpressionList();

  @NotNull
  List<ILTemplateHolder> getILTemplateHolderList();

}
