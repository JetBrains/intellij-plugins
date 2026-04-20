package org.intellij.plugin.mdx

import com.intellij.openapi.application.PathManager

object MdxTestUtil {
  @JvmStatic
  val testDataRelativePath = "/contrib/mdx/testData"

  @JvmStatic
  val testDataPath = PathManager.getHomeDir().toString() + testDataRelativePath
}
