// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlFileNSInfoProvider
import com.intellij.xml.util.XmlUtil
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.lang.AstroFileType

class AstroNSInfoProvider : XmlFileNSInfoProvider {
    override fun getDefaultNamespaces(file: XmlFile): Array<Array<String>>? {
        return if (file is AstroFileImpl) arrayOf(arrayOf("", XmlUtil.XHTML_URI)) else null
    }

    override fun overrideNamespaceFromDocType(file: XmlFile): Boolean {
        return file.fileType === AstroFileType.INSTANCE
    }
}