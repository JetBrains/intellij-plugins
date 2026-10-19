package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement

/**
 * Gives the semantic identity of an XML problem for the baseline fingerprints.
 *
 * The identity replaces the problem location in `equalIndicator/v2`.
 * A problem keeps its fingerprint when its XML file moves or when the tags around it change order.
 */
interface QodanaXmlSemanticIdentityProvider {
  /** Returns the identity of the XML node that holds [element], or null when [element] is not in XML. */
  fun extractIdentity(element: PsiElement): String?

  companion object {
    val EP_NAME: ExtensionPointName<QodanaXmlSemanticIdentityProvider> =
      ExtensionPointName("org.intellij.qodana.xmlSemanticIdentityProvider")
  }
}
