// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MakefileDefine extends PsiElement {

  @NotNull
  List<MakefileFunction> getFunctionList();

  @Nullable
  MakefileVariable getVariable();

  @NotNull
  List<MakefileVariableUsage> getVariableUsageList();

  @Nullable
  PsiElement getAssignment();

  @Nullable
  String getValue();

}
