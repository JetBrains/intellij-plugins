// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefileVpath extends PsiElement {

  @NotNull
  List<MakefileDirectory> getDirectoryList();

  @Nullable
  MakefilePattern getPattern();

}
