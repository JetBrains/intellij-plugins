// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

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
