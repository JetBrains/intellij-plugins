package org.jetbrains.qodana.inspectionKts.examples

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.NlsContexts
import java.net.URL
import javax.swing.Icon

class InspectionKtsExample(
  val icon: Icon,
  @NlsContexts.ListItem val text: String,
  val weight: Int,
  val resourceUrl: URL,
) {
  interface Provider {
    companion object {
      private val EP_NAME = ExtensionPointName<Provider>("org.intellij.qodana.inspectionKtsExampleProvider")

      fun examples(): List<InspectionKtsExample> {
        return EP_NAME.extensionList.mapNotNull { it.example() }.sortedBy { it.weight }
      }
    }

    fun example(): InspectionKtsExample?
  }
}