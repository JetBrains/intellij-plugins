package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.Trinity;
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
import java.util.List;

public class JavaScriptGenerateEventHandler extends BaseJSGenerateHandler {

  private static final String EVENT_BASE_CLASS_FQN = "flash.events.Event";

  protected String getTitleKey() {
    return ""; // not used in this action
  }

  protected BaseCreateMethodsFix createFix(final JSClass jsClass) {
    return new GenerateEventHandlerFix(jsClass);
  }

  protected boolean collectCandidatesAndShowDialog() {
    return false;
  }

  protected boolean canHaveEmptySelectedElements() {
    return true;
  }


  @Nullable
  public static XmlAttribute getXmlAttribute(final PsiFile psiFile, final Editor editor) {
    PsiElement context = null;
    if (psiFile instanceof JSFile) {
      context = psiFile.getContext();
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

    final JSArgumentList argumentList = callExpression.getArgumentList();
    final JSExpression[] params = argumentList != null ? argumentList.getArguments() : JSExpression.EMPTY_ARRAY;

    if (params.length > 0 &&
        ((params[0] instanceof JSReferenceExpression && ((JSReferenceExpression)params[0]).getQualifier() != null) ||
         (params[0] instanceof JSLiteralExpression && ((JSLiteralExpression)params[0]).isQuotedLiteral()))) {
      if (params.length == 1 || params.length > 1 && isUnresolvedReference(params[1])) {
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
   * Trinity.first is JSExpressionStatement (if it looks like ButtonEvent.CLICK),
   * Trinity.second is event class FQN (like "flash.events.MouseEvent"),
   * Trinity.third is event name (like "click")
   */
  @Nullable
  public static Trinity<JSExpressionStatement, String, String> getEventConstantInfo(final PsiFile psiFile, final Editor editor) {
    if (!(psiFile instanceof JSFile)) {
      return null;
    }

    final JSClass jsClass = BaseJSGenerateHandler.findClass(psiFile, editor);
    if (jsClass == null || !JavaScriptGenerateAccessorHandler.isEventDispatcher(jsClass)) {
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
    if (expressionResolve instanceof JSVariable) {
      final JSAttributeList varAttributes = ((JSVariable)expressionResolve).getAttributeList();
      final String text = ((JSVariable)expressionResolve).getInitializerText();
      if (varAttributes != null &&
          varAttributes.hasModifier(JSAttributeList.ModifierType.STATIC) &&
          varAttributes.getAccessType() == JSAttributeList.AccessType.PUBLIC &&
          StringUtil.isQuotedString(text)) {
        return Trinity.create(expressionStatement, ((JSClass)qualifierResolve).getQualifiedName(), initializerToPartialMethodName(text));
      }
    }

    return null;
  }

  public static boolean isEventClass(final JSClass jsClass) {
    final PsiElement eventClass = JSResolveUtil.unwrapProxy(JSResolveUtil.findClassByQName(EVENT_BASE_CLASS_FQN, jsClass));
    if (!(eventClass instanceof JSClass)) return false;
    if (JSResolveUtil.checkClassHasParentOfAnotherOne(jsClass, (JSClass)eventClass, null)) {
      return true;
    }
    return false;
  }

  private static String initializerToPartialMethodName(final String initializerText) {
    final String unquoted = StringUtil.stripQuotesAroundValue(initializerText);
    final int dotIndex = unquoted.lastIndexOf('.');
    return unquoted.substring(dotIndex + 1).replaceAll("[^\\p{Alnum}]", "_");
  }

  public static class GenerateEventHandlerFix extends BaseCreateMethodsFix {
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
      eventClassFqn = EVENT_BASE_CLASS_FQN;
      userCancelled = false;
    }

    // called outside of write action - required for class chooser
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

      final Trinity<JSExpressionStatement, String, String> eventConstantInfo = getEventConstantInfo(psiFile, editor);
      if (eventConstantInfo != null) {
        inEventConstantExpression = true;
        eventConstantExpression = eventConstantInfo.first;
        eventClassFqn = eventConstantInfo.second;
        final String eventName = eventConstantInfo.third;
        eventHandlerName = eventName + "Handler";
        eventHandlerName2 = "on" + (eventName.isEmpty() ? "Event" : Character.toUpperCase(eventName.charAt(0)) + eventName.substring(1));
        return;
      }

      // no suitable context -> ask for event class and create handler without usage
      final Module module = ModuleUtil.findModuleForPsiElement(psiFile);
      if (module != null && !ApplicationManager.getApplication().isUnitTestMode()) {
        final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        final JSClassChooserDialog dialog =
          new JSClassChooserDialog(module.getProject(), FlexBundle.message("choose.event.class.title"), scope, getEventBaseClass(),
                                   new JSClassChooserDialog.PublicInheritor(module, EVENT_BASE_CLASS_FQN, false));
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

    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      if (userCancelled) return;
      final PsiElement referenceElement = insertEventHandlerReference(editor, file);
      evalAnchor(editor, file);
      final String eventClassShortName = StringUtil.getShortName(eventClassFqn);
      final String functionText =
        "private function " + eventHandlerName + "(event:" + eventClassShortName + "):void{" + methodBody + "\n}\n";

      final JSFunction addedElement = (JSFunction)doAddOneMethod(project, functionText, anchor);
      ImportUtils.importAndShortenReference(eventClassFqn, addedElement, true, false);

      final PsiElement templateBaseElement = referenceElement == null ? addedElement : myJsClass;
      final TemplateBuilderImpl templateBuilder = new TemplateBuilderImpl(templateBaseElement);

      final PsiElement lastElement = PsiTreeUtil.getDeepestLast(addedElement);
      final PsiElement prevElement = lastElement.getPrevSibling();
      templateBuilder.setEndVariableBefore((prevElement != null ? prevElement : lastElement));

      templateBuilder
        .replaceElement(addedElement.getAttributeList().findAccessTypeElement(), new MyExpression("private", "protected", "public"));
      templateBuilder
        .replaceElement(addedElement.findNameIdentifier().getPsi(), "handlerName", new MyExpression(eventHandlerName, eventHandlerName2),
                        true);
      templateBuilder
        .replaceElement(addedElement.getParameterList().getParameters()[0].findNameIdentifier().getPsi(), new MyExpression("event", "e"));

      if (referenceElement != null && referenceElement.isValid()) {
        templateBuilder.replaceElement(referenceElement, "handlerReference", "handlerName", false);
      }

      final Editor topEditor = InjectedLanguageUtil.getTopLevelEditor(editor);
      PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(topEditor.getDocument());

      final Template template = templateBuilder.buildInlineTemplate();

      final int startOffset = templateBaseElement.getTextRange().getStartOffset();
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
            final Ref<PsiElement> ref = new Ref<PsiElement>();
            InjectedLanguageUtil.enumerate(valueElement, new PsiLanguageInjectionHost.InjectedPsiVisitor() {
              public void visit(@NotNull final PsiFile injectedPsi, @NotNull final List<PsiLanguageInjectionHost.Shred> places) {
                int i = injectedPsi.getText().indexOf(attributeValue);
                if (i != -1) {
                  ref.set(PsiTreeUtil.findElementOfClassAtOffset(injectedPsi, i, JSReferenceExpression.class, false));
                }
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

          if (handlerCallerAnchorInArgumentList.getNode().getElementType() != JSTokenTypes.COMMA) {
            final PsiElement psi = JSChangeUtil.createJSTreeFromText(psiFile.getProject(), "a,b").getPsi();
            final JSCommaExpression commaExpression = PsiTreeUtil.getChildOfType(psi, JSCommaExpression.class);
            final LeafPsiElement comma = PsiTreeUtil.getChildOfType(commaExpression, LeafPsiElement.class);
            if (comma != null && comma.getNode().getElementType() == JSTokenTypes.COMMA) {
              handlerCallerAnchorInArgumentList.getParent().addAfter(comma, handlerCallerAnchorInArgumentList);
            }
          }
          return created;
        }
        else if (myExistingUnresolvedReverence != null) {
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

    @Nullable
    private JSClass getEventBaseClass() {
      final PsiElement eventClass = JSResolveUtil
        .unwrapProxy(JSResolveUtil.findClassByQName(EVENT_BASE_CLASS_FQN, myJsClass));
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
      final JSExpression[] params = callExpression.getArgumentList().getArguments();
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
            eventName = initializerToPartialMethodName(((JSVariable)resolved).getInitializerText());
          }
        }
        else if (params[0] instanceof JSLiteralExpression) {
          eventName = initializerToPartialMethodName(params[0].getText());
        }
      }

      if (handlerCallerAnchorInArgumentList != null) {
        final JSExpression qualifier =
          ((JSReferenceExpression)callExpression.getMethodExpression()).getQualifier();
        if (qualifier != null &&
            LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.JAVASCRIPT.getLanguage())
              .isIdentifier(qualifier.getText(), null)) {
          eventHandlerName = MessageFormat.format(METHOD_NAME_PATTERN, qualifier.getText(), eventName);
        }
        else {
          eventHandlerName = eventName + "Handler";
        }
      }
      eventHandlerName2 = "on" + (eventName.isEmpty() ? "Event" : Character.toUpperCase(eventName.charAt(0)) + eventName.substring(1));
    }

    private static class MyExpression extends Expression {
      private final TextResult myResult;
      private final LookupElement[] myLookupItems;

      public MyExpression(final String... variants) {
        myResult = new TextResult(variants[0]);
        myLookupItems = variants.length == 1 ? LookupElement.EMPTY_ARRAY : new LookupElement[variants.length];
        if (variants.length > 1) {
          for (int i = 0; i < variants.length; i++) {
            myLookupItems[i] = LookupElementBuilder.create(variants[i]);
          }
        }
      }

      public Result calculateResult(ExpressionContext context) {
        return myResult;
      }

      public Result calculateQuickResult(ExpressionContext context) {
        return myResult;
      }

      public LookupElement[] calculateLookupItems(ExpressionContext context) {
        return myLookupItems;
      }
    }
  }
}