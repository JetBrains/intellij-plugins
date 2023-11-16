package com.intellij.dts.ide

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.dts.DtsBundle
import com.intellij.dts.DtsIcons
import com.intellij.dts.api.DtsNodeVisitor
import com.intellij.dts.api.DtsVisitorCanceledException
import com.intellij.dts.api.dtsAccept
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.psi.PsiElement

class DtsOverrideLineMarkerProvider : RelatedItemLineMarkerProvider() {
    private fun findOverwrittenProperty(node: DtsNode, name: String): DtsProperty? {
        var result: DtsProperty? = null

        val visitor = object : DtsNodeVisitor {
            override fun visitProperty(property: DtsProperty) {
                if (property.dtsName != name) return

                result = property
                throw DtsVisitorCanceledException()
            }
        }
        node.dtsAccept(visitor, forward = false, strict = true)

        return result
    }

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is DtsProperty) return
        val node = DtsTreeUtil.parentNode(element) ?: return

        val overwrittenProperty = findOverwrittenProperty(node, element.dtsName) ?: return

        val marker = NavigationGutterIconBuilder.create(DtsIcons.OverrwriteProperty)
            .setTooltipText(DtsBundle.message("line_marker.property_override.navigate"))
            .setTargets(overwrittenProperty)
            .createLineMarkerInfo(element.dtsNameElement)

        result.add(marker)
    }
}