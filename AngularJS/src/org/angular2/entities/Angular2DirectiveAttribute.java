package org.angular2.entities;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.model.Pointer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.angular2.web.Angular2PsiSourcedSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.KIND_NG_DIRECTIVE_ATTRIBUTES;

public interface Angular2DirectiveAttribute extends Angular2PsiSourcedSymbol, Angular2Element {

  @Override
  @NotNull
  String getName();

  @Nullable
  @Override
  JSType getType();

  @NotNull
  @Override
  Pointer<? extends Angular2DirectiveAttribute> createPointer();

  @NotNull
  @Override
  default PsiElement getSource() {
    return getSourceElement();
  }

  @Override
  default @NotNull Project getProject() {
    return getSourceElement().getProject();
  }

  @NotNull
  @Override
  default String getNamespace() {
    return NAMESPACE_JS;
  }

  @NotNull
  @Override
  default String getKind() {
    return KIND_NG_DIRECTIVE_ATTRIBUTES;
  }
}
