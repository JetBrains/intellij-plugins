package com.intellij.javascript.flex.maven;

import com.intellij.facet.FacetModel;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkAdditionalData;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.server.NativeMavenProjectHolder;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;

import javax.swing.event.HyperlinkEvent;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlexMojos3FacetImporter extends FlexFacetImporter implements FlexConfigInformer {
  private static final String FLEXMOJOS_GROUP_ID = "org.sonatype.flexmojos";
  private static final String FLEXMOJOS_ARTIFACT_ID = "flexmojos-maven-plugin";
  private static final String FLEX_COMPILER_GROUP_ID = "com.adobe.flex";
  private static final String FLEX_COMPILER_ARTIFACT_ID = "compiler";
  private static final String RESOURCE_MODULE_FACET_PREFIX = "Resource module-";
  private static final String MX_MODULE_FACET_PREFIX = "Flex module ";  // here 'module' means Flex class inherited from mx.modules.Module

  private static final Pattern[] ADDITIONAL_JAR_NAME_PATTERNS_TO_INCLUDE_IN_FLEXMOJOS_SDK_CLASSPATH =
    {Pattern.compile("afe"), Pattern.compile("aglj[0-9]+"), Pattern.compile("flex-fontkit"), Pattern.compile("license"),
      Pattern.compile("rideau")};

  private Notification myFlexConfigNotification;

  public FlexMojos3FacetImporter() {
    super(FLEXMOJOS_GROUP_ID, FLEXMOJOS_ARTIFACT_ID);
  }

  protected boolean isApplicable(char majorVersion) {
    return majorVersion == '3';
  }

  @Override
  public boolean isApplicable(MavenProject project) {
    if (!super.isApplicable(project)) {
      return false;
    }

    MavenPlugin plugin = getFlexmojosPlugin(project);
    String version = plugin.getVersion();
    return version != null && isApplicable(plugin.getVersion().charAt(0));
  }

  protected synchronized void prepareImporter(final MavenProject p) {
    super.prepareImporter(p);
    if (myFlexConfigNotification != null && !myFlexConfigNotification.isExpired()) {
      myFlexConfigNotification.expire();
    }
    myFlexConfigNotification = null;
  }

  @Override
  public void resolve(Project project,
                      MavenProject mavenProject,
                      NativeMavenProjectHolder nativeMavenProject,
                      MavenEmbedderWrapper embedder)
    throws MavenProcessCanceledException {
    MavenPlugin plugin = mavenProject.findPlugin(myPluginGroupID, myPluginArtifactID);
    if (plugin != null && (plugin.getVersion() == null || plugin.getVersion().charAt(0) < 4)) {
      embedder.resolvePlugin(plugin, mavenProject.getRemoteRepositories(), nativeMavenProject, true);
    }
  }

  @Override
  public void preProcess(Module module,
                         MavenProject mavenProject,
                         MavenProjectChanges changes,
                         MavenModifiableModelsProvider modifiableModelsProvider) {
    super.preProcess(module, mavenProject, changes, modifiableModelsProvider);
    preProcessResourceFacets(module, mavenProject, modifiableModelsProvider);
    preprocessMxModuleFacets(module, mavenProject, modifiableModelsProvider);
    fixRbSwcLibraries(mavenProject, modifiableModelsProvider);
  }

  private void preprocessMxModuleFacets(final Module module,
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

  private void deleteUnusedMxModuleFacets(final ModifiableFacetModel model, final List<String> mxModuleFilePaths) {
    for (final FlexFacet facet : model.getFacetsByType(myFacetType.getId())) {
      if (isMxModuleFacet(facet) && !mxModuleFilePaths.contains(getMxModuleFileRelativePath(facet))) {
        model.removeFacet(facet);
      }
    }
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

  /**
   * Changes flexmojos resource bundle placeholder SWCs to SWCs that are really used in compilation.
   * Files like framework-3.3.0.4852.rb.swc are just a placeholders and may have incorrect format.
   * Locale classificators are added so these order entries are changed to one or more files like framework-3.3.0.4852-en_US.rb.swc
   */
  private void fixRbSwcLibraries(final MavenProject mavenProject, final MavenModifiableModelsProvider modifiableModelsProvider) {
    for (final Library library : modifiableModelsProvider.getAllLibraries()) {
      if (MavenRootModelAdapter.isMavenLibrary(library) && library.getName().contains(":rb.swc:")) {
        final Library.ModifiableModel libraryModifiableModel = modifiableModelsProvider.getLibraryModel(library);
        final Collection<String> rbSwcPlaceholderUrls = findRbSwcPlaceholderUrls(libraryModifiableModel);
        for (final String rbSwcPlaceholdersUrl : rbSwcPlaceholderUrls) {
          final Collection<String> rootsToAdd = findRbSwcsForCompiledLocales(mavenProject, rbSwcPlaceholdersUrl);
          libraryModifiableModel.removeRoot(rbSwcPlaceholdersUrl, OrderRootType.CLASSES);
          for (final String rootToAdd : rootsToAdd) {
            if (!ArrayUtil.contains(rootToAdd, libraryModifiableModel.getUrls(OrderRootType.CLASSES))) {
              libraryModifiableModel.addRoot(rootToAdd, OrderRootType.CLASSES);
            }
          }
          // TODO: sources and docs could be updated as well, but currently they are always senseless, because they do not exist
        }
      }
    }
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

  private Collection<String> findRbSwcsForCompiledLocales(final MavenProject mavenProject, final String rbSwcPlaceholderUrl) {
    final String RB_SWC_URL_END = ".rb.swc!/";
    assert rbSwcPlaceholderUrl.endsWith(RB_SWC_URL_END);
    final String rbSwcUrlCommonPart = rbSwcPlaceholderUrl.substring(0, rbSwcPlaceholderUrl.length() - RB_SWC_URL_END.length());

    final Collection<String> result = new ArrayList<String>();
    List<String> compiledLocales = getLocales(mavenProject, true);
    for (final String locale : compiledLocales) {
      result.add(rbSwcUrlCommonPart + "-" + locale + RB_SWC_URL_END);
    }
    return result;
  }

  private void deleteUnusedResourceFacets(ModifiableFacetModel model, List<String> runtimeLocales) {
    for (FlexFacet facet : model.getFacetsByType(myFacetType.getId())) {
      if (isResourceFacet(facet) && !runtimeLocales.contains(getResourceFacetLocale(facet))) {
        model.removeFacet(facet);
      }
    }
  }

  @Override
  protected void reimportFacet(MavenModifiableModelsProvider modelsProvider,
                               Module module,
                               MavenRootModelAdapter rootModel,
                               FlexFacet facet,
                               MavenProjectsTree mavenTree,
                               MavenProject project,
                               MavenProjectChanges changes,
                               Map<MavenProject, String> mavenProjectToModuleName,
                               List<MavenProjectsProcessorTask> postTasks) {
    super.reimportFacet(modelsProvider, module, rootModel, facet, mavenTree, project, changes, mavenProjectToModuleName, postTasks);

    final MavenPlugin flexmojosPlugin = getFlexmojosPlugin(project);
    final MavenId flexCompilerId =
      new MavenId(FLEX_COMPILER_GROUP_ID, FLEX_COMPILER_ARTIFACT_ID, getFlexCompilerPomVersion(flexmojosPlugin));

    final String path = getArtifactFilePath(project, flexCompilerId, MavenConstants.TYPE_POM);
    final Sdk flexSdk = FlexSdkUtils.createOrGetSdk(FlexmojosSdkType.getInstance(), path);
    if (flexSdk != null) {
      ensureSdkHasRequiredAdditionalJarPaths(flexSdk, project, flexmojosPlugin);
    }

    final ModifiableFacetModel facetModel = modelsProvider.getFacetModel(module);

    for (FlexFacet flexFacet : facetModel.getFacetsByType(myFacetType.getId())) {
      flexFacet.getConfiguration().setFlexSdk(flexSdk, rootModel.getRootModel());
    }

    if (StringUtil.compareVersionNumbers(flexmojosPlugin.getVersion(), "3.4") >= 0) {
      addGenerateFlexConfigTask(postTasks, facet, project, mavenTree);
    }
    else {
      showFlexConfigWarningIfNeeded(module.getProject());
    }

    if (isGenerateFlexConfigFilesForMxModules()) {
      for (final FlexFacet flexFacet : facetModel.getFacetsByType(myFacetType.getId())) {
        if (isMxModuleFacet(flexFacet)) {
          postTasks.add(new GenerateFlexConfigFilesForMxModulesTask(getCompilerConfigXmlSuffix(), module, project, mavenTree));
          break;
        }
      }
    }
  }

  protected boolean isGenerateFlexConfigFilesForMxModules() {
    return true;
  }

  protected void addGenerateFlexConfigTask(List<MavenProjectsProcessorTask> postTasks, FlexFacet facet,
                                           MavenProject project, MavenProjectsTree mavenTree) {
    if (FlexCompilerProjectConfiguration.getInstance(facet.getModule().getProject()).GENERATE_FLEXMOJOS_CONFIGS) {
      postTasks.add(new GenerateFlexConfigTask(facet, project, mavenTree, FLEXMOJOS_GROUP_ID, FLEXMOJOS_ARTIFACT_ID, this));
    }
  }

  private static void ensureSdkHasRequiredAdditionalJarPaths(final @NotNull Sdk flexSdk,
                                                             final MavenProject mavenProject,
                                                             final MavenPlugin flexmojosPlugin) {
    assert flexSdk.getSdkType() instanceof FlexmojosSdkType;
    final FlexmojosSdkAdditionalData additionalData = ((FlexmojosSdkAdditionalData)flexSdk.getSdkAdditionalData());
    additionalData.getFlexCompilerClasspath();

    for (MavenId dependency : flexmojosPlugin.getDependencies()) {
      for (Pattern jarNamePattern : ADDITIONAL_JAR_NAME_PATTERNS_TO_INCLUDE_IN_FLEXMOJOS_SDK_CLASSPATH) {
        if (jarNamePattern.matcher(dependency.getArtifactId()).matches()) {
          final String jarFilePath = getArtifactFilePath(mavenProject, dependency, MavenConstants.TYPE_JAR);
          additionalData.addFlexCompilerClasspathEntryIfNotPresentAndRemoveDifferentVersionOfThisJar(jarNamePattern, jarFilePath);
          break;
        }
      }
    }
  }

  public void showFlexConfigWarningIfNeeded(final Project project) {
    if (myFlexConfigNotification != null) return; // already shown
    doShowFlexConfigWarning(project);
  }

  private synchronized void doShowFlexConfigWarning(final Project project) {
    final NotificationListener listener = new NotificationListener() {
      public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
        Messages
          .showWarningDialog(project, FlexBundle.message("flexmojos.warning.detailed"), FlexBundle.message("flexmojos.project.import"));
        notification.expire();
      }
    };
    myFlexConfigNotification =
      new Notification("Maven", FlexBundle.message("flexmojos.project.import"), FlexBundle.message("flexmojos.warning.short"),
                       NotificationType.WARNING, listener);

    myFlexConfigNotification.notify(project);
  }

  @NotNull
  static MavenPlugin getFlexmojosPlugin(final MavenProject mavenProject) {
    final MavenPlugin mavenPlugin = mavenProject.findPlugin(FLEXMOJOS_GROUP_ID, FLEXMOJOS_ARTIFACT_ID);
    assert mavenPlugin != null;
    return mavenPlugin;
  }

  static String getOutputFilePath(final MavenProject mavenProject) {
    final Element configurationElement = mavenProject.getPluginConfiguration(FLEXMOJOS_GROUP_ID, FLEXMOJOS_ARTIFACT_ID);
    String overriddenTargetFileName =
      configurationElement == null ? null : configurationElement.getChildTextNormalize("output", configurationElement.getNamespace());
    if (!StringUtil.isEmpty(overriddenTargetFileName) && !FileUtil.isAbsolute(overriddenTargetFileName)) {
      overriddenTargetFileName = mavenProject.getDirectory() + "/" + overriddenTargetFileName;
    }

    if (overriddenTargetFileName != null) {
      return FileUtil.toSystemIndependentName(overriddenTargetFileName);
    }
    else {
      final Element classifierElement =
        configurationElement == null ? null : configurationElement.getChild("classifier", configurationElement.getNamespace());
      final String suffix = classifierElement == null ? "" : "-" + classifierElement.getTextNormalize();
      return FileUtil.toSystemIndependentName(mavenProject.getBuildDirectory())
             + "/" + mavenProject.getFinalName() + suffix + "." + mavenProject.getPackaging();
    }
  }

  private static String getArtifactFilePath(final MavenProject mavenProject, final MavenId mavenId, final String type) {
    return FileUtil.toSystemIndependentName(MavenArtifactUtil.getArtifactFile(mavenProject.getLocalRepository(), mavenId, type).getPath());
  }

  private static String getFlexCompilerPomVersion(final @NotNull MavenPlugin flexmojosPlugin) {
    for (final MavenId mavenId : flexmojosPlugin.getDependencies()) {
      if (FLEX_COMPILER_GROUP_ID.equals(mavenId.getGroupId()) && FLEX_COMPILER_ARTIFACT_ID.equals(mavenId.getArtifactId())) {
        return mavenId.getVersion();
      }
    }
    // TODO: correct flexmojos-maven-plugin resolving and taking version from 'flex.sdk.version' property value is rather expensive, so currently version is hardcoded
    final String pluginVersion = flexmojosPlugin.getVersion();
    return (pluginVersion != null && pluginVersion.startsWith("4.")) ? "4.5.0.18623" : "3.2.0.3958";
  }

  @Override
  protected void reimportFlexFacet(MavenProject project,
                                   Module module,
                                   FlexBuildConfiguration config,
                                   MavenModifiableModelsProvider modelsProvider) {
    super.reimportFlexFacet(project, module, config, modelsProvider);

    final MavenPlugin flexmojosPlugin = getFlexmojosPlugin(project);
    final Element configurationElement = flexmojosPlugin.getConfigurationElement();
    final Element classifierElement =
      configurationElement == null ? null : configurationElement.getChild("classifier", configurationElement.getNamespace());
    final String suffix = classifierElement == null ? "" : "-" + classifierElement.getTextNormalize();

    config.USE_DEFAULT_SDK_CONFIG_FILE = false;
    config.USE_CUSTOM_CONFIG_FILE = true;
    config.CUSTOM_CONFIG_FILE = getCompilerConfigFile(module, project, suffix);

    final String outputFilePath = getOutputFilePath(project);
    final int lastSlashIndex = outputFilePath.lastIndexOf("/");
    config.OUTPUT_FILE_NAME = outputFilePath.substring(lastSlashIndex + 1);
    config.FACET_COMPILE_OUTPUT_PATH = outputFilePath.substring(0, Math.max(0, lastSlashIndex));

    if (configurationElement != null && isApplication(project)) {
      final String sourceFile = configurationElement.getChildTextNormalize("sourceFile");
      if (sourceFile != null && (sourceFile.endsWith(".as") || sourceFile.endsWith(".mxml"))) {
        config.MAIN_CLASS = sourceFile.substring(0, sourceFile.lastIndexOf(".")).replace("/", ".").replace("\\", ".");
      }
    }

    reimportCompileLocales(project, config);
    reimportRuntimeLocalesFacets(project, module, modelsProvider);
    reimportMxModuleFacets(project, module, modelsProvider);
  }

  protected String getCompilerConfigFile(Module module, MavenProject mavenProject, String suffix) {
    return getTargetFilePath(mavenProject, suffix + getCompilerConfigXmlSuffix());
  }

  @Override
  public void collectSourceFolders(MavenProject mavenProject, List<String> result) {
    List<String> locales = new ArrayList<String>();
    locales.addAll(getLocales(mavenProject, true));
    locales.addAll(getLocales(mavenProject, false));
    if (!locales.isEmpty()) {
      final String localesDir = findConfigValue(mavenProject, "resourceBundlePath", "src/main/locales/{locale}");
      for (String each : locales) {
        result.add(localesDir.replaceAll("\\{locale\\}", Matcher.quoteReplacement(each)));
      }
    }
  }

  private void reimportCompileLocales(MavenProject project, FlexBuildConfiguration config) {
    config.USE_LOCALE_SETTINGS = true;
    config.LOCALE = StringUtil.join(getLocales(project, true), ",");
  }

  private void reimportRuntimeLocalesFacets(final MavenProject project, final Module module, final MavenModifiableModelsProvider modelsProvider) {
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

  protected @Nullable Element getLocalesElement(MavenProject mavenProject, boolean compiled) {
    Element localesElement = getConfig(mavenProject, (compiled ? "compiled" : "runtime") + "Locales");
    if (compiled && localesElement == null) {
      localesElement = getConfig(mavenProject, "locales");
    }

    return localesElement;
  }

  private List<String> getLocales(MavenProject mavenProject, boolean compiled) {
    Element localesElement = getLocalesElement(mavenProject, compiled);
    if (localesElement == null) {
      if (compiled && isApplication(mavenProject)) {
        final String defaultLocale = getDefaultLocale(mavenProject);
        if (defaultLocale != null) {
          return Collections.singletonList(defaultLocale);
        }
      }
      
      return Collections.emptyList();
    }

    List<String> result = new ArrayList<String>();
    //noinspection unchecked
    for (Element eachChild : (Iterable<Element>)localesElement.getChildren()) {
      String name = eachChild.getTextNormalize();
      if (StringUtil.isEmptyOrSpaces(name)) continue;
      result.add(name);
    }
    return result;
  }

  private String getDefaultLocale(MavenProject mavenProject) {
    if (findConfigValue(mavenProject, "useDefaultLocale", "true").equals("true")) {
      return findConfigValue(mavenProject, "defaultLocale", "en_US");
    }
    else {
      return null;
    }
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

  protected String getCompilerConfigXmlSuffix() {
    return "-config-report.xml";
  }
}
