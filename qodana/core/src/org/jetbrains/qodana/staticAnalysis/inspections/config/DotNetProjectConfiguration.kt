package org.jetbrains.qodana.staticAnalysis.inspections.config

data class DotNetProjectConfiguration(val solution: String? = null,
                                      val project: String? = null,
                                      val configuration: String? = null,
                                      val platform: String? = null,
                                      val frameworks: String? = null)