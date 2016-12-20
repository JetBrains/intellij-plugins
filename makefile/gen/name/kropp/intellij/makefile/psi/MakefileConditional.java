// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefileConditional extends PsiElement {

  @NotNull
  MakefileElse_ getElse_();

  @NotNull
  MakefileElsebranch getElsebranch();

  @NotNull
  MakefileEndif getEndif();

  @NotNull
  MakefileIfeq getIfeq();

  @NotNull
  MakefileThenbranch getThenbranch();

}
