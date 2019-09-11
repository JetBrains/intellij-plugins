package com.intellij.grazie.language.broker

import com.intellij.grazie.GrazieDynamic
import org.languagetool.tools.classbroker.ClassBroker

object GrazieDynamicClassBroker : ClassBroker {
  override fun forName(qualifiedName: String): Class<*>? {
    return GrazieDynamic.loadClass(qualifiedName)
  }
}
