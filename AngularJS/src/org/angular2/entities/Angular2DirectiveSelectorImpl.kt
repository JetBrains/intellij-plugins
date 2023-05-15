// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.model.Pointer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.SmartList
import com.intellij.util.concurrency.SynchronizedClearableLazy
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector.Angular2DirectiveSimpleSelectorWithRanges
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector.ParseException
import java.util.*

class Angular2DirectiveSelectorImpl(private val myElement: PsiElement,
                                    private val myText: String?,
                                    private val myRangeOffset: Int?) : Angular2DirectiveSelector {
  private val myLazyParent: SynchronizedClearableLazy<PsiElement>? = if (myElement is Angular2MetadataDirectiveBase<*>)
    SynchronizedClearableLazy { myElement.typeScriptClass ?: myElement }
  else
    null
  private val mySimpleSelectors: NotNullLazyValue<List<Angular2DirectiveSimpleSelector>> = NotNullLazyValue.lazy {
    if (myText == null) {
      emptyList()
    }
    else try {
      Collections.unmodifiableList(Angular2DirectiveSimpleSelector.parse(myText))
    }
    catch (e: ParseException) {
      emptyList()
    }
  }
  private val mySimpleSelectorsWithPsi: NotNullLazyValue<List<SimpleSelectorWithPsi>> = NotNullLazyValue.lazy {
    if (myText == null) {
      emptyList()
    }
    else try {
      Angular2DirectiveSimpleSelector.parseRanges(myText)
        .map { SimpleSelectorWithPsiImpl(it, null) }
    }
    catch (e: ParseException) {
      emptyList()
    }
  }

  override val text: String
    get() = myText ?: "<null>"

  val psiParent: PsiElement
    get() = if (myElement is Angular2MetadataDirectiveBase<*>) myLazyParent!!.value else myElement

  override val simpleSelectors: List<Angular2DirectiveSimpleSelector>
    get() = mySimpleSelectors.value

  override val simpleSelectorsWithPsi: List<SimpleSelectorWithPsi>
    get() = mySimpleSelectorsWithPsi.value

  fun createPointer(): Pointer<Angular2DirectiveSelectorImpl> {
    val element = myElement.createSmartPointer()
    val text = myText
    val rangeOffset = myRangeOffset
    return Pointer {
      element.element?.let { Angular2DirectiveSelectorImpl(it, text, rangeOffset) }
    }
  }

  override fun getSymbolForElement(elementName: String): Angular2DirectiveSelectorSymbol {
    for (selector in simpleSelectorsWithPsi) {
      val selectorElement = selector.element
      if (selectorElement != null && elementName.equals(selectorElement.name, ignoreCase = true)) {
        return selectorElement
      }
      for (notSelector in selector.notSelectors) {
        val notSelectorElement = notSelector.element
        if (notSelectorElement != null && elementName.equals(notSelectorElement.name, ignoreCase = true)) {
          return notSelectorElement
        }
      }
    }
    return Angular2DirectiveSelectorSymbol(this, TextRange(0, 0), elementName, null, true)
  }

  override fun toString(): String {
    return text
  }

  private fun convert(range: Pair<String, Int>,
                      elementSelector: String?,
                      isElement: Boolean): Angular2DirectiveSelectorSymbol {
    return Angular2DirectiveSelectorSymbol(
      this,
      if (myRangeOffset != null)
        TextRange(range.second + myRangeOffset, range.second + range.first.length + myRangeOffset)
      else
        TextRange.EMPTY_RANGE,
      range.first, elementSelector, isElement)
  }

  /*fun replaceText(range: TextRange, name: String) {
    myElement = ElementManipulators.getManipulator(myElement!!)!!
      .handleContentChange(myElement!!, range, name)
  }*/

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val selector = other as Angular2DirectiveSelectorImpl?
    return myElement == selector!!.myElement &&
           myText == selector.myText &&
           myRangeOffset == selector.myRangeOffset
  }

  override fun hashCode(): Int {
    return Objects.hash(myElement, myText, myRangeOffset)
  }

  private inner class SimpleSelectorWithPsiImpl(selectorWithRanges: Angular2DirectiveSimpleSelectorWithRanges,
                                                mainElementSelector: String?) : SimpleSelectorWithPsi {

    override val element: Angular2DirectiveSelectorSymbol?
    private val myAttributes = SmartList<Angular2DirectiveSelectorSymbol>()
    private val myNotSelectors = SmartList<SimpleSelectorWithPsi>()

    override val attributes: List<Angular2DirectiveSelectorSymbol>
      get() = myAttributes

    override val notSelectors: List<SimpleSelectorWithPsi>
      get() = myNotSelectors

    init {
      var myElementName: String? = null
      if (selectorWithRanges.elementRange != null) {
        element = convert(selectorWithRanges.elementRange!!, null, true)
        myElementName = element.name
      }
      else {
        element = null
      }
      for (attr in selectorWithRanges.attributeRanges) {
        myAttributes.add(convert(attr, myElementName ?: mainElementSelector, false))
      }
      for (notSelector in selectorWithRanges.notSelectors) {
        myNotSelectors.add(SimpleSelectorWithPsiImpl(notSelector, myElementName))
      }
    }

    override fun getElementAt(offset: Int): Angular2DirectiveSelectorSymbol? {
      return myAttributes
        .plus(myNotSelectors.flatMap { it.attributes })
        .plus(element)
        .firstOrNull { it?.textRangeInSource?.contains(offset) == true }
    }
  }
}
