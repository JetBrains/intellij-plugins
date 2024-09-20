package org.angular2.intentions

import com.intellij.featureStatistics.ProductivityFeatureNames
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.names.JSNameSuggestionsUtil
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.refactoring.introduce.BaseIntroduceSettings
import com.intellij.lang.javascript.refactoring.introduce.BasicIntroducedEntityInfoProvider
import com.intellij.lang.javascript.refactoring.introduce.JSBaseInplaceIntroducer
import com.intellij.lang.javascript.refactoring.introduce.JSBaseIntroduceHandler
import com.intellij.lang.javascript.refactoring.introduceVariable.InplaceSettings
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableDialog
import com.intellij.lang.javascript.refactoring.introduceVariable.Settings
import com.intellij.lang.javascript.refactoring.introduceVariable.Settings.IntroducedVarType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser
import com.intellij.util.NullableConsumer
import com.intellij.util.takeWhileInclusive
import org.angular2.codeInsight.blocks.BLOCK_LET
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlBlock

class AngularIntroduceLetVariableHandler : JSBaseIntroduceHandler<PsiElement, BaseIntroduceSettings, JSIntroduceVariableDialog>() {

  override fun getRefactoringId(): String =
    "refactoring.angular.introduceLetVariable"

  @Suppress("DialogTitleCapitalization")
  override fun getRefactoringName(): String =
    Angular2Bundle.message("angular.intention.introduce.let.variable.title")

  override fun getCannotIntroduceMessagePropertyKey(): String =
    "javascript.introduce.variable.error.no.expression.selected"

  override fun getCannotIntroduceVoidExpressionTypeMessagePropertyKey(): String =
    "javascript.introduce.variable.error.expression.has.void.type"

  override fun getDeclText(baseIntroduceContext: BaseIntroduceContext<BaseIntroduceSettings>, anchor: PsiElement): String {
    return "@let " + baseIntroduceContext.settings.getVariableName()
  }

  override fun getProductivityFeatureId(): String {
    return ProductivityFeatureNames.REFACTORING_INTRODUCE_VARIABLE
  }

  override fun getInplaceSettings(
    expr: Pair<JSExpression, TextRange>,
    occurrences: Array<out JSExpression>,
    scope: PsiElement?,
    choice: OccurrencesChooser.ReplaceChoice?,
  ): InplaceSettings<BaseIntroduceSettings> {
    val entityInfoProvider = BasicIntroducedEntityInfoProvider(expr.first, occurrences, scope)
    val candidateNames = entityInfoProvider.suggestCandidateNames()
    val myIntroducedName = if (candidateNames.isNotEmpty())
      candidateNames[0].trim { it <= ' ' }
    else
      JSNameSuggestionsUtil.ensureUniqueVariableName("newVar", scope, HashSet(), false)

    return object : InplaceSettings<BaseIntroduceSettings> {
      override fun getSuggestedNames(): Array<String> {
        return candidateNames
      }

      override fun getSettings(): Settings {
        return object : Settings {
          override fun isReplaceAllOccurrences(): Boolean {
            return choice == OccurrencesChooser.ReplaceChoice.ALL
          }

          override fun getVariableName(): String {
            return myIntroducedName
          }

          override fun getVariableType(): String? {
            return null
          }

          override fun getIntroducedVarType(): IntroducedVarType {
            return IntroducedVarType.LET
          }
        }
      }
    }
  }

  override fun createDialog(project: Project?, expression: JSExpression?, occurrences: Array<out JSExpression>?, scope: PsiElement?): JSIntroduceVariableDialog {
    return JSIntroduceVariableDialog(project, occurrences, expression, scope, Settings.IntroducedVarType.LET)
  }

  override fun isInplaceIntroduce(editor: Editor, scope: PsiElement?, file: PsiFile?): Boolean {
    return editor.getSettings().isVariableInplaceRenameEnabled()
  }

