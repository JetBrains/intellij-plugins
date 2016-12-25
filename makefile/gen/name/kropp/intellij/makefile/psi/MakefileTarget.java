// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface MakefileTarget extends MakefileNamedElement {

  @Nullable
  String getName();

  PsiElement setName(String newName);

  @Nullable
  PsiElement getNameIdentifier();

}
