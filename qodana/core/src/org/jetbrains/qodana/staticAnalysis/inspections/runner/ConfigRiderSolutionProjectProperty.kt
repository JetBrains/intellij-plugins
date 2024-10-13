package org.jetbrains.qodana.staticAnalysis.inspections.runner

const val QODANA_NET_SOLUTION = "qodana.net.solution"
const val QODANA_NET_PROJECT = "qodana.net.project"
const val QODANA_NET_CONFIGURATION = "qodana.net.configuration"
const val QODANA_NET_PLATFORM = "qodana.net.platform"
const val QODANA_NET_FRAMEWORKS = "qodana.net.targetFrameworks"
const val QODANA_NET_OPEN_TIMEOUT = "qodana.net.project.open.timeout"

fun getDotnetSolutionName(): String? = System.getProperties().getProperty(QODANA_NET_SOLUTION)?.trim('"')
fun getDotnetProjectName(): String? = System.getProperties().getProperty(QODANA_NET_PROJECT)?.trim('"')
fun getDotnetConfiguration(): String? = System.getProperties().getProperty(QODANA_NET_CONFIGURATION)?.trim('"')
fun getDotnetPlatform(): String? = System.getProperties().getProperty(QODANA_NET_PLATFORM)?.trim('"')
fun getDotnetFrameworks(): String? = System.getProperties().getProperty(QODANA_NET_FRAMEWORKS)?.trim('"')
fun getDotnetProjectOpenTimeout(): String? = System.getProperties().getProperty(QODANA_NET_OPEN_TIMEOUT)