package org.angular2.library.forms.quickFixes

import com.intellij.codeInsight.template.Template
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.ImportExportType
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.modules.JSImportCandidateDescriptor
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import org.angular2.lang.Angular2Bundle
import org.angular2.library.forms.Angular2FormGroup
import org.angular2.library.forms.FORM_BUILDER_GROUP_METHOD

class CreateFormGroupPropertyQuickFix(
  formGroup: Angular2FormGroup,
  private val nameToCreate: String,
  private val controlKind: String,
) : CreateJSVariableIntentionAction(nameToCreate, false, false, false) {

  init {
    myElementPointer = formGroup.initializer?.createSmartPointer()
    myIsProperty = true
  }

  private val formGroupName = formGroup.name

  init {
    assert(controlKind == "Control" || controlKind == "Group" || controlKind == "Array") {
      "controlKind must be either 'Control', 'Array' or 'Group', but was '$controlKind'"
    }
  }

  override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression?, PsiElement?> =
    Pair.create(null, findInsertionAnchorForScope(psiElement, true))

  override fun assertValidContext(psiElement: PsiElement, referenceExpression: JSReferenceExpression?) {
    assert(referenceExpression == null && psiElement is JSObjectLiteralExpression)
  }

  override fun produceDeclarationInScope(element: PsiElement?): Boolean = true

  override fun applyFix(project: Project?, psiElement: PsiElement, file: PsiFile, editor: Editor?) {
    val objectLiteral = psiElement as? JSObjectLiteralExpression ?: return
    withTypeEvaluationLocation(psiElement) {
      if (getFormBuilderReferenceTextForObjectLiteral(objectLiteral) == null) {
        // Ensure we have an import for the control constructor
        val targetModules = JSFileReferencesUtil.resolveModuleReference(objectLiteral.getContainingFile(), "@angular/forms")
        if (targetModules.size == 1) {
          ES6ImportPsiUtil.insertJSImport(
            objectLiteral.containingFile,
            JSImportCandidateDescriptor("@angular/forms", "Form$controlKind", null, ES6ImportExportDeclaration.ImportExportPrefixKind.IMPORT, ImportExportType.SPECIFIER),
            targetModules.first())
        }
      }
      doApplyFix(project, objectLiteral, objectLiteral.containingFile, null,
                 findInsertionAnchorForScope(objectLiteral, true),
                 objectLiteral)
    }
  }

  override fun buildTemplate(template: Template, referenceExpression: JSReferenceExpression?, isStaticContext: Boolean, anchorParent: PsiElement) {
    val formBuilderReference = getFormBuilderReferenceTextForObjectLiteral(anchorParent)
    val name = if (referenceExpression != null) referenceExpression.getReferenceName() else myReferencedName
    template.addTextSegment("$name: ")
    if (formBuilderReference != null)
      when (controlKind) {
        "Control" -> {
          template.addTextSegment("'")
          template.addEndVariable()
          template.addTextSegment("'")
        }
        "Array" -> {
          template.addTextSegment("${formBuilderReference}.array([")
          template.addEndVariable()
          template.addTextSegment("])")
        }
        "Group" -> {
          template.addTextSegment("${formBuilderReference}.group({\n")
          template.addEndVariable()
          template.addTextSegment("\n})")
        }
        else -> {
          throw IllegalStateException("Unexpected control kind: $controlKind")
        }
      }
    else
      when (controlKind) {
        "Control" -> {
          template.addTextSegment("new FormControl(")
          template.addEndVariable()
          template.addTextSegment(")")
        }
        "Array" -> {
          template.addTextSegment("new FormArray([")
          template.addEndVariable()
          template.addTextSegment("])")
        }
        "Group" -> {
          template.addTextSegment("new FormGroup({\n")
          template.addEndVariable()
          template.addTextSegment("\n})")
        }
        else -> {
          throw IllegalStateException("Unexpected control kind: $controlKind")
        }
      }
  }

  private fun getFormBuilderReferenceTextForObjectLiteral(literal: PsiElement?) =
    literal.asSafely<JSObjectLiteralExpression>()
      ?.parent?.asSafely<JSArgumentList>()
      ?.parent?.asSafely<JSCallExpression>()
      ?.takeIf { !it.isNewExpression }
      ?.methodExpression?.asSafely<JSReferenceExpression>()
      ?.takeIf { it.referenceName == FORM_BUILDER_GROUP_METHOD }
      ?.qualifier?.text?.replace("\n", "")

  override fun getName(): String =
    Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.name",
                           nameToCreate, StringUtil.decapitalize(controlKind), formGroupName)

  override fun getFamilyName(): @IntentionFamilyName String =
    Angular2Bundle.message("angular.quickfix.forms.create-form-ctrl-in-form-group.family")

}