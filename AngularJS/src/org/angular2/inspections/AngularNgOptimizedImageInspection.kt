// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.htmltools.xml.util.HtmlReferenceProvider
import com.intellij.lang.html.HtmlCompatibleFile
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.IMG_TAG
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.NG_SRC_ATTR
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.SRC_ATTR
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.isNgSrcAttribute
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.inspections.actions.Angular2ActionFactory
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.inspections.quickfixes.CreateAttributeQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.PropertyBindingType

class AngularNgOptimizedImageInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return if (holder.file is HtmlCompatibleFile && Angular2LangUtil.isAngular2Context(holder.file)) {
      object : XmlElementVisitor() {
        override fun visitXmlAttribute(attribute: XmlAttribute) {
          val tag = attribute.parent
          if (tag != null
              && tag.localName.equals(IMG_TAG, true)
          ) {
            val directive = getNgSrcDirective(attribute)
            if (directive == null) return
            val nameElement = attribute.nameElement ?: return
            val info = Angular2AttributeNameParser.parse(attribute.localName, tag)
            if (info.isRegularOrBinding(SRC_ATTR)
                && attribute.value.let { it != null && !it.startsWith("data:") }) {
              holder.registerProblem(
                nameElement,
                Angular2Bundle.message("angular.inspection.ng-optimized-image.message.use-ngsrc"),
                ConvertToNgSrcAttributeFix()
              )
            }
            else if (isNgSrcAttribute(info)) {
              val attrsInfo = ngSrcAttrsInfo(tag)
              if (!attrsInfo.hasFill && !attrsInfo.hasHeight && !attrsInfo.hasWidth) {
                if (directive.inputs.any { it.name == FILL_ATTR }) {
                  holder.registerProblem(
                    nameElement,
                    Angular2Bundle.message("angular.inspection.ng-optimized-image.message.ngsrc.requires.width.height.or.fill.attributes"),
                    CreateWidthAndHeightAttributesQuickFix(false, false),
                    CreateAttributeQuickFix(FILL_ATTR)
                  )
                }
                else {
                  holder.registerProblem(
                    nameElement,
                    Angular2Bundle.message("angular.inspection.ng-optimized-image.message.ngsrc.requires.width.height.attributes"),
                    CreateWidthAndHeightAttributesQuickFix(false, false),
                  )
                }
              }
            }
            else if (info.isRegularOrBinding(WIDTH_ATTR)
                     || info.isRegularOrBinding(HEIGHT_ATTR)) {
              val attrsInfo = ngSrcAttrsInfo(tag)
              if (attrsInfo.hasNgSrc) {
                if (attrsInfo.hasFill) {
                  holder.registerProblem(
                    nameElement,
                    Angular2Bundle.message("angular.inspection.ng-optimized-image.message.both.fill.attributes.not.allowed",
                                           attribute.name),
                    RemoveAttributeIntentionFix(attribute.name)
                  )
                }
                else if (attrsInfo.hasHeight xor attrsInfo.hasWidth) {
                  holder.registerProblem(
                    nameElement,
                    Angular2Bundle.message("angular.inspection.ng-optimized-image.message.both.width.height.attributes.required"),
                    CreateWidthAndHeightAttributesQuickFix(attrsInfo.hasWidth, attrsInfo.hasHeight),
                  )
                }
              }
            }
            else if (info.isRegularOrBinding(FILL_ATTR)) {
              val attrsInfo = ngSrcAttrsInfo(tag)
              if ((attrsInfo.hasHeight || attrsInfo.hasWidth) && attrsInfo.hasNgSrc) {
                holder.registerProblem(
                  nameElement,
                  Angular2Bundle.message("angular.inspection.ng-optimized-image.message.both.fill.width.or.height.attributes.not.allowed"),
                  RemoveAttributeIntentionFix(attribute.name)
                )
              }
            }
          }
        }
      }
    }
    else PsiElementVisitor.EMPTY_VISITOR
  }

  private fun ngSrcAttrsInfo(tag: XmlTag) =
    CachedValuesManager.getCachedValue(tag) {
      val attributeInfos = tag.attributes.map { Angular2AttributeNameParser.parse(it.localName, tag) }
      val hasWidth = attributeInfos.any { it.isRegularOrBinding(WIDTH_ATTR) }
      val hasHeight = attributeInfos.any { it.isRegularOrBinding(HEIGHT_ATTR) }
      val hasFill = attributeInfos.any { it.isRegularOrBinding(FILL_ATTR) }
      val hasNgSrc = attributeInfos.any { isNgSrcAttribute(it) }
      CachedValueProvider.Result.create(NgSrcInfo(hasWidth, hasHeight, hasFill, hasNgSrc), tag)
    }

  companion object {

    const val WIDTH_ATTR = "width"

    const val FILL_ATTR = "fill"

    const val HEIGHT_ATTR = "height"

    internal fun getNgSrcDirective(context: PsiElement): Angular2Directive? =
      context.containingFile.let { file ->
        CachedValuesManager.getCachedValue(file) {
          CachedValueProvider.Result.create(
            Angular2EntitiesProvider.findAttributeDirectivesCandidates(file.project, NG_SRC_ATTR)
              .find { it.getName() == "NgOptimizedImage" },
            PsiModificationTracker.MODIFICATION_COUNT)
        }
      }

    private fun Angular2AttributeNameParser.AttributeInfo.isRegularOrBinding(name: String) =
      this.name.equals(name, true)
      && (this.asSafely<Angular2AttributeNameParser.PropertyBindingInfo>()?.bindingType == PropertyBindingType.PROPERTY
          || type == Angular2AttributeType.REGULAR)

    private fun fixWidthAndHeightAttributes(tag: XmlTag) {
      fun String.sizeToInt() =
        trim().removeSuffix("px").toIntOrNull()

      val curWidth = tag.getAttributeValue(WIDTH_ATTR)?.sizeToInt()
      val curHeight = tag.getAttributeValue(HEIGHT_ATTR)?.sizeToInt()
      if (curWidth == null || curHeight == null) {
        val info = HtmlReferenceProvider.SizeReference.getImageInfo(tag)
                     ?.takeIf { it.height > 0 && it.width > 0 }
                   ?: return
        if (curWidth == null && curHeight == null) {
          tag.setAttribute(HEIGHT_ATTR, info.height.toString())
          tag.setAttribute(WIDTH_ATTR, info.width.toString())
        }
        else if (curWidth != null) {
          val height = (curWidth * info.height) / info.width
          tag.setAttribute(HEIGHT_ATTR, height.toString())
        }
        else {
          val width = (curHeight!! * info.width) / info.height
          tag.setAttribute(WIDTH_ATTR, width.toString())
        }
      }
    }
  }

  private data class NgSrcInfo(
    val hasWidth: Boolean,
    val hasHeight: Boolean,
    val hasFill: Boolean,
    val hasNgSrc: Boolean,
  )

  private class ConvertToNgSrcAttributeFix : LocalQuickFix {
    override fun getFamilyName(): String =
      Angular2Bundle.message("angular.quickfix.template.covert-to-ng-src.family")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val attribute = descriptor.psiElement.parent as? XmlAttribute ?: return
      val newAttribute = attribute.setName(NG_SRC_ATTR) as? XmlAttribute ?: return
      val candidates = Angular2FixesFactory.getCandidatesForResolution(newAttribute, true)
      if (!candidates.containsKey(Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE)
          && candidates.get(Angular2DeclarationsScope.DeclarationProximity.IMPORTABLE).isNotEmpty()) {
        Angular2ActionFactory.createNgModuleImportAction(null, newAttribute, false).execute()
      }
      fixWidthAndHeightAttributes(newAttribute.parent)
    }

  }

  private class CreateWidthAndHeightAttributesQuickFix(val hasWidth: Boolean, val hasHeight: Boolean) : LocalQuickFix {
    override fun getName(): String =
      if (!hasHeight && !hasWidth)
        Angular2Bundle.message("angular.quickfix.template.create-height-width-attributes.name")
      else if (!hasHeight)
        Angular2Bundle.message("angular.quickfix.template.create-attribute.name", HEIGHT_ATTR)
      else
        Angular2Bundle.message("angular.quickfix.template.create-attribute.name", WIDTH_ATTR)

    override fun getFamilyName(): String =
      Angular2Bundle.message("angular.quickfix.template.create-attribute.family")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val attribute = descriptor.psiElement.parent as? XmlAttribute ?: return
      val tag = attribute.parent
      fixWidthAndHeightAttributes(tag)
    }

  }
}