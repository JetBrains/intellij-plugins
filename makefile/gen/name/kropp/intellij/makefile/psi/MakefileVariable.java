// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.NavigationItem;

public interface MakefileVariable extends MakefileNamedElement, NavigationItem {

  @NotNull
  MakefileIdentifier getIdentifier();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String newName);

  @Nullable
  PsiElement getNameIdentifier();

}
