package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInsight.daemon.impl.quickfix.RenameElementFix;
import com.intellij.codeInsight.daemon.impl.quickfix.RenameFileFix;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.ASTNode;
import com.intellij.lang.actionscript.highlighting.ActionScriptSemanticHighlightingUtil;
import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.highlighting.JSFixFactory;
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
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.*;
import java.util.stream.Stream;

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
  @ApiStatus.ScheduledForRemoval(inVersion = "2021.3")
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
      client.reportError(nameIdentifier.getNode(), FlexBundle.message("javascript.validation.message.file.should.be.under.source.root"),
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
                               FlexBundle.message("javascript.validation.message.more.than.one.named.object.in.package"),
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

          myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle
            .message("javascript.expected.class.or.descendant", classesForMessage)).range(calcRangeForReferences(lastReference)).create();
        }
      }
      else if (resolved !=
               attributeNameValuePair) { // for some reason int and uint are resolved to self-reference JSResolveUtil.MyResolveResult() instead of usual JSClass
        myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle
          .message("javascript.qualified.class.name.expected")).range(attributeNameValuePair.getValueNode()).create();
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
        String message = JavaScriptBundle.message(messageId,
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
                                      JavaScriptBundle.message("javascript.validation.message.interface.method.invalid.access.modifier"),
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
                                        JavaScriptBundle.message(
                                          "javascript.validation.message.interface.method.invalid.signature",
                                          expectedParameterList != null ? expectedParameterList.getText() : "()"
                                        ),
                                        ErrorReportingClient.ProblemKind.ERROR,
                                        new JSChangeSignatureFix(implementationFunction, expectedParameterList, false) {
                                          @Override
                                          @NotNull
                                          public String getText() {
                                            return JavaScriptBundle.message("javascript.fix.message.change.parameters.to.expected");
                                          }
                                        },
                                        changeSignatureFix);
          }
          else if (incompatibleSignature == SignatureMatchResult.RETURN_TYPE_DIFFERS) {
            PsiElement implementationReturnTypeExpr = implementationFunction.getReturnTypeElement();
            JSType type = interfaceFunction.getReturnType();
            final String interfaceReturnType = type != null ? type.getResolvedTypeText() : null;
            String msg = JavaScriptBundle
              .message("javascript.validation.message.interface.method.invalid.signature2", StringUtil.notNullize(interfaceReturnType));
            reportingClient.reportError(
              implementationReturnTypeExpr != null ? implementationReturnTypeExpr.getNode() : implementationFunction.findNameIdentifier(),
              msg, ErrorReportingClient.ProblemKind.ERROR,
              new JSChangeTypeFix(implementationFunction, interfaceReturnType, "javascript.fix.message.change.return.type.to.expected"),
              createChangeBaseMethodSignatureFix(interfaceFunction, implementationFunction));
          }
          else if (incompatibleSignature == SignatureMatchResult.FUNCTION_KIND_DIFFERS) {
            String msg = JavaScriptBundle.message("javascript.validation.message.interface.method.invalid.signature3", interfaceFunction.getKind());
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
  public void visitJSFunctionDeclaration(@NotNull final JSFunction node) {
    super.visitJSFunctionDeclaration(node);

    checkPackageElement(node);

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
        myHolder.newAnnotation(HighlightSeverity.ERROR,
                               FlexBundle.message("javascript.validation.message.constructor.in.mxml.is.not.allowed")
        ).range(nameIdentifier)
        .withFix(new RemoveASTNodeFix("javascript.fix.remove.constructor", node)).create();
      }
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
        boolean b = JSResolveUtil.iterateOverrides(node, null, new JSOverrideHandler() {
          @Override
          public boolean process(@NotNull final List<? extends JSPsiElementBase> elements, final PsiElement scope, final @Nullable String className) {
            if (Objects.equals(qName, className)) return true;
            JSFunction value = (JSFunction)elements.iterator().next();
            set.set(value);

            DialectOptionHolder holder= myHighlighter.getDialectOptionsHolder();
            if ("Object".equals(className)) {
              if (hasOverride && !attributeList.hasModifier(JSAttributeList.ModifierType.NATIVE)) { /*native modifier is written always*/
                final ASTNode astNode = attributeList.getNode().findChildByType(JSTokenTypes.OVERRIDE_KEYWORD);

                myHolder.newAnnotation(HighlightSeverity.ERROR,
                                       JavaScriptBundle.message("javascript.validation.message.function.override.for.object.method"))
                  .range(astNode)
                  .withFix(new RemoveASTNodeFix("javascript.fix.remove.override.modifier", astNode.getPsi()))
                  .create();
              }
              return false;
            }
            else if (!hasOverride && holder.isECMA4) {
              myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle.message(
                "javascript.validation.message.function.override.without.override.modifier", className))
                .range(nameIdentifier)
                .withFix(new AddOverrideIntentionAction(node))
                .create();
            }
            else {
              JSAttributeList attrList = value.getAttributeList();
              JSAttributeList parentAttrList = ((JSAttributeListOwner)scope).getAttributeList();

              if (attrList != null && attrList.hasModifier(JSAttributeList.ModifierType.FINAL) ||
                  parentAttrList != null && parentAttrList.hasModifier(JSAttributeList.ModifierType.FINAL)
                ) {
                myHolder.newAnnotation(HighlightSeverity.ERROR,
                                       JavaScriptBundle.message("javascript.validation.message.can.not.override.final.method", className))
                  .range(attributeList.getNode().findChildByType(JSTokenTypes.OVERRIDE_KEYWORD))
                  .create();
              }
            }
            if (clazz.isInterface()) {
              myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle.message(
                "javascript.validation.message.function.override.for.interface", className)).range(nameIdentifier).create();
            }
            return false;
          }
        }, true);

        if (b && hasOverride) {
          final ASTNode astNode = attributeList.getNode().findChildByType(JSTokenTypes.OVERRIDE_KEYWORD);
          myHolder.newAnnotation(HighlightSeverity.ERROR,
                                 JavaScriptBundle.message("javascript.validation.message.function.override.without.parent.method"))
            .range(astNode)
            .withFix(new RemoveASTNodeFix("javascript.fix.remove.override.modifier", astNode.getPsi()))
            .create();
        }

        if (!b && hasOverride) {
          final JSFunction override = set.get();
          final JSAttributeList overrideAttrList = override.getAttributeList();
          String overrideNs = null;

          if (attributeList.getAccessType() != overrideAttrList.getAccessType() ||
              (overrideNs = ActionScriptPsiImplUtil.getNamespaceValue(overrideAttrList)) != null &&
              !overrideNs.equals(ActionScriptPsiImplUtil.getNamespaceValue(attributeList))) {
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
            myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle
              .message("javascript.validation.message.function.override.incompatible.access.modifier", newVisibility))
              .range(findElementForAccessModifierError(node, attributeList))
              .withFix(fix)
              .create();
          }

          final SignatureMatchResult incompatibleSignature = checkCompatibleSignature(node, override);

          if (incompatibleSignature == SignatureMatchResult.PARAMETERS_DIFFERS) {
            final JSParameterList nodeParameterList = node.getParameterList();
            final JSParameterList overrideParameterList = override.getParameterList();

            myHolder.newAnnotation(HighlightSeverity.ERROR,
                                   JavaScriptBundle.message("javascript.validation.message.function.override.incompatible.signature",
                                                            overrideParameterList != null ? overrideParameterList.getText() : "()"
                                   ))
              .range(nodeParameterList != null ? nodeParameterList.getNode() : node.findNameIdentifier())
              .withFix(new JSChangeSignatureFix(node, overrideParameterList, false) {
                @Override
                @NotNull
                public String getText() {
                  return JavaScriptBundle.message("javascript.fix.message.change.parameters.to.expected");
                }
              })
              .withFix(new JSChangeSignatureFix(override, nodeParameterList))
              .create();
          }
          else if (incompatibleSignature == SignatureMatchResult.RETURN_TYPE_DIFFERS) {
            PsiElement returnTypeExpr = node.getReturnTypeElement();
            JSType type = override.getReturnType();
            final String baseReturnType = type != null ? type.getResolvedTypeText() : null;
            String msg = JavaScriptBundle
              .message("javascript.validation.message.function.override.incompatible.signature2", StringUtil.notNullize(baseReturnType));
            myHolder.newAnnotation(HighlightSeverity.ERROR, msg)
              .range(returnTypeExpr != null ? returnTypeExpr.getNode() : node.findNameIdentifier())
              .withFix(new JSChangeTypeFix(node, baseReturnType, "javascript.fix.message.change.return.type.to.expected"))
              .withFix(createChangeBaseMethodSignatureFix(override, node))
              .create();
          }
          else if (incompatibleSignature == SignatureMatchResult.FUNCTION_KIND_DIFFERS) {
            String msg = JavaScriptBundle
              .message("javascript.validation.message.function.override.incompatible.signature3", override.getKind().toString());
            myHolder.newAnnotation(HighlightSeverity.ERROR, msg).range(node.findNameIdentifier())
              .create();
          }
        }
      }
    }
  }

  @Override
  public void visitJSVariable(@NotNull JSVariable var) {
    super.visitJSVariable(var);
    checkPackageElement(var);
  }

  @Override
  public void visitJSClass(JSClass jsClass) {
    super.visitJSClass(jsClass);
    checkPackageElement(jsClass);

    final JSFunction constructor = jsClass.getConstructor();
    if (constructor == null) createConstructorChecker().checkMissedConstructor(jsClass);

    PsiElement clazzParent = jsClass.getParent();
    final PsiElement context = clazzParent.getContext();
    boolean clazzParentIsInjectedJsFile = clazzParent instanceof JSFile &&
                                          (context instanceof XmlAttributeValue || context instanceof XmlText) &&
                                          !XmlBackedJSClassImpl.isImplementsAttribute((JSFile)clazzParent);

    if (PsiTreeUtil.getParentOfType(jsClass, JSFunction.class, JSClass.class) != null || clazzParentIsInjectedJsFile) {
      String message = JavaScriptBundle.message("javascript.validation.message.nested.classes.are.not.allowed");
      AnnotationBuilder builder = myHolder.newAnnotation(HighlightSeverity.ERROR, message);
      PsiElement nameIdentifier = jsClass.getNameIdentifier();
      if (nameIdentifier != null) builder = builder.range(nameIdentifier);
      builder.create();
    }
    checkClass(jsClass);
  }

  private static boolean isNative(final JSFunction function) {
    final JSAttributeList attributeList = function.getAttributeList();
    return attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.NATIVE);
  }

  private void reportStaticMethodProblem(JSAttributeList attributeList, @PropertyKey(resourceBundle = "messages.FlexBundle") String key) {
    final ASTNode astNode = attributeList.getNode().findChildByType(JSTokenTypes.STATIC_KEYWORD);
    myHolder.newAnnotation(HighlightSeverity.ERROR, FlexBundle.message(key)).range(astNode)
    .withFix(new RemoveASTNodeFix("javascript.fix.remove.static.modifier", astNode.getPsi())).create();
  }


  private static class AddOverrideIntentionAction implements IntentionAction {
    private final JSFunction myNode;

    AddOverrideIntentionAction(final JSFunction node) {
      myNode = node;
    }

    @Override
    @NotNull
    public String getText() {
      return JavaScriptBundle.message("javascript.fix.add.override.modifier");
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
      myHolder.newAnnotation(HighlightSeverity.ERROR, FlexBundle
        .message("javascript.validation.message.nested.packages.are.not.allowed")).range(packageStatement.getFirstChild()).create();
      return;
    }

    for (PsiElement el = packageStatement.getPrevSibling(); el != null; el = el.getPrevSibling()) {
      if (!(el instanceof PsiWhiteSpace) && !(el instanceof PsiComment)) {
        myHolder.newAnnotation(HighlightSeverity.ERROR, FlexBundle
          .message("javascript.validation.message.package.shouldbe.first.statement")).range(packageStatement.getFirstChild()).create();
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
      myHolder.newAnnotation(HighlightSeverity.ERROR,
                             FlexBundle.message(
          "javascript.validation.message.incorrect.package.name", s, expected
        )
      ).range(nameIdentifier != null ? nameIdentifier : packageStatement.getFirstChild().getNode())
      .withFix(new IntentionAction() {
        @Override
        @NotNull
        public String getText() {
          return FlexBundle.message("javascript.fix.package.name", expected);
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
      }).create();
    }

    checkFileUnderSourceRoot(packageStatement, new SimpleErrorReportingClient());
  }

  private void checkPackageElement(@NotNull JSNamedElement el) {
    if (!(el instanceof JSAttributeListOwner)) return;

    PsiElement parent = el.getParent();
    if (parent instanceof JSVarStatement) parent = parent.getParent();
    if (!(parent instanceof JSPackageStatement)) return;

    boolean hasOtherElement = Arrays.stream(((JSPackageStatement)parent).getStatements()).flatMap(e -> {
      return e instanceof JSVarStatement ? Arrays.stream(((JSVarStatement)e).getVariables()) : Stream.of(e);
    }).anyMatch(e -> {
      if (e == el || !(e instanceof JSNamedElement) || e instanceof JSImportStatement) {
        return false;
      }
      if (e instanceof JSAttributeListOwner) {
        JSAttributeList list = ((JSAttributeListOwner)e).getAttributeList();
        if (list != null && list.getAccessType() == JSAttributeList.AccessType.PRIVATE) {
          return false;
        }
      }
      return true;
    });

    if (hasOtherElement) {
      JSAttributeList attributeList = ((JSAttributeListOwner)el).getAttributeList();
      if (attributeList != null && ActionScriptPsiImplUtil.getConditionalCompileVariableReference(attributeList) != null) return;
      final ASTNode nameIdentifier = el.findNameIdentifier();
      myHolder.newAnnotation(HighlightSeverity.ERROR,
                             FlexBundle.message("javascript.validation.message.more.than.one.externally.visible.symbol"))
        .range(nameIdentifier != null ? nameIdentifier : el.getFirstChild().getNode())
        .withFix(new RemoveASTNodeFix("javascript.fix.remove.externally.visible.symbol", el))
        .create();
    }

    checkNamedObjectIsInCorrespondingFile(el);
  }

  @Override
  public void visitJSReferenceExpression(JSReferenceExpression node) {
    super.visitJSReferenceExpression(node);

    final PsiElement parent = node.getParent();

    if (node.getQualifier() == null) {
      String nodeText = node.getText();
      if (!(parent instanceof JSCallExpression) && JSResolveUtil.isExprInStrictTypeContext(node) &&
          JSCommonTypeNames.VECTOR_CLASS_NAME.equals(nodeText)) {
        myHolder.newAnnotation(HighlightSeverity.WARNING, FlexBundle.message("javascript.validation.message.vector.without.parameters")).create();
      }
      else if (parent instanceof JSNewExpression &&
               JSCommonTypeNames.VECTOR_CLASS_NAME.equals(nodeText)) {
        myHolder.newAnnotation(HighlightSeverity.WARNING, FlexBundle
          .message("javascript.validation.message.vector.without.parameters2")).create();
      }
    }


    if (parent instanceof JSNamedElement) {
      JSNamedElement namedElement = (JSNamedElement)parent;
      final ASTNode nameIdentifier = namedElement.findNameIdentifier();

      if (nameIdentifier != null && nameIdentifier.getPsi() == node) {
        if (parent instanceof JSPackageStatement) {
          checkPackageStatement((JSPackageStatement)parent);
        }
        else if (parent instanceof JSNamespaceDeclaration) {
          checkDuplicates((JSNamespaceDeclaration)parent);
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
            myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle
              .message("javascript.validation.message.arguments.with.rest.parameter")).create();
          }
        }
      }
    }
  }

  private void checkClass(JSClass jsClass) {
    checkDuplicates(jsClass);
  }

  @Override
  public void visitJSReferenceList(JSReferenceList referenceList) {
    PsiElement jsClass = referenceList.getParent();
    if (jsClass instanceof JSClass && !((JSClass)jsClass).isInterface()
        && (((JSClass)jsClass).getExtendsList() == referenceList || ((JSClass)jsClass).getImplementsList() == referenceList)) {
      checkIfExtendsFinalOrMultipleClasses((JSClass)jsClass);
    }
    super.visitJSReferenceList(referenceList);
  }

  private void checkIfExtendsFinalOrMultipleClasses(final JSClass jsClass) {
    final JSReferenceList extendsList = jsClass.getExtendsList();
    if (extendsList != null) {
      final String[] extendsListTexts = extendsList.getReferenceTexts();
      // in some cases jsClass.getSuperClasses() contains several elements for one class, so counting extendsListTexts is more correct
      if (extendsListTexts.length > 1) {
        myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle
          .message("javascript.validation.message.extend.multiple.classes")).range(extendsList).create();
      }
      else if (extendsListTexts.length == 1) {
        final JSClass[] superClasses = jsClass.getSuperClasses();
        final JSAttributeList attributeList = superClasses.length > 0 ? superClasses[0].getAttributeList() : null;
        if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.FINAL)) {
          final JSExpression[] referencesToSuper = extendsList.getExpressions();
          if (referencesToSuper.length == 1) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle.message("javascript.validation.message.extend.final.class",
                                                                                     superClasses[0].getQualifiedName())).range(referencesToSuper[0]).create();
          }
        }
      }
    }
  }


  @Override
  public void visitJSAttributeList(JSAttributeList attributeList) {
    PsiElement namespaceElement = ActionScriptPsiImplUtil.getNamespaceElement(attributeList);
    PsiElement accessTypeElement = attributeList.findAccessTypeElement();
    PsiElement namespaceOrAccessModifierElement = namespaceElement;

    ASTNode[] children = attributeList.getNode().getChildren(JSAttributeListImpl.ourModifiersTypeSet);

    if (namespaceOrAccessModifierElement == null) {
      namespaceOrAccessModifierElement = accessTypeElement;
    }
    else if (accessTypeElement != null) {
      myHolder.newAnnotation(HighlightSeverity.ERROR,
                             JavaScriptBundle
                               .message("javascript.validation.message.use.namespace.reference.or.access.modifier")).range(namespaceOrAccessModifierElement)
        .withFix(
          new RemoveASTNodeFix("javascript.fix.remove.namespace.reference", namespaceOrAccessModifierElement)).create();

      myHolder.newAnnotation(HighlightSeverity.ERROR,
                             JavaScriptBundle
                               .message("javascript.validation.message.use.namespace.reference.or.access.modifier")).range(accessTypeElement)
        .withFix(new RemoveASTNodeFix("javascript.fix.remove.visibility.modifier", accessTypeElement)).create();
    }

    if (children.length > 1 && namespaceElement == null) {
      for (ASTNode astNode : children) {
        myHolder.newAnnotation(HighlightSeverity.ERROR,
                               JavaScriptBundle.message("javascript.validation.message.one.visibility.modifier.allowed")).range(astNode)
          .withFix(new RemoveASTNodeFix("javascript.fix.remove.visibility.modifier", astNode.getPsi())).create();
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

          String message =
            FlexBundle.message(
              nodeUnderPackage ?
              "javascript.validation.message.access.modifier.allowed.only.for.package.members"
                               : nsRef ?
                                 "javascript.validation.message.namespace.allowed.only.for.class.members"
                                       : "javascript.validation.message.access.modifier.allowed.only.for.class.members");

          HighlightSeverity severity;
          if (parentForCheckingNsOrAccessModifier instanceof JSFile && !(element instanceof JSClass)) {
            // TODO: till we resolve issues with includes
            severity = HighlightSeverity.WARNING;
          }
          else {
            severity = HighlightSeverity.ERROR;
          }
          myHolder.newAnnotation(severity, message).range(namespaceOrAccessModifierElement).withFix(new RemoveASTNodeFix(nsRef
                                                      ? "javascript.fix.remove.namespace.reference"
                                                      : "javascript.fix.remove.access.modifier",
                    namespaceOrAccessModifierElement)).create();
        }
      }
      else if (((JSClass)parentForCheckingNsOrAccessModifier).isInterface()) {

        if (attributeList.getAccessType() != JSAttributeList.AccessType.PACKAGE_LOCAL ||
            attributeList.getNode().findChildByType(JSTokenTypes.INTERNAL_KEYWORD) != null

          ) {
          ASTNode astNode = attributeList.getNode().findChildByType(JSTokenTypes.ACCESS_MODIFIERS);
          String message = JavaScriptBundle.message("javascript.validation.message.interface.members.cannot.have.access.modifiers");
          String fixMessageKey = "javascript.fix.remove.access.modifier";
          if (astNode == null) {
            astNode = attributeList.getNode().findChildByType(JSElementTypes.REFERENCE_EXPRESSION);
            message = JavaScriptBundle.message("javascript.validation.message.interface.members.cannot.have.namespace.attributes");
            fixMessageKey = "javascript.fix.remove.namespace.reference";
          }
          PsiElement psi = astNode.getPsi();
          myHolder.newAnnotation(HighlightSeverity.ERROR, message).range(psi)
          .withFix(new RemoveASTNodeFix(fixMessageKey, psi)).create();
        }
      }
      else if (JSResolveUtil.isConstructorFunction(element)) {
        JSAttributeList.AccessType accessType = attributeList.getAccessType();

        if (accessType != JSAttributeList.AccessType.PUBLIC) {
          myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle
            .message("javascript.validation.message.constructor.cannot.have.custom.visibility")).range(namespaceOrAccessModifierElement).create();
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
          String message = JavaScriptBundle.message("javascript.validation.message.static.modifier.is.allowed.only.for.class.members");

          HighlightSeverity severity;
          if (parentForCheckingNsOrAccessModifier instanceof JSFile) {
            severity = HighlightSeverity.WARNING;
          }
          else {
            severity = HighlightSeverity.ERROR;
          }
          myHolder.newAnnotation(severity, message).range(modifierElement)
          .withFix(new RemoveASTNodeFix("javascript.fix.remove.static.modifier", modifierElement)).create();
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

    if (attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) {
      PsiElement function = attributeList.getParent();
      if (function instanceof JSFunction && ((JSFunction)function).getAttributeList() == attributeList) {
        PsiElement parent = function.getParent();
        if (parent instanceof JSClass) {
          if (((JSClass)parent).isInterface()) {
            reportStaticMethodProblem(attributeList, "javascript.validation.message.static.method.in.interface");
          }
          if (attributeList.hasModifier(JSAttributeList.ModifierType.OVERRIDE)) {
            reportStaticMethodProblem(attributeList, "javascript.validation.message.static.method.with.override");
          }
        }
      }
    }
  }

  private void finalModifierProblem(JSAttributeList attributeList, String messageKey) {
    modifierProblem(attributeList, JSAttributeList.ModifierType.FINAL, messageKey, "javascript.fix.remove.final.modifier");
  }

  private void modifierProblem(JSAttributeList attributeList,
                               JSAttributeList.ModifierType modifierType,
                               String messageKey,
                               String removeFixNameKey) {
    PsiElement modifierElement = attributeList.findModifierElement(modifierType);
    String message = JavaScriptBundle.message(messageKey);
    myHolder.newAnnotation(HighlightSeverity.ERROR, message).range(modifierElement)
    .withFix(new RemoveASTNodeFix(removeFixNameKey, modifierElement)).create();
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
      String s = StringUtil.toLowerCase(modifiers[0].getElementType().toString());
      final String type = s.substring(s.indexOf(':') + 1, s.indexOf('_'));
      for (ASTNode a : modifiers) {
        myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle.message(
          "javascript.validation.message.attribute.was.specified.multiple.times",
          type)).range(a.getPsi())
          .highlightType(ProblemHighlightType.ERROR)
        .withFix(new RemoveASTNodeFix(ourModifierFixIds[i], a.getPsi())).create();
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
          JSType type = var.getJSType();

          if (!(type instanceof JSStringType) &&
              !(type instanceof JSTypeImpl && "Class".equals(type.getTypeText(JSType.TypeTextFormat.SIMPLE)))) {
            myHolder.newAnnotation(HighlightSeverity.ERROR,
                                   FlexBundle.message("javascript.validation.message.embed.annotation.used.with.var.of.wrong.type")).create();
          }
        }
      }
    }

    ActionScriptSemanticHighlightingUtil.highlight(jsAttribute, myHolder);

    PsiReference psiReference = jsAttribute.getReference();
    if (psiReference != null && psiReference.resolve() == null) {
      myHolder.newAnnotation(HighlightSeverity.WEAK_WARNING, JavaScriptBundle
        .message("javascript.validation.message.unknown.metadata.annotation.used")).range(jsAttribute.getNameIdentifier()).create();
    }
  }

  @Override
  public void visitJSNamespaceDeclaration(JSNamespaceDeclaration namespaceDeclaration) {
    checkPackageElement(namespaceDeclaration);
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
        myHolder.newAnnotation(HighlightSeverity.ERROR, FlexBundle
          .message("javascript.namespace.initializer.should.be.string.or.another.namespace.reference")).range(initializer)
        .highlightType(ProblemHighlightType.ERROR).create();
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
        final String message = FlexBundle.message(aClass instanceof JSClass
                                                ? "javascript.validation.message.class.should.be.in.file" :
                                                aClass instanceof JSNamespaceDeclaration
                                                ? "javascript.validation.message.namespace.should.be.in.file" :
                                                aClass instanceof JSVariable
                                                ? "javascript.validation.message.variable.should.be.in.file"
                                                : "javascript.validation.message.function.should.be.in.file", name, nameWithExtension);
        myHolder.newAnnotation(HighlightSeverity.ERROR, message).range(node)
        .withFix(new RenameFileFix(nameWithExtension))
        .withFix(new RenameElementFix(aClass) {
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
        }).create();
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
      final String typeString = type.getTypeText(JSType.TypeTextFormat.PRESENTABLE);
      myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle
        .message("javascript.validation.message.get.method.should.be.valid.type", typeString))
        .range(getter.getReturnTypeElement())
        .create();
    }
    else {
        if (setter != null) {
          JSParameterList setterParameterList = setter.getParameterList();
          JSParameter[] setterParameters = setterParameterList != null ?
                                           setterParameterList.getParameterVariables() : JSParameter.EMPTY_ARRAY;

          JSType setterType;
          if (setterParameters.length == 1 &&
              !((setterType = setterParameters[0].getJSType()) instanceof JSAnyType) &&
              !(type instanceof JSAnyType) &&
              !JSTypeUtils.areTypesCompatible(setterType, type,  null, getter)) {
            PsiElement typeElement = getter.getReturnTypeElement();

            myHolder.newAnnotation(HighlightSeverity.ERROR,
                                   JavaScriptBundle.message("javascript.validation.message.get.method.type.is.different.from.setter",
                                                            setterType != null
                                                            ? setterType.getTypeText(JSType.TypeTextFormat.PRESENTABLE)
                                                            : "empty"))
              .range(typeElement != null ? typeElement : getPlaceForNamedElementProblem(getter))
              .create();
          }

          checkAccessorAccessTypeMatch(getter, setter,
                                       "actionscript.validation.message.get.method.access.type.is.different.from.setter");
        }
    }
  }

  @Override
  protected boolean needsInitializer(JSParameterListElement parameter) {
    return !parameter.isRest();
  }

  @Override
  protected void validateRestParameterType(JSParameterListElement parameter) {
    PsiElement typeElement = parameter.getTypeElement();
    if (typeElement != null && !"Array".equals(typeElement.getText())) {
      final Pair<ASTNode, ASTNode> nodesBefore = getNodesBefore(typeElement, JSTokenTypes.COLON);
      myHolder.newAnnotation(HighlightSeverity.ERROR,
                             JavaScriptBundle.message("javascript.validation.message.unexpected.type.for.rest.parameter")
      ).range(typeElement).withFix(JSFixFactory.getInstance().removeASTNodeFix("javascript.fix.remove.type.reference", false,
                                                                nodesBefore.first.getPsi(), nodesBefore.second.getPsi())).create();
    }
  }

  @Override
  public void visitJSThisExpression(final JSThisExpression node) {
    checkClassReferenceInStaticContext(node, "javascript.validation.message.this.referenced.from.static.context");
  }

  private void checkClassReferenceInStaticContext(final JSExpression node, @PropertyKey(resourceBundle = JavaScriptBundle.BUNDLE) String key) {
    PsiElement element =
      PsiTreeUtil.getParentOfType(node, JSExecutionScope.class, JSClass.class, JSObjectLiteralExpression.class);

    if (element instanceof JSFunction) {
      final JSFunction function = (JSFunction)element;

      final JSAttributeList attributeList = function.getAttributeList();
      if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) {
        myHolder.newAnnotation(HighlightSeverity.ERROR, JavaScriptBundle.message(key)).create();
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
        final String message = JavaScriptBundle.message("javascript.validation.message.super.referenced.without.class.instance.context");
        myHolder.newAnnotation(HighlightSeverity.ERROR, message).create();
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
         myHolder.newAnnotation(HighlightSeverity.ERROR, message).range(returnedExpr).create();
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
