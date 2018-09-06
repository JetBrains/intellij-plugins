// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion.vuetify.script

import java.io.File
import java.io.PrintWriter

//Used for generating VuetifyIcons.kt file
private object GenerateVuetifyIconsFile {
  fun generateFile() {
    val writer = PrintWriter("VuetifyIcons.kt", "UTF-8")
    writer.println("object VuetifyIcons {\n" +
                   "\n" +
                   "  val materialAndFontAwesome = listOf(\n" +
                   "    //  https://github.com/google/material-design-icons/blob/master/iconfont/codepoints")
    File("materialIcons.txt").inputStream().bufferedReader().use {
      it.readLines().forEach {
        writer.println("\"" + it.substringBefore(" ") + "\",")
      }
    }
    writer.println("//  https://github.com/FortAwesome/Font-Awesome/blob/master/svg-with-js/js/fontawesome-all.js")
    File("fontAwesome.txt").inputStream().bufferedReader().use {
      it.readLines().forEach {
        writer.println("\"fa-" + it.substringBefore(":").substringAfter(" \"") + ",")
      }
    }
    writer.println(("  )\n" +
                    "}"))
    writer.close()
  }
}