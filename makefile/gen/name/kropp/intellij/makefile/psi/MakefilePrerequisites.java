// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefilePrerequisites extends PsiElement {

  @NotNull
  MakefileNormalPrerequisites getNormalPrerequisites();

  @Nullable
  MakefileOrderOnlyPrerequisites getOrderOnlyPrerequisites();

}
