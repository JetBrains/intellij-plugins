package com.intellij.openRewrite.recipe

import com.intellij.psi.PsiAnchor

internal class OpenRewriteRecipeDescriptor(val name: String,
                                           val displayName: String?,
                                           val description: String?,
                                           val isComposite: Boolean,
                                           val options: List<OpenRewriteOptionDescriptor>,
                                           val declaration: PsiAnchor)