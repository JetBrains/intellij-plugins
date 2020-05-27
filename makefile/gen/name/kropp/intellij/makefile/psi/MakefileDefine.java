// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefileDefine extends PsiElement {

  @NotNull
  List<MakefileFunction> getFunctionList();

  @NotNull
  List<MakefileString> getStringList();

  @NotNull
  List<MakefileSubstitution> getSubstitutionList();

  @Nullable
  MakefileVariable getVariable();

  @NotNull
  List<MakefileVariableUsage> getVariableUsageList();

  @Nullable
  PsiElement getAssignment();

  @Nullable
  String getValue();

}
