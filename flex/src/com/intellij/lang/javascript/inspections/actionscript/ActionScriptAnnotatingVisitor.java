package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInsight.daemon.impl.quickfix.RenameElementFix;
import com.intellij.codeInsight.daemon.impl.quickfix.RenameFileFix;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.highlighting.JSFixFactory;
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingUtil;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.index.JSTypeEvaluateManager;
import com.intellij.lang.javascript.inspections.JSClosureCompilerSyntaxInspection;
import com.intellij.lang.javascript.inspections.actionscript.fixes.ActionScriptConstructorChecker;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeImpl;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeListImpl;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSPackageStatementImpl;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSTypeImpl;
import com.intellij.lang.javascript.psi.types.primitives.JSStringType;
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType;
import com.intellij.lang.javascript.validation.ActionScriptImplementedMethodProcessor;
import com.intellij.lang.javascript.validation.DuplicatesCheckUtil;
import com.intellij.lang.javascript.validation.JSAnnotatorProblemReporter;
import com.intellij.lang.javascript.validation.TypedJSAnnotatingVisitor;
import com.intellij.lang.javascript.validation.fixes.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTagChild;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.*;

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

  public ActionScriptAnnotatingVisitor(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    super(psiElement, holder);
  }

  @NotNull
  @Override
  protected ActionScriptConstructorChecker createConstructorChecker() {
    return new ActionScriptConstructorChecker(myProblemReporter);
  }

  protected static SignatureMatchResult checkCompatibleSignature(final JSFunction fun, final JSFunction override) {
    JSParameterList nodeParameterList = fun.getParameterList();
    JSParameterList overrideParameterList = override.getParameterList();
    final JSParameter[] parameters = nodeParameterList != null ? nodeParameterList.getParameterVariables() : JSParameter.EMPTY_ARRAY;
    final JSParameter[] overrideParameters =
      overrideParameterList != null ? overrideParameterList.getParameterVariables() : JSParameter.EMPTY_ARRAY;

    SignatureMatchResult result = parameters.length != overrideParameters.length ?
                                  SignatureMatchResult.PARAMETERS_DIFFERS : SignatureMatchResult.COMPATIBLE_SIGNATURE;

    if (result == SignatureMatchResult.COMPATIBLE_SIGNATURE) {
      for (int i = 0; i < parameters.length; ++i) {
        if (!compatibleType(overrideParameters[i].getTypeString(), parameters[i].getTypeString(), overrideParameterList,
                            nodeParameterList) ||
            overrideParameters[i].hasInitializer() != parameters[i].hasInitializer()
          ) {
          result = SignatureMatchResult.PARAMETERS_DIFFERS;
          break;
        }
      }
    }

    if (result == SignatureMatchResult.COMPATIBLE_SIGNATURE) {
      if (!compatibleType(override.getReturnTypeString(), fun.getReturnTypeString(), override, fun)) {
        result = SignatureMatchResult.RETURN_TYPE_DIFFERS;
      }
    }

    if (result == SignatureMatchResult.COMPATIBLE_SIGNATURE) {
      if (override.getKind() != fun.getKind()) result = SignatureMatchResult.FUNCTION_KIND_DIFFERS;
    }
    return result;
  }

  /**
   * @deprecated use {@link com.intellij.lang.javascript.psi.JSTypeUtils
   * #areTypesCompatible(com.intellij.lang.javascript.psi.JSType, com.intellij.lang.javascript.psi.JSType)} instead.
   */
  @Deprecated
  protected static boolean compatibleType(String overrideParameterType,
                                          String parameterType,
                                          PsiElement overrideContext,
                                          PsiElement funContext) {
    // TODO: This should be more accurate
    if (overrideParameterType != null && !overrideParameterType.equals(parameterType)) {
      parameterType = JSImportHandlingUtil.resolveTypeName(parameterType, funContext);
      overrideParameterType = JSImportHandlingUtil.resolveTypeName(overrideParameterType, overrideContext);

      if (!overrideParameterType.equals(parameterType)) {
        if (parameterType != null &&  // TODO: getter / setter to have the same types
            (JSTypeEvaluateManager.isArrayType(overrideParameterType) &&
             JSTypeEvaluateManager.getBaseArrayType(overrideParameterType).equals(parameterType) ||
             JSTypeEvaluateManager.isArrayType(parameterType) ||
             JSTypeEvaluateManager.getBaseArrayType(parameterType).equals(overrideParameterType))
          ) {
          return true;
        }
        return false;
      }
      return true;
    }
    else if (overrideParameterType == null && parameterType != null && !"*".equals(parameterType)) {
      return false;
    }

    return true;
  }

  @NotNull
  @Override
  protected JSAnnotatorProblemReporter createProblemReporter(PsiElement context) {
    return new JSAnnotatorProblemReporter(myHolder) {
      @Nullable
      @Override
      protected String getAnnotatorInspectionId() {
        return null;
      }
    };
  }

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
      if (parent == null) return; // EA-90191

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

  protected static JSChangeSignatureFix createChangeBaseMethodSignatureFix(final JSFunction superMethod, final JSFunction override) {
    JSType type = override.getReturnType();
    String s = StringUtil.notNullize(type != null ? type.getResolvedTypeText() : null);
    JSChangeSignatureFix fix = new JSChangeSignatureFix(superMethod);
    fix.setReturnType(s);
    return fix;
  }

  @Override
  public void visitJSAttributeNameValuePair(@NotNull final JSAttributeNameValuePair attributeNameValuePair) {
    final boolean ok = checkReferences(attributeNameValuePair);

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
        final Collection<String> resolvedBaseClasses = new ArrayList<>();
        final GlobalSearchScope scope = JSResolveUtil.getResolveScope(attributeNameValuePair);

        for (String baseClassFqn : StringUtil.split(baseClassFqns, ",")) {
          if ("Object".equals(baseClassFqn)) {
            correctClass = true;
            break;
          }

          final PsiElement baseClass = ActionScriptClassResolver.findClassByQNameStatic(baseClassFqn, attributeNameValuePair);

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
  public void visitJSIncludeDirective(@NotNull final JSIncludeDirective includeDirective) {
    checkReferences(includeDirective);
  }

  @Override
  protected void checkImplementedMethods(@NotNull JSClass jsClass, ErrorReportingClient reportingClient) {
    checkActionScriptImplementedMethods(jsClass, reportingClient);
  }

  public static void checkActionScriptImplementedMethods(@NotNull final JSClass jsClass, final ErrorReportingClient reportingClient) {
    final JSCollectMembersToImplementProcessor implementedMethodProcessor = new ActionScriptImplementedMethodProcessor(jsClass) {
      ImplementMethodsFix implementMethodsFix = null;

      @Override
      protected void addNonImplementedFunction(final JSFunction function) {
        final ASTNode node = myJsClass.findNameIdentifier();
        if (node == null) return;
        if (implementMethodsFix == null) implementMethodsFix = new ImplementMethodsFix(myJsClass);
        implementMethodsFix.addElementToProcess(function);
        String messageId = JSClosureCompilerSyntaxInspection.getNotImplementedTextId(false, function.isGetProperty(),
                           function.isSetProperty());
        String message = JSBundle.message(messageId,
                                          function.getName(),
                                          ((JSClass)JSResolveUtil.findParent(function)).getQualifiedName());
        reportingClient.reportError(node, message,
                                    ErrorReportingClient.ProblemKind.ERROR,
                                    implementMethodsFix);
      }

      @Override
      protected void addImplementedFunction(final JSFunction interfaceFunction, final JSFunction implementationFunction) {
        final JSAttributeList attributeList = implementationFunction.getAttributeList();
        if (attributeList == null || attributeList.getAccessType() != JSAttributeList.AccessType.PUBLIC) {
          final ASTNode node = findElementForAccessModifierError(implementationFunction, attributeList);
          reportingClient.reportError(node,
                                      JSBundle.message("javascript.validation.message.interface.method.invalid.access.modifier"),
                                      ErrorReportingClient.ProblemKind.ERROR,
                                      JSFixFactory.getInstance().createChangeVisibilityFix(implementationFunction, JSAttributeList.AccessType.PUBLIC, null)
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
            addNonImplementedFunction(interfaceFunction);
            return;
          }

          if (incompatibleSignature == SignatureMatchResult.PARAMETERS_DIFFERS) {
            final JSParameterList parameterList = implementationFunction.getParameterList();
            final JSParameterList expectedParameterList = interfaceFunction.getParameterList();

            JSChangeSignatureFix changeSignatureFix = new JSChangeSignatureFix(interfaceFunction, parameterList);
            reportingClient.reportError(parameterList.getNode(),
                                        JSBundle.message(
                                          "javascript.validation.message.interface.method.invalid.signature",
                                          expectedParameterList != null ? expectedParameterList.getText() : "()"
                                        ),
                                        ErrorReportingClient.ProblemKind.ERROR,
                                        new JSChangeSignatureFix(implementationFunction, expectedParameterList, false) {
                                          @Override
                                          @NotNull
                                          public String getText() {
                                            return JSBundle.message("javascript.fix.message.change.parameters.to.expected");
                                          }
                                        },
                                        changeSignatureFix);
          }
          else if (incompatibleSignature == SignatureMatchResult.RETURN_TYPE_DIFFERS) {
            PsiElement implementationReturnTypeExpr = implementationFunction.getReturnTypeElement();
            JSType type = interfaceFunction.getReturnType();
            final String interfaceReturnType = type != null ? type.getResolvedTypeText() : null;
            String msg = JSBundle
              .message("javascript.validation.message.interface.method.invalid.signature2", StringUtil.notNullize(interfaceReturnType));
            reportingClient.reportError(
              implementationReturnTypeExpr != null ? implementationReturnTypeExpr.getNode() : implementationFunction.findNameIdentifier(),
              msg, ErrorReportingClient.ProblemKind.ERROR,
              new JSChangeTypeFix(implementationFunction, interfaceReturnType, "javascript.fix.message.change.return.type.to.expected"),
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
    JSResolveUtil.processInterfaceMembers(jsClass, implementedMethodProcessor);
  }

  @Override
  protected void checkFunctionDeclaration(@NotNull final JSFunction node) {
    super.checkFunctionDeclaration(node);

    final ASTNode nameIdentifier = node.findNameIdentifier();
    if (nameIdentifier == null) return;
    PsiElement parent = node.getParent();

    if (parent instanceof JSFile) {
      parent = JSResolveUtil.getClassReferenceForXmlFromContext(parent);
      final String name = node.getName();

      if (parent instanceof JSClass &&
          name != null &&
          name.equals(((JSClass)parent).getName()) &&
          !isNative(node) &&
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

        final Ref<JSFunction> set = new Ref<>();
        boolean b = JSResolveUtil.iterateType(node, parent, qName, new JSOverrideHandler() {
          @Override
          public boolean process(@NotNull final List<JSPsiElementBase> elements, final PsiElement scope, final String className) {
            //noinspection StringEquality
            if (qName == className || qName != null && qName.equals(className)) return true;
            JSFunction value = (JSFunction)elements.iterator().next();
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
            else if (!hasOverride && (holder = myHighlighter.getDialectOptionsHolder()) != null && holder.isECMA4) {
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
        }, true);

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
            IntentionAction fix;
            if (overrideNs != null) {
              newVisibility = overrideNs;
              fix = JSFixFactory.getInstance().createChangeVisibilityFix(node, null ,overrideNs);
            }
            else {
              newVisibility = JSFormatUtil.formatVisibility(overrideAttrList.getAccessType());
              fix = JSFixFactory.getInstance().createChangeVisibilityFix(node, overrideAttrList.getAccessType() ,null);
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

            annotation.registerFix(new JSChangeSignatureFix(node, overrideParameterList, false) {
              @Override
              @NotNull
              public String getText() {
                return JSBundle.message("javascript.fix.message.change.parameters.to.expected");
              }
            });
            annotation.registerFix(new JSChangeSignatureFix(override, nodeParameterList));
          }
          else if (incompatibleSignature == SignatureMatchResult.RETURN_TYPE_DIFFERS) {
            PsiElement returnTypeExpr = node.getReturnTypeElement();
            JSType type = override.getReturnType();
            final String baseReturnType = type != null ? type.getResolvedTypeText() : null;
            String msg = JSBundle
              .message("javascript.validation.message.function.override.incompatible.signature2", StringUtil.notNullize(baseReturnType));
            final Annotation annotation =
              myHolder.createErrorAnnotation(returnTypeExpr != null ? returnTypeExpr.getNode() : node.findNameIdentifier(), msg);
            annotation.registerFix(new JSChangeTypeFix(node, baseReturnType,
                                                       "javascript.fix.message.change.return.type.to.expected"));
            annotation.registerFix(createChangeBaseMethodSignatureFix(override, node));
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
  }

  private static boolean isNative(final JSFunction function) {
    final JSAttributeList attributeList = function.getAttributeList();
    return attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.NATIVE);
  }

  private void reportStaticMethodProblem(JSAttributeList attributeList, String key) {
    final ASTNode astNode = attributeList.getNode().findChildByType(JSTokenTypes.STATIC_KEYWORD);
    final Annotation annotation =
      myHolder.createErrorAnnotation(astNode, JSBundle.message(key));
    annotation.registerFix(new RemoveASTNodeFix("javascript.fix.remove.static.modifier", astNode));
  }


  private static class AddOverrideIntentionAction implements IntentionAction {
    private final JSFunction myNode;

    AddOverrideIntentionAction(final JSFunction node) {
      myNode = node;
    }

    @Override
    @NotNull
    public String getText() {
      return JSBundle.message("javascript.fix.add.override.modifier");
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return getText();
    }

    @Override
    public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
      return myNode.isValid();
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      JSAttributeListWrapper w = new JSAttributeListWrapper(myNode);
      w.overrideModifier(JSAttributeList.ModifierType.OVERRIDE, true);
      w.applyTo(myNode);
    }

    @Override
    public boolean startInWriteAction() {
      return true;
    }
  }

  @Override
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
        @Override
        @NotNull
        public String getText() {
          return JSBundle.message("javascript.fix.package.name", expected);
        }

        @Override
        @NotNull
        public String getFamilyName() {
          return getText();
        }

        @Override
        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
          return packageStatement.isValid();
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
          JSPackageStatementImpl.doChangeName(project, packageStatement, expected);
        }

        @Override
        public boolean startInWriteAction() {
          return true;
        }
      });
    }

    final Set<JSNamedElement> elements = new THashSet<>();

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
          DuplicatesCheckUtil.checkDuplicates((JSNamespaceDeclaration)parent, myProblemReporter);
        }

        if (parent instanceof JSClass) {
          final JSClass jsClass = (JSClass)parent;
          final JSFunction constructor = jsClass.getConstructor();
          if (constructor == null) createConstructorChecker().checkMissedConstructor(jsClass);

          PsiElement clazzParent = jsClass.getParent();
          final PsiElement context = clazzParent.getContext();
          boolean clazzParentIsInjectedJsFile = clazzParent instanceof JSFile &&
                                                (context instanceof XmlAttributeValue || context instanceof XmlText) &&
                                                !XmlBackedJSClassImpl.isImplementsAttribute((JSFile)clazzParent);

          if (PsiTreeUtil.getParentOfType(jsClass, JSFunction.class, JSClass.class) != null || clazzParentIsInjectedJsFile) {
            myHolder.createErrorAnnotation(node, JSBundle.message("javascript.validation.message.nested.classes.are.not.allowed"));
          }
          checkClass(jsClass);
        }
      }
    }

    JSFunction fun;
    if (JSSymbolUtil.isAccurateReferenceExpressionName(node, JSFunction.ARGUMENTS_VAR_NAME) &&
        (fun = PsiTreeUtil.getParentOfType(node, JSFunction.class)) != null &&
        node.resolve() instanceof ImplicitJSVariableImpl) {

      JSParameterList parameterList = fun.getParameterList();
      if (parameterList != null) {
        for (JSParameter p : parameterList.getParameterVariables()) {
          if (p.isRest()) {
            myHolder.createErrorAnnotation(node, JSBundle.message("javascript.validation.message.arguments.with.rest.parameter"));
          }
        }
      }
    }
  }

  private void checkClass(JSClass jsClass) {
    if (!jsClass.isInterface()) {
      checkIfExtendsFinalOrMultipleClasses(jsClass);
    }
    DuplicatesCheckUtil.checkDuplicates(jsClass, myProblemReporter);
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
          final JSExpression[] referencesToSuper = extendsList.getExpressions();
          if (referencesToSuper.length == 1) {
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
      else if (JSResolveUtil.isConstructorFunction(element)) {
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
        else if (JSResolveUtil.isConstructorFunction(element)) {
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
        final Annotation errorAnnotation = JSAnnotatorProblemReporter.createErrorAnnotation(a.getPsi(),
                                                                                            JSBundle.message(
                                                                                              "javascript.validation.message.attribute.was.specified.multiple.times",
                                                                                              type),
                                                                                            ProblemHighlightType.ERROR,
                                                                                            myHolder
        );
        errorAnnotation.registerFix(new RemoveASTNodeFix(ourModifierFixIds[i], a));
      }
    }
  }

  @Override
  public void visitJSAttribute(JSAttribute jsAttribute) {
    if ("Embed".equals(jsAttribute.getName())) {
      JSVarStatement varStatement = PsiTreeUtil.getParentOfType(jsAttribute, JSVarStatement.class);

      if (varStatement != null) {
        JSVariable var = ArrayUtil.getFirstElement(varStatement.getVariables());

        if (var != null) {
          JSType type = var.getType();

          if (!(type instanceof JSStringType) &&
              !(type instanceof JSTypeImpl && "Class".equals(type.getTypeText(JSType.TypeTextFormat.SIMPLE)))) {
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
        JSAnnotatorProblemReporter.createErrorAnnotation(
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
  protected void validateSetter(@NotNull JSFunction setter,
                                @NotNull JSFunction getter,
                                JSParameterListElement param,
                                JSType setterType,
                                JSType retType) {
    super.validateSetter(setter, getter, param, setterType, retType);
    checkAccessorAccessTypeMatch(setter, getter, "actionscript.validation.message.set.method.access.type.is.different.from.getter");
  }


  @Override
  protected void validateGetter(@NotNull JSFunction getter, JSFunction setter, JSType type) {
    if (type instanceof JSVoidType) {
      // TODO: fix!
      final String typeString = type != null ? type.getTypeText(JSType.TypeTextFormat.PRESENTABLE) : "empty";
      myHolder.createErrorAnnotation(
        type != null ?
        getter.getReturnTypeElement() :
        getPlaceForNamedElementProblem(getter),
        JSBundle
          .message("javascript.validation.message.get.method.should.be.valid.type", typeString));
    }
    else {
        if (setter != null) {
          JSParameterList setterParameterList = setter.getParameterList();
          JSParameter[] setterParameters = setterParameterList != null ?
                                           setterParameterList.getParameterVariables() : JSParameter.EMPTY_ARRAY;

          JSType setterType;
          if (setterParameters.length == 1 &&
              !((setterType = setterParameters[0].getType()) instanceof JSAnyType) &&
              !(type instanceof JSAnyType) &&
              !JSTypeUtils.areTypesCompatible(setterType, type,  null, getter)) {
            PsiElement typeElement = getter.getReturnTypeElement();

            myHolder.createErrorAnnotation(
              typeElement != null ? typeElement : getPlaceForNamedElementProblem(getter),
              JSBundle.message("javascript.validation.message.get.method.type.is.different.from.setter",
                               setterType != null ? setterType.getTypeText(JSType.TypeTextFormat.PRESENTABLE) : "empty")
            );
          }

          checkAccessorAccessTypeMatch(getter, setter,
                                       "actionscript.validation.message.get.method.access.type.is.different.from.setter");
        }
    }
  }

  @Override
  protected void validateRestParameterType(JSParameterListElement parameter) {
    PsiElement typeElement = parameter.getTypeElement();
    if (typeElement != null && !"Array".equals(typeElement.getText())) {
      final Pair<ASTNode, ASTNode> nodesBefore = getNodesBefore(typeElement, JSTokenTypes.COLON);
      myHolder.createErrorAnnotation(
        typeElement,
        JSBundle.message("javascript.validation.message.unexpected.type.for.rest.parameter")
      ).registerFix(JSFixFactory.getInstance().removeASTNodeFix("javascript.fix.remove.type.reference", false, nodesBefore.first, nodesBefore.second));
    }
  }

  @Override
  public void visitJSThisExpression(final JSThisExpression node) {
    checkClassReferenceInStaticContext(node, "javascript.validation.message.this.referenced.from.static.context");
  }

  private void checkClassReferenceInStaticContext(final JSExpression node, @PropertyKey(resourceBundle = JSBundle.BUNDLE) String key) {
    PsiElement element =
      PsiTreeUtil.getParentOfType(node, JSExecutionScope.class, JSClass.class, JSObjectLiteralExpression.class);

    if (element instanceof JSFunction) {
      final JSFunction function = (JSFunction)element;

      final JSAttributeList attributeList = function.getAttributeList();
      if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) {
        myHolder.createErrorAnnotation(node, JSBundle.message(key));
        return;
      }
    }

    if (node instanceof JSSuperExpression) {
      if (element instanceof JSObjectLiteralExpression) {
        element = PsiTreeUtil.getParentOfType(node, JSExecutionScope.class, JSClass.class);
      }

      if(element == null ||
         !(element instanceof JSClass) &&
         !(JSResolveUtil.findParent(element) instanceof JSClass)
        ) {
        final String message = JSBundle.message("javascript.validation.message.super.referenced.without.class.instance.context");
        myHolder.createErrorAnnotation(node, message);
      }
    }
  }

  @Override
  public void visitJSSuperExpression(final JSSuperExpression node) {
    checkClassReferenceInStaticContext(node, "javascript.validation.message.super.referenced.from.static.context");
  }

  @Override
  public void visitJSReturnStatement(@NotNull JSReturnStatement node) {
    super.visitJSReturnStatement(node);

    final PsiElement element =
      PsiTreeUtil.getParentOfType(node, JSFunction.class, XmlTagChild.class, XmlAttributeValue.class, JSFile.class);

    if (element instanceof JSFunction) {
      JSExpression returnedExpr = node.getExpression();
       if (returnedExpr != null && ((JSFunction)element).isConstructor() && JSResolveUtil.findParent(element) instanceof JSClass) {
         final String message = FlexBundle.message("javascript.validation.message.no.return.value.required.for.constructor");
         myHolder.createErrorAnnotation(returnedExpr, message);
       }
    }
  }

  @Override
  protected boolean isConstNeedInitializer(JSVariable var) {
    return true;
  }

  @Override
  protected ProblemHighlightType getHighlightTypeForTypeOrSignatureProblem(@NotNull PsiElement node) {
    return ProblemHighlightType.GENERIC_ERROR;
  }
}
