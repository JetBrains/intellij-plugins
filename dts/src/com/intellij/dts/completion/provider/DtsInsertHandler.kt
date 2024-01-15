package com.intellij.dts.completion.provider

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.dts.completion.insert.writeNodeContent
import com.intellij.dts.completion.insert.writePropertyValue
import com.intellij.dts.lang.symbols.DtsPropertySymbol
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.util.asSafely
import dtsInsertIntoDocument

object DtsInsertHandler {
  private inline fun <reified T : Symbol> getSymbol(item: LookupElement): T? {
    return item.`object`.asSafely<Pointer<*>>()?.dereference().asSafely<T>()
  }

  val PROPERTY = InsertHandler<LookupElement> { context, item ->
    val symbol = getSymbol<DtsPropertySymbol>(item) ?: return@InsertHandler

    dtsInsertIntoDocument(context) {
      writePropertyValue(symbol)
    }
  }

  val SUB_NODE = InsertHandler<LookupElement> { context, _ ->
    dtsInsertIntoDocument(context) {
      writeNodeContent()
    }
  }

  val ROOT_NODE = InsertHandler<LookupElement> { context, _ ->
    dtsInsertIntoDocument(context) {
      setThreshold(2)
      writeNodeContent()
    }
  }
}