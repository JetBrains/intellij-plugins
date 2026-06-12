package com.intellij.openRewrite.recipe

import com.intellij.psi.PsiAnchor
import com.intellij.psi.SmartTypePointer

internal class OpenRewriteOptionDescriptor(val name: String,
                                           val typePointer: SmartTypePointer,
                                           val displayName: String?,
                                           val description: String?,
                                           val example: String?,
                                           val valid: List<String>,
                                           val required: Boolean,
                                           val declaration: PsiAnchor)