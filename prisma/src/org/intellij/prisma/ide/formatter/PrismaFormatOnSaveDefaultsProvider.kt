// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.formatter

import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptionsBase
import com.intellij.openapi.fileTypes.FileType
import org.intellij.prisma.lang.PrismaFileType

class PrismaFormatOnSaveDefaultsProvider : FormatOnSaveOptionsBase.DefaultsProvider {
  override fun getFileTypesFormattedOnSaveByDefault(): Collection<FileType> = listOf(PrismaFileType)
}