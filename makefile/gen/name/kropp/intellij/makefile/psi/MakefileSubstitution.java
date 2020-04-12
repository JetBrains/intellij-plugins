// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MakefileSubstitution extends PsiLanguageInjectionHost {

  @NotNull
  List<MakefileFunction> getFunctionList();

  @NotNull
  List<MakefileVariableUsage> getVariableUsageList();

}