  override fun createInplaceIntroducer(
    context: BaseIntroduceContext<BaseIntroduceSettings>, scope: PsiElement,
    editor: Editor, project: Project, occurrences: Array<out JSExpression>,
    callback: Runnable,
  ): JSBaseInplaceIntroducer<BaseIntroduceSettings> {
    val first = context.expressionDescriptor.first
    val filteredOccurrences = if (first.parent is JSExpressionStatement)
      occurrences.filter { it !== first }.toTypedArray()
    else
      occurrences
    return AngularLetVariableInplaceIntroducer(project, editor, filteredOccurrences,
                                               scope.containingFile.fileType, this, context, callback)
  }

  override fun findAnchor(context: BaseIntroduceContext<BaseIntroduceSettings>, replaceAllOccurrences: Boolean): PsiElement? {
    val firstExpression =
      (if (replaceAllOccurrences)
        context.occurrences.asSequence()
      else
        sequenceOf(context.expressionDescriptor.first)
      ).minOf { it.textRange.startOffset }

    return context.scope.children.asSequence()
      .dropWhile { it.elementType != XmlTokenType.XML_TAG_END }
      .drop(1)
      .filter { it !is XmlText || it.text.isNotBlank() }
      .dropWhile { it is Angular2HtmlBlock && it.name == BLOCK_LET && it.endOffset < firstExpression }
      .takeWhileInclusive { it.elementType != XmlTokenType.XML_END_TAG_START }
      .firstOrNull()
  }

  override fun findIntroducedScope(editor: Editor, expressionDescriptor: Pair<out JSExpression, out TextRange>, callback: NullableConsumer<in PsiElement>) {
    calculateScopes(expressionDescriptor.first).firstOrNull()?.let { callback.consume(it) }
  }

  override fun prepareDeclaration(
    varDeclText: String,
    context: BaseIntroduceContext<BaseIntroduceSettings>,
    project: Project,
    languageDialect: JSLanguageDialect?,
    anchorStatement: PsiElement,
    editor: Editor,
  ): Pair<JSVarStatement, Boolean> {
    val initializerReplacementExpression = getReplacementExpression(context.expressionDescriptor)

    val text = varDeclText + (if (initializerReplacementExpression != null) " = 0;" else ";")
    val letBlock = XmlElementFactory.getInstance(project)
      .createTagFromText("<div>$text</div>", anchorStatement.containingFile.language)
      .children
      .firstNotNullOf { it as? Angular2HtmlBlock }

    val declaration = letBlock.parameters[0].variables[0].parent as JSVarStatement

    replaceInitializer(initializerReplacementExpression, declaration)
    return Pair.create(declaration, false)
  }

  override fun insertVariableDeclaration(anchorStatement: PsiElement, declaration: JSVarStatement, expression: JSExpression): JSVariable {
    val block = declaration.parentOfType<Angular2HtmlBlock>()!!
    return (anchorStatement.parent.addBefore(block, anchorStatement) as Angular2HtmlBlock)
      .parameters[0].variables[0]
  }

  private fun calculateScopes(expression: JSExpression): List<XmlElement> {
    val owner = expression.parentOfTypes(XmlTag::class, XmlAttribute::class, XmlDocument::class, Angular2HtmlBlock::class)
                  ?.let { if (it is XmlAttribute) it.parent?.parent else it }
                ?: return emptyList()
    // find scopes
    return owner.parents(true)
      .filterIsInstance<XmlElement>()
      .filter {
        it is XmlDocument
        || (it is Angular2HtmlBlock && it.name != BLOCK_LET)
        || (it is XmlTag && (isTemplateTag(it) || hasStructuralDirective(it)))
      }
      .toList()
  }

  private fun hasStructuralDirective(tag: XmlTag): Boolean =
    tag.attributes.any {
      Angular2AttributeNameParser.parse(it.name, tag).type == Angular2AttributeType.TEMPLATE_BINDINGS
    }

}