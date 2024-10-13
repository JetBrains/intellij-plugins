package org.jetbrains.qodana.staticAnalysis

import com.intellij.openapi.util.JDOMUtil
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager

fun newProfileWithInspections(vararg inspectionNames: String): QodanaInspectionProfile {
  @Language("XML")
  val xml = buildString {
    appendLine("""<profile version="1.0" is_locked="true">""")
    appendLine("""  <option name="myName" value="test-profile" is_locked="true"/>""")
    for (name in inspectionNames) {
      appendLine("""  <inspection_tool class="$name" enabled="true" level="WARNING" enabled_by_default="true" />""")
    }
    appendLine("</profile>")
  }

  return QodanaInspectionProfile.newFromXml(JDOMUtil.load(xml), "test-profile", QodanaInspectionProfileManager.getInstance())
}

inline fun <T> withSystemProperty(key: String, value: String?, action: () -> T): T {
  val prev = System.getProperty(key)
  if (value != null) System.setProperty(key, value) else System.clearProperty(key)
  try {
    return action()
  }
  finally {
    if (prev != null) System.setProperty(key, prev) else System.clearProperty(key)
  }
}