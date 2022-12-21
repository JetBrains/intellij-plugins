// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.model.Pointer;
import com.intellij.openapi.util.ClearableLazyValue;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import com.intellij.util.containers.JBIterable;
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector.Angular2DirectiveSimpleSelectorWithRanges;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.intellij.refactoring.suggested.UtilsKt.createSmartPointer;
import static com.intellij.util.ObjectUtils.notNull;

public final class Angular2DirectiveSelectorImpl implements Angular2DirectiveSelector {
  private final ClearableLazyValue<PsiElement> myLazyParent;
  private PsiElement myElement;
  private final String myText;
  private final Integer myRangeOffset;
  private final NotNullLazyValue<List<Angular2DirectiveSimpleSelector>> mySimpleSelectors;
  private final NotNullLazyValue<List<SimpleSelectorWithPsi>> mySimpleSelectorsWithPsi;

  public Angular2DirectiveSelectorImpl(@NotNull PsiElement element,
                                       @Nullable String text,
                                       @Nullable Integer rangeOffset) {
    myElement = element;
    myLazyParent = element instanceof Angular2MetadataDirectiveBase
                   ? ClearableLazyValue.createAtomic(() -> notNull(((Angular2MetadataDirectiveBase<?>)myElement).getTypeScriptClass(),
                                                                   myElement))
                   : null;
    myText = text;
    myRangeOffset = rangeOffset;
    mySimpleSelectors = NotNullLazyValue.lazy(() -> {
      if (myText == null) {
        return Collections.emptyList();
      }
      try {
        return Collections.unmodifiableList(Angular2DirectiveSimpleSelector.parse(myText));
      }
      catch (ParseException e) {
        return Collections.emptyList();
      }
    });
    mySimpleSelectorsWithPsi = NotNullLazyValue.lazy(() -> {
      if (myText == null) {
        return Collections.emptyList();
      }
      try {
        List<Angular2DirectiveSimpleSelectorWithRanges> simpleSelectorsWithRanges = Angular2DirectiveSimpleSelector.parseRanges(myText);
        List<SimpleSelectorWithPsi> result = new ArrayList<>(simpleSelectorsWithRanges.size());
        for (Angular2DirectiveSimpleSelectorWithRanges sel : simpleSelectorsWithRanges) {
          result.add(new SimpleSelectorWithPsiImpl(sel, null));
        }
        return Collections.unmodifiableList(result);
      }
      catch (ParseException e) {
        return Collections.emptyList();
      }
    });
  }

  public Pointer<Angular2DirectiveSelectorImpl> createPointer() {
    var element = createSmartPointer(myElement);
    var text = myText;
    var rangeOffset = myRangeOffset;
    return () -> {
      var newElement = element.getElement();
      return newElement != null ? new Angular2DirectiveSelectorImpl(newElement, text, rangeOffset) : null;
    };
  }

  @Override
  public @NotNull String getText() {
    return myText == null ? "<null>" : myText;
  }

  @NotNull
  public PsiElement getPsiParent() {
    return myElement instanceof Angular2MetadataDirectiveBase ? myLazyParent.getValue() : myElement;
  }

  @Override
  public @NotNull List<Angular2DirectiveSimpleSelector> getSimpleSelectors() {
    return mySimpleSelectors.getValue();
  }

  @Override
  public @NotNull List<SimpleSelectorWithPsi> getSimpleSelectorsWithPsi() {
    return mySimpleSelectorsWithPsi.getValue();
  }

  @Override
  public @NotNull Angular2DirectiveSelectorSymbol getSymbolForElement(@NotNull String elementName) {
    for (SimpleSelectorWithPsi selector : getSimpleSelectorsWithPsi()) {
      if (selector.getElement() != null && elementName.equalsIgnoreCase(selector.getElement().getName())) {
        return selector.getElement();
      }
      for (SimpleSelectorWithPsi notSelector : selector.getNotSelectors()) {
        if (notSelector.getElement() != null && elementName.equalsIgnoreCase(notSelector.getElement().getName())) {
          return notSelector.getElement();
        }
      }
    }
    return new Angular2DirectiveSelectorSymbol(this, new TextRange(0, 0), elementName, null, true);
  }

  @Override
  public String toString() {
    return getText();
  }

  @NotNull
  private Angular2DirectiveSelectorSymbol convert(@NotNull Pair<String, Integer> range,
                                                  @Nullable String elementSelector,
                                                  boolean isElement) {
    return new Angular2DirectiveSelectorSymbol(
      this,
      myRangeOffset != null
      ? new TextRange(range.second + myRangeOffset, range.second + range.first.length() + myRangeOffset)
      : TextRange.EMPTY_RANGE,
      range.first, elementSelector, isElement);
  }

  public void replaceText(@NotNull TextRange range, @NotNull String name) {
    myElement = ElementManipulators.getManipulator(myElement)
      .handleContentChange(myElement, range, name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2DirectiveSelectorImpl selector = (Angular2DirectiveSelectorImpl)o;
    return Objects.equals(myElement, selector.myElement) &&
           Objects.equals(myText, selector.myText) &&
           Objects.equals(myRangeOffset, selector.myRangeOffset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myElement, myText, myRangeOffset);
  }

  private class SimpleSelectorWithPsiImpl implements SimpleSelectorWithPsi {

    private final Angular2DirectiveSelectorSymbol myElement;
    private final List<Angular2DirectiveSelectorSymbol> myAttributes = new SmartList<>();
    private final List<SimpleSelectorWithPsi> myNotSelectors = new SmartList<>();

    SimpleSelectorWithPsiImpl(@NotNull Angular2DirectiveSimpleSelectorWithRanges selectorWithRanges,
                              @Nullable String mainElementSelector) {
      String myElementName = null;
      if (selectorWithRanges.getElementRange() != null) {
        myElement = convert(selectorWithRanges.getElementRange(), null, true);
        myElementName = myElement.getName();
      }
      else {
        myElement = null;
      }
      for (Pair<String, Integer> attr : selectorWithRanges.getAttributeRanges()) {
        myAttributes.add(convert(attr, myElementName != null ? myElementName : mainElementSelector, false));
      }
      for (Angular2DirectiveSimpleSelectorWithRanges notSelector : selectorWithRanges.getNotSelectors()) {
        myNotSelectors.add(new SimpleSelectorWithPsiImpl(notSelector, myElementName));
      }
    }

    @Override
    public @Nullable Angular2DirectiveSelectorSymbol getElement() {
      return myElement;
    }

    @Override
    public @NotNull List<@NotNull Angular2DirectiveSelectorSymbol> getAttributes() {
      return myAttributes;
    }

    @Override
    public @NotNull List<@NotNull SimpleSelectorWithPsi> getNotSelectors() {
      return myNotSelectors;
    }

    @Override
    public @Nullable Angular2DirectiveSelectorSymbol getElementAt(int offset) {
      return JBIterable.from(myAttributes)
        .append(JBIterable.from(myNotSelectors).flatMap(sel -> sel.getAttributes()))
        .append(myElement)
        .filter(element -> element.getTextRangeInSource().contains(offset))
        .first();
    }
  }
}
