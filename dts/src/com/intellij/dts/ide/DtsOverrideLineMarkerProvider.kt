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
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.asSafely
import javax.swing.Icon

class DtsOverrideLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun getName(): String = DtsBundle.message("line_marker.property_override.name")

    override fun getIcon(): Icon = DtsIcons.OverrideProperty

    override fun getOptions(): Array<Option> = arrayOf(Option(id, name, icon))

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
        if (element.elementType != DtsTypes.NAME) return
        val property = element.parent?.asSafely<DtsProperty>() ?: return

        val node = DtsTreeUtil.parentNode(element) ?: return

        val overwrittenProperty = findOverwrittenProperty(node, property.dtsName) ?: return

        val marker = NavigationGutterIconBuilder.create(icon)
            .setTooltipText(DtsBundle.message("line_marker.property_override.navigate"))
            .setTargets(overwrittenProperty)
            .createLineMarkerInfo(element)

        result.add(marker)
    }
}