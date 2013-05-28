package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.daemon.impl.quickfix.RenameElementFix;
import com.intellij.codeInsight.daemon.impl.quickfix.RenameFileFix;
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector;
import com.intellij.lang.javascript.flex.ActionScriptSmartCompletionContributor;
import com.intellij.lang.javascript.flex.AddImportECMAScriptClassOrFunctionAction;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingUtil;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeImpl;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeListImpl;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSClassBase;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSPackageStatementImpl;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.refactoring.changeSignature.JSMethodDescriptor;
import com.intellij.lang.javascript.ui.JSFormatUtil;
import com.intellij.lang.javascript.validation.*;
import com.intellij.lang.javascript.validation.fixes.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.*;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.FUNCTION_CLASS_NAME;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptAnnotatingVisitor extends TypedJSAnnotatingVisitor {
  private static final String[] EXTENSIONS_TO_CHECK = {
    JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION,
    JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION2,
    JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION3,
    JavaScriptSupportLoader.MXML_FILE_EXTENSION,
    JavaScriptSupportLoader.FXG_FILE_EXTENSION
  };

  public static void checkFileUnderSourceRoot(final JSNamedElement aClass, ErrorReportingClient client) {
    PsiElement nameIdentifier = aClass.getNameIdentifier();
    if (nameIdentifier == null) {
      nameIdentifier = aClass.getFirstChild();
    }

    final PsiFile containingFile = aClass.getContainingFile();
    final VirtualFile file = containingFile.getVirtualFile();
    if (file == null) return;
    final VirtualFile rootForFile = ProjectRootManager.getInstance(containingFile.getProject()).getFileIndex().getSourceRootForFile(file);

    if (rootForFile == null) {
      client.reportError(nameIdentifier.getNode(), JSBundle.message("javascript.validation.message.file.should.be.under.source.root"),
                         ErrorReportingClient.ProblemKind.WARNING);
    }

    if (!(aClass instanceof JSPackageStatement)) {
      VirtualFile parent = file.getParent();
      boolean found = false;
      for (String ext : EXTENSIONS_TO_CHECK) {
        String name = file.getNameWithoutExtension() + "." + ext;
        VirtualFile child = parent.findChild(name);
        if (child != null && name.equals(child.getName())) { // check for case-insensitive filesystems
          if (found) {
            client.reportError(nameIdentifier.getNode(),
                               JSBundle.message("javascript.validation.message.more.than.one.named.object.in.package"),
                               ErrorReportingClient.ProblemKind.ERROR);
            break;
          }
          else {
            found = true;
          }
        }
      }
    }
  }

  @Override
  public JSAnnotatingVisitor newInstance() {
    return new ActionScriptAnnotatingVisitor();
  }

  protected static ChangeSignatureFix createChangeBaseMethodSignatureFix(final JSFunction superMethod, final JSFunction override) {
    return new ChangeSignatureFix(superMethod, JSMethodDescriptor.getParameters(superMethod)) {
      @Override
      protected String getOverriddenReturnType() {
        return StringUtil.notNullize(override.getReturnType().getResolvedTypeText());
      }
    };
  }

  @Override
  public void visitJSAttributeNameValuePair(final JSAttributeNameValuePair attributeNameValuePair) {
    final boolean ok = checkReferences(attributeNameValuePair, ProblemKind.ERROR);

    if (!ok) return;

    // check if attribute value must be FQN of a class class inherited from some other class
    if (attributeNameValuePair.getValueNode() == null) return;

    final PsiElement parent = attributeNameValuePair.getParent();
    final XmlElementDescriptor descriptor = parent instanceof JSAttributeImpl ? ((JSAttributeImpl)parent).getBackedDescriptor() : null;

    final String attributeName = StringUtil.notNullize(attributeNameValuePair.getName(), JSAttributeNameValuePair.DEFAULT);
    final XmlAttributeDescriptor attributeDescriptor = descriptor == null ? null : descriptor.getAttributeDescriptor(attributeName, null);

    final String baseClassFqns = attributeDescriptor == null ? null : attributeDescriptor.getDefaultValue();
    if (baseClassFqns != null && !StringUtil.isEmptyOrSpaces(baseClassFqns)) {
      final PsiReference[] references = attributeNameValuePair.getReferences();
      PsiReference lastReference = references.length > 0 ? references[0] : null;

      for (final PsiReference reference : references) {
        if (reference.getRangeInElement().getEndOffset() > lastReference.getRangeInElement().getEndOffset()) {
          lastReference = reference;
        }
      }

      final PsiElement resolved = lastReference != null ? lastReference.resolve() : null;

      if (resolved instanceof JSClass) {
        boolean correctClass = false;
        final Collection<String> resolvedBaseClasses = new ArrayList<String>();
        final GlobalSearchScope scope = JSResolveUtil.getResolveScope(attributeNameValuePair);

        for (String baseClassFqn : StringUtil.split(baseClassFqns, ",")) {
          if ("Object".equals(baseClassFqn)) {
            correctClass = true;
            break;
          }

          final PsiElement baseClass = JSResolveUtil.findClassByQName(baseClassFqn, attributeNameValuePair);

          if (baseClass instanceof JSClass) {
            resolvedBaseClasses.add(baseClassFqn);
            if (JSInheritanceUtil.isParentClass((JSClass)resolved, (JSClass)baseClass, false, scope)) {
              correctClass = true;
              break;
            }
          }
        }

        if (!correctClass) {
          final String classesForMessage = resolvedBaseClasses.isEmpty() ? StringUtil.replace(baseClassFqns, ",", ", ")
                                                                         : StringUtil.join(resolvedBaseClasses, ", ");

          myHolder.createErrorAnnotation(calcRangeForReferences(lastReference),
                                         JSBundle.message("javascript.expected.class.or.descendant", classesForMessage));
        }
      }
      else if (resolved !=
               attributeNameValuePair) { // for some reason int and uint are resolved to self-reference JSResolveUtil.MyResolveResult() instead of usual JSClass
        myHolder.createErrorAnnotation(attributeNameValuePair.getValueNode(), JSBundle.message("javascript.qualified.class.name.expected"));
      }
    }
  }

  @Override
  public void visitJSIncludeDirective(final JSIncludeDirective includeDirective) {
    checkReferences(includeDirective, ProblemKind.ERROR);
  }

  @Override
  protected void checkImplementedMethods(JSClass jsClass, ErrorReportingClient reportingClient) {
    checkActionScriptImplementedMethods(jsClass, reportingClient);
  }

  public static void checkActionScriptImplementedMethods(final JSClass jsClass, final ErrorReportingClient reportingClient) {
    final JSResolveUtil.CollectMethodsToImplementProcessor implementedMethodProcessor = new ImplementedMethodProcessor(jsClass) {
      ImplementMethodsFix implementMethodsFix = null;

      protected void addNonimplementedFunction(final JSFunction function) {
        final ASTNode node = myJsClass.findNameIdentifier();
        if (node == null) return;
        if (implementMethodsFix == null) implementMethodsFix = new ImplementMethodsFix(myJsClass);
        implementMethodsFix.addElementToProcess(function);
        String messageId = function.isGetProperty() ?
                           "javascript.validation.message.interface.method.not.implemented2" :
                           function.isSetProperty() ?
                           "javascript.validation.message.interface.method.not.implemented3" :
                           "javascript.validation.message.interface.method.not.implemented";
        String message = JSBundle.message(messageId,
                                          function.getName(),
                                          ((JSClass)JSResolveUtil.findParent(function)).getQualifiedName());
        reportingClient.reportError(node, message,
                                    ErrorReportingClient.ProblemKind.ERROR,
                                    implementMethodsFix);
      }

      protected void addImplementedFunction(final JSFunction interfaceFunction, final JSFunction implementationFunction) {
        final JSAttributeList attributeList = implementationFunction.getAttributeList();
        if (attributeList == null || attributeList.getAccessType() != JSAttributeList.AccessType.PUBLIC) {
          final ASTNode node = findElementForAccessModifierError(implementationFunction, attributeList);
          reportingClient.reportError(node,
                                      JSBundle.message("javascript.validation.message.interface.method.invalid.access.modifier"),
                                      ErrorReportingClient.ProblemKind.ERROR,
                                      new SetElementVisibilityFix(implementationFunction, JSAttributeList.AccessType.PUBLIC)
          );
        }

        final SignatureMatchResult incompatibleSignature = checkCompatibleSignature(implementationFunction, interfaceFunction);

        if (incompatibleSignature != SignatureMatchResult.COMPATIBLE_SIGNATURE) {
          PsiElement parent = JSResolveUtil.findParent(implementationFunction);
          if (parent instanceof JSFile) {
            parent = JSResolveUtil.getClassReferenceForXmlFromContext(parent);
          }

          if (parent != myJsClass) {
            // some parent incorrectly implements method from our interface
            addNonimplementedFunction(interfaceFunction);
            return;
          }

          if (incompatibleSignature == SignatureMatchResult.PARAMETERS_DIFFERS) {
            final JSParameterList parameterList = implementationFunction.getParameterList();
            final JSParameterList expectedParameterList = interfaceFunction.getParameterList();

            ChangeSignatureFix changeSignatureFix = new ChangeSignatureFix(interfaceFunction, parameterList, true);
            reportingClient.reportError(parameterList.getNode(),
                                        JSBundle.message(
                                          "javascript.validation.message.interface.method.invalid.signature",
                                          expectedParameterList != null ? expectedParameterList.getText() : "()"
                                        ),
                                        ErrorReportingClient.ProblemKind.ERROR,
                                        new ChangeSignatureFix(implementationFunction, expectedParameterList, false) {
                                          @NotNull
                                          public String getText() {
                                            return JSBundle.message("javascript.fix.message.change.parameters.to.expected");
                                          }
                                        },
                                        changeSignatureFix);
          }
          else if (incompatibleSignature == SignatureMatchResult.RETURN_TYPE_DIFFERS) {
            PsiElement implementationReturnTypeExpr = implementationFunction.getReturnTypeElement();
            final String interfaceReturnType = interfaceFunction.getReturnType().getResolvedTypeText();
            String msg = JSBundle
              .message("javascript.validation.message.interface.method.invalid.signature2", StringUtil.notNullize(interfaceReturnType));
            reportingClient.reportError(
              implementationReturnTypeExpr != null ? implementationReturnTypeExpr.getNode() : implementationFunction.findNameIdentifier(),
              msg, ErrorReportingClient.ProblemKind.ERROR,
              new ChangeTypeFix(implementationFunction, interfaceReturnType, "javascript.fix.message.change.return.type.to.expected"),
              createChangeBaseMethodSignatureFix(interfaceFunction, implementationFunction));
          }
          else if (incompatibleSignature == SignatureMatchResult.FUNCTION_KIND_DIFFERS) {
            String msg = JSBundle.message("javascript.validation.message.interface.method.invalid.signature3", interfaceFunction.getKind());
            reportingClient.reportError(
              implementationFunction.findNameIdentifier(),
              msg, ErrorReportingClient.ProblemKind.ERROR);       // TODO: fix
          }
        }
      }
    };
    JSResolveUtil.processInterfaceMethods(jsClass, implementedMethodProcessor);
  }

  @Override
  protected void checkFunction(final JSFunction node) {
    final ASTNode nameIdentifier = node.findNameIdentifier();
    if (nameIdentifier == null) return;
    PsiElement parent = node.getParent();

    if (parent instanceof JSFile) {
      parent = JSResolveUtil.getClassReferenceForXmlFromContext(parent);
      final String name = node.getName();

      if (parent instanceof JSClass &&
          name != null &&
          name.equals(((JSClass)parent).getName()) &&
          JavaScriptSupportLoader.isFlexMxmFile(parent.getContainingFile())) {
        final Annotation annotation = myHolder.createErrorAnnotation(
          nameIdentifier,
          JSBundle.message("javascript.validation.message.constructor.in.mxml.is.not.allowed")
        );

        annotation.registerFix(new RemoveASTNodeFix("javascript.fix.remove.constructor", node.getNode()));
      }
    }

    if (parent instanceof JSPackageStatement) {
      checkNamedObjectIsInCorrespondingFile(node);
    }

    if (parent instanceof JSClass && !node.isConstructor()) {
      final JSAttributeList attributeList = node.getAttributeList();
      final JSClass clazz = (JSClass)parent;

      if (attributeList == null ||
          !attributeList.hasModifier(JSAttributeList.ModifierType.STATIC) &&
          (attributeList.getAccessType() != JSAttributeList.AccessType.PRIVATE ||
           attributeList.hasModifier(JSAttributeList.ModifierType.OVERRIDE)
          )) {
        final String qName = clazz.getQualifiedName();
        final boolean hasOverride = attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.OVERRIDE);

        final Ref<JSFunction> set = new Ref<JSFunction>();
        boolean b = JSResolveUtil.iterateType(node, parent, qName, new JSResolveUtil.OverrideHandler() {
          public boolean process(final ResolveProcessor processor, final PsiElement scope, final String className) {
            //noinspection StringEquality
            if (qName == className || qName != null && qName.equals(className)) return true;
            JSFunction value = (JSFunction)processor.getResult();
            set.set(value);

            DialectOptionHolder holder;
            if ("Object".equals(className)) {
              if (hasOverride && !attributeList.hasModifier(JSAttributeList.ModifierType.NATIVE)) { /*native modifier is written always*/
                final ASTNode astNode = attributeList.getNode().findChildByType(JSTokenTypes.OVERRIDE_KEYWORD);
                final Annotation annotation = myHolder.createErrorAnnotation(astNode, JSBundle.message(
                  "javascript.validation.message.function.override.for.object.method"));

                annotation.registerFix(
                  new RemoveASTNodeFix("javascript.fix.remove.override.modifier", astNode)
                );
              }
              return false;
            }
            else if (!hasOverride && (holder = myHighlighter.getDialectOptionsHolder()) != null && holder.isECMAL4Level) {
              final Annotation annotation = myHolder.createErrorAnnotation(nameIdentifier, JSBundle.message(
                "javascript.validation.message.function.override.without.override.modifier", className));

              annotation.registerFix(new AddOverrideIntentionAction(node));
            }
            else {
              JSAttributeList attrList = value.getAttributeList();
              JSAttributeList parentAttrList = ((JSAttributeListOwner)scope).getAttributeList();

              if (attrList != null && attrList.hasModifier(JSAttributeList.ModifierType.FINAL) ||
                  parentAttrList != null && parentAttrList.hasModifier(JSAttributeList.ModifierType.FINAL)
                ) {
                myHolder.createErrorAnnotation(
                  attributeList.getNode().findChildByType(JSTokenTypes.OVERRIDE_KEYWORD),
                  JSBundle.message("javascript.validation.message.can.not.override.final.method", className)
                );
              }
            }
            if (clazz.isInterface()) {
              myHolder.createErrorAnnotation(nameIdentifier, JSBundle.message(
                "javascript.validation.message.function.override.for.interface", className));
            }
            return false;
          }
        });

        if (b && hasOverride) {
          final ASTNode astNode = attributeList.getNode().findChildByType(JSTokenTypes.OVERRIDE_KEYWORD);
          final Annotation annotation = myHolder.createErrorAnnotation(astNode, JSBundle.message(
            "javascript.validation.message.function.override.without.parent.method"));

          annotation.registerFix(
            new RemoveASTNodeFix("javascript.fix.remove.override.modifier", astNode)
          );
        }

        if (!b && hasOverride) {
          final JSFunction override = set.get();
          final JSAttributeList overrideAttrList = override.getAttributeList();
          String overrideNs = null;

          if (attributeList.getAccessType() != overrideAttrList.getAccessType() ||
              (overrideNs = JSResolveUtil.getNamespaceValue(overrideAttrList)) != null &&
              !overrideNs.equals(JSResolveUtil.getNamespaceValue(attributeList))) {
            String newVisibility;
            SetElementVisibilityFix fix;
            if (overrideNs != null) {
              newVisibility = overrideNs;
              fix = new SetElementVisibilityFix(node, overrideNs);
            }
            else {
              newVisibility = JSFormatUtil.formatVisibility(overrideAttrList.getAccessType());
              fix = new SetElementVisibilityFix(node, overrideAttrList.getAccessType());
            }
            final Annotation annotation = myHolder.createErrorAnnotation(
              findElementForAccessModifierError(node, attributeList),
              JSBundle.message("javascript.validation.message.function.override.incompatible.access.modifier", newVisibility));

            annotation.registerFix(fix);
          }

          final SignatureMatchResult incompatibleSignature = checkCompatibleSignature(node, override);

          if (incompatibleSignature == SignatureMatchResult.PARAMETERS_DIFFERS) {
            final JSParameterList nodeParameterList = node.getParameterList();
            final JSParameterList overrideParameterList = override.getParameterList();

            final Annotation annotation = myHolder.createErrorAnnotation(
              nodeParameterList != null ? nodeParameterList.getNode() : node.findNameIdentifier(),
              JSBundle.message("javascript.validation.message.function.override.incompatible.signature",
                               overrideParameterList != null ? overrideParameterList.getText() : "()"
              )
            );

            annotation.registerFix(new ChangeSignatureFix(node, overrideParameterList, false) {
              @NotNull
              public String getText() {
                return JSBundle.message("javascript.fix.message.change.parameters.to.expected");
              }
            });
            annotation.registerFix(new ChangeSignatureFix(override, nodeParameterList, true));
          }
          else if (incompatibleSignature == SignatureMatchResult.RETURN_TYPE_DIFFERS) {
            PsiElement returnTypeExpr = node.getReturnTypeElement();
            final String baseReturnType = override.getReturnType().getResolvedTypeText();
            String msg = JSBundle
              .message("javascript.validation.message.function.override.incompatible.signature2", StringUtil.notNullize(baseReturnType));
            final Annotation annotation =
              myHolder.createErrorAnnotation(returnTypeExpr != null ? returnTypeExpr.getNode() : node.findNameIdentifier(), msg);
            annotation.registerFix(new ChangeTypeFix(node, baseReturnType,
                                                     "javascript.fix.message.change.return.type.to.expected"));
            annotation.registerFix(ActionScriptAnnotatingVisitor.createChangeBaseMethodSignatureFix(override, node));
          }
          else if (incompatibleSignature == SignatureMatchResult.FUNCTION_KIND_DIFFERS) {
            String msg = JSBundle
              .message("javascript.validation.message.function.override.incompatible.signature3", override.getKind().toString());
            final Annotation annotation =
              myHolder.createErrorAnnotation(node.findNameIdentifier(), msg);
            //annotation.registerFix();
          }
        }
      }
      else if (attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) {
        if (clazz.isInterface()) {
          reportStaticMethodProblem(attributeList, "javascript.validation.message.static.method.in.interface");
        }
        if (attributeList.hasModifier(JSAttributeList.ModifierType.OVERRIDE)) {
          reportStaticMethodProblem(attributeList, "javascript.validation.message.static.method.with.override");
        }
      }
    }

    super.checkFunction(node);
  }

  private void reportStaticMethodProblem(JSAttributeList attributeList, String key) {
    final ASTNode astNode = attributeList.getNode().findChildByType(JSTokenTypes.STATIC_KEYWORD);
    final Annotation annotation =
      myHolder.createErrorAnnotation(astNode, JSBundle.message(key));
    annotation.registerFix(new RemoveASTNodeFix("javascript.fix.remove.static.modifier", astNode));
  }


  private static class AddOverrideIntentionAction implements IntentionAction {
    private final JSFunction myNode;

    public AddOverrideIntentionAction(final JSFunction node) {
      myNode = node;
    }

    @NotNull
    public String getText() {
      return JSBundle.message("javascript.fix.add.override.modifier");
    }

    @NotNull
    public String getFamilyName() {
      return getText();
    }

    public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
      return myNode.isValid();
    }

    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;

      JSAttributeListWrapper w = new JSAttributeListWrapper(myNode.getAttributeList());
      w.overrideModifier(JSAttributeList.ModifierType.OVERRIDE, true);
      w.applyTo(myNode);
    }

    public boolean startInWriteAction() {
      return true;
    }
  }

  public void visitJSPackageStatement(final JSPackageStatement packageStatement) {
    final JSFile jsFile = PsiTreeUtil.getParentOfType(packageStatement, JSFile.class);
    final PsiElement context = jsFile == null ? null : jsFile.getContext();
    boolean injected = context instanceof XmlAttributeValue || context instanceof XmlText;
    if (injected) {
      myHolder.createErrorAnnotation(packageStatement.getFirstChild().getNode(),
                                     JSBundle.message("javascript.validation.message.nested.packages.are.not.allowed"));
      return;
    }

    for (PsiElement el = packageStatement.getPrevSibling(); el != null; el = el.getPrevSibling()) {
      if (!(el instanceof PsiWhiteSpace) && !(el instanceof PsiComment)) {
        myHolder.createErrorAnnotation(
          packageStatement.getFirstChild().getNode(),
          JSBundle.message("javascript.validation.message.package.shouldbe.first.statement")
        );
        break;
      }
    }
    final ASTNode node = packageStatement.findNameIdentifier();
    if (node == null) checkPackageStatement(packageStatement);
  }

  private void checkPackageStatement(final JSPackageStatement packageStatement) {
    final String s = packageStatement.getQualifiedName();

    final PsiFile containingFile = packageStatement.getContainingFile();
    final String expected = JSResolveUtil.getExpectedPackageNameFromFile(containingFile.getVirtualFile(), containingFile.getProject());

    if (expected != null && (s == null && expected.length() != 0 || s != null && !expected.equals(s))) {
      final ASTNode nameIdentifier = packageStatement.findNameIdentifier();
      final Annotation annotation = myHolder.createErrorAnnotation(
        nameIdentifier != null ? nameIdentifier : packageStatement.getFirstChild().getNode(),
        JSBundle.message(
          "javascript.validation.message.incorrect.package.name", s, expected
        )
      );
      annotation.registerFix(new IntentionAction() {
        @NotNull
        public String getText() {
          return JSBundle.message("javascript.fix.package.name", expected);
        }

        @NotNull
        public String getFamilyName() {
          return getText();
        }

        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
          return packageStatement.isValid();
        }

        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
          JSPackageStatementImpl.doChangeName(project, packageStatement, expected);
        }

        public boolean startInWriteAction() {
          return true;
        }
      });
    }

    final Set<JSNamedElement> elements = new THashSet<JSNamedElement>();

    for (JSSourceElement statement : packageStatement.getStatements()) {
      if (statement instanceof JSNamedElement && !(statement instanceof JSImportStatement)) {
        elements.add((JSNamedElement)statement);
      }
      else if (statement instanceof JSVarStatement) {
        ContainerUtil.addAll(elements, ((JSVarStatement)statement).getVariables());
      }
    }

    if (elements.size() > 1) {
      for (JSNamedElement el : elements) {
        if (!(el instanceof JSAttributeListOwner)) continue;
        JSAttributeList attributeList = ((JSAttributeListOwner)el).getAttributeList();
        if (attributeList != null && attributeList.getConditionalCompileVariableReference() != null) continue;
        final ASTNode nameIdentifier = el.findNameIdentifier();
        myHolder.createErrorAnnotation(
          nameIdentifier != null ? nameIdentifier : el.getFirstChild().getNode(),
          JSBundle.message("javascript.validation.message.more.than.one.externally.visible.symbol")
        ).registerFix(new RemoveASTNodeFix("javascript.fix.remove.externally.visible.symbol", el.getNode()));
      }
    }

    checkFileUnderSourceRoot(packageStatement, new SimpleErrorReportingClient());
  }

  @Override
  public void visitJSReferenceExpression(JSReferenceExpression node) {
    super.visitJSReferenceExpression(node);

    final PsiElement parent = node.getParent();

    if (node.getQualifier() == null) {
      String nodeText = node.getText();
      if (!(parent instanceof JSCallExpression) && JSResolveUtil.isExprInStrictTypeContext(node) &&
          JSCommonTypeNames.VECTOR_CLASS_NAME.equals(nodeText)) {
        myHolder.createWarningAnnotation(node, JSBundle.message("javascript.validation.message.vector.without.parameters"));
      }
      else if (parent instanceof JSNewExpression &&
               JSCommonTypeNames.VECTOR_CLASS_NAME.equals(nodeText)) {
        myHolder.createWarningAnnotation(node, JSBundle.message("javascript.validation.message.vector.without.parameters2"));
      }
    }


    if (parent instanceof JSNamedElement) {
      JSNamedElement namedElement = (JSNamedElement)parent;
      final ASTNode nameIdentifier = namedElement.findNameIdentifier();

      if (nameIdentifier != null && nameIdentifier.getPsi() == node) {
        if (parent instanceof JSPackageStatement) {
          checkPackageStatement((JSPackageStatement)parent);
        }
        else if (!(parent instanceof JSImportStatement) && parent.getParent() instanceof JSPackageStatement) {
          checkNamedObjectIsInCorrespondingFile(namedElement);
        }
        else if (parent instanceof JSVariable) {
          if (parent.getParent().getParent() instanceof JSPackageStatement) {
            checkNamedObjectIsInCorrespondingFile((JSVariable)parent);
          }
        }
        else if (parent instanceof JSNamespaceDeclaration) {
          DuplicatesCheckUtil.checkDuplicates((JSNamespaceDeclaration)parent, myHolder);
        }

        if (parent instanceof JSClass) {
          final JSClass jsClass = (JSClass)parent;
          final JSFunction constructor = jsClass.getConstructor();
          if (constructor == null) checkMissedSuperCall(node, constructor, jsClass);

          PsiElement clazzParent = jsClass.getParent();
          final PsiElement context = clazzParent.getContext();
          boolean clazzParentIsInjectedJsFile = clazzParent instanceof JSFile &&
                                                (context instanceof XmlAttributeValue || context instanceof XmlText) &&
                                                !XmlBackedJSClassImpl.isImplementsAttribute((JSFile)clazzParent);

          if (PsiTreeUtil.getParentOfType(jsClass, JSFunction.class, JSClass.class) != null || clazzParentIsInjectedJsFile) {
            if (getAnnotateHighlighterFilter(jsClass).shouldHighlightNestedClass()) {
              myHolder.createErrorAnnotation(node, JSBundle.message("javascript.validation.message.nested.classes.are.not.allowed"));
            }
          }
          checkClass(jsClass);
        }
      }
    }
  }

  private void checkClass(JSClass jsClass) {
    if (!jsClass.isInterface()) {
      checkIfExtendsFinalOrMultipleClasses(jsClass);
    }
    DuplicatesCheckUtil.checkDuplicates(jsClass, myHolder);
  }

  private void checkIfExtendsFinalOrMultipleClasses(final JSClass jsClass) {
    final JSReferenceList extendsList = jsClass.getExtendsList();
    if (extendsList != null) {
      final String[] extendsListTexts = extendsList.getReferenceTexts();
      // in some cases jsClass.getSuperClasses() contains several elements for one class, so counting extendsListTexts is more correct
      if (extendsListTexts.length > 1) {
        myHolder
          .createErrorAnnotation(extendsList.getTextRange(), JSBundle.message("javascript.validation.message.extend.multiple.classes"));
      }
      else if (extendsListTexts.length == 1) {
        final JSClass[] superClasses = jsClass.getSuperClasses();
        final JSAttributeList attributeList = superClasses.length > 0 ? superClasses[0].getAttributeList() : null;
        if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.FINAL)) {
          final JSReferenceExpression[] referencesToSuper = extendsList.getExpressions();
          if (referencesToSuper != null && referencesToSuper.length == 1) {
            myHolder.createErrorAnnotation(referencesToSuper[0], JSBundle.message("javascript.validation.message.extend.final.class",
                                                                                  superClasses[0].getQualifiedName()));
          }
        }
      }
    }
  }


  @Override
  public void visitJSAttributeList(JSAttributeList attributeList) {
    PsiElement namespaceElement = attributeList.getNamespaceElement();
    PsiElement accessTypeElement = attributeList.findAccessTypeElement();
    PsiElement namespaceOrAccessModifierElement = namespaceElement;

    ASTNode[] children = attributeList.getNode().getChildren(JSAttributeListImpl.ourModifiersTypeSet);

    if (namespaceOrAccessModifierElement == null) {
      namespaceOrAccessModifierElement = accessTypeElement;
    }
    else if (accessTypeElement != null) {
      myHolder.createErrorAnnotation(namespaceOrAccessModifierElement,
                                     JSBundle.message("javascript.validation.message.use.namespace.reference.or.access.modifier"))
        .registerFix(
          new RemoveASTNodeFix("javascript.fix.remove.namespace.reference", namespaceOrAccessModifierElement.getNode()));

      myHolder.createErrorAnnotation(accessTypeElement,
                                     JSBundle.message("javascript.validation.message.use.namespace.reference.or.access.modifier"))
        .registerFix(new RemoveASTNodeFix("javascript.fix.remove.visibility.modifier", accessTypeElement.getNode()));
    }

    if (children.length > 1 && namespaceElement == null) {
      for (ASTNode astNode : children) {
        myHolder.createErrorAnnotation(astNode,
                                       JSBundle.message("javascript.validation.message.one.visibility.modifier.allowed"))
          .registerFix(new RemoveASTNodeFix("javascript.fix.remove.visibility.modifier", astNode));
      }
    }

    PsiElement element = attributeList.getParent();
    PsiElement parentForCheckingNsOrAccessModifier = JSResolveUtil.findParent(element);

    if (namespaceOrAccessModifierElement != null) {
      if (!(parentForCheckingNsOrAccessModifier instanceof JSClass)) {
        String typeElementText;
        boolean nodeUnderPackage;

        if (!(nodeUnderPackage = parentForCheckingNsOrAccessModifier instanceof JSPackageStatement) &&
            !hasQualifiedName(element) &&
            (!(parentForCheckingNsOrAccessModifier instanceof JSFile) ||
             attributeList.getAccessType() != JSAttributeList.AccessType.PACKAGE_LOCAL
            ) ||
            !"public".equals(typeElementText = namespaceOrAccessModifierElement.getText()) && !"internal".equals(typeElementText)) {
          boolean nsRef = namespaceOrAccessModifierElement instanceof JSReferenceExpression;
          Annotation annotation;

          String message =
            JSBundle.message(
              nodeUnderPackage ?
              "javascript.validation.message.access.modifier.allowed.only.for.package.members"
                               : nsRef ?
                                 "javascript.validation.message.namespace.allowed.only.for.class.members"
                                       : "javascript.validation.message.access.modifier.allowed.only.for.class.members");

          if (parentForCheckingNsOrAccessModifier instanceof JSFile && !(element instanceof JSClass)) {
            // TODO: till we resolve issues with includes
            annotation = myHolder.createWarningAnnotation(namespaceOrAccessModifierElement, message);
          }
          else {
            annotation = myHolder.createErrorAnnotation(
              namespaceOrAccessModifierElement, message
            );
          }

          annotation.registerFix(new RemoveASTNodeFix(nsRef
                                                      ? "javascript.fix.remove.namespace.reference"
                                                      : "javascript.fix.remove.access.modifier",
                                                      namespaceOrAccessModifierElement.getNode()));
        }
      }
      else if (((JSClass)parentForCheckingNsOrAccessModifier).isInterface()) {

        if (attributeList.getAccessType() != JSAttributeList.AccessType.PACKAGE_LOCAL ||
            attributeList.getNode().findChildByType(JSTokenTypes.INTERNAL_KEYWORD) != null

          ) {
          ASTNode astNode = attributeList.getNode().findChildByType(JSTokenTypes.ACCESS_MODIFIERS);
          String message = JSBundle.message("javascript.validation.message.interface.members.cannot.have.access.modifiers");
          String fixMessageKey = "javascript.fix.remove.access.modifier";
          if (astNode == null) {
            astNode = attributeList.getNode().findChildByType(JSElementTypes.REFERENCE_EXPRESSION);
            message = JSBundle.message("javascript.validation.message.interface.members.cannot.have.namespace.attributes");
            fixMessageKey = "javascript.fix.remove.namespace.reference";
          }
          final Annotation annotation = myHolder.createErrorAnnotation(astNode, message);

          annotation.registerFix(new RemoveASTNodeFix(fixMessageKey, astNode));
        }
      }
      else if (element instanceof JSFunction && ((JSFunction)element).isConstructor()) {
        JSAttributeList.AccessType accessType = attributeList.getAccessType();

        if (accessType != JSAttributeList.AccessType.PUBLIC) {
          myHolder.createErrorAnnotation(
            namespaceOrAccessModifierElement.getNode(),
            JSBundle.message("javascript.validation.message.constructor.cannot.have.custom.visibility")
          );
        }
      }
    }

    if (attributeList.hasModifier(JSAttributeList.ModifierType.FINAL)) {
      PsiElement parent;
      if (element instanceof JSClass) {
        if (((JSClass)element).isInterface()) {
          finalModifierProblem(attributeList, "javascript.validation.message.interface.cannot.be.final.modifiers");
        }
      }
      else if (parentForCheckingNsOrAccessModifier instanceof JSClass &&
               ((JSClass)parentForCheckingNsOrAccessModifier).isInterface()) {
        finalModifierProblem(attributeList, "javascript.validation.message.interface.members.cannot.be.final.modifiers");
      }
      else if (!(element instanceof JSFunction) ||
               (parent = element.getParent()) instanceof JSPackageStatement ||
               parent instanceof JSFile && parent.getContext() == null) {
        finalModifierProblem(attributeList, "javascript.validation.message.final.modifier.allowed.only.for.methods");
      }
    }

    if (attributeList.hasExplicitModifier(JSAttributeList.ModifierType.STATIC)) {
      if (element instanceof JSFunction || element instanceof JSVarStatement) {
        if (!(parentForCheckingNsOrAccessModifier instanceof JSClass)) {
          PsiElement modifierElement = attributeList.findModifierElement(JSAttributeList.ModifierType.STATIC);
          String message = JSBundle.message("javascript.validation.message.static.modifier.is.allowed.only.for.class.members");
          Annotation annotation;
          if (parentForCheckingNsOrAccessModifier instanceof JSFile) {
            annotation = myHolder.createWarningAnnotation(modifierElement, message);
          }
          else {
            annotation = myHolder.createErrorAnnotation(modifierElement, message);
          }

          annotation.registerFix(new RemoveASTNodeFix("javascript.fix.remove.static.modifier", modifierElement.getNode()));
        }
        else if (element instanceof JSFunction && ((JSFunction)element).isConstructor()) {
          modifierProblem(attributeList, JSAttributeList.ModifierType.STATIC,
                          "javascript.validation.message.constructor.cannot.be.static",
                          "javascript.fix.remove.static.modifier");
        }
      }
      else if (element instanceof JSNamespaceDeclaration || element instanceof JSClass) {
        modifierProblem(attributeList, JSAttributeList.ModifierType.STATIC,
                        "javascript.validation.message.static.modifier.is.allowed.only.for.class.members",
                        "javascript.fix.remove.static.modifier");
      }

      if (attributeList.hasModifier(JSAttributeList.ModifierType.FINAL) && element instanceof JSFunction) {
        finalModifierProblem(
          attributeList,
          "javascript.validation.message.static.method.cannot.be.final"
        );
      }
    }

    if (attributeList.hasModifier(JSAttributeList.ModifierType.OVERRIDE) &&
        !(element instanceof JSFunction)
      ) {
      modifierProblem(attributeList, JSAttributeList.ModifierType.OVERRIDE,
                      "javascript.validation.message.override.can.be.applied.to.method", "javascript.fix.remove.override.modifier");
    }

    if (attributeList.hasModifier(JSAttributeList.ModifierType.DYNAMIC) &&
        (!(element instanceof JSClass) || ((JSClass)element).isInterface())) {
      modifierProblem(attributeList, JSAttributeList.ModifierType.DYNAMIC, "javascript.validation.message.dynamic.can.be.applied.to.class",
                      "javascript.fix.remove.dynamic.modifier");
    }

    checkMultipleModifiersProblem(attributeList);
  }

  private void finalModifierProblem(JSAttributeList attributeList, String messageKey) {
    modifierProblem(attributeList, JSAttributeList.ModifierType.FINAL, messageKey, "javascript.fix.remove.final.modifier");
  }

  private void modifierProblem(JSAttributeList attributeList,
                               JSAttributeList.ModifierType modifierType,
                               String messageKey,
                               String removeFixNameKey) {
    PsiElement modifierElement = attributeList.findModifierElement(modifierType);
    String message = JSBundle.message(messageKey);
    Annotation annotation = myHolder.createErrorAnnotation(modifierElement, message);

    annotation.registerFix(new RemoveASTNodeFix(removeFixNameKey, modifierElement.getNode()));
  }

  private static boolean hasQualifiedName(PsiElement element) {
    String qName = element instanceof JSQualifiedNamedElement ? ((JSQualifiedNamedElement)element).getQualifiedName() : null;
    return qName != null && qName.indexOf('.') != -1;
  }

  private static final List<TokenSet> ourModifiersList = Arrays.asList(TokenSet.create(JSTokenTypes.DYNAMIC_KEYWORD),
                                                                       TokenSet.create(JSTokenTypes.STATIC_KEYWORD),
                                                                       TokenSet.create(JSTokenTypes.FINAL_KEYWORD),
                                                                       TokenSet.create(JSTokenTypes.OVERRIDE_KEYWORD),
                                                                       TokenSet.create(JSTokenTypes.VIRTUAL_KEYWORD));
  private static final String[] ourModifierFixIds = {"javascript.fix.remove.dynamic.modifier", "javascript.fix.remove.static.modifier",
    "javascript.fix.remove.final.modifier", "javascript.fix.remove.override.modifier", "javascript.fix.remove.virtual.modifier"};

  private void checkMultipleModifiersProblem(JSAttributeList attributeList) {
    final ASTNode node = attributeList.getNode();

    for (int i = 0; i < ourModifiersList.size(); ++i) {
      final ASTNode[] modifiers = node.getChildren(ourModifiersList.get(i));
      if (modifiers.length < 2) continue;
      String s = modifiers[0].getElementType().toString().toLowerCase(Locale.ENGLISH);
      final String type = s.substring(s.indexOf(':') + 1, s.indexOf('_'));
      for (ASTNode a : modifiers) {
        final Annotation errorAnnotation = createErrorAnnotation(a.getPsi(),
                                                                 JSBundle.message(
                                                                   "javascript.validation.message.attribute.was.specified.multiple.times",
                                                                   type),
                                                                 ProblemHighlightType.ERROR,
                                                                 myHolder);
        errorAnnotation.registerFix(new RemoveASTNodeFix(ourModifierFixIds[i], a));
      }
    }
  }

  @Nullable
  @Override
  protected LocalQuickFix getPreferredQuickFixForUnresolvedRef(final PsiElement nameIdentifier) {
    final JSCallExpression callExpression = PsiTreeUtil.getParentOfType(nameIdentifier, JSCallExpression.class);
    if (callExpression == null) return null;

    if (JSResolveUtil.isEventListenerCall(callExpression)) {
      final JSExpression[] params = callExpression.getArguments();

      if (params.length >= 2 && PsiTreeUtil.isAncestor(params[1], nameIdentifier, true)) {
        return new CreateJSEventMethod(nameIdentifier.getText(), new Computable<String>() {
          @Override
          public String compute() {
            PsiElement responsibleElement = null;
            if (params[0] instanceof JSReferenceExpression) {
              responsibleElement = ((JSReferenceExpression)params[0]).getQualifier();
            }

            return responsibleElement == null ? FlexCommonTypeNames.FLASH_EVENT_FQN : responsibleElement.getText();
          }
        });
      }
    }
    else if (needsFlexMobileViewAsFirstArgument(callExpression)) {
      final JSExpression[] params = callExpression.getArguments();

      if (params.length >= 1 && PsiTreeUtil.isAncestor(params[0], nameIdentifier, true)) {
        final String contextPackage = JSResolveUtil.getPackageNameFromPlace(callExpression);
        final String fqn = StringUtil.getQualifiedName(contextPackage, nameIdentifier.getText());

        final CreateFlexMobileViewIntentionAndFix fix = new CreateFlexMobileViewIntentionAndFix(fqn, nameIdentifier, true);
        fix.setCreatedClassFqnConsumer(new Consumer<String>() {
          public void consume(final String fqn) {
            final String packageName = StringUtil.getPackageName(fqn);
            if (StringUtil.isNotEmpty(packageName) && !packageName.equals(contextPackage)) {
              ImportUtils.doImport(nameIdentifier, fqn, true);
            }
          }
        });
        return fix;
      }
    }

    return null;
  }

  private static boolean needsFlexMobileViewAsFirstArgument(final JSCallExpression callExpression) {
    final JSExpression methodExpr = callExpression.getMethodExpression();
    final PsiElement function = methodExpr instanceof JSReferenceExpression ? ((JSReferenceExpression)methodExpr).resolve() : null;
    final PsiElement clazz =
      function instanceof JSFunction && ArrayUtil.contains(((JSFunction)function).getName(), "pushView", "replaceView")
      ? function.getParent()
      : null;
    return clazz instanceof JSClass && "spark.components.ViewNavigator".equals(((JSClass)clazz).getQualifiedName());
  }

  @Override
  public void visitJSAttribute(JSAttribute jsAttribute) {
    if ("Embed".equals(jsAttribute.getName())) {
      JSVarStatement varStatement = PsiTreeUtil.getParentOfType(jsAttribute, JSVarStatement.class);

      if (varStatement != null) {
        JSVariable var = ArrayUtil.getFirstElement(varStatement.getVariables());

        if (var != null) {
          String type = var.getTypeString();

          if (!"Class".equals(type) && !"String".equals(type)) {
            myHolder.createErrorAnnotation(jsAttribute,
                                           JSBundle.message("javascript.validation.message.embed.annotation.used.with.var.of.wrong.type"));
          }
        }
      }
    }

    JSSemanticHighlightingUtil.highlight(jsAttribute, myHolder);

    PsiReference psiReference = jsAttribute.getReference();
    if (psiReference != null && psiReference.resolve() == null) {
      myHolder.createWeakWarningAnnotation(
        jsAttribute.getNameIdentifier(),
        JSBundle.message("javascript.validation.message.unknown.metadata.annotation.used")
      );
    }
  }

  @Override
  public void visitJSNamespaceDeclaration(JSNamespaceDeclaration namespaceDeclaration) {
    final PsiElement initializer = namespaceDeclaration.getInitializer();
    if (initializer instanceof JSExpression) {
      PsiElement resolve;
      if (initializer instanceof JSLiteralExpression ||
          initializer instanceof JSReferenceExpression &&
          ((resolve = ((JSReferenceExpression)initializer).resolve()) instanceof JSNamespaceDeclaration ||
           resolve instanceof JSVariable &&
           "Namespace".equals(((JSVariable)resolve).getTypeString())
          )
        ) {
        // ok
      }
      else {
        createErrorAnnotation(
          initializer,
          JSBundle.message("javascript.namespace.initializer.should.be.string.or.another.namespace.reference"),
          ProblemHighlightType.ERROR,
          myHolder
        );
      }
    }
  }

  private void checkNamedObjectIsInCorrespondingFile(final JSNamedElement aClass) {
    final PsiFile containingFile = aClass.getContainingFile();

    if (containingFile.getContext() != null) return;
    final VirtualFile file = containingFile.getVirtualFile();

    if (file != null &&
        !file.getNameWithoutExtension().equals(aClass.getName()) &&
        ProjectRootManager.getInstance(containingFile.getProject()).getFileIndex().getSourceRootForFile(file) != null) {
      final ASTNode node = aClass.findNameIdentifier();

      if (node != null) {
        final String name = aClass.getName();
        String nameWithExtension = name + "." + file.getExtension();
        final String message = JSBundle.message(aClass instanceof JSClass
                                                ? "javascript.validation.message.class.should.be.in.file" :
                                                aClass instanceof JSNamespaceDeclaration
                                                ? "javascript.validation.message.namespace.should.be.in.file" :
                                                aClass instanceof JSVariable
                                                ? "javascript.validation.message.variable.should.be.in.file"
                                                : "javascript.validation.message.function.should.be.in.file", name, nameWithExtension);
        final Annotation annotation = myHolder.createErrorAnnotation(node, message);

        annotation.registerFix(new RenameFileFix(nameWithExtension));
        annotation.registerFix(new RenameElementFix(aClass) {
          final String text;
          final String familyName;

          {
            String term = message.substring(0, message.indexOf(' '));
            text = super.getText().replace("class", StringUtil.decapitalize(term));
            familyName = super.getFamilyName().replace("Class", term);
          }

          @NotNull
          @Override
          public String getText() {
            return text;
          }

          @NotNull
          @Override
          public String getFamilyName() {
            return familyName;
          }
        });
      }
    }

    checkFileUnderSourceRoot(aClass, new SimpleErrorReportingClient());
  }

  @Override
  protected void checkExpressionIsAssignableToVariable(JSVariable p,
                                                       final JSExpression expr,
                                                       PsiFile containingFile,
                                                       @PropertyKey(resourceBundle = JSBundle.BUNDLE) String problemKey,
                                                       boolean allowChangeVariableTypeFix) {
    final JSType type = p.getType();
    final String parameterTypeResolved = type.getResolvedTypeText();
    Pair<Annotation, String> annotationAndExprType =
      ValidateTypesUtil.checkExpressionIsAssignableToType(expr, type, myHolder, containingFile, problemKey,
                                                          allowChangeVariableTypeFix ? p : null);

    if (annotationAndExprType != null &&
        p.getParent() instanceof JSParameterList &&
        expr.getParent() instanceof JSArgumentList &&
        !JSCommonTypeNames.VOID_TYPE_NAME.equals(annotationAndExprType.second)) {
      JSFunction method = (JSFunction)p.getParent().getParent();
      JSFunction topMethod = JSInheritanceUtil.findTopMethods(method).iterator().next();
      annotationAndExprType.first.registerFix(new ChangeSignatureFix(topMethod, ((JSArgumentList)expr.getParent()).getArguments()));
    }

    PsiElement _fun;
    if (annotationAndExprType == null &&
        FUNCTION_CLASS_NAME.equals(parameterTypeResolved) &&
        p instanceof JSParameter &&
        "addEventListener".equals(((JSFunction)p.getParent().getParent()).getName()) &&
        (( expr instanceof JSReferenceExpression &&
           (_fun = ((JSReferenceExpression)expr).resolve()) instanceof JSFunction
         ) ||
         (
           expr instanceof JSFunctionExpression &&
           (_fun = ((JSFunctionExpression)expr).getFunction()) != null
         )
        )) {
      JSFunction fun = (JSFunction)_fun;
      JSParameterList parameterList = fun.getParameterList();

      if (parameterList != null) {
        JSParameter[] parameters = parameterList.getParameters();
        boolean invalidArgs = parameters.length == 0;

        if (!invalidArgs && parameters.length > 1) {
          for(int i = parameters.length - 1; i > 0; --i) {
            if (!parameters[i].isRest() && parameters[i].getInitializer() == null) {
              invalidArgs = true;
              break;
            }
          }
        }

        Computable.NotNullCachedComputable<JSParameterList> expectedParameterListForEventListener =
          new Computable.NotNullCachedComputable<JSParameterList>() {
            @NotNull
            @Override
            protected JSParameterList internalCompute() {
              JSClass jsClass = calcNontrivialExpectedEventType(expr);
              ASTNode treeFromText =
                JSChangeUtil.createJSTreeFromText(
                  expr.getProject(),
                  "function f(event:" + (jsClass != null ? jsClass.getQualifiedName() : FlexCommonTypeNames.FLASH_EVENT_FQN) + ") {}",
                  JavaScriptSupportLoader.ECMA_SCRIPT_L4
                );
              return ((JSFunction)treeFromText.getPsi()).getParameterList();
            }
          };

        if (invalidArgs) {
          PsiElement expr_;
          if (expr instanceof JSFunctionExpression) {
            expr_ = ((JSFunctionExpression)expr).getParameterList();
          }
          else {
            expr_ = expr;
          }
          registerProblem(
            expr_,
            JSBundle.message("javascript.callback.signature.mismatch"),
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING, myHolder, JSValidateTypesInspection.SHORT_NAME,
            new ChangeSignatureFix(fun, expectedParameterListForEventListener)
          );
        } else {
          final JSClass expectedEventClass = calcNontrivialExpectedEventType(expr);
          final String actualParameterType = parameters[0].getType().getResolvedTypeText();

          if (expectedEventClass == null) {
            if (!JSResolveUtil.isAssignableType(FlexCommonTypeNames.FLASH_EVENT_FQN, actualParameterType, parameters[0]) &&
                !JSResolveUtil.isAssignableType(FlexCommonTypeNames.STARLING_EVENT_FQN, actualParameterType, parameters[0])) {
              JSAnnotatingVisitor.registerProblem(
                expr instanceof JSFunctionExpression ? parameters[0] : expr,
                JSBundle.message("javascript.callback.signature.mismatch"),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING, myHolder, JSValidateTypesInspection.SHORT_NAME,
                new ChangeSignatureFix(fun, expectedParameterListForEventListener)
              );
            }
          }
          else {
            if (!JSResolveUtil.isAssignableType(actualParameterType, expectedEventClass.getQualifiedName(), parameters[0])) {
              JSAnnotatingVisitor.registerProblem(
                expr instanceof JSFunctionExpression ? parameters[0] : expr,
                JSBundle.message("javascript.callback.signature.mismatch.event.class", expectedEventClass.getQualifiedName()),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING, myHolder, JSValidateTypesInspection.SHORT_NAME,
                new ChangeSignatureFix(fun, expectedParameterListForEventListener)
              );
            }
          }
        }
      }
    }
  }

  private static @Nullable JSClass calcNontrivialExpectedEventType(JSExpression expr) {
    JSExpression prevExpr = PsiTreeUtil.findChildOfAnyType(expr.getParent(), JSExpression.class);

    String type = null;
    JSExpression adHocQualifierExpr = null;

    if (prevExpr instanceof JSReferenceExpression && prevExpr != expr) {
      PsiElement constantRef = ((JSReferenceExpression)prevExpr).resolve();

      if (constantRef instanceof JSVariable) {
        final String initializerText = ((JSVariable)constantRef).getInitializerText();
        if (initializerText != null &&
            (StringUtil.startsWith(initializerText, "\'") ||
             StringUtil.startsWith(initializerText, "\"")
            )) {
          type = StringUtil.stripQuotesAroundValue(initializerText);
        }
      }

      adHocQualifierExpr = ((JSReferenceExpression)prevExpr).getQualifier();
    } else if (prevExpr instanceof JSLiteralExpression) {
      type = StringUtil.stripQuotesAroundValue(prevExpr.getText());
    }

    if (type != null) {
      JSExpression methodExpression = ((JSCallExpression)expr.getParent().getParent()).getMethodExpression();
      if (methodExpression instanceof JSReferenceExpression) {
        JSClass clazz = ActionScriptSmartCompletionContributor.findClassOfQualifier((JSReferenceExpression)methodExpression);

        if (clazz != null) {
          Map<String,String> eventsMap = ActionScriptSmartCompletionContributor.getEventsMap(clazz);
          String qName = eventsMap.get(type);
          if (qName != null) {
            PsiElement classFromNamespace = JSClassBase.findClassFromNamespace(qName, clazz);
            if (classFromNamespace instanceof JSClass) return (JSClass)classFromNamespace;
            // if uncomment next 2 lines then the following event listener parameter won't be highlighted with warning
            // new Sprite().addEventListener(ErrorEvent.ERROR, function(e:AccelerometerEvent):void{})
            //} else if (JSInheritanceUtil.isParentClass(clazz, "flash.events.EventDispatcher", true)) {
            //  adHocQualifierExpr = null;
          }
        }
      }
    }

    if (adHocQualifierExpr instanceof JSReferenceExpression) {
      PsiElement resolve = ((JSReferenceExpression)adHocQualifierExpr).resolve();
      if (resolve instanceof JSClass) {
        JSClass clazz = (JSClass)resolve;
        if (JSInheritanceUtil.isParentClass((JSClass)resolve, FlexCommonTypeNames.FLASH_EVENT_FQN, false) ||
            JSInheritanceUtil.isParentClass((JSClass)resolve, FlexCommonTypeNames.STARLING_EVENT_FQN, false)) {
          return clazz;
        }
      }
    }

    return null;
  }

  protected void validateGetPropertyReturnType(ASTNode nameIdentifier, JSFunction function, String typeString) {
    if (VOID_TYPE_NAME.equals(typeString)) {
      // TODO: fix!
      myHolder.createErrorAnnotation(
        typeString != null ?
        function.getReturnTypeElement() :
        nameIdentifier.getPsi(),
        JSBundle
          .message("javascript.validation.message.get.method.should.be.valid.type", typeString != null ? typeString : "empty"));
    }
    else {
      PsiElement element = JSResolveUtil.findParent(function);

      if (element instanceof JSClass && !isBindable((JSClass)element)) {
        JSFunction setter = ((JSClass)element).findFunctionByNameAndKind(function.getName(), JSFunction.FunctionKind.SETTER);

        if (setter != null) {
          JSParameterList setterParameterList = setter.getParameterList();
          JSParameter[] setterParameters = setterParameterList != null ?
                                           setterParameterList.getParameters() : JSParameter.EMPTY_ARRAY;

          String setterType;
          if (setterParameters.length == 1 &&
              !JSCommonTypeNames.ANY_TYPE.equals(setterType = setterParameters[0].getTypeString()) &&
              !JSCommonTypeNames.ANY_TYPE.equals(typeString) &&
              !compatibleType(setterType, typeString, setterParameters[0], function)) {
            PsiElement typeElement = function.getReturnTypeElement();

            myHolder.createErrorAnnotation(
              typeElement != null ? typeElement : function.findNameIdentifier().getPsi(),
              JSBundle.message("javascript.validation.message.get.method.type.is.different.from.setter",
                               setterType != null ? setterType : "empty")
            );
          }

          checkAccessorAccessTypeMatch(function, setter,
                                       "javascript.validation.message.get.method.access.type.is.different.from.setter");
        }
      }
    }
  }

  protected void validateRestParameterType(JSParameter parameter) {
    PsiElement typeElement = parameter.getTypeElement();
    if (typeElement != null && !"Array".equals(typeElement.getText())) {
      final Pair<ASTNode, ASTNode> nodesBefore = getNodesBefore(typeElement, JSTokenTypes.COLON);
      myHolder.createErrorAnnotation(
        typeElement,
        JSBundle.message("javascript.validation.message.unexpected.type.for.rest.parameter")
      ).registerFix(new RemoveASTNodeFix("javascript.fix.remove.type.reference", false, nodesBefore.first, nodesBefore.second));
    }
  }

  @Override
  protected boolean addCreateFromUsageFixes(JSReferenceExpression node,
                                            ResolveResult[] resolveResults,
                                            List<LocalQuickFix> fixes,
                                            boolean inTypeContext,
                                            boolean ecma) {
    final PsiElement nodeParent = node.getParent();
    final JSExpression qualifier = node.getQualifier();
    PsiElement nameIdentifier = node.getReferenceNameElement();
    final String referencedName = nameIdentifier.getText();

    inTypeContext = super.addCreateFromUsageFixes(node, resolveResults, fixes, inTypeContext, ecma);
    if (!(nodeParent instanceof JSArgumentList) && nodeParent.getParent() instanceof JSCallExpression) {
      inTypeContext = true;
    }

    if (!inTypeContext) {
      boolean getter = !(node.getParent() instanceof JSDefinitionExpression);
      String invokedName = nameIdentifier.getText();
      fixes.add(new CreateJSPropertyAccessorIntentionAction(invokedName, getter));
    }
    if (qualifier == null) {
      boolean canHaveTypeFix = false;
      JSClass contextClass = JSResolveUtil.getClassOfContext(node);

      if (nodeParent instanceof JSReferenceList) {
        canHaveTypeFix = true;
        fixes.add(new CreateClassOrInterfaceFix(node, contextClass.isInterface() ||
                                                      node.getParent().getNode().getElementType() ==
                                                      JSStubElementTypes.IMPLEMENTS_LIST, null, null));
      }
      else if (!(nodeParent instanceof JSDefinitionExpression) && resolveResults.length == 0) {
        canHaveTypeFix = true;
        fixes.add(new CreateClassOrInterfaceFix(node, false, null, null));
        fixes.add(new CreateClassOrInterfaceFix(node, true, null, null));
      }

      if (!inTypeContext && JSReadWriteAccessDetector.ourInstance.getExpressionAccess(node) == ReadWriteAccessDetector.Access.Read) {
        canHaveTypeFix = true;
        fixes.add(new CreateJSFunctionIntentionAction(referencedName, true));
      }

      if (canHaveTypeFix) fixes.add(new AddImportECMAScriptClassOrFunctionAction(null, node));
    }
    else if (canHaveImportTo(resolveResults)) {
      fixes.add(new AddImportECMAScriptClassOrFunctionAction(null, node));
    }
    return inTypeContext;
  }

  @Override
  protected void addCreateFromUsageFixesForCall(JSCallExpression node,
                                                JSReferenceExpression referenceExpression,
                                                ResolveResult[] resolveResults,
                                                List<LocalQuickFix> quickFixes) {
    if (canHaveImportTo(resolveResults)) {
      quickFixes.add(new AddImportECMAScriptClassOrFunctionAction(null, referenceExpression));
    }

    super.addCreateFromUsageFixesForCall(node, referenceExpression, resolveResults, quickFixes);
  }

  private static boolean canHaveImportTo(ResolveResult[] resolveResults) {
    if (resolveResults.length == 0) return true;
    for (ResolveResult r : resolveResults) {
      if (!r.isValidResult()) {
        if (r instanceof JSResolveResult &&
            ((JSResolveResult)r).getResolveProblemKey() == JSResolveResult.QUALIFIED_NAME_IS_NOT_IMPORTED) {
          return true;
        }
        continue;
      }
      PsiElement element = r.getElement();
      if (element instanceof JSClass) return true;
      if (element instanceof JSFunction) {
        if (((JSFunction)element).isConstructor()) return true;
      }
    }
    return false;
  }

  @Override
  protected boolean suggestCreateVarFromUsage(JSReferenceExpression node) {
    JSExpression qualifier = node.getQualifier();
    JSClass targetClass = null;
    if (qualifier == null) {
      targetClass = JSResolveUtil.getClassOfContext(node);
    }
    else if (qualifier instanceof JSReferenceExpression) {
      final JSClass clazz = JSResolveUtil.findClassOfQualifier(qualifier, node.getContainingFile());
      if (clazz != null) {
        targetClass = clazz;
      }
    }

    if (targetClass != null) {
      return !targetClass.isInterface();
    }
    return true;
  }
}
