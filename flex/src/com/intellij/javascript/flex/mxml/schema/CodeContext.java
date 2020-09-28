// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml.schema;

import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.javascript.flex.FlexMxmlLanguageAttributeNames;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexReferenceContributor;
import com.intellij.javascript.flex.FlexStateElementNames;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.resolve.SwcCatalogXmlUtil;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.index.JSPackageIndexInfo;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Maxim.Mossienko
 */
public class CodeContext {
  private static final CodeContext EMPTY = new CodeContext(null, null, null);
  private static final String CLASS_FACTORY = "mx.core.ClassFactory";

  @NonNls static final String DEFINITION_TAG_NAME = "Definition";
  @NonNls public static final String REPARENT_TAG_NAME = "Reparent";
  @NonNls public static final String TARGET_ATTR_NAME = "target";
  public static final String AS3_VEC_VECTOR_QUALIFIED_NAME = "__AS3__.vec.Vector";
  static final String FORMAT_ATTR_NAME = "format";
  static final String TWO_WAY_ATTR_NAME = "twoWay";
  private static final String XML_CLASS = "XML";
  private static final String XMLNODE_CLASS = "flash.xml.XMLNode";

  final static String[] GUMBO_ATTRIBUTES = {FlexStateElementNames.INCLUDE_IN, FlexStateElementNames.EXCLUDE_FROM,
    FlexStateElementNames.ITEM_CREATION_POLICY, FlexStateElementNames.ITEM_DESTRUCTION_POLICY};

  // Component name to descriptor
  private final Map<String, ClassBackedElementDescriptor> myNameToDescriptorsMap;
  final GlobalSearchScope scope;
  final String namespace;
  final Module module;
  private final Set<Object> dependencies = new THashSet<>();

  CodeContext(String _namespace, Module _module, GlobalSearchScope scope) {
    myNameToDescriptorsMap = new THashMap<>(100);
    namespace = _namespace;
    module = _module;
    this.scope = scope;
    if (JavaScriptSupportLoader.isLanguageNamespace(namespace)) {
      addPredefinedTags(this);

      // XML and XMLList language tags represent respective classes, so they must not be marked as 'predefined'
      myNameToDescriptorsMap.put(MxmlJSClass.XML_TAG_NAME, createXmlTagDescriptor(this, XML_CLASS));
      myNameToDescriptorsMap.put(MxmlJSClass.XMLLIST_TAG_NAME,
                                 new ClassBackedElementDescriptor(MxmlJSClass.XMLLIST_TAG_NAME, this, module.getProject(), false));
    }
  }

  private void putDescriptor(final String name, final ClassBackedElementDescriptor descriptor, final boolean addGumboAttributesIfNeeded) {
    if (JavaScriptSupportLoader.isLanguageNamespace(namespace)) {
      if (MxmlJSClass.XML_TAG_NAME.equals(name) || MxmlJSClass.XMLLIST_TAG_NAME.equals(name)) {
        // XML and XMLList are added in constructor
        return;
      }

      if (JSCommonTypeNames.STRING_CLASS_NAME.equals(name) ||
          JSCommonTypeNames.BOOLEAN_CLASS_NAME.equals(name) ||
          JSCommonTypeNames.INT_TYPE_NAME.equals(name) ||
          JSCommonTypeNames.UINT_TYPE_NAME.equals(name) ||
          JSCommonTypeNames.NUMBER_CLASS_NAME.equals(name)) {
        descriptor.addPredefinedMemberDescriptor(
          new AnnotationBackedDescriptorImpl(FlexReferenceContributor.SOURCE_ATTR_NAME, descriptor, true, null, null, null));
      }
    }

    if (JavaScriptSupportLoader.MXML_URI3.equals(namespace) && JSCommonTypeNames.VECTOR_CLASS_NAME.equals(name)) {
      final AnnotationBackedDescriptorImpl typeDescriptor = new AnnotationBackedDescriptorImpl("type", descriptor, true, null, null, null);
      typeDescriptor.setRequired(true);
      descriptor.addPredefinedMemberDescriptor(typeDescriptor);
    }
    else if (addGumboAttributesIfNeeded && FlexSdkUtils.isFlex4Sdk(FlexUtils.getSdkForActiveBC(module))) {
      // The most correct way is not to check sdk but to check language level of current mxml file. But it is impossible because CodeContext is not per file.
      for (String gumboAttr : GUMBO_ATTRIBUTES) {
        descriptor.addPredefinedMemberDescriptor(new AnnotationBackedDescriptorImpl(gumboAttr, descriptor, true, null, null, null));
      }
    }
    myNameToDescriptorsMap.put(name, descriptor);
  }

  private void addDependency(final @NotNull Object dep) {
    dependencies.add(dep);
  }

  Object[] getDependencies() {
    return dependencies.toArray();
  }

  public static CodeContext getContext(final String namespace, final Module module) {
    return getContext(namespace, module, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
  }

  public static CodeContext getContext(String namespace, Module module, @NotNull GlobalSearchScope scope) {
    if (StringUtil.isEmptyOrSpaces(namespace) ||
        module == null || module.isDisposed() || !(ModuleType.get(module) instanceof FlexModuleType)) {
      return EMPTY;
    }

    final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
    if (bc == null) return EMPTY;

    CodeContext codeContext;

    synchronized (CodeContext.class) {
      if (isStdNamespace(namespace)) {
        return getStdCodeContext(namespace, module, scope, bc);
      }

      final CodeContextHolder contextHolder = CodeContextHolder.getInstance(module.getProject());
      codeContext = contextHolder.getCodeContext(namespace, module, scope);

      if (codeContext == null) {
        codeContext = createCodeContext(namespace, module, bc, scope);
        if (codeContext.getAllDescriptorsSize() > 0) {
          // avoid adding of incorrect namespaces that appear during completion like "http://www.adobe.IntellijIdeaRulezzz com/2006/mxml"
          contextHolder.putCodeContext(namespace, module, scope, codeContext);
        }
      }
    }

    return codeContext;
  }

  public static boolean isStdNamespace(final String namespace) {
    return FlexUtils.isMxmlNs(namespace);
  }

  public static boolean isPackageBackedNamespace(final @NotNull String namespace) {
    return namespace.equals("*") || namespace.endsWith(".*");
  }

  private static CodeContext createCodeContext(String namespace, Module module, FlexBuildConfiguration bc, GlobalSearchScope scope) {
    if (!isPackageBackedNamespace(namespace)) {
      return createCodeContextFromLibraries(namespace, module, scope, bc);
    }

    final Project project = module.getProject();
    final CodeContext codeContext = new CodeContext(namespace, module, scope);
    final String packageName = namespace.endsWith(".*") ? namespace.substring(0, namespace.length() - 2) : "";

    JSPackageIndex.processElementsInScope(packageName, null, new JSPackageIndex.PackageElementsProcessor() {
      @Override
      public boolean process(VirtualFile file, @NotNull String name, JSPackageIndexInfo.Kind kind, boolean isPublic) {
        if (kind != JSPackageIndexInfo.Kind.CLASS) return true;

        if (JavaScriptSupportLoader.isMxmlOrFxgFile(file)) {
          addFileBackedDescriptor(file, codeContext, packageName, project);
          PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(file.getParent());
          if (psiDirectory != null) codeContext.addDependency(psiDirectory);
        }
        else {
          String qName = JSPackageIndex.buildQualifiedName(packageName, name);
          codeContext.putDescriptor(name, new ClassBackedElementDescriptor(name, qName, codeContext, project), true);
          final PsiFile containingFile = PsiManager.getInstance(project).findFile(file);
          if (containingFile == null) return true;
          final PsiDirectory containingDirectory = containingFile.getParent();
          final Object dependency = containingDirectory != null ? containingDirectory : containingFile;
          codeContext.addDependency(dependency);
        }

        return true;
      }
    }, scope, project);

    return codeContext;
  }

  private static void handleSwcFromSdk(@NotNull Module module, @NotNull GlobalSearchScope scope, @NotNull FlexBuildConfiguration bc) {
    final Sdk sdk = bc.getSdk();
    if (sdk == null) return;

    final Map<String, CodeContext> contextsOfModule = new THashMap<>();
    for (final VirtualFile file : sdk.getRootProvider().getFiles(OrderRootType.CLASSES)) {
      final String swcPath = VirtualFileManager.extractPath(StringUtil.trimEnd(file.getUrl(), JarFileSystem.JAR_SEPARATOR));
      if (BCUtils.getSdkEntryLinkageType(swcPath, bc) != null) {
        handleFileDependency(module, contextsOfModule, file);
      }
    }
    final CodeContextHolder contextHolder = CodeContextHolder.getInstance(module.getProject());
    for (final Map.Entry<String, CodeContext> entry : contextsOfModule.entrySet()) {
      contextHolder.putCodeContext(entry.getKey(), module, scope, entry.getValue());
    }
  }

  private static CodeContext createCodeContextFromLibraries(String namespace,
                                                            Module module,
                                                            GlobalSearchScope scope,
                                                            FlexBuildConfiguration bc) {
    final Map<String, CodeContext> contextsOfModule = new THashMap<>();
    final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);

    // TODO: this code should not be invoked per ns!
    // If fixed - then method usage at getStdCodeContext() must be changed to make sure that all namespaces handled at that point.
    for (DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry.getDependencyType().getLinkageType() == LinkageType.LoadInRuntime) continue;

      if (entry instanceof BuildConfigurationEntry) {
        final FlexBuildConfiguration bcDependency = ((BuildConfigurationEntry)entry).findBuildConfiguration();
        if (bcDependency != null && bcDependency.getOutputType() == OutputType.Library) {
          addComponentsFromManifests(module, contextsOfModule, bcDependency, true);
        }
      }
      else if (entry instanceof ModuleLibraryEntry) {
        final LibraryOrderEntry orderEntry = FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, rootManager);
        if (orderEntry != null) {
          for (VirtualFile file : orderEntry.getRootFiles(OrderRootType.CLASSES)) {
            handleFileDependency(module, contextsOfModule, file);
          }
        }
      }
      else if (entry instanceof SharedLibraryEntry) {
        final Library library = FlexProjectRootsUtil.findOrderEntry(module.getProject(), (SharedLibraryEntry)entry);
        if (library != null) {
          for (VirtualFile file : library.getFiles(OrderRootType.CLASSES)) {
            handleFileDependency(module, contextsOfModule, file);
          }
        }
      }
    }

    addComponentsFromManifests(module, contextsOfModule, bc, false);

    final CodeContextHolder contextHolder = CodeContextHolder.getInstance(module.getProject());
    for (Map.Entry<String, CodeContext> entry : contextsOfModule.entrySet()) {
      contextHolder.putCodeContext(entry.getKey(), module, scope, entry.getValue());
    }

    CodeContext codeContext = contextsOfModule.get(namespace);
    if (codeContext == null) {
      codeContext = EMPTY;
    }
    return codeContext;
  }

  private static void addComponentsFromManifests(final Module module, final Map<String, CodeContext> contextsOfModule,
                                                 final FlexBuildConfiguration bc, boolean onlyIncludedInSwc) {
    final String configFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    final VirtualFile configFile = StringUtil.isEmptyOrSpaces(configFilePath)
                                   ? null
                                   : LocalFileSystem.getInstance().findFileByPath(configFilePath);

    for (FlexCompilerConfigFileUtil.NamespacesInfo info : FlexCompilerConfigFileUtil.getNamespacesInfos(configFile)) {
      if (onlyIncludedInSwc && !info.includedInSwc) continue;

      final VirtualFile manifestFile = VfsUtilCore.findRelativeFile(info.manifest, configFile);
      if (manifestFile != null && !manifestFile.isDirectory()) {
        processManifestFile(module, contextsOfModule, manifestFile, info.namespace, configFile);
      }
    }

    FlexUtils.processCompilerOption(module, bc, "compiler.namespaces.namespace", namespaceAndManifest -> {
      // namespaces configured in IDEA are always included in SWC
      final VirtualFile manifestFile = VfsUtilCore.findRelativeFile(namespaceAndManifest.second, configFile);
      if (manifestFile != null && !manifestFile.isDirectory()) {
        processManifestFile(module, contextsOfModule, manifestFile, namespaceAndManifest.first, configFile);
      }
      return true;
    });
  }

  private static void handleFileDependency(Module module, Map<String, CodeContext> contextsOfModule, VirtualFile file) {
    if (FileTypeRegistry.getInstance().isFileOfType(file, ArchiveFileType.INSTANCE) &&
        ("swc".equalsIgnoreCase(file.getExtension()) ||
         "ane".equalsIgnoreCase(file.getExtension()) ||
         "jar".equalsIgnoreCase(file.getExtension()))) {
      final VirtualFile jarRoot = file.getFileSystem() instanceof JarFileSystem
                                  ? file : JarFileSystem.getInstance().getJarRootForLocalFile(file);
      if (jarRoot == null) return;
      final VirtualFile catalog = jarRoot.findChild("catalog.xml");
      if (catalog == null) return;

      processCatalogFile(module, contextsOfModule, catalog);
    }
  }

  private static void processCatalogFile(final Module module,
                                         final Map<String, CodeContext> contextsOfModule,
                                         final VirtualFile catalogFile) {
    SwcCatalogXmlUtil.processComponentsFromCatalogXml(catalogFile, componentFromCatalogXml -> {
      CodeContext codeContext = identifyCodeContext(module, contextsOfModule, componentFromCatalogXml.myUri);
      codeContext.addDependency(catalogFile);
      codeContext.putDescriptor(componentFromCatalogXml.myName,
                                new ClassBackedElementDescriptor(componentFromCatalogXml.myName,
                                                                 componentFromCatalogXml.myClassFqn,
                                                                 codeContext,
                                                                 module.getProject(),
                                                                 false,
                                                                 componentFromCatalogXml.myIcon),
                                true);
    });
  }

  private static CodeContext identifyCodeContext(Module module, Map<String, CodeContext> contextsOfModule, String uri) {
    CodeContext codeContext;
    if (isStdNamespace(uri)) {
      final CodeContextHolder contextHolder = CodeContextHolder.getInstance(module.getProject());
      codeContext = contextHolder.getStandardContext(uri, module);
      if (codeContext == null) {
        codeContext = new CodeContext(uri, module, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
        contextHolder.putStandardContext(uri, module, codeContext);
      }
    }
    else {
      codeContext = contextsOfModule.get(uri);
      if (codeContext == null) {
        codeContext = new CodeContext(uri, module, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
        contextsOfModule.put(uri, codeContext);
      }
    }
    return codeContext;
  }

  private static void processManifestFile(final Module module,
                                          final Map<String, CodeContext> contextsOfModule,
                                          final VirtualFile manifestFile,
                                          final String uri,
                                          final @Nullable ModificationTracker dependency) {
    final CodeContext codeContext = identifyCodeContext(module, contextsOfModule, uri);
    if (dependency != null) {
      codeContext.addDependency(dependency);
    }

    processManifestFile(manifestFile, codeContext);
  }

  private static void processManifestFile(final VirtualFile manifestFile, final CodeContext codeContext) {
    codeContext.addDependency(manifestFile);

    SwcCatalogXmlUtil.processManifestFile(manifestFile, componentFromManifest -> codeContext.putDescriptor(componentFromManifest.myComponentName,
                                                                                                       new ClassBackedElementDescriptor(componentFromManifest.myComponentName,
                                                               componentFromManifest.myClassFqn,
                                                               codeContext,
                                                               codeContext.module.getProject()),
                                                                                                       true));
  }

  private static void addFileBackedDescriptor(final VirtualFile file,
                                              final CodeContext codeContext,
                                              final String packageName,
                                              final Project project) {
    codeContext.putDescriptor(file.getNameWithoutExtension(), new MxmlBackedElementDescriptor(
      JSPackageIndex.buildQualifiedName(packageName, file.getNameWithoutExtension()), codeContext, project, file), true);
  }

  private static CodeContext getStdCodeContext(final String namespace, final Module module, GlobalSearchScope scope, final FlexBuildConfiguration bc) {
    final CodeContextHolder contextHolder = CodeContextHolder.getInstance(module.getProject());

    if (!contextHolder.areSdkComponentsHandledForModule(module)) { // handleAllStandardManifests only once per module
      handleAllStandardManifests(module, bc);
      handleSwcFromSdk(module, scope, bc); //swc files attached to Flex SDK may contribute to standard context
      createCodeContextFromLibraries(namespace, module, scope, bc); // other libraries may contribute to standard context
      contextHolder.setSdkComponentsHandledForModule(module);
    }

    final CodeContext context = contextHolder.getStandardContext(namespace, module);
    return context != null ? context : EMPTY;
  }

  @Nullable
  public XmlElementDescriptor getElementDescriptor(final @NonNls String localName, final @Nullable XmlTag tag) {
    ClassBackedElementDescriptor descriptor = this == EMPTY ? null : myNameToDescriptorsMap.get(localName);

    if (tag != null && MxmlJSClass.XML_TAG_NAME.equals(localName)
        && JavaScriptSupportLoader.isLanguageNamespace(tag.getNamespace())) {
      final String format = tag.getAttributeValue(FORMAT_ATTR_NAME);
      if (format != null && !"e4x".equalsIgnoreCase(format)) {
        return createXmlTagDescriptor(this, XMLNODE_CLASS);
      }
    }
    return descriptor;
  }

  public void appendDescriptorsWithAllowedDeclaration(final Collection<XmlElementDescriptor> resultList) {
    for (ClassBackedElementDescriptor descriptor : myNameToDescriptorsMap.values()) {
      if (checkDeclaration(descriptor)) {
        resultList.add(descriptor);
      }
    }
  }

  public static boolean checkDeclaration(final @NotNull ClassBackedElementDescriptor descriptor) {
    final PsiElement declaration = descriptor.getDeclaration();
    return ((declaration instanceof XmlFile || ((declaration instanceof JSClass) && hasDefaultConstructor((JSClass)declaration))));
  }

  public XmlElementDescriptor[] getDescriptorsWithAllowedDeclaration() {
    THashSet<XmlElementDescriptor> descriptors = new THashSet<>();
    appendDescriptorsWithAllowedDeclaration(descriptors);
    return descriptors.toArray(new XmlElementDescriptor[descriptors.size()]);
  }

  public static boolean hasDefaultConstructor(final @NotNull JSClass jsClass) {
    final JSFunction constructor = jsClass.getConstructor();
    final JSParameter[] parameters = constructor == null ? null : constructor.getParameterVariables();
    return parameters == null || parameters.length == 0 || parameters[0].isOptional() || parameters[0].isRest();
  }

  public int getAllDescriptorsSize() {
    return myNameToDescriptorsMap.size();
  }

  @Nullable
  public ClassBackedElementDescriptor getElementDescriptor(@NotNull final String name, @NotNull final String qname) {
    ClassBackedElementDescriptor descriptor = myNameToDescriptorsMap.get(name);

    if (descriptor != null && !qname.equals(descriptor.getQualifiedName())) {
      descriptor = null;
    }

    if (descriptor == null && !name.equals(qname)) descriptor = myNameToDescriptorsMap.get(qname);

    return descriptor;
  }

  private static void handleAllStandardManifests(final Module module, @NotNull final FlexBuildConfiguration bc) {
    final Sdk sdk = bc.getSdk();
    final String homePath = sdk == null ? null : sdk.getHomePath();
    final VirtualFile sdkHome = homePath == null ? null : LocalFileSystem.getInstance().findFileByPath(homePath);
    if (sdkHome == null || sdk.getSdkType() == FlexmojosSdkType.getInstance()) return;

    FlexSdkUtils.processStandardNamespaces(bc, (namespace1, relativePath) -> {
      final VirtualFile manifestFile = VfsUtilCore.findRelativeFile(relativePath, sdkHome);

      if (manifestFile != null) {
        handleStandardManifest(module, namespace1, manifestFile, sdkHome);
      }
    });
  }

  private static void handleStandardManifest(final Module module,
                                             final String namespace,
                                             final VirtualFile manifestFile,
                                             final VirtualFile flexSdkRoot) {
    final CodeContextHolder contextHolder = CodeContextHolder.getInstance(module.getProject());
    CodeContext _context = contextHolder.getStandardContext(namespace, module);
    if (_context == null) {
      _context = new CodeContext(namespace, module, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
      contextHolder.putStandardContext(namespace, module, _context);
    }

    final CodeContext context = _context;
    context.addDependency(flexSdkRoot);

    processManifestFile(manifestFile, context);
  }

  private static void addPredefinedTags(final CodeContext codeContext) {
    Collection<String> predefinedTags = new ArrayList<>();
    predefinedTags.add(FlexPredefinedTagNames.SCRIPT);
    predefinedTags.add(FlexPredefinedTagNames.STYLE);
    predefinedTags.add(FlexPredefinedTagNames.METADATA);

    if (JavaScriptSupportLoader.MXML_URI3.equals(codeContext.namespace)) {
      predefinedTags.add(FlexPredefinedTagNames.DECLARATIONS);
      predefinedTags.add(MxmlJSClass.PRIVATE_TAG_NAME);
      predefinedTags.add(FlexPredefinedTagNames.LIBRARY);

      final ClassBackedElementDescriptor definitionDescriptor =
        new ClassBackedElementDescriptor(DEFINITION_TAG_NAME, codeContext, codeContext.module.getProject(), true);
      definitionDescriptor.addPredefinedMemberDescriptor(
        new AnnotationBackedDescriptorImpl(MxmlLanguageTagsUtil.NAME_ATTRIBUTE, definitionDescriptor, true, null, null, null));
      codeContext.putDescriptor(DEFINITION_TAG_NAME, definitionDescriptor, false);

      final ClassBackedElementDescriptor reparentDescriptor =
        new ClassBackedElementDescriptor(REPARENT_TAG_NAME, codeContext, codeContext.module.getProject(), true);
      reparentDescriptor
        .addPredefinedMemberDescriptor(new AnnotationBackedDescriptorImpl(TARGET_ATTR_NAME, reparentDescriptor, true, null, null, null));
      reparentDescriptor.addPredefinedMemberDescriptor(
        new AnnotationBackedDescriptorImpl(FlexStateElementNames.INCLUDE_IN, reparentDescriptor, true, null, null, null));
      reparentDescriptor.addPredefinedMemberDescriptor(
        new AnnotationBackedDescriptorImpl(FlexStateElementNames.EXCLUDE_FROM, reparentDescriptor, true, null, null, null));
      codeContext.putDescriptor(REPARENT_TAG_NAME, reparentDescriptor, false);
    }

    for (String predefined : predefinedTags) {
      codeContext
        .putDescriptor(predefined, new ClassBackedElementDescriptor(predefined, codeContext, codeContext.module.getProject(), true), false);
    }

    final ClassBackedElementDescriptor modelDescriptor =
      new ClassBackedElementDescriptor(FlexPredefinedTagNames.MODEL, codeContext, codeContext.module.getProject(), true);
    modelDescriptor.addPredefinedMemberDescriptor(
      new AnnotationBackedDescriptorImpl(FlexMxmlLanguageAttributeNames.ID, modelDescriptor, true, null, null, null));
    modelDescriptor.addPredefinedMemberDescriptor(
      new AnnotationBackedDescriptorImpl(FlexReferenceContributor.SOURCE_ATTR_NAME, modelDescriptor, true, null, null, null));
    codeContext.putDescriptor(FlexPredefinedTagNames.MODEL, modelDescriptor, false);

    final ClassBackedElementDescriptor bindingDescriptor =
      new ClassBackedElementDescriptor(FlexPredefinedTagNames.BINDING, codeContext, codeContext.module.getProject(), true);
    bindingDescriptor.addPredefinedMemberDescriptor(
      new AnnotationBackedDescriptorImpl(FlexReferenceContributor.SOURCE_ATTR_NAME, bindingDescriptor, true, null, null, null));
    bindingDescriptor.addPredefinedMemberDescriptor(
      new AnnotationBackedDescriptorImpl(FlexReferenceContributor.DESTINATION_ATTR_NAME, bindingDescriptor, true, null, null, null));

    if (JavaScriptSupportLoader.MXML_URI3.equals(codeContext.namespace)) {
      bindingDescriptor.addPredefinedMemberDescriptor(
        new AnnotationBackedDescriptorImpl(TWO_WAY_ATTR_NAME, bindingDescriptor, true, null, null, null));
    }

    codeContext.putDescriptor(FlexPredefinedTagNames.BINDING, bindingDescriptor, false);

    // not predefined!
    final ClassBackedElementDescriptor componentDescriptor =
      new ClassBackedElementDescriptor(XmlBackedJSClassImpl.COMPONENT_TAG_NAME, CLASS_FACTORY, codeContext,
                                       codeContext.module.getProject());
    codeContext.putDescriptor(XmlBackedJSClassImpl.COMPONENT_TAG_NAME, componentDescriptor, false);
  }

  private static ClassBackedElementDescriptor createXmlTagDescriptor(final CodeContext codeContext, final String backedClassFqn) {
    final ClassBackedElementDescriptor xmlDescriptor =
      new ClassBackedElementDescriptor(MxmlJSClass.XML_TAG_NAME, backedClassFqn, codeContext, codeContext.module.getProject());
    xmlDescriptor.addPredefinedMemberDescriptor(
      new AnnotationBackedDescriptorImpl(FlexReferenceContributor.SOURCE_ATTR_NAME, xmlDescriptor, true, null, null, null));
    xmlDescriptor
      .addPredefinedMemberDescriptor(new AnnotationBackedDescriptorImpl(FORMAT_ATTR_NAME, xmlDescriptor, true, null, null, null));
    return xmlDescriptor;
  }
}
