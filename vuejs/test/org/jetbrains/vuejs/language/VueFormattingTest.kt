package org.jetbrains.vuejs.language

import com.intellij.psi.formatter.FormatterTestCase
import com.jetbrains.plugins.jade.JadeLanguage

class VueFormattingTest : FormatterTestCase() {
  override fun getFileExtension(): String {
    return "vue"
  }

  override fun getBasePath(): String {
    return ""
  }

  fun testPugUsesLanguageSpecificSettings() {
    val settings = getSettings(JadeLanguage.INSTANCE)
    settings.indentOptions?.USE_TAB_CHARACTER = true
    doTextTest("<template lang=\"pug\">\n" +
               "    div Hello, {{ name }}\n" +
               "</template>\n" +
               "<script>\n" +
               "    function func() {\n" +
               "        console.log('foobar')\n" +
               "    }\n" +
               "</script>\n", 
               "<template lang=\"pug\">\n" +
               "\tdiv Hello, {{ name }}\n" +
               "</template>\n" +
               "<script>\n" +
               "    function func() {\n" +
               "        console.log('foobar')\n" +
               "    }\n" +
               "</script>\n")
  }
}