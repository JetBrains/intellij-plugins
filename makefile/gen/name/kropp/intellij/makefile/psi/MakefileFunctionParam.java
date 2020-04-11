// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MakefileFunctionParam extends PsiElement {

  @NotNull
  List<MakefileFunction> getFunctionList();

  @NotNull
  List<MakefileFunctionName> getFunctionNameList();

  @NotNull
  List<MakefileSubstitution> getSubstitutionList();

  @NotNull
  List<MakefileVariableUsage> getVariableUsageList();

}
