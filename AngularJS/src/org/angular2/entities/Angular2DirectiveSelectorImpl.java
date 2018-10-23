package org.angular2.entities;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector.Angular2DirectiveSimpleSelectorWithRanges;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Angular2DirectiveSelectorImpl implements Angular2DirectiveSelector {

  private final PsiElement mySelectorElement;
  private final String myText;
  private final Function<Pair<String, Integer>, TextRange> myCreateRange;
  private List<Angular2DirectiveSimpleSelector> mySimpleSelectors;
  private List<SimpleSelectorWithPsi> mySimpleSelectorsWithPsi;

  public Angular2DirectiveSelectorImpl(PsiElement element, String text, Function<Pair<String, Integer>, TextRange> createRange) {
    mySelectorElement = element;
    myText = text;
    myCreateRange = createRange;
  }

  @NotNull
  @Override
  public String getText() {
    return myText;
  }

  @NotNull
  @Override
  public synchronized List<Angular2DirectiveSimpleSelector> getSimpleSelectors() {
    if (mySimpleSelectors == null) {
      try {
        mySimpleSelectors = Collections.unmodifiableList(Angular2DirectiveSimpleSelector.parse(myText));
      }
      catch (ParseException e) {
        mySimpleSelectors = Collections.emptyList();
      }
    }
    return mySimpleSelectors;
  }

  @NotNull
  @Override
  public synchronized List<SimpleSelectorWithPsi> getSimpleSelectorsWithPsi() {
    if (mySimpleSelectorsWithPsi == null) {
      try {
        List<Angular2DirectiveSimpleSelectorWithRanges> simpleSelectorsWithRanges = Angular2DirectiveSimpleSelector.parseRanges(myText);
        mySimpleSelectorsWithPsi = new ArrayList<>(simpleSelectorsWithRanges.size());
        for (Angular2DirectiveSimpleSelectorWithRanges sel : simpleSelectorsWithRanges) {
          mySimpleSelectorsWithPsi.add(new SimpleSelectorWithPsiImpl(sel));
        }
      }
      catch (ParseException e) {
        mySimpleSelectorsWithPsi = Collections.emptyList();
      }
    }
    return mySimpleSelectorsWithPsi;
  }

  @NotNull
  @Override
  public Angular2DirectiveSelectorPsiElement getPsiElementForElement(@NotNull String elementName) {
    for (SimpleSelectorWithPsi selector : getSimpleSelectorsWithPsi()) {
      if (selector.getElement() != null && elementName.equalsIgnoreCase(selector.getElement().getName())) {
        return selector.getElement();
      }
    }
    throw new IllegalArgumentException("Element " + elementName + " is not present in the selector: " + getText());
  }

  @Override
  public String toString() {
    return getText();
  }

  @NotNull
  protected Angular2DirectiveSelectorPsiElement convert(@NotNull Pair<String, Integer> range, boolean isElement) {
    return new Angular2DirectiveSelectorPsiElement(mySelectorElement, myCreateRange.apply(range), range.first, isElement);
  }

  private class SimpleSelectorWithPsiImpl implements SimpleSelectorWithPsi {

    private final Angular2DirectiveSelectorPsiElement myElement;
    private final List<Angular2DirectiveSelectorPsiElement> myAttributes = new SmartList<>();
    private final List<SimpleSelectorWithPsi> myNotSelectors = new SmartList<>();

    SimpleSelectorWithPsiImpl(Angular2DirectiveSimpleSelectorWithRanges selectorWithRanges) {
      if (selectorWithRanges.getElementRange() != null) {
        myElement = convert(selectorWithRanges.getElementRange(), true);
      }
      else {
        myElement = null;
      }
      for (Pair<String, Integer> attr : selectorWithRanges.getAttributeRanges()) {
        myAttributes.add(convert(attr, false));
      }
      for (Angular2DirectiveSimpleSelectorWithRanges notSelector : selectorWithRanges.getNotSelectors()) {
        myNotSelectors.add(new SimpleSelectorWithPsiImpl(notSelector));
      }
    }

    @Override
    public Angular2DirectiveSelectorPsiElement getElement() {
      return myElement;
    }

    @Override
    public List<Angular2DirectiveSelectorPsiElement> getAttributes() {
      return myAttributes;
    }

    @Override
    public List<SimpleSelectorWithPsi> getNotSelectors() {
      return myNotSelectors;
    }
  }
}
