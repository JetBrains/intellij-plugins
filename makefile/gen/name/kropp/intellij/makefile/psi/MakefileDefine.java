// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface MakefileDefine extends PsiElement {

  @Nullable
  MakefileVariable getVariable();

  @Nullable
  PsiElement getAssignment();

  @Nullable
  String getValue();

}
