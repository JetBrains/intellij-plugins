package org.angular2.entities;

import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Angular2DirectiveSelector {

  @NotNull String getText();

  @NotNull List<@NotNull Angular2DirectiveSimpleSelector> getSimpleSelectors();

  @NotNull List<@NotNull  SimpleSelectorWithPsi> getSimpleSelectorsWithPsi();

  @NotNull Angular2DirectiveSelectorSymbol getSymbolForElement(@NotNull String elementName);

  interface SimpleSelectorWithPsi {

    @Nullable Angular2DirectiveSelectorSymbol getElement();

    @NotNull List<@NotNull Angular2DirectiveSelectorSymbol> getAttributes();

    @NotNull List<@NotNull SimpleSelectorWithPsi> getNotSelectors();

    @Nullable Angular2DirectiveSelectorSymbol getElementAt(int offset);
  }
}
