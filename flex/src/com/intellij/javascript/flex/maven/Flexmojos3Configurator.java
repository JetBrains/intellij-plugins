package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
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
import com.intellij.util.containers.hash.HashSet;
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

import java.util.*;
import java.util.regex.Pattern;

import static com.intellij.javascript.flex.maven.RuntimeModulesGenerateConfigTask.RLMInfo;

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
  protected final MavenPlugin myFlexmojosPlugin;
  private final Map<MavenProject, String> myMavenProjectToModuleName;
  private final List<String> myCompiledLocales;
  private final List<String> myRuntimeLocales;
  private final FlexConfigInformer myInformer;


  public Flexmojos3Configurator(final Module module,
                                final MavenModifiableModelsProvider modelsProvider,
                                final FlexProjectConfigurationEditor flexEditor,
                                final MavenProjectsTree mavenTree,
                                final Map<MavenProject, String> mavenProjectToModuleName,
                                final MavenProject mavenProject,
                                final MavenPlugin flexmojosPlugin,
                                final List<String> compiledLocales,
                                final List<String> runtimeLocales,
                                final FlexConfigInformer informer) {
    myModelsProvider = modelsProvider;
    myMavenTree = mavenTree;
    myMavenProjectToModuleName = mavenProjectToModuleName;
    myCompiledLocales = compiledLocales;
    myRuntimeLocales = runtimeLocales;
    myModule = module;
    myFlexEditor = flexEditor;
    myMavenProject = mavenProject;
    myFlexmojosPlugin = flexmojosPlugin;
    myInformer = informer;
  }

  public void configureAndAppendTasks(final List<MavenProjectsProcessorTask> postTasks) {
    final ModifiableFlexIdeBuildConfiguration[] oldBCs = myFlexEditor.getConfigurations(myModule);
    for (ModifiableFlexIdeBuildConfiguration oldBC : oldBCs) {
      myFlexEditor.configurationRemoved(oldBC);
    }

    final ModifiableFlexIdeBuildConfiguration mainBC = setupMainBuildConfiguration();

    final Collection<String> usedNames = new HashSet<String>();
    usedNames.add(mainBC.getName());

    final Collection<RLMInfo> rlmInfos = FlexmojosImporter.isFlexApp(myMavenProject) ? getRLMInfos() : Collections.<RLMInfo>emptyList();
    for (RLMInfo info : rlmInfos) {
      configureRuntimeLoadedModule(mainBC, info, usedNames);
    }

    respectPreviousBCState(myFlexEditor.getConfigurations(myModule), oldBCs);

    if (FlexCompilerProjectConfiguration.getInstance(myModule.getProject()).GENERATE_FLEXMOJOS_CONFIGS) {
      if (StringUtil.compareVersionNumbers(myFlexmojosPlugin.getVersion(), "3.4") >= 0) {
        appendGenerateConfigTask(postTasks, mainBC.getCompilerOptions().getAdditionalConfigFilePath());
        if (!rlmInfos.isEmpty()) {
          postTasks.add(new RuntimeModulesGenerateConfigTask(myModule, myMavenProject, myMavenTree,
                                                             mainBC.getCompilerOptions().getAdditionalConfigFilePath(), rlmInfos));
        }
      }
      else {
        myInformer.showFlexConfigWarningIfNeeded(myModule.getProject());
      }
    }
  }

  private ModifiableFlexIdeBuildConfiguration setupMainBuildConfiguration() {
    final ModifiableFlexIdeBuildConfiguration mainBC = myFlexEditor.createConfiguration(myModule);
    mainBC.setName(myModule.getName());

    final TargetPlatform targetPlatform = handleDependencies(mainBC);
    mainBC.setTargetPlatform(targetPlatform);
    mainBC.setPureAs(false);
    mainBC.setOutputType(FlexmojosImporter.isFlexApp(myMavenProject) ? OutputType.Application : OutputType.Library);

    final Element configurationElement = myFlexmojosPlugin.getConfigurationElement();
    if (FlexmojosImporter.isFlexApp(myMavenProject) && configurationElement != null) {
      final String sourceFile = configurationElement.getChildTextNormalize("sourceFile");
      if (sourceFile != null && (sourceFile.endsWith(".as") || sourceFile.endsWith(".mxml"))) {
        mainBC.setMainClass(sourceFile.substring(0, sourceFile.lastIndexOf(".")).replace("/", ".").replace("\\", "."));
      }
      // todo set later when config is generated?
    }

    final String outputFilePath = FlexmojosImporter.getOutputFilePath(myMavenProject);
    mainBC.setOutputFileName(PathUtil.getFileName(outputFilePath));
    mainBC.setOutputFolder(PathUtil.getParentPath(outputFilePath));

    setupSdk(mainBC);

    final String locales = StringUtil.join(myCompiledLocales, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
    mainBC.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.locale", locales));

    if (BCUtils.canHaveResourceFiles(mainBC.getNature())) {
      // Don't copy whatever by default. If user had other setting before reimport - it will be set in #respectPreviousBCState()
      mainBC.getCompilerOptions().setResourceFilesMode(CompilerOptions.ResourceFilesMode.None);
    }

    mainBC.getCompilerOptions().setAdditionalConfigFilePath(getCompilerConfigFilePath());
    return mainBC;
  }

  private TargetPlatform handleDependencies(final ModifiableFlexIdeBuildConfiguration bc) {
    boolean playerglobal = false;
    boolean airglobal = false;
    boolean mobilecomponents = false;

    final ModifiableRootModel rootModel = myModelsProvider.getRootModel(myModule);
    for (OrderEntry entry : rootModel.getOrderEntries()) {
      final DependencyScope scope = entry instanceof ExportableOrderEntry ? ((ExportableOrderEntry)entry).getScope()
                                                                          : DependencyScope.COMPILE;
      final boolean isExported = entry instanceof ExportableOrderEntry && ((ExportableOrderEntry)entry).isExported();

      if (entry instanceof ModuleOrderEntry) {
        rootModel.removeOrderEntry(entry);

        final String dependencyModuleName = ((ModuleOrderEntry)entry).getModuleName();

        final MavenProject dependencyMavenProject = findMavenProjectByModuleName(dependencyModuleName);
        if (dependencyMavenProject == null ||
            !ArrayUtil.contains(dependencyMavenProject.getPackaging(), FlexmojosImporter.SUPPORTED_PACKAGINGS)) {
          continue;
        }

        final ModifiableBuildConfigurationEntry bcEntry =
          myFlexEditor.createBcEntry(bc.getDependencies(), dependencyModuleName, dependencyModuleName);
        bcEntry.getDependencyType().setLinkageType(FlexUtils.convertLinkageType(scope, isExported));
        bc.getDependencies().getModifiableEntries().add(0, bcEntry);

        continue;
      }

      if (entry instanceof JdkOrderEntry) {
        rootModel.removeOrderEntry(entry);
      }

      if (!(entry instanceof LibraryOrderEntry)) continue;
      rootModel.removeOrderEntry(entry);

      if (!LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)entry).getLibraryLevel())) continue;
      final Library library = ((LibraryOrderEntry)entry).getLibrary();
      if (library == null || !MavenRootModelAdapter.isMavenLibrary(library)) continue;

      final String libraryName = library.getName();
      if (libraryName.contains(":rb.swc:") || libraryName.contains(":resource-bundle:")) {
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

      if (libraryName.contains(":swc:") || libraryName.contains(":rb.swc:") || libraryName.contains(":resource-bundle:")) {
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

        final ModifiableDependencyEntry sharedLibraryEntry =
          myFlexEditor.createSharedLibraryEntry(bc.getDependencies(), ((LibraryOrderEntry)entry).getLibraryName(),
                                                ((LibraryOrderEntry)entry).getLibraryLevel());
        sharedLibraryEntry.getDependencyType().setLinkageType(FlexUtils.convertLinkageType(scope, isExported));
        bc.getDependencies().getModifiableEntries().add(sharedLibraryEntry);
      }
      else {
        MavenLog.LOG.info("Non-swc dependency for flexmojos project '" + myModule.getName() + "': " + libraryName);
      }
    }

    // todo better target platform detection if both airglobal and playerglobal present?
    return mobilecomponents && airglobal ? TargetPlatform.Mobile
                                         : airglobal && !playerglobal ? TargetPlatform.Desktop
                                                                      : TargetPlatform.Web;
  }

  @Nullable
  private MavenProject findMavenProjectByModuleName(final String moduleName) {
    for (Map.Entry<MavenProject, String> entry : myMavenProjectToModuleName.entrySet()) {
      if (moduleName.equals(entry.getValue())) return entry.getKey();
    }
    return null;
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
    return getCompilerConfigFilePath(null);
  }

  protected String getCompilerConfigFilePath(final @Nullable String rlmName) {
    final Element configurationElement = myFlexmojosPlugin.getConfigurationElement();
    final String classifier =
      configurationElement == null ? null : configurationElement.getChildTextNormalize("classifier", configurationElement.getNamespace());

    String suffix = "";
    if (rlmName != null) {
      suffix = "-" + rlmName;
    }
    else if (classifier != null) {
      suffix = "-" + classifier;
    }
    return FileUtil.toSystemIndependentName(myMavenProject.getBuildDirectory()) +
           "/" + myMavenProject.getFinalName() + suffix + "-config-report.xml";
  }

  protected void appendGenerateConfigTask(final List<MavenProjectsProcessorTask> postTasks, final String configFilePath) {
    postTasks.add(new Flexmojos3GenerateConfigTask(myModule, myMavenProject, myMavenTree, configFilePath, myInformer));
  }

  private void configureRuntimeLoadedModule(final ModifiableFlexIdeBuildConfiguration mainBC,
                                            final RLMInfo info,
                                            final Collection<String> usedNames) {
    final String bcName = getUniqueName(info.myRLMName, usedNames);
    usedNames.add(bcName);

    final BuildConfigurationNature nature = new BuildConfigurationNature(mainBC.getTargetPlatform(), mainBC.isPureAs(),
                                                                         OutputType.RuntimeLoadedModule);
    final ModifiableFlexIdeBuildConfiguration rlmBC = myFlexEditor.copyConfiguration(mainBC, nature);

    rlmBC.setName(bcName);
    rlmBC.setMainClass(info.myMainClass);
    rlmBC.setOutputFileName(info.myOutputFileName);
    rlmBC.setOutputFolder(info.myOutputFolderPath);
    rlmBC.getCompilerOptions().setAdditionalConfigFilePath(getCompilerConfigFilePath(info.myRLMName));
  }

  private static String getUniqueName(final String name, final Collection<String> usedNames) {
    String uniqueName = name;
    int index = 2;
    while (usedNames.contains(uniqueName)) {
      uniqueName = name + " " + index++;
    }
    return uniqueName;
  }

  protected Collection<RLMInfo> getRLMInfos() {
    final Element configurationElement = myFlexmojosPlugin.getConfigurationElement();
    final Element moduleFilesElement = configurationElement == null
                                       ? null : configurationElement.getChild("moduleFiles", configurationElement.getNamespace());

    if (moduleFilesElement == null) {
      return Collections.emptyList();
    }

    final List<RLMInfo> result = new ArrayList<RLMInfo>();
    //noinspection unchecked
    for (final Element moduleFilePathElement : (Iterable<Element>)moduleFilesElement.getChildren()) {
      final String path = moduleFilePathElement.getTextNormalize();
      if (path.endsWith(".mxml") || path.endsWith(".as")) {
        final String mainClassRelativePath = FileUtil.toSystemIndependentName(path);
        final String mainClass = FileUtil.getNameWithoutExtension(mainClassRelativePath.replace('/', '.'));
        final String rlmName = StringUtil.getShortName(mainClass);
        final String outputFileName = myMavenProject.getFinalName() + "-" + rlmName + ".swf";
        final String outputFolderPath = FileUtil.toSystemIndependentName(myMavenProject.getBuildDirectory());
        final String configFilePath = getCompilerConfigFilePath(rlmName);
        result.add(new RLMInfo(rlmName, mainClass, mainClassRelativePath, outputFileName, outputFolderPath, configFilePath));
      }
    }
    return result;
  }

  private static void respectPreviousBCState(final ModifiableFlexIdeBuildConfiguration[] newBCsc,
                                             final FlexIdeBuildConfiguration[] oldBCs) {
    for (ModifiableFlexIdeBuildConfiguration newBC : newBCsc) {
      for (FlexIdeBuildConfiguration oldBC : oldBCs) {
        if (oldBC.getName().equals(newBC.getName())) {
          newBC.setSkipCompile(oldBC.isSkipCompile());

          final BuildConfigurationNature nature = newBC.getNature();
          if (nature.isApp() && nature.isWebPlatform()) {
            newBC.setUseHtmlWrapper(oldBC.isUseHtmlWrapper());
            newBC.setWrapperTemplatePath(oldBC.getWrapperTemplatePath());
          }

          if (BCUtils.canHaveResourceFiles(nature)) {
            newBC.getCompilerOptions().setResourceFilesMode(oldBC.getCompilerOptions().getResourceFilesMode());
          }
        }
      }
    }
  }

  /*
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
  */
}
