/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson


import org.jdom.Element
import training.util.DataLoader

class Scenario(var path: String) {

  val root: Element = DataLoader.getXmlRootElement(path)
  private val LANG = "lang"
  private val NAME = "name"
  private val ID = "id"

  val lang: String
    get() {
      val lang = root.getAttribute(LANG)
      if (lang != null) return lang.value else throw Exception("Cannot get '$LANG' property for the lesson file with path: $path")
    }

  val name: String
    get() = root.getAttribute(NAME).value

  /**
   * It is a unique String attribute to distinguish different lessons with a probably similar names
   */
  val id: String
    get() = root.getAttribute(ID).value

}
