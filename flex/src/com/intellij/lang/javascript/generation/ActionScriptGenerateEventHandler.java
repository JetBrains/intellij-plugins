// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.PublicInheritorFilter;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMembersFix;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

public class ActionScriptGenerateEventHandler extends BaseJSGenerateHandler {

  @Override
  protected @NlsContexts.DialogTitle String getTitle() {
    return null; // not used in this action
  }

  @Override
  protected BaseCreateMembersFix createFix(final PsiElement jsClass) {
    return new GenerateEventHandlerFix((JSClass)jsClass);
  }

  @Override
  protected boolean collectCandidatesAndShowDialog() {
    return false;
  }

  @Override
  protected boolean canHaveEmptySelectedElements() {
    return true;
  }


  @Nullable
  public static XmlAttribute getXmlAttribute(final PsiFile psiFile, final Editor editor) {
    PsiElement context = null;
    if (psiFile instanceof JSFile) {
      context = InjectedLanguageManager.getInstance(psiFile.getProject()).getInjectionHost(psiFile);
    }
    else if (psiFile instanceof XmlFile) {
      context = psiFile.findElementAt(editor.getCaretModel().getOffset());
    }

    return PsiTreeUtil.getParentOfType(context, XmlAttribute.class);
  }

  @Nullable
  public static String getEventType(final XmlAttribute xmlAttribute) {
    final XmlAttributeDescriptor descriptor = xmlAttribute == null ? null : xmlAttribute.getDescriptor();
    final PsiElement declaration = descriptor instanceof AnnotationBackedDescriptor ? descriptor.getDeclaration() : null;
    final PsiElement declarationParent = declaration == null ? null : declaration.getParent();

    if (declaration instanceof JSAttributeNameValuePair &&
        (((JSAttributeNameValuePair)declaration).getName() == null ||
         "name".equals(((JSAttributeNameValuePair)declaration).getName())) &&
        declarationParent instanceof JSAttribute &&
        "Event".equals(((JSAttribute)declarationParent).getName())) {
      return ((AnnotationBackedDescriptor)descriptor).getType();
    }

    return null;
  }

  @Nullable
  public static JSCallExpression getEventListenerCallExpression(final PsiFile psiFile, final Editor editor) {
    if (!(psiFile instanceof JSFile)) {
      return null;
    }

    final PsiElement elementAtCursor = psiFile.findElementAt(editor.getCaretModel().getOffset());
    final JSCallExpression callExpression = PsiTreeUtil.getParentOfType(elementAtCursor, JSCallExpression.class);

    if (callExpression == null || !JSResolveUtil.isEventListenerCall(callExpression)) {
      return null;
    }

    final JSExpression[] params = callExpression.getArguments();

    if (params.length > 0 &&
        ((params[0] instanceof JSReferenceExpression && ((JSReferenceExpression)params[0]).getQualifier() != null) ||
         (params[0] instanceof JSLiteralExpression && ((JSLiteralExpression)params[0]).isQuotedLiteral()))) {
      if (params.length == 1 || isUnresolvedReference(params[1])) {
        return callExpression;
      }
    }

    return null;
  }

