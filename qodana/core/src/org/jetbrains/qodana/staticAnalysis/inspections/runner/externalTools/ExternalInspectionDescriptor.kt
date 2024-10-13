package org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools

import com.intellij.codeHighlighting.HighlightDisplayLevel
import org.jetbrains.annotations.Nls

data class ExternalInspectionDescriptor(val shortName: String,
                                        @Nls val displayName: String,
                                        @Nls val groupPath: String,
                                        @Nls val groupDisplayName: String,
                                        @Nls val description: String,
                                        val isEnabledByDefault: Boolean,
                                        val defaultLevel: HighlightDisplayLevel)