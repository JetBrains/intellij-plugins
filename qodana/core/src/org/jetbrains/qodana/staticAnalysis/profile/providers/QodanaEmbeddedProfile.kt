package org.jetbrains.qodana.staticAnalysis.profile.providers

enum class QodanaEmbeddedProfile(val profileName: String, private val aliases: Set<String> = emptySet(), val isYaml: Boolean = false) {
  QODANA_RECOMMENDED_OLD("qodana.recommended.old", setOf("qodana.recommended.full")),
  QODANA_STARTER_OLD("qodana.starter.old", setOf("qodana.starter.full")),
  QODANA_STARTER("qodana.starter", isYaml = true),
  QODANA_RECOMMENDED("qodana.recommended", isYaml = true),
  QODANA_SANITY("qodana.sanity", isYaml = true);

  fun matchesName(profileName: String): Boolean {
    return profileName == this.profileName || profileName in aliases
  }

  override fun toString(): String = profileName
}
