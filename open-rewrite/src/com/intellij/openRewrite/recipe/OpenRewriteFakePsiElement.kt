package com.intellij.openRewrite.recipe

import com.intellij.lang.Language
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.psi.impl.FakePsiElement
import org.jetbrains.yaml.YAMLLanguage
import javax.swing.Icon

internal abstract class OpenRewriteFakePsiElement : FakePsiElement() {
  override fun getIcon(open: Boolean): Icon = OpenRewriteIcons.OpenRewrite

  override fun getLanguage(): Language = YAMLLanguage.INSTANCE
}