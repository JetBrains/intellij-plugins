package org.angular2.entities;

import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Angular2DirectiveSelector {

  @NotNull
  String getText();

  @NotNull
  List<Angular2DirectiveSimpleSelector> getSimpleSelectors();

  @NotNull
  List<SimpleSelectorWithPsi> getSimpleSelectorsWithPsi();

  @NotNull
  Angular2DirectiveSelectorPsiElement getPsiElementForElement(@NotNull String elementName);

  interface SimpleSelectorWithPsi {

    Angular2DirectiveSelectorPsiElement getElement();

    List<Angular2DirectiveSelectorPsiElement> getAttributes();

    List<SimpleSelectorWithPsi> getNotSelectors();
  }
}
