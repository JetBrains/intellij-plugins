package com.intellij.aws.cloudformation.tests

import com.intellij.openapi.application.impl.ApplicationInfoImpl
import com.intellij.util.PlatformUtils

object PrintIdeaVersionToTeamCity {
  @JvmStatic fun main(args: Array<String>) {
    System.setProperty(PlatformUtils.PLATFORM_PREFIX_KEY, PlatformUtils.IDEA_CE_PREFIX)

    val instance = ApplicationInfoImpl.getShadowInstance()
    println("##teamcity[buildStatus text='${instance.build.asString()} (${instance.fullVersion}) - {build.status.text}']")
  }
}
