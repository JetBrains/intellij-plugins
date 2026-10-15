package com.intellij.dts.pp.test.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IElementType
import javax.swing.Icon

object TestLanguage : Language("test")

class TestTokenType(debugName: String) : IElementType(debugName, TestLanguage)

class TestElementType(debugName: String) : IElementType(debugName, TestLanguage)

object TestFileType : LanguageFileType(TestLanguage) {
  override fun getName(): String = TestLanguage.id

  override fun getDescription(): String = "test host language"

  override fun getDefaultExtension(): String = "test"

  override fun getIcon(): Icon = AllIcons.FileTypes.Text
}

class TestFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, TestLanguage) {
  override fun getFileType(): FileType = TestFileType
}