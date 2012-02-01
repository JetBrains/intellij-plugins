package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkAdditionalData;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.utils.MavenLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Flexmojos3Configurator {
  private static final String FLEX_COMPILER_GROUP_ID = "com.adobe.flex";
  private static final String FLEX_COMPILER_ARTIFACT_ID = "compiler";

  private static final String FLEX_FRAMEWORK_GROUP_ID = "com.adobe.flex.framework";
  private static final String FLEX_FRAMEWORK_ARTIFACT_ID = "flex-framework";
  private static final String AIR_FRAMEWORK_ARTIFACT_ID = "air-framework";

  private static final Pattern[] ADDITIONAL_JAR_NAME_PATTERNS_TO_INCLUDE_IN_FLEXMOJOS_SDK_CLASSPATH =
    {Pattern.compile("afe"), Pattern.compile("aglj[0-9]+"), Pattern.compile("flex-fontkit"), Pattern.compile("license"),
      Pattern.compile("rideau")};

  protected Module myModule;
  private final MavenModifiableModelsProvider myModelsProvider;
  private final FlexProjectConfigurationEditor myFlexEditor;
  protected MavenProjectsTree myMavenTree;
  protected final MavenProject myMavenProject;
  private final MavenPlugin myFlexmojosPlugin;
  private final List<String> myCompiledLocales;
  private final List<String> myRuntimeLocales;
  protected final @Nullable String myClassifier;
  private final FlexConfigInformer myInformer;


  public Flexmojos3Configurator(final Module module,
                                final MavenModifiableModelsProvider modelsProvider,
                                final FlexProjectConfigurationEditor flexEditor,
                                final MavenProjectsTree mavenTree, final MavenProject mavenProject,
                                final MavenPlugin flexmojosPlugin,
                                final List<String> compiledLocales,
                                final List<String> runtimeLocales,
                                final FlexConfigInformer informer) {
    myModelsProvider = modelsProvider;
    myMavenTree = mavenTree;
    myCompiledLocales = compiledLocales;
    myRuntimeLocales = runtimeLocales;
    myModule = module;
    myFlexEditor = flexEditor;
    myMavenProject = mavenProject;
    myFlexmojosPlugin = flexmojosPlugin;
    myInformer = informer;
    final Element configurationElement = flexmojosPlugin.getConfigurationElement();
    myClassifier =
      configurationElement == null ? null : configurationElement.getChildTextNormalize("classifier", configurationElement.getNamespace());
  }

  public void configureAndAppendTasks(final List<MavenProjectsProcessorTask> postTasks) {
    for (ModifiableFlexIdeBuildConfiguration oldBC : myFlexEditor.getConfigurations(myModule)) {
      myFlexEditor.removeConfiguration(oldBC);
    }

    final ModifiableFlexIdeBuildConfiguration bc = myFlexEditor.createConfiguration(myModule);
    bc.setName(myModule.getName());

    final TargetPlatform targetPlatform = handleDependencies(bc);
    bc.setTargetPlatform(targetPlatform);
    bc.setPureAs(false);
    final boolean app = FlexmojosImporter.isFlexApp(myMavenProject);
    bc.setOutputType(app ? OutputType.Application : OutputType.Library);

    final Element configurationElement = myFlexmojosPlugin.getConfigurationElement();
    if (app && configurationElement != null) {
      final String sourceFile = configurationElement.getChildTextNormalize("sourceFile");
      if (sourceFile != null && (sourceFile.endsWith(".as") || sourceFile.endsWith(".mxml"))) {
        bc.setMainClass(sourceFile.substring(0, sourceFile.lastIndexOf(".")).replace("/", ".").replace("\\", "."));
      }
      // todo set later when config is generated?
    }

    final String outputFilePath = getOutputFilePath();
    bc.setOutputFileName(PathUtil.getFileName(outputFilePath));
    bc.setOutputFolder(PathUtil.getParentPath(outputFilePath));

    setupSdk(bc);

    final String locales = StringUtil.join(myCompiledLocales, String.valueOf(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR));
    bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.locale", locales));

    final String configFilePath = getCompilerConfigFilePath();
    bc.getCompilerOptions().setAdditionalConfigFilePath(configFilePath);

/*
    reimportRuntimeLocalesFacets(project, module, modelsProvider);
    reimportMxModuleFacets(project, module, modelsProvider);

    preProcessResourceFacets(module, mavenProject, modifiableModelsProvider);
    preprocessMxModuleFacets(module, mavenProject, modifiableModelsProvider);
*/

    if (FlexCompilerProjectConfiguration.getInstance(myModule.getProject()).GENERATE_FLEXMOJOS_CONFIGS) {
      if (StringUtil.compareVersionNumbers(myFlexmojosPlugin.getVersion(), "3.4") >= 0) {
        appendGenerateConfigTask(postTasks, configFilePath);
      }
      else {
        myInformer.showFlexConfigWarningIfNeeded(myModule.getProject());
      }
    }

    /*if (isGenerateFlexConfigFilesForMxModules()) {
      for (final FlexFacet flexFacet : facetModel.getFacetsByType(myFacetType.getId())) {
        if (isMxModuleFacet(flexFacet)) {
          postTasks.add(new GenerateFlexConfigFilesForMxModulesTask(getCompilerConfigXmlSuffix(), module, project, mavenTree));
          break;
        }
      }
    }*/
  }

  private TargetPlatform handleDependencies(final ModifiableFlexIdeBuildConfiguration bc) {
    boolean playerglobal = false;
    boolean airglobal = false;
    boolean mobilecomponents = false;

    final ModifiableRootModel rootModel = myModelsProvider.getRootModel(myModule);
    for (OrderEntry entry : rootModel.getOrderEntries()) {
      if ((entry instanceof ModuleOrderEntry)) {
        rootModel.removeOrderEntry(entry);

        final String dependencyModuleName = ((ModuleOrderEntry)entry).getModuleName();
        final ModifiableBuildConfigurationEntry bcEntry =
          myFlexEditor.createBcEntry(bc.getDependencies(), dependencyModuleName, dependencyModuleName);
        bc.getDependencies().getModifiableEntries().add(bcEntry);

        continue;
      }

      if (!(entry instanceof LibraryOrderEntry)) continue;

      if (!LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)entry).getLibraryLevel())) continue;
      final Library library = ((LibraryOrderEntry)entry).getLibrary();
      if (library == null || !MavenRootModelAdapter.isMavenLibrary(library)) continue;

      final String libraryName = library.getName();
      if (libraryName.contains(":rb.swc:")) {
        // fix rb.swc placeholders to real SWCs according to used locales
        final Library.ModifiableModel libraryModifiableModel = myModelsProvider.getLibraryModel(library);
        for (final String rbSwcPlaceholdersUrl : findRbSwcPlaceholderUrls(libraryModifiableModel)) {
          final Collection<String> rootsToAdd = getRbSwcUrlsForCompiledLocales(rbSwcPlaceholdersUrl);
          libraryModifiableModel.removeRoot(rbSwcPlaceholdersUrl, OrderRootType.CLASSES);
          for (final String rootToAdd : rootsToAdd) {
            if (!ArrayUtil.contains(rootToAdd, libraryModifiableModel.getUrls(OrderRootType.CLASSES))) {
              libraryModifiableModel.addRoot(rootToAdd, OrderRootType.CLASSES);
            }
          }
          // sources and docs could be updated as well, but currently they are always senseless, because they do not exist
        }
      }

      if (libraryName.contains(":swc:") || libraryName.contains(":rb.swc:")) {
        playerglobal |= libraryName.contains("playerglobal");
        airglobal |= libraryName.contains("airglobal");
        mobilecomponents |= libraryName.contains("mobilecomponents");

        final LibraryType<?> type = ((LibraryEx)library).getType();
        final FlexLibraryType flexLibraryType = FlexLibraryType.getInstance();

        if (type != flexLibraryType) {
          if (type == null) {
            final LibraryEx.ModifiableModelEx libraryModel = (LibraryEx.ModifiableModelEx)myModelsProvider.getLibraryModel(library);
            libraryModel.setType(flexLibraryType);
            libraryModel.setProperties(flexLibraryType.createDefaultProperties());
          }
        }

        rootModel.removeOrderEntry(entry);
        final ModifiableDependencyEntry sharedLibraryEntry =
          myFlexEditor.createSharedLibraryEntry(bc.getDependencies(), ((LibraryOrderEntry)entry).getLibraryName(),
                                                ((LibraryOrderEntry)entry).getLibraryLevel());
        bc.getDependencies().getModifiableEntries().add(sharedLibraryEntry);
      }
      else {
        MavenLog.LOG.warn("Unexpected library in dependencies of module '" + myModule.getName() + "': " + libraryName
                          + ". Only swc and rb.swc libraries expected.");
      }
    }

    // todo better target platform detection if both airglobal and playerglobal present?
    return mobilecomponents && airglobal ? TargetPlatform.Mobile
                                         : airglobal && !playerglobal ? TargetPlatform.Desktop
                                                                      : TargetPlatform.Web;
  }

  /**
   * @return resource bundle placeholder SWCs, i.e. library roots that have no locale classifier (for example framework-3.3.0.4852.rb.swc)
   */
  private static Collection<String> findRbSwcPlaceholderUrls(final Library.ModifiableModel libraryModifiableModel) {
    final Collection<String> rbSwcPlaceholdersUrls = new ArrayList<String>();
    final String[] libraryClassesRoots = libraryModifiableModel.getUrls(OrderRootType.CLASSES);
    final String libName = libraryModifiableModel.getName();
    final String version = libName.substring(libName.lastIndexOf(':') + 1);
    for (final String librarySwcPath : libraryClassesRoots) {
      if (librarySwcPath.matches(".*" + version + "\\.rb\\.swc!/")) {
        rbSwcPlaceholdersUrls.add(librarySwcPath);
      }
    }
    return rbSwcPlaceholdersUrls;
  }

  private Collection<String> getRbSwcUrlsForCompiledLocales(final String rbSwcPlaceholderUrl) {
    final String RB_SWC_URL_END = ".rb.swc!/";
    assert rbSwcPlaceholderUrl.endsWith(RB_SWC_URL_END);
    final String rbSwcUrlCommonPart = rbSwcPlaceholderUrl.substring(0, rbSwcPlaceholderUrl.length() - RB_SWC_URL_END.length());

    final Collection<String> result = new ArrayList<String>();
    List<String> compiledLocales = myCompiledLocales;
    for (final String locale : compiledLocales) {
      result.add(rbSwcUrlCommonPart + "-" + locale + RB_SWC_URL_END);
    }
    return result;
  }

  private String getOutputFilePath() {
    final Element configurationElement = myFlexmojosPlugin.getConfigurationElement();
    String overriddenTargetFileName =
      configurationElement == null ? null : configurationElement.getChildTextNormalize("output", configurationElement.getNamespace());
    if (overriddenTargetFileName != null && !overriddenTargetFileName.isEmpty() && !FileUtil.isAbsolute(overriddenTargetFileName)) {
      overriddenTargetFileName = myMavenProject.getDirectory() + "/" + overriddenTargetFileName;
    }

    if (overriddenTargetFileName != null) {
      return FileUtil.toSystemIndependentName(overriddenTargetFileName);
    }
    else {
      final String suffix = myClassifier == null ? "" : "-" + myClassifier;
      return FileUtil.toSystemIndependentName(myMavenProject.getBuildDirectory())
             + "/" + myMavenProject.getFinalName() + suffix + "." + myMavenProject.getPackaging();
    }
  }

  private void setupSdk(final ModifiableFlexIdeBuildConfiguration bc) {
    final MavenId flexCompilerId = new MavenId(FLEX_COMPILER_GROUP_ID, FLEX_COMPILER_ARTIFACT_ID, getFlexCompilerPomVersion());

    final String path = getArtifactFilePath(myMavenProject, flexCompilerId, MavenConstants.TYPE_POM);
    final Sdk flexSdk = FlexSdkUtils.createOrGetSdk(FlexmojosSdkType.getInstance(), path);
    if (flexSdk != null) {
      ensureSdkHasRequiredAdditionalJarPaths(flexSdk);
      bc.getDependencies().setSdkEntry(Factory.createSdkEntry(flexSdk.getName()));
    }
  }

  private String getFlexCompilerPomVersion() {
    for (final MavenId mavenId : myFlexmojosPlugin.getDependencies()) {
      if (FLEX_COMPILER_GROUP_ID.equals(mavenId.getGroupId()) && FLEX_COMPILER_ARTIFACT_ID.equals(mavenId.getArtifactId())) {
        final String version = mavenId.getVersion();
        if (version != null) return version;
      }
    }

    for (final MavenArtifact mavenArtifact : myMavenProject.getDependencies()) {
      if (FLEX_FRAMEWORK_GROUP_ID.equals(mavenArtifact.getGroupId()) &&
          (FLEX_FRAMEWORK_ARTIFACT_ID.equals(mavenArtifact.getArtifactId()) ||
           AIR_FRAMEWORK_ARTIFACT_ID.equals(mavenArtifact.getArtifactId()))) {
        return mavenArtifact.getVersion();
      }
    }

    // correct flexmojos-maven-plugin resolving and taking version from 'flex.sdk.version' property value is rather expensive, so version is hardcoded
    final String pluginVersion = myFlexmojosPlugin.getVersion();
    if (pluginVersion != null && pluginVersion.startsWith("4.")) {
      return pluginVersion.startsWith("4.0-RC") || StringUtil.compareVersionNumbers(pluginVersion, "4.1") >= 0
             ? "4.5.1.21328"
             : "4.5.0.18623";
    }
    else {
      return "3.2.0.3958";
    }
  }

  private static String getArtifactFilePath(final MavenProject mavenProject, final MavenId mavenId, final String type) {
    return FileUtil.toSystemIndependentName(MavenArtifactUtil.getArtifactFile(mavenProject.getLocalRepository(), mavenId, type).getPath());
  }

  private void ensureSdkHasRequiredAdditionalJarPaths(final @NotNull Sdk sdk) {
    assert sdk.getSdkType() instanceof FlexmojosSdkType;
    final FlexmojosSdkAdditionalData additionalData = ((FlexmojosSdkAdditionalData)sdk.getSdkAdditionalData());
    assert additionalData != null;

    for (MavenId dependency : myFlexmojosPlugin.getDependencies()) {
      for (Pattern jarNamePattern : ADDITIONAL_JAR_NAME_PATTERNS_TO_INCLUDE_IN_FLEXMOJOS_SDK_CLASSPATH) {
        if (jarNamePattern.matcher(dependency.getArtifactId()).matches()) {
          final String jarFilePath = getArtifactFilePath(myMavenProject, dependency, MavenConstants.TYPE_JAR);
          additionalData.addFlexCompilerClasspathEntryIfNotPresentAndRemoveDifferentVersionOfThisJar(jarNamePattern, jarFilePath);
          break;
        }
      }
    }
  }

  protected String getCompilerConfigFilePath() {
    final String suffix = myClassifier == null ? "" : "-" + myClassifier;
    return FileUtil.toSystemIndependentName(myMavenProject.getBuildDirectory()) +
           "/" + myMavenProject.getFinalName() + suffix + "-config-report.xml";
  }

  protected void appendGenerateConfigTask(final List<MavenProjectsProcessorTask> postTasks, final String configFilePath) {
    postTasks.add(new Flexmojos3GenerateConfigTask(myMavenProject, myMavenTree, configFilePath, myInformer));
  }

