// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MakefileTargetLine extends PsiElement {

  @Nullable
  MakefileOverride getOverride();

  @Nullable
  MakefilePrerequisites getPrerequisites();

  @Nullable
  MakefilePrivatevar getPrivatevar();

  @Nullable
  MakefileTargetPattern getTargetPattern();

  @NotNull
  MakefileTargets getTargets();

  @Nullable
  MakefileVariableAssignment getVariableAssignment();

  @Nullable
  String getTargetName();

}
