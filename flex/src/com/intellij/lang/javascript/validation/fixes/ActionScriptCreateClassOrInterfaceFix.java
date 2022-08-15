// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.impl.BundledFileTemplate;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSIconProvider;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.ui.newclass.CreateFlashClassWizard;
import com.intellij.lang.javascript.ui.newclass.CustomVariablesStep;
import com.intellij.lang.javascript.ui.newclass.MainStep;
import com.intellij.lang.javascript.ui.newclass.WizardModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.StringTokenizer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class ActionScriptCreateClassOrInterfaceFix extends FixAndIntentionAction implements CreateClassIntentionWithCallback {

  @NonNls public static final String ACTION_SCRIPT_CLASS_TEMPLATE_NAME = "ActionScript Class";
  @NonNls public static final String ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME = "ActionScript Class with Supers";
  @NonNls public static final String ACTION_SCRIPT_INTERFACE_TEMPLATE_NAME = "ActionScript Interface";

  private static final Logger LOG = Logger.getInstance(ActionScriptCreateClassOrInterfaceFix.class.getName());

  @NonNls public static final String ACCESS_MODIFIER_PROPERTY = "Access_modifier";
  @NonNls public static final String SUPERCLASS = "Superclass";
  @NonNls public static final String SUPER_INTERFACES = "SuperInterfaces";

  public static final Collection<String> ACTIONSCRIPT_TEMPLATES_EXTENSIONS =
    ContainerUtil.immutableList(JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION, JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION2,
                  JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION3);

  private static String ourPreviousClassTemplateName;
  private static String ourPreviousInterfaceTemplateName;

  protected String myClassNameToCreate;
  protected final PsiElement myContext;
  protected String myPackageName;
  private final boolean myIsInterface;
  @Nullable private final JSClass myBaseClassifier;
  private final boolean myAddImportForCreatedClass;
  private final boolean myIdentifierIsValid;
  private Consumer<? super String> myCreatedClassFqnConsumer;
  @Nullable private final JSArgumentList myConstructorArguments;
  private final boolean myIsClassNameEditable;

  public ActionScriptCreateClassOrInterfaceFix(JSReferenceExpression context,
                                               boolean isInterface,
                                               @Nullable JSArgumentList constructorArguments,
                                               @Nullable JSType expectedType) {
    myIsClassNameEditable = false;
    myConstructorArguments = constructorArguments;
    myClassNameToCreate = context.getReferencedName();

    myBaseClassifier = getBaseClassifier(expectedType);
    myContext = context;
    myIsInterface = isInterface;
    myAddImportForCreatedClass = true;
    myIdentifierIsValid =
      myClassNameToCreate != null && LanguageNamesValidation.isIdentifier(JavascriptLanguage.INSTANCE, myClassNameToCreate);
  }

  public ActionScriptCreateClassOrInterfaceFix(final String fqn, @Nullable final String baseClassFqn, final PsiElement context) {
    myClassNameToCreate = StringUtil.getShortName(fqn);
    myPackageName = StringUtil.getPackageName(fqn);

    myBaseClassifier = getBaseClassifier(baseClassFqn, context);
    myContext = context;
    myIsInterface = false;
    myAddImportForCreatedClass = false;
    myIdentifierIsValid =
      LanguageNamesValidation.isIdentifier(JavascriptLanguage.INSTANCE, myClassNameToCreate);
    myConstructorArguments = null;
    myIsClassNameEditable = true;
    registerElementRefForFix(context, null);
  }

  public ActionScriptCreateClassOrInterfaceFix(final PsiDirectory dir) {
    myClassNameToCreate = null;
    myIsClassNameEditable = true;
    myPackageName = DirectoryIndex.getInstance(dir.getProject()).getPackageName(dir.getVirtualFile());
    LOG.assertTrue(myPackageName != null, "No package for file " + dir.getVirtualFile().getPath());
    myBaseClassifier = null;
    myContext = dir;
    myIsInterface = false;
    myAddImportForCreatedClass = false;
    myIdentifierIsValid = true;
    myConstructorArguments = null;
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @Nullable
  private static JSClass getBaseClassifier(@Nullable JSType type) {
    if (type == null) {
      return null;
    }
    String typeString = type.getResolvedTypeText();
    if (ArrayUtil.contains(typeString, JSCommonTypeNames.ALL)) {
      return null;
    }

    return getBaseClassifier(typeString, type.getSource().getScope());
  }

  private static JSClass getBaseClassifier(@Nullable final String fqn, final PsiElement context) {
    if (fqn == null || context == null) {
      return null;
    }
    PsiElement clazz = JSDialectSpecificHandlersFactory.forElement(context).getClassResolver().findClassByQName(fqn, context);
    return clazz instanceof JSClass ? (JSClass)clazz : null;
  }

  @Override
  @NotNull
  public String getName() {
    final String key = myIsInterface ? "javascript.create.interface.intention.name" : "javascript.create.class.intention.name";
    return JavaScriptBundle.message(key, myClassNameToCreate);
  }

  @Override
  protected void applyFix(final Project project, PsiElement psiElement, @NotNull PsiFile file, @Nullable Editor editor) {
    execute();
  }

  public void execute() {
    if (myPackageName == null) {
      PsiFile contextFile = getTopLevelContextFile();

      assert myContext instanceof JSReferenceExpression;
      final JSExpression qualifier = ((JSReferenceExpression)myContext).getQualifier();
      myPackageName = qualifier != null ?
                      qualifier.getText() :
                      JSResolveUtil.getExpectedPackageNameFromFile(contextFile.getVirtualFile(), myContext.getProject());
    }

    final String superClassFqn;
    final Collection<String> interfacesFqns;
    final Map<String, Object> templateAttributes;
    String defaultTemplateName =
      getTemplateName(myIsInterface, myBaseClassifier != null, ourPreviousInterfaceTemplateName, ourPreviousClassTemplateName, myContext.getProject());
    String templateName;
    if (myIsInterface) {
      templateName = ourPreviousInterfaceTemplateName == null ? defaultTemplateName : ourPreviousInterfaceTemplateName;
    }
    else {
      templateName = ourPreviousClassTemplateName == null || myBaseClassifier != null ? defaultTemplateName : ourPreviousClassTemplateName;
    }

    final PsiDirectory targetDirectory;
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      final CreateClassParameters params = createDialog(templateName);
      if (params == null) {
        return;
      }
      myPackageName = params.getPackageName();
      templateName = params.getTemplateName();
      targetDirectory = params.getTargetDirectory();
      superClassFqn = params.getSuperclassFqn();
      interfacesFqns = params.getInterfacesFqns();
      myClassNameToCreate = params.getClassName();
      templateAttributes = new HashMap<>(params.getTemplateAttributes());
      if (myIsInterface) {
        ourPreviousInterfaceTemplateName = templateName;
      }
      else {
        ourPreviousClassTemplateName = templateName;
      }
    }
    else {
      if (StringUtil.isEmpty(myPackageName)) {
        myPackageName = "foo";
      }
      templateName = getTemplateForTest(myIsInterface);
      templateAttributes = fillAttributes(myBaseClassifier == null || myBaseClassifier.isInterface() ? null : myBaseClassifier,
                                          myBaseClassifier != null && myBaseClassifier.isInterface() ? Collections
                                            .singletonList(myBaseClassifier.getQualifiedName()) : Collections.emptyList());
      targetDirectory = WriteAction.compute(() -> findOrCreateDirectory(myPackageName, getTopLevelContextFile()));

      superClassFqn = myBaseClassifier == null || myBaseClassifier.isInterface() ? null : myBaseClassifier.getQualifiedName();
      interfacesFqns = myBaseClassifier != null && myBaseClassifier.isInterface()
                       ? Collections.singletonList(myBaseClassifier.getQualifiedName())
                       : Collections.emptyList();
    }

    JSClass jsClass =
      createClass(templateName, myClassNameToCreate, myPackageName, calcClass(superClassFqn, myContext), interfacesFqns, targetDirectory,
                  getName(), myConstructorArguments == null, templateAttributes,
                  jsClass1 -> {
                    if (myAddImportForCreatedClass) {
                      String contextPackage = JSResolveUtil.findPackageStatementQualifier(myContext);
                      if (StringUtil.isNotEmpty(myPackageName) && !myPackageName.equals(contextPackage)) {
                        ImportUtils.doImport(myContext, StringUtil.getQualifiedName(myPackageName, StringUtil.notNullize(myClassNameToCreate)), true);
                      }
                    }

                    if (myCreatedClassFqnConsumer != null) {
                      myCreatedClassFqnConsumer.consume(StringUtil.getQualifiedName(myPackageName, StringUtil.notNullize(myClassNameToCreate)));
                    }
                  });

    if (jsClass != null) {
      postProcess(jsClass, superClassFqn);
    }
  }

  @Nullable
  public static JSClass calcClass(@Nullable final String superClassFqn, PsiElement context) {
    if (superClassFqn != null) {
      Module module = ModuleUtilCore.findModuleForPsiElement(context);
      GlobalSearchScope superClassScope = module != null
                                          ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
                                          : GlobalSearchScope.projectScope(context.getProject());
      PsiElement byQName = JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
        .findClassByQName(superClassFqn, superClassScope);
      return byQName instanceof JSClass ? (JSClass)byQName : null;
    }
    else {
      return null;
    }
  }

  protected String getTemplateForTest(final boolean isInterface) {
    return getTemplateName(myIsInterface, true, ourPreviousInterfaceTemplateName, ourPreviousClassTemplateName, myContext.getProject());
  }

  protected void postProcess(@NotNull final JSClass jsClass, final String superClassFqn) {
    if (!fixConstructor(jsClass)) {
      jsClass.navigate(true);
    }
  }

  @Nullable
  protected CreateClassParameters createDialog(final String templateName) {
    String title = JavaScriptBundle
      .message(myIsInterface ? "new.actionscript.interface.dialog.title" : "new.actionscript.class.dialog.title");
    return createAndShow(templateName, myContext, myClassNameToCreate, myIsClassNameEditable, myPackageName, myBaseClassifier, title,
                         () -> computeApplicableTemplates());
  }

  @Nullable
  public static CreateClassParameters createAndShow(final String templateName,
                                                    final PsiElement context,
                                                    final String classNameToCreate,
                                                    final boolean classNameEditable,
                                                    final String packageName,
                                                    final JSClass baseClassifier,
                                                    @NlsContexts.DialogTitle String title,
                                                    Computable<List<FileTemplate>> templatesProvider) {
    final WizardModel model = new WizardModel(context, true);
    MainStep mainStep = new MainStep(model, context.getProject(),
                                     classNameToCreate,
                                     classNameEditable,
                                     packageName,
                                     baseClassifier,
                                     baseClassifier == null,
                                     templateName,
                                     context,
                                     JavaScriptBundle.message("choose.super.class.title"),
                                     templatesProvider);
    CustomVariablesStep customVariablesStep = new CustomVariablesStep(model);
    final CreateFlashClassWizard w =
      new CreateFlashClassWizard(title, context.getProject(), model, "New_ActionScript_Class_dialog", mainStep, customVariablesStep);
    w.show();
    if (w.getExitCode() != DialogWrapper.OK_EXIT_CODE) return null;
    return model;
  }

  protected List<FileTemplate> computeApplicableTemplates() {
    return getApplicableTemplates(ACTIONSCRIPT_TEMPLATES_EXTENSIONS, myContext.getProject());
  }

  private PsiFile getTopLevelContextFile() {
    PsiFile contextFile = myContext.getContainingFile();
    PsiElement context = contextFile.getContext();
    if (context != null) contextFile = context.getContainingFile();
    return contextFile;
  }

  private boolean fixConstructor(@NotNull final JSClass jsClass) {
    if (myConstructorArguments == null) {
      return false;
    }
    JSExpression[] arguments = myConstructorArguments.getArguments();
    final Collection<String> toImport = ContainerUtil.map2SetNotNull(Arrays.asList(arguments), argument -> {
      String type = ActionScriptResolveUtil.getQualifiedExpressionType(argument, argument.getContainingFile());
      return StringUtil.isNotEmpty(type) && ImportUtils.needsImport(jsClass, StringUtil.getPackageName(type))
             ? type : null;
    });
    final PsiFile jsClassContainingFile = jsClass.getContainingFile();
    final Project project = jsClass.getProject();
    final JSFunction constructor = jsClass.getConstructor();
    if (constructor != null) {
      // this constructor could come only from file template, since we skipped creating our own to match super constructor
      // just replace parameter list with live template
      if (constructor.getParameterList().getParameters().length > 0) {
        ApplicationManager.getApplication().runWriteAction(() -> {
          JSFunction func = JSPsiElementFactory.createJSSourceElement("function foo() {}", constructor, JSFunction.class);
          constructor.getParameterList().replace(func.getParameterList());
          PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
          documentManager.doPostponedOperationsAndUnblockDocument(documentManager.getDocument(jsClassContainingFile));
        });
      }

      final ASTNode rpar = constructor.getParameterList().getNode().findChildByType(JSTokenTypes.RPAR);
      if (rpar == null) {
        return false; // broken template
      }
      final TemplateManager templateManager = TemplateManager.getInstance(project);
      Template template = templateManager.createTemplate("", "");
      template.setToReformat(true);
      JSReferenceExpression referenceExpression = (JSReferenceExpression)myContext;
      CreateJSFunctionIntentionAction.addParameters(template, arguments, referenceExpression, jsClassContainingFile);
      Editor editor = BaseCreateFix.getEditor(project, jsClassContainingFile);
      if (editor == null) {
        return false;
      }
      BaseCreateFix.navigate(project, editor,
                             rpar.getTextRange().getStartOffset(),
                             jsClassContainingFile.getVirtualFile());
      templateManager.startTemplate(editor, template);
    }
    else {
      final ASTNode lbrace = jsClass.getNode().findChildByType(JSTokenTypes.LBRACE);
      if (lbrace == null) {
        return false; // broken template
      }
      new CreateJSFunctionIntentionAction(jsClass.getName(), true, false) {
        @NotNull
        @Override
        protected Pair<JSReferenceExpression, PsiElement> calculateAnchors(PsiElement psiElement1) {
          return Pair.create(((JSReferenceExpression)myContext), lbrace.getPsi());
        }

        @Override
        protected void appendFunctionBody(Template template, JSReferenceExpression refExpr, PsiElement anchorParent) {
        }
      }.applyFix(project, myContext, jsClassContainingFile, null);
    }

    if (!toImport.isEmpty()) {
      ApplicationManager.getApplication().runWriteAction(() -> {
        FormatFixer fixer = ImportUtils.insertImportStatements(jsClass, toImport);
        if (fixer != null) {
          fixer.fixFormat();
        }
      });
    }
    return true;
  }

  public static JSClass createClass(final String templateName,
                                    final String className,
                                    final String packageName,
                                    @Nullable final JSClass superClass,
                                    Collection<String> superInterfaces,
                                    final PsiDirectory targetDirectory,
                                    @NlsContexts.DialogTitle @NotNull String errorTitle,
                                    final boolean fixSuperCall,
                                    Map<String, Object> templateAttributes,
                                    final Consumer<? super JSClass> postProcessRunnable) {
    Exception error = null;
    JSClass result = null;

    try {
      result = WriteAction.compute(() -> {
        Map<String, Object> additionalTemplateProperties = fillAttributes(superClass, superInterfaces);
        additionalTemplateProperties.putAll(templateAttributes);

        PsiElement createdFile = doCreateClass(className, packageName, targetDirectory, templateName, additionalTemplateProperties);
        JSClass createdClass =
          createdFile instanceof JSFile ? JSPsiImplUtils.findClass((JSFile)createdFile) : XmlBackedJSClassFactory.getXmlBackedClass(
            ((XmlFile)createdFile));

        Collection<String> toImport = new ArrayList<>();
        if (createdClass != null && !(createdClass instanceof XmlBackedJSClass) && superClass != null) {
          String superClassPackage = StringUtil.getPackageName(superClass.getQualifiedName());
          if (!StringUtil.isEmpty(superClassPackage) && !superClassPackage.equals(packageName)) {
            toImport.add(superClass.getQualifiedName());
          }

          if (fixSuperCall) {
            JSFunction superConstructor = superClass.getConstructor();
            if (superConstructor != null && superConstructor.getParameterList().getParameters().length > 0) {
              String text = ActionScriptAddConstructorAndSuperInvocationFix.getConstructorText(createdClass, superConstructor, toImport);
              PsiElement newConstructor =
                JSChangeUtil.createJSTreeFromText(createdClass.getProject(), text, JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
              if (newConstructor != null) {
                JSFunction constructor = createdClass.getConstructor();
                if (constructor != null) {
                  constructor.replace(newConstructor);
                }
                else {
                  createdClass.add(newConstructor);
                }
              }
            }
          }
        }

        if (createdClass != null && !(createdClass instanceof XmlBackedJSClass)) {
          for (String superInterface : superInterfaces) {
            String aPackage = StringUtil.getPackageName(superInterface);
            if (!StringUtil.isEmpty(aPackage) && !aPackage.equals(packageName)) {
              toImport.add(superInterface);
            }
          }
          if (!toImport.isEmpty()) {
            ImportUtils.insertImportStatements(createdClass, toImport);
          }
          new ECMAScriptImportOptimizer().processFile(createdClass.getContainingFile()).run();
        }

        postProcessRunnable.consume(createdClass);
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(targetDirectory.getProject());
        documentManager.doPostponedOperationsAndUnblockDocument(documentManager.getDocument((PsiFile)createdFile));
        return createdClass;
      });
    }
    catch (IncorrectOperationException e) {
      error = e;
    }
    catch (Exception e) {
      LOG.error(e);
    }

    if (error != null) {
      String message = error.getCause() instanceof IOException ? error.getCause().getMessage() : error.getMessage();
      ApplicationUtil.showDialogAfterWriteAction(() -> Messages.showErrorDialog(targetDirectory.getProject(), message, errorTitle));
    }
    return result;
  }

  private static Map<String, Object> fillAttributes(final JSClass superClass, final Collection<String> superInterfaces) {
    Map<String, Object> additionalTemplateProperties = new HashMap<>();
    if (superClass != null) {
      additionalTemplateProperties.put(SUPERCLASS, superClass.getQualifiedName());
    }
    if (!superInterfaces.isEmpty()) {
      additionalTemplateProperties.put(SUPER_INTERFACES, superInterfaces);
    }
    return additionalTemplateProperties;
  }

  @Override
  public void setCreatedClassFqnConsumer(final Consumer<? super String> consumer) {
    myCreatedClassFqnConsumer = consumer;
  }

  public static PsiDirectory findOrCreateDirectory(String packageName, PsiElement context) {
    LOG.assertTrue(ApplicationManager.getApplication().isUnitTestMode());
    ApplicationManager.getApplication().assertWriteAccessAllowed();

    final Module module = ModuleUtilCore.findModuleForPsiElement(context);
    VirtualFile base = ModuleRootManager.getInstance(module).getSourceRoots()[0];
    VirtualFile relativeFile = VfsUtilCore.findRelativeFile(packageName, base);

    if (relativeFile == null) {
      relativeFile = base;
      StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
      while (tokenizer.hasMoreTokens()) {
        String nextNameSegment = tokenizer.nextToken();
        VirtualFile next = relativeFile.findChild(nextNameSegment);
        if (next == null) {
          try {
            next = relativeFile.createChildDirectory(ActionScriptCreateClassOrInterfaceFix.class, nextNameSegment);
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
        relativeFile = next;
      }
    }

    assert relativeFile != null;
    return context.getManager().findDirectory(relativeFile);
  }

  // TODO move this code to FileTemplateUtil
  public static String getClassText(String className, String packageName, final boolean isInterface, String accessModifier, Project project)
    throws IOException {
    final Map<String, Object> props = createProperties(className, packageName, accessModifier);

    final FileTemplate template = FileTemplateManager.getInstance(project).getInternalTemplate(getTemplateName(isInterface, false,
                                                                                                        ourPreviousInterfaceTemplateName,
                                                                                                        ourPreviousClassTemplateName,
                                                                                                        project));

    String mergedText = ClassLoaderUtil.computeWithClassLoader(
      ActionScriptCreateClassOrInterfaceFix.class.getClassLoader(),
      () -> template.getText(props));
    return StringUtil.convertLineSeparators(mergedText);
  }

  public static Map<String, Object> createProperties(String className, String packageName, String accessModifier) {
    final Map<String, Object> props = new HashMap<>();
    FileTemplateUtil.putAll(props, FileTemplateManager.getDefaultInstance().getDefaultProperties());
    props.put(FileTemplate.ATTRIBUTE_NAME, className);
    props.put(FileTemplate.ATTRIBUTE_PACKAGE_NAME, packageName);
    props.put(ACCESS_MODIFIER_PROPERTY, accessModifier);
    return props;
  }

  private static String getTemplateName(boolean isInterface,
                                        boolean hasSuperClassifier,
                                        @Nullable final String interfaceDefault,
                                        @Nullable final String classDefault, Project project) {
    if (isInterface) {
      if (interfaceDefault != null && FileTemplateManager.getInstance(project).getTemplate(interfaceDefault) != null) {
        return interfaceDefault;
      }
      else {
        return ACTION_SCRIPT_INTERFACE_TEMPLATE_NAME;
      }
    }
    else {
      FileTemplate t;
      if (classDefault != null &&
          (t = FileTemplateManager.getInstance(project).getTemplate(classDefault)) != null && !(t instanceof BundledFileTemplate)) {
        return classDefault;
      }
      return hasSuperClassifier
             ? ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME
             : ACTION_SCRIPT_CLASS_TEMPLATE_NAME;
    }
  }

  private static PsiElement createClass(String className, String packageName, PsiDirectory directory, final String templateName)
    throws Exception {
    return doCreateClass(className, packageName, directory, templateName, null);
  }

  public static void createClass(String className,
                                 String packageName,
                                 final PsiDirectory directory,
                                 boolean isInterface) throws Exception {
    createClass(className, packageName, directory,
                getTemplateName(isInterface, false, ourPreviousInterfaceTemplateName, ourPreviousClassTemplateName, directory.getProject()));
  }

  private static PsiElement doCreateClass(final String className,
                                          final String packageName,
                                          final PsiDirectory directory,
                                          final String templateName,
                                          @Nullable final Map<String, Object> additionalTemplateProperties)
    throws Exception {
    final Map<String, Object> props =
      createProperties(className, packageName, JSFormatUtil.formatVisibility(JSAttributeList.AccessType.PUBLIC));
    if (additionalTemplateProperties != null) {
      props.putAll(additionalTemplateProperties);
    }
    // getInternalTemplate() will fallback to normal templates
    FileTemplate template = ClassLoaderUtil.computeWithClassLoader(
      ActionScriptCreateClassOrInterfaceFix.class.getClassLoader(),
      () -> FileTemplateManager.getInstance(directory.getProject()).getInternalTemplate(templateName));
    return FileTemplateUtil.createFromTemplate(template, className, props, directory, ActionScriptCreateClassOrInterfaceFix.class.getClassLoader());
  }

  @Override
  public boolean isAvailable(@NotNull Project project, PsiElement element, Editor editor, PsiFile file) {
    if (!DialectDetector.isActionScript(file)) return false;
    if (!myIdentifierIsValid) return false;

    final Module module = ModuleUtilCore.findModuleForPsiElement(file);
    return module != null && ModuleRootManager.getInstance(module).getSourceRoots().length > 0;
  }

  public static Icon getTemplateIcon(FileTemplate t) {
    if (ACTION_SCRIPT_CLASS_TEMPLATE_NAME.equals(t.getName()) ||
        ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME.equals(t.getName())) {
      return JSIconProvider.AS_INSTANCE.getClassIcon();
    }
    if (ACTION_SCRIPT_INTERFACE_TEMPLATE_NAME.equals(t.getName())) {
      return JSIconProvider.AS_INSTANCE.getInterfaceIcon();
    }
    return FileTypeManager.getInstance().getFileTypeByExtension(t.getExtension()).getIcon();
  }

  public static @NlsContexts.Label String getTemplateShortName(@NlsSafe String templateName) {
    if (ACTION_SCRIPT_CLASS_TEMPLATE_NAME.equals(templateName)) {
      return JavaScriptBundle.message("class.template.title");
    }
    if (ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME.equals(templateName)) {
      return JavaScriptBundle.message("class.with.supers.template.title");
    }
    if (ACTION_SCRIPT_INTERFACE_TEMPLATE_NAME.equals(templateName)) {
      return JavaScriptBundle.message("interface.template.title");
    }
    return templateName;
  }

  public static List<FileTemplate> getApplicableTemplates(final Collection<String> extensions, Project project) {
    List<FileTemplate> applicableTemplates = new SmartList<>();

    Condition<FileTemplate> filter = fileTemplate -> extensions.contains(fileTemplate.getExtension());

    applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance(project).getInternalTemplates(), filter)); // bundled go first
    applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance(project).getAllTemplates(), filter));
    return applicableTemplates;
  }
}
