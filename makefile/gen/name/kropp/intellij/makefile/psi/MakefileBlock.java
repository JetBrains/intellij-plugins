// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefileBlock extends PsiElement {

  @Nullable
  MakefileDefine getDefine();

  @Nullable
  MakefileExport getExport();

  @Nullable
  MakefileInclude getInclude();

  @Nullable
  MakefileOverride getOverride();

  @Nullable
  MakefilePrivatevar getPrivatevar();

  @Nullable
  MakefileUndefine getUndefine();

  @Nullable
  MakefileVariableAssignment getVariableAssignment();

  @Nullable
  MakefileVpath getVpath();

}
