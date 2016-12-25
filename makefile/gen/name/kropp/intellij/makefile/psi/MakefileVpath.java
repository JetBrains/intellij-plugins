// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MakefileVpath extends PsiElement {

  @NotNull
  List<MakefileDirectory> getDirectoryList();

  @Nullable
  MakefilePattern getPattern();

}