/*  private void preprocessMxModuleFacets(final Module module,
                                        final MavenProject mavenProject,
                                        final MavenModifiableModelsProvider modifiableModelsProvider) {
    final List<String> mxModuleFilePaths = getMxModuleFilePaths(mavenProject);
    final ModifiableFacetModel model = modifiableModelsProvider.getFacetModel(module);
    deleteUnusedMxModuleFacets(model, mxModuleFilePaths);

    for (final String mxModuleFilePath : mxModuleFilePaths) {
      final String facetName = getMxModuleFacetName(mxModuleFilePath);
      FlexFacet facet = model.findFacet(myFacetType.getId(), facetName);
      if (facet == null) {
        facet = myFacetType.createFacet(module, facetName, myFacetType.createDefaultConfiguration(), null);
        model.addFacet(facet);
        setupFacet(facet, mavenProject);
      }
    }
  }

  private List<String> getMxModuleFilePaths(final MavenProject mavenProject) {
    if (!isApplication(mavenProject)) {
      return Collections.emptyList();
    }

    final Element moduleFilesElement = getConfig(mavenProject, "moduleFiles");

    if (moduleFilesElement == null) {
      return Collections.emptyList();
    }

    final List<String> result = new ArrayList<String>();
    //noinspection unchecked
    for (final Element moduleFilePathElement : (Iterable<Element>)moduleFilesElement.getChildren()) {
      final String moduleFilePath = moduleFilePathElement.getTextNormalize();
      if (moduleFilePath.endsWith(".mxml") || moduleFilePath.endsWith(".as")) {
        result.add(moduleFilePath);
      }
    }
    return result;
  }

  private void preProcessResourceFacets(Module module, MavenProject mavenProject, MavenModifiableModelsProvider modifiableModelsProvider) {
    List<String> runtimeLocales = getLocales(mavenProject, false);
    ModifiableFacetModel model = modifiableModelsProvider.getFacetModel(module);
    deleteUnusedResourceFacets(model, runtimeLocales);

    for (String eachLocale : runtimeLocales) {
      String facetName = getResourceFacetName(eachLocale);
      FlexFacet f = model.findFacet(myFacetType.getId(), facetName);
      if (f == null) {
        f = myFacetType.createFacet(module, facetName, myFacetType.createDefaultConfiguration(), null);
        model.addFacet(f);
        setupFacet(f, mavenProject);
      }
    }
  }

  protected boolean isGenerateFlexConfigFilesForMxModules() {
    return true;
  }

  private void reimportRuntimeLocalesFacets(final MavenProject project,
                                            final Module module,
                                            final MavenModifiableModelsProvider modelsProvider) {
    FacetModel model = modelsProvider.getFacetModel(module);

    String packaging = project.getPackaging();
    String extension = "swc".equalsIgnoreCase(packaging) ? "rb.swc" : packaging;

    String runtimeLocaleOutputPathPattern =
      findConfigValue(project, "runtimeLocaleOutputPath", "/{contextRoot}/locales/{artifactId}-{version}-{locale}.{extension}");
    runtimeLocaleOutputPathPattern = getRuntimeLocalesOutputPathPattern(project, runtimeLocaleOutputPathPattern, extension);

    for (FlexFacet eachFacet : model.getFacetsByType(myFacetType.getId())) {
      if (!isResourceFacet(eachFacet)) continue;

      String locale = getResourceFacetLocale(eachFacet);
      FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(eachFacet);

      String outputPath = getRuntimeLocaleOutputPath(runtimeLocaleOutputPathPattern, locale);

      String outputDir = outputPath.substring(0, outputPath.lastIndexOf("/"));
      String outputName = outputPath.substring(outputDir.length() + 1);

      config.OUTPUT_TYPE = getOutputType(project);
      config.OUTPUT_FILE_NAME = outputName;
      config.USE_FACET_COMPILE_OUTPUT_PATH = true;
      config.FACET_COMPILE_OUTPUT_PATH = outputDir;
      config.FACET_COMPILE_OUTPUT_PATH_FOR_TESTS = FileUtil.toSystemIndependentName(project.getTestOutputDirectory());

      config.USE_DEFAULT_SDK_CONFIG_FILE = false;
      config.USE_CUSTOM_CONFIG_FILE = true;
      config.CUSTOM_CONFIG_FILE = outputPath.replace("." + extension, getCompilerConfigXmlSuffix());
    }
  }

  private void reimportMxModuleFacets(final MavenProject project, final Module module, final MavenModifiableModelsProvider modelsProvider) {
    final FacetModel facetModel = modelsProvider.getFacetModel(module);

    if (!isApplication(project)) {
      return;
    }

    for (final FlexFacet facet : facetModel.getFacetsByType(myFacetType.getId())) {
      if (!isMxModuleFacet(facet)) continue;

      final String mxModuleFilePath = FileUtil.toSystemIndependentName(getMxModuleFileRelativePath(facet));
      final int dotIndex = mxModuleFilePath.lastIndexOf('.');
      final int slashIndex = mxModuleFilePath.lastIndexOf('/');
      final String mxModuleName = mxModuleFilePath.substring(slashIndex + 1, dotIndex > slashIndex ? dotIndex : mxModuleFilePath.length());
      final String outputPath = getTargetFilePath(project, "-" + mxModuleName + ".swf");
      final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(facet);

      config.OUTPUT_TYPE = FlexBuildConfiguration.APPLICATION;
      config.OUTPUT_FILE_NAME = outputPath.substring(outputPath.substring(0, outputPath.lastIndexOf("/")).length() + 1);
      config.MAIN_CLASS = mxModuleFilePath.substring(0, dotIndex > 0 ? dotIndex : mxModuleFilePath.length()).replace('/', '.');
      config.USE_FACET_COMPILE_OUTPUT_PATH = true;
      config.FACET_COMPILE_OUTPUT_PATH = outputPath.substring(0, outputPath.lastIndexOf("/"));
      config.FACET_COMPILE_OUTPUT_PATH_FOR_TESTS = FileUtil.toSystemIndependentName(project.getTestOutputDirectory());

      config.USE_DEFAULT_SDK_CONFIG_FILE = false;
      config.USE_CUSTOM_CONFIG_FILE = true;
      config.CUSTOM_CONFIG_FILE = outputPath.replace(".swf", getCompilerConfigXmlSuffix());
    }
  }

  private static String getRuntimeLocalesOutputPathPattern(MavenProject project, String runtimeLocaleOuputPathPattern, String extension) {
    MavenId projectId = project.getMavenId();
    String result = runtimeLocaleOuputPathPattern;

    String buildDirReplacement = Matcher.quoteReplacement(FileUtil.toSystemIndependentName(project.getBuildDirectory()));
    result = result.replaceFirst("\\A/\\{contextRoot\\}", buildDirReplacement);
    result = result.replaceAll("\\{contextRoot\\}", buildDirReplacement);

    result = result.replaceAll("\\{groupId\\}", Matcher.quoteReplacement(projectId.getGroupId()));
    result = result.replaceAll("\\{artifactId\\}", Matcher.quoteReplacement(projectId.getArtifactId()));
    result = result.replaceAll("\\{version\\}", Matcher.quoteReplacement(projectId.getVersion()));
    result = result.replaceAll("\\{extension\\}", Matcher.quoteReplacement(extension));

    return result;
  }

  private static String getRuntimeLocaleOutputPath(String runtimeLocaleOuputPathPattern, String locale) {
    return runtimeLocaleOuputPathPattern.replaceAll("\\{locale\\}", Matcher.quoteReplacement(locale));
  }

  private static boolean isResourceFacet(final FlexFacet f) {
    return f.getName().startsWith(RESOURCE_MODULE_FACET_PREFIX);
  }

  private static String getResourceFacetName(final String locale) {
    return RESOURCE_MODULE_FACET_PREFIX + locale;
  }

  private static String getResourceFacetLocale(final FlexFacet facet) {
    assert isResourceFacet(facet);
    return facet.getName().substring(RESOURCE_MODULE_FACET_PREFIX.length());
  }

  static boolean isMxModuleFacet(final FlexFacet f) {
    return f.getName().startsWith(MX_MODULE_FACET_PREFIX);
  }

  private static String getMxModuleFacetName(final String mxModuleFilePath) {
    return MX_MODULE_FACET_PREFIX + mxModuleFilePath;
  }

  static String getMxModuleFileRelativePath(final FlexFacet facet) {
    assert isMxModuleFacet(facet);
    return facet.getName().substring(MX_MODULE_FACET_PREFIX.length());
  }
  */
}
