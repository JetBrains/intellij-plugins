package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.vuejs.VueFileType

fun fromAsset(text: String): String {
  val split = StringUtil.unquoteString(text).split("(?=[A-Z])".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
  for (i in split.indices) {
    split[i] = StringUtil.decapitalize(split[i])
  }
  return StringUtil.join(split, "-")
}

fun toAsset(name: String): String {
  val words = name.split("-".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
  for (i in 1..words.size - 1) {
    words[i] = StringUtil.capitalize(words[i])
  }
  return StringUtil.join(*words)
}


fun isComponent(property: JSProperty): Boolean {
  return property.parent is JSObjectLiteralExpression && property.parent.parent is JSExportAssignment &&
         property.containingFile.fileType == VueFileType.INSTANCE
}