  private static boolean isUnresolvedReference(final JSExpression parameter) {
    if (parameter instanceof JSReferenceExpression) {
      final PsiElement referenceNameElement = ((JSReferenceExpression)parameter).getReferenceNameElement();
      final ASTNode nameNode = referenceNameElement == null ? null : referenceNameElement.getNode();
      if (nameNode != null &&
          nameNode.getElementType() == JSTokenTypes.IDENTIFIER &&
          ((JSReferenceExpression)parameter).resolve() == null) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param expr JSExpressionStatement (if it looks like ButtonEvent.CLICK),
   * @param eventClass event class FQN (like "flash.events.MouseEvent")
   * @param eventName is event name (like "click")
   */
  public record EventConstantInfo(@NotNull JSExpressionStatement expr, String eventClass, @NotNull String eventName) {}

  @Nullable
  public static EventConstantInfo getEventConstantInfo(final PsiFile psiFile, final Editor editor) {
    if (!(psiFile instanceof JSFile)) {
      return null;
    }

    final PsiElement jsClass = BaseJSGenerateHandler.findClassOrObjectLiteral(psiFile, editor, null);
    if (!(jsClass instanceof JSClass) || !ActionScriptEventDispatchUtils.isEventDispatcher((JSClass)jsClass)) {
      return null;
    }

    final PsiElement elementAtCursor = psiFile.findElementAt(editor.getCaretModel().getOffset());
    final JSExpressionStatement expressionStatement = PsiTreeUtil.getParentOfType(elementAtCursor, JSExpressionStatement.class);
    final PsiElement expressionStatementParent = expressionStatement == null ? null : expressionStatement.getParent();
    final JSFunction jsFunction = PsiTreeUtil.getParentOfType(expressionStatement, JSFunction.class);

    final JSExpression expression = expressionStatement == null ? null : expressionStatement.getExpression();
    final JSReferenceExpression refExpression = expression instanceof JSReferenceExpression ? (JSReferenceExpression)expression : null;
    final JSExpression qualifier = refExpression == null ? null : refExpression.getQualifier();
    final PsiReference qualifierReference = qualifier == null ? null : qualifier.getReference();
    final PsiElement referenceNameElement = refExpression == null ? null : refExpression.getReferenceNameElement();

    JSAttributeList functionAttributes;
    if (jsFunction == null ||
        ((functionAttributes = jsFunction.getAttributeList()) != null &&
         functionAttributes.hasModifier(JSAttributeList.ModifierType.STATIC)) ||
        qualifierReference == null ||
        !(referenceNameElement instanceof LeafPsiElement) ||
        (!(expressionStatementParent instanceof JSFunction) && !(expressionStatementParent instanceof JSBlockStatement))
      ) {
      return null;
    }

    final PsiElement qualifierResolve = qualifierReference.resolve();
    if (!(qualifierResolve instanceof JSClass) || !isEventClass((JSClass)qualifierResolve)) {
      return null;
    }

    final PsiElement expressionResolve = refExpression.resolve();
    if (expressionResolve instanceof JSVariable variable) {
      final JSAttributeList varAttributes = variable.getAttributeList();
      final String text = variable.getLiteralOrReferenceInitializerText();
      if (varAttributes != null &&
          varAttributes.hasModifier(JSAttributeList.ModifierType.STATIC) &&
          varAttributes.getAccessType() == JSAttributeList.AccessType.PUBLIC &&
          text != null && StringUtil.isQuotedString(text)) {
        return new EventConstantInfo(expressionStatement,
                                     ((JSClass)qualifierResolve).getQualifiedName(),
                                     initializerToPartialMethodName(text));
      }
    }

    return null;
  }

  public static boolean isEventClass(final JSClass jsClass) {
    final PsiElement eventClass = ActionScriptClassResolver.findClassByQNameStatic(FlexCommonTypeNames.FLASH_EVENT_FQN, jsClass);
    if ((eventClass instanceof JSClass) && JSInheritanceUtil.isParentClass(jsClass, (JSClass)eventClass)) {
      return true;
    }

    final PsiElement eventClass2 =
      ActionScriptClassResolver.findClassByQNameStatic(FlexCommonTypeNames.STARLING_EVENT_FQN, jsClass);
    if ((eventClass2 instanceof JSClass) && JSInheritanceUtil.isParentClass(jsClass, (JSClass)eventClass2)) {
      return true;
    }

    return false;
  }

  private static String initializerToPartialMethodName(final String initializerText) {
    final String unquoted = StringUtil.stripQuotesAroundValue(initializerText);
    final int dotIndex = unquoted.lastIndexOf('.');
    return unquoted.substring(dotIndex + 1).replaceAll("[^\\p{Alnum}]", "_");
  }

  public static class GenerateEventHandlerFix extends BaseCreateMembersFix {
    private boolean inMxmlEventAttributeValue;
    private boolean inEventListenerCall;
    private PsiElement handlerCallerAnchorInArgumentList;
    private JSReferenceExpression myExistingUnresolvedReverence;
    private boolean inEventConstantExpression;
    private JSExpressionStatement eventConstantExpression;
    private String eventHandlerName;
    private String eventHandlerName2;
    private String methodBody;
    private String eventClassFqn;
    private boolean userCancelled;

    private static final String METHOD_NAME_PATTERN = "{0}_{1}Handler";
    private final JSClass myJsClass;

    public GenerateEventHandlerFix(final JSClass jsClass) {
      super(jsClass);
      myJsClass = jsClass;
      inMxmlEventAttributeValue = false;
      inEventListenerCall = false;
      handlerCallerAnchorInArgumentList = null;
      eventHandlerName = "eventHandler";
      eventHandlerName2 = "onEvent";
      methodBody = "";
      eventClassFqn = FlexCommonTypeNames.FLASH_EVENT_FQN;
      userCancelled = false;
    }

    // called outside of write action - required for class chooser
    @Override
    public void beforeInvoke(@NotNull final Project project, final Editor editor, final PsiFile psiFile) {
      // keep consistency with CreateEventHandlerIntention.isAvailable()

      final XmlAttribute xmlAttribute = getXmlAttribute(psiFile, editor);
      final String eventType = xmlAttribute == null ? null : getEventType(xmlAttribute);
      if (eventType != null) {
        inMxmlEventAttributeValue = true;
        prepareForMxmlEventAttributeValue(xmlAttribute, eventType);
        return;
      }

      final JSCallExpression callExpression = getEventListenerCallExpression(psiFile, editor);
      if (callExpression != null) {
        inEventListenerCall = true;
        prepareForEventListenerCall(callExpression);
        return;
      }

      final EventConstantInfo eventConstantInfo = getEventConstantInfo(psiFile, editor);
      if (eventConstantInfo != null) {
        inEventConstantExpression = true;
        eventConstantExpression = eventConstantInfo.expr();
        eventClassFqn = eventConstantInfo.eventClass();
        final String eventName = eventConstantInfo.eventName();
        eventHandlerName = eventName + "Handler";
        eventHandlerName2 = "on" + (eventName.isEmpty() ? "Event" : Character.toUpperCase(eventName.charAt(0)) + eventName.substring(1));
        return;
      }

      // no suitable context -> ask for event class and create handler without usage
      final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
      if (module != null && !ApplicationManager.getApplication().isUnitTestMode()) {
        final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        final JSClassChooserDialog dialog =
          new JSClassChooserDialog(module.getProject(), FlexBundle.message("choose.event.class.title"), scope, getEventBaseClass(),
                                   new PublicInheritorFilter(module.getProject(),
                                                                            FlexCommonTypeNames.FLASH_EVENT_FQN,
                                                                            scope,
                                                                            false));
        if (dialog.showDialog()) {
          final JSClass selectedClass = dialog.getSelectedClass();
          if (selectedClass != null) {
            eventClassFqn = selectedClass.getQualifiedName();
          }
        }
        else {
          userCancelled = true;
        }
      }
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      if (userCancelled) return;
      final PsiElement referenceElement = insertEventHandlerReference(editor, file);
      evalAnchor(editor, file);
      final String eventClassShortName = StringUtil.getShortName(eventClassFqn);
      final String functionText =
        "private function " + eventHandlerName + "(event:" + eventClassShortName + "):void{" + methodBody + "\n}\n";

      JSFunction addedElement = (JSFunction)doAddOneMethod(project, functionText, anchor);
      addedElement = (JSFunction)ImportUtils.importAndShortenReference(eventClassFqn, addedElement, true, false).second;

      final PsiElement templateBaseElement = referenceElement == null ? addedElement : myJsClass;
      final TemplateBuilderImpl templateBuilder = new TemplateBuilderImpl(templateBaseElement);

      final PsiElement lastElement = PsiTreeUtil.getDeepestLast(addedElement);
      final PsiElement prevElement = lastElement.getPrevSibling();
      templateBuilder.setEndVariableBefore((prevElement != null ? prevElement : lastElement));

      templateBuilder
        .replaceElement(addedElement.getAttributeList().findAccessTypeElement(), createExpression("private", "protected", "public"));
      templateBuilder
        .replaceElement(addedElement.findNameIdentifier().getPsi(), "handlerName", createExpression(eventHandlerName, eventHandlerName2),
                        true);
      templateBuilder
        .replaceElement(addedElement.getParameterVariables()[0].findNameIdentifier().getPsi(), createExpression("event", "e"));

      if (referenceElement != null && referenceElement.isValid()) {
        templateBuilder.replaceElement(referenceElement, "handlerReference", "handlerName", false);
      }

      final Editor topEditor = InjectedLanguageUtil.getTopLevelEditor(editor);
      PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(topEditor.getDocument());
      final int startOffset = templateBaseElement.getTextRange().getStartOffset();

      final Template template = templateBuilder.buildInlineTemplate();

      topEditor.getCaretModel().moveToOffset(InjectedLanguageManager.getInstance(project).injectedToHost(templateBaseElement, startOffset));

      TemplateManager.getInstance(project).startTemplate(topEditor, template);
    }

    @Nullable
    private PsiElement insertEventHandlerReference(final Editor editor, final PsiFile psiFile) {
      if (inMxmlEventAttributeValue) {
        final XmlAttribute xmlAttribute = getXmlAttribute(psiFile, editor);
        if (xmlAttribute != null) {
          final String attributeValue = eventHandlerName + "(event)";
          xmlAttribute.setValue(attributeValue);
          final PsiLanguageInjectionHost valueElement = (PsiLanguageInjectionHost)xmlAttribute.getValueElement();
          if (valueElement != null) {
            final Ref<PsiElement> ref = new Ref<>();
            InjectedLanguageManager.getInstance(psiFile.getProject()).enumerate(valueElement, (injectedPsi, places) -> {
              int i = injectedPsi.getText().indexOf(attributeValue);
              if (i != -1) {
                ref.set(PsiTreeUtil.findElementOfClassAtOffset(injectedPsi, i, JSReferenceExpression.class, false));
              }
            });
            return ref.get();
          }
        }
      }
      else if (inEventListenerCall) {
        if (handlerCallerAnchorInArgumentList != null) {
          PsiElement element =
            JSChangeUtil.createJSTreeFromText(psiFile.getProject(), eventHandlerName, JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
          PsiElement created = null;
          if (element != null) {
            created = handlerCallerAnchorInArgumentList.getParent().addAfter(element, handlerCallerAnchorInArgumentList);
          }

          // comma in argument list
          if (handlerCallerAnchorInArgumentList.getNode().getElementType() != JSTokenTypes.COMMA) {
            final PsiElement psi = JSChangeUtil.createJSTreeFromText(psiFile.getProject(), "a,b").getPsi();
            final JSCommaExpression commaExpression = PsiTreeUtil.getChildOfType(psi, JSCommaExpression.class);
            final LeafPsiElement comma = PsiTreeUtil.getChildOfType(commaExpression, LeafPsiElement.class);
            if (comma != null && comma.getNode().getElementType() == JSTokenTypes.COMMA) {
              handlerCallerAnchorInArgumentList.getParent().addAfter(comma, handlerCallerAnchorInArgumentList);
            }
          }

          ensureTrailingSemicolonPresent(psiFile, created);
          return created;
        }
        else if (myExistingUnresolvedReverence != null) {
          ensureTrailingSemicolonPresent(psiFile, myExistingUnresolvedReverence);
          return myExistingUnresolvedReverence;
        }
      }
      else if (inEventConstantExpression) {
        final String text = "addEventListener(" + eventConstantExpression.getExpression().getText() + ", " + eventHandlerName + ");";
        final PsiElement element =
          JSChangeUtil.createJSTreeFromText(psiFile.getProject(), text, JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
        if (element != null) {
          final PsiElement addedElement = eventConstantExpression.replace(element);
          final JSExpression expression = ((JSExpressionStatement)addedElement).getExpression();
          final JSArgumentList argumentList = PsiTreeUtil.findChildOfType(expression, JSArgumentList.class);
          final JSExpression[] arguments = argumentList == null ? JSExpression.EMPTY_ARRAY : argumentList.getArguments();
          if (arguments.length == 2) {
            return arguments[1];
          }
        }
      }

      return null;
    }

    private static void ensureTrailingSemicolonPresent(final PsiFile psiFile, final PsiElement element) {
      final JSCallExpression callExpression = PsiTreeUtil.getParentOfType(element, JSCallExpression.class);
      if (callExpression != null && JSResolveUtil.isEventListenerCall(callExpression)) {
        final PsiElement parent = callExpression.getParent();
        if (parent instanceof JSExpressionStatement) {
          final PsiElement lastChild = parent.getLastChild();
          if (lastChild == callExpression) {
            final PsiElement psi = JSChangeUtil.createJSTreeFromText(psiFile.getProject(), ";").getPsi();
            final PsiElement semicolon = psi.getFirstChild();
            if (semicolon != null && semicolon.getNode().getElementType() == JSTokenTypes.SEMICOLON) {
              parent.addAfter(semicolon, callExpression);
            }
          }
        }
      }
    }

    @Nullable
    private JSClass getEventBaseClass() {
      final PsiElement eventClass = JSDialectSpecificHandlersFactory.forElement(myJsClass).getClassResolver()
        .findClassByQName(FlexCommonTypeNames.FLASH_EVENT_FQN, myJsClass);
      if (eventClass instanceof JSClass) return (JSClass)eventClass;
      return null;
    }

    private void prepareForMxmlEventAttributeValue(final XmlAttribute xmlAttribute, final String eventType) {
      eventClassFqn = eventType;
      methodBody = StringUtil.notNullize(xmlAttribute.getValue()).trim();
      if (methodBody.length() > 0 && !methodBody.endsWith(";") && !methodBody.endsWith("}")) methodBody += ";";

      final XmlTag xmlTag = xmlAttribute.getParent();
      final String eventName = xmlAttribute.getName();
      final String id = xmlTag == null ? null : xmlTag.getAttributeValue("id");
      if (xmlTag != null && xmlTag.getParent() instanceof XmlDocument) {
        eventHandlerName = eventName + "Handler";
      }
      else if (id == null) {
        final String name = xmlTag == null ? "" : xmlTag.getLocalName();
        final String idBase = name.isEmpty() ? "" : Character.toLowerCase(name.charAt(0)) + name.substring(1);
        int i = 0;
        do {
          i++;
          eventHandlerName = MessageFormat.format(METHOD_NAME_PATTERN, idBase + i, eventName);
        }
        while (myJsClass.findFunctionByName(eventHandlerName) != null);
      }
      else {
        eventHandlerName = MessageFormat.format(METHOD_NAME_PATTERN, id, eventName);
      }
      eventHandlerName2 = "on" + (eventName.isEmpty() ? "Event" : Character.toUpperCase(eventName.charAt(0)) + eventName.substring(1));
    }

    private void prepareForEventListenerCall(final JSCallExpression callExpression) {
      final JSExpression[] params = callExpression.getArguments();
      String eventName = "event";

      if (params.length > 0) {
        handlerCallerAnchorInArgumentList = params[0];

        PsiElement sibling = params[0];
        while ((sibling = sibling.getNextSibling()) != null) {
          final ASTNode node = sibling.getNode();
          if (node != null && node.getElementType() == JSTokenTypes.COMMA) {
            handlerCallerAnchorInArgumentList = sibling;

            if (params.length >= 2) {
              handlerCallerAnchorInArgumentList = null;
              if (isUnresolvedReference(params[1])) {
                myExistingUnresolvedReverence = (JSReferenceExpression)params[1];
                eventHandlerName = myExistingUnresolvedReverence.getReferencedName();
              }
            }

            break;
          }
        }

        if (params[0] instanceof JSReferenceExpression) {
          final JSReferenceExpression referenceExpression = (JSReferenceExpression)params[0];

          final JSExpression qualifier = referenceExpression.getQualifier();
          if (qualifier != null) {
            final PsiReference[] references = qualifier.getReferences();
            PsiElement resolveResult;
            if (references.length == 1 &&
                ((resolveResult = references[0].resolve()) instanceof JSClass) &&
                isEventClass((JSClass)resolveResult)) {
              eventClassFqn = ((JSClass)resolveResult).getQualifiedName();
            }
          }

          final PsiReference reference = referenceExpression.getReference();
          final PsiElement resolved = reference == null ? null : reference.resolve();
          if (resolved instanceof JSVariable && ((JSVariable)resolved).hasInitializer()) {
            eventName = initializerToPartialMethodName(((JSVariable)resolved).getInitializer().getText());
          }
        }
        else if (params[0] instanceof JSLiteralExpression) {
          eventName = initializerToPartialMethodName(params[0].getText());
        }
      }

      if (handlerCallerAnchorInArgumentList != null) {
        final JSExpression qualifier =
          ((JSReferenceExpression)callExpression.getMethodExpression()).getQualifier();
        final NamesValidator validator = LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.JAVASCRIPT.getLanguage());
        if (qualifier != null && validator.isIdentifier(qualifier.getText(), null)) {
          String qualifierText = qualifier.getText();
          if (qualifierText.length() > 1 && qualifierText.charAt(0) == '_' && validator.isIdentifier(qualifierText.substring(1), null)) {
            qualifierText = qualifierText.substring(1);
          }
          eventHandlerName = MessageFormat.format(METHOD_NAME_PATTERN, qualifierText, eventName);
        }
        else {
          eventHandlerName = eventName + "Handler";
        }
      }
      eventHandlerName2 = "on" + (eventName.isEmpty() ? "Event" : Character.toUpperCase(eventName.charAt(0)) + eventName.substring(1));
    }

    @NotNull
    private static Expression createExpression(String... variants) {
      return new ConstantNode(variants[0]).withLookupStrings(variants);
    }

  }

  @Override
  protected boolean isValidForTarget(PsiElement jsClass) {
    return jsClass instanceof JSClass && !((JSClass)jsClass).isInterface();
  }
}