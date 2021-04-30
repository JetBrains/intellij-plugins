// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.maven;

import com.intellij.flex.model.bc.*;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkAdditionalData;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryKind;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
  private static final String FLEX_COMPILER_ADOBE_GROUP_ID = "com.adobe.flex";
  private static final String FLEX_COMPILER_APACHE_GROUP_ID = "org.apache.flex";
  private static final String FLEX_COMPILER_ARTIFACT_ID = "compiler";

  private static final Pattern[] ADDITIONAL_JAR_NAME_PATTERNS_TO_INCLUDE_IN_FLEXMOJOS_SDK_CLASSPATH =
    {Pattern.compile("afe"), Pattern.compile("aglj[0-9]+"), Pattern.compile("flex-fontkit"), Pattern.compile("license"),
      Pattern.compile("rideau")};

  protected final Module myModule;
  private final IdeModifiableModelsProvider myModelsProvider;
  private final FlexProjectConfigurationEditor myFlexEditor;
  protected final MavenProjectsTree myMavenTree;
  protected final MavenProject myMavenProject;
  protected final MavenPlugin myFlexmojosPlugin;
  private final Map<MavenProject, String> myMavenProjectToModuleName;
  private final List<String> myCompiledLocales;
  private final List<String> myRuntimeLocales;
  private final FlexConfigInformer myInformer;


  public Flexmojos3Configurator(final Module module,
                                final IdeModifiableModelsProvider modelsProvider,
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
    final ModifiableFlexBuildConfiguration[] oldBCs = myFlexEditor.getConfigurations(myModule);
    if (oldBCs.length == 1 && oldBCs[0].getName().equals(FlexBuildConfiguration.UNNAMED)) {
      myFlexEditor.configurationRemoved(oldBCs[0]);
    }

    final String mainBCName = myModule.getName();
    final ModifiableFlexBuildConfiguration existingBC = ContainerUtil.find(oldBCs, bc -> mainBCName.equals(bc.getName()));

    final ModifiableFlexBuildConfiguration mainBC = setupMainBuildConfiguration(existingBC);

    final Collection<RLMInfo> rlmInfos = FlexmojosImporter.isFlexApp(myMavenProject) ? getRLMInfos() : Collections.emptyList();
    for (final RLMInfo info : rlmInfos) {
      final ModifiableFlexBuildConfiguration existingRlmBC =
        ContainerUtil.find(oldBCs, bc -> bc.getName().equals(info.myRLMName));

      configureRuntimeLoadedModule(mainBC, info, existingRlmBC);
    }

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

  private ModifiableFlexBuildConfiguration setupMainBuildConfiguration(final @Nullable ModifiableFlexBuildConfiguration existingBC) {
    final boolean isNewBC = existingBC == null;
    final ModifiableFlexBuildConfiguration mainBC = isNewBC ? myFlexEditor.createConfiguration(myModule) : existingBC;

    mainBC.setName(myModule.getName());

    final TargetPlatform guessedTargetPlatform = handleDependencies(mainBC);
    final boolean producesAirPackage = "air".equals(myMavenProject.getPackaging()) || containsSignAirGoal(myFlexmojosPlugin);
    if (isNewBC) {
      final TargetPlatform targetPlatform = producesAirPackage ? TargetPlatform.Desktop : guessedTargetPlatform;
      mainBC.setTargetPlatform(targetPlatform);
      mainBC.setPureAs(false);
    }
    final OutputType outputType = FlexmojosImporter.isFlexApp(myMavenProject) ? OutputType.Application : OutputType.Library;

    // keep outputType == RLM set manually
    if (!(outputType == OutputType.Application && mainBC.getOutputType() == OutputType.RuntimeLoadedModule)) {
      if (outputType != mainBC.getOutputType()) {
        mainBC.setOutputType(outputType);
        FlexProjectConfigurationEditor.resetNonApplicableValuesToDefaults(mainBC);
      }
    }

    final Element configurationElement = myFlexmojosPlugin.getConfigurationElement();
    if (FlexmojosImporter.isFlexApp(myMavenProject)) {
      final String sourceFile = configurationElement == null ? null : configurationElement.getChildTextNormalize("sourceFile");
      if (sourceFile != null && (sourceFile.endsWith(".as") || sourceFile.endsWith(".mxml"))) {
        mainBC.setMainClass(sourceFile.substring(0, sourceFile.lastIndexOf(".")).replace("/", ".").replace("\\", "."));
      }

      if (producesAirPackage) {
        setupPackagingOptions(mainBC.getAirDesktopPackagingOptions(), configurationElement);
      }
    }

    final String outputFilePath = FlexmojosImporter.getOutputFilePath(myMavenProject);
    final String fileName = PathUtil.getFileName(outputFilePath);
    mainBC.setOutputFileName(fileName);
    mainBC.setOutputFolder(PathUtil.getParentPath(outputFilePath));

    final BuildConfigurationNature nature = mainBC.getNature();
    if (nature.isApp() && isNewBC) {
      final String packageFileName = FileUtilRt.getNameWithoutExtension(fileName);
      if (nature.isDesktopPlatform()) {
        mainBC.getAirDesktopPackagingOptions().setPackageFileName(packageFileName);
      }
      else if (nature.isMobilePlatform()) {
        mainBC.getAndroidPackagingOptions().setPackageFileName(packageFileName);
        mainBC.getIosPackagingOptions().setPackageFileName(packageFileName);
      }
    }

    setupSdk(mainBC);

    final String locales = StringUtil.join(myCompiledLocales, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
    final Map<String, String> options = new HashMap<>(mainBC.getCompilerOptions().getAllOptions());
    options.put("compiler.locale", locales);
    mainBC.getCompilerOptions().setAllOptions(options);

    if (BCUtils.canHaveResourceFiles(nature) && isNewBC) {
      mainBC.getCompilerOptions().setResourceFilesMode(CompilerOptions.ResourceFilesMode.None);
    }

    mainBC.getCompilerOptions().setAdditionalConfigFilePath(getCompilerConfigFilePath());
    return mainBC;
  }

  private static boolean containsSignAirGoal(final MavenPlugin flexmojosPlugin) {
    for (MavenPlugin.Execution execution : flexmojosPlugin.getExecutions()) {
      for (String goal : execution.getGoals()) {
        if ("sign-air".equals(goal)) {
          return true;
        }
      }
    }
    return false;
  }

  private void setupPackagingOptions(final ModifiableAirDesktopPackagingOptions packagingOptions,
                                     final @Nullable Element configurationElement) {
    final String descriptorPath = StringUtil.notNullize(getPathOption(myMavenProject, configurationElement, "descriptorTemplate"),
                                                        myMavenProject.getDirectory() + "/src/main/resources/descriptor.xml");
    packagingOptions.setUseGeneratedDescriptor(false);
    packagingOptions.setCustomDescriptorPath(descriptorPath);

    final String keystorePath = StringUtil.notNullize(getPathOption(myMavenProject, configurationElement, "keystore"),
                                                      myMavenProject.getDirectory() + "/src/main/resources/sign.p12");
    packagingOptions.getSigningOptions().setUseTempCertificate(false);
    packagingOptions.getSigningOptions().setKeystorePath(keystorePath);

    final String keystoreType =
      configurationElement == null ? null : configurationElement.getChildTextNormalize("storetype", configurationElement.getNamespace());
    if (keystoreType != null) {
      packagingOptions.getSigningOptions().setKeystoreType(keystoreType);
    }
  }

  @Nullable
  private static String getPathOption(final MavenProject mavenProject,
                                      final @Nullable Element configurationElement,
                                      final String optionName) {
    final String path =
      configurationElement == null ? null : configurationElement.getChildTextNormalize(optionName, configurationElement.getNamespace());
    if (path != null) {
      VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (descriptorFile == null) {
        descriptorFile = LocalFileSystem.getInstance().findFileByPath(mavenProject.getDirectory() + "/" + path);
      }
      if (descriptorFile != null) return descriptorFile.getPath();
    }
    return path;
  }

  private TargetPlatform handleDependencies(final ModifiableFlexBuildConfiguration bc) {
    bc.getDependencies().getModifiableEntries().clear();

    boolean playerglobal = false;
    boolean airglobal = false;
    boolean mobilecomponents = false;

    final ModifiableRootModel rootModel = myModelsProvider.getModifiableRootModel(myModule);
    for (OrderEntry entry : rootModel.getOrderEntries()) {
      final DependencyScope scope = entry instanceof ExportableOrderEntry ? ((ExportableOrderEntry)entry).getScope()
                                                                          : DependencyScope.COMPILE;
      final boolean isExported = entry instanceof ExportableOrderEntry && ((ExportableOrderEntry)entry).isExported();

      if (entry instanceof ModuleOrderEntry) {
        rootModel.removeOrderEntry(entry);

        final String dependencyModuleName = ((ModuleOrderEntry)entry).getModuleName();

        final MavenProject dependencyMavenProject = findMavenProjectByModuleName(dependencyModuleName);

        if (dependencyMavenProject == null) {
          MavenLog.LOG.warn("Maven project not found, module dependency skipped: " + myModule.getName() + " on " + dependencyModuleName);
          continue;
        }
        if (!ArrayUtil.contains(dependencyMavenProject.getPackaging(), FlexmojosImporter.SUPPORTED_PACKAGINGS)) {
          MavenLog.LOG.info("Unexpected packaging (" + dependencyMavenProject.getPackaging() + "), module dependency skipped: " +
                            myModule.getName() + " on " + dependencyModuleName);
          continue;
        }

        final ModifiableDependencyEntry existingEntry = ContainerUtil
          .find(bc.getDependencies().getModifiableEntries(),
                entry1 -> (entry1 instanceof BuildConfigurationEntry) &&
                          ((BuildConfigurationEntry)entry1).getModuleName().equals(dependencyModuleName) &&
                          ((BuildConfigurationEntry)entry1).getBcName().equals(dependencyModuleName));

        final LinkageType linkageType = "swc".equals(dependencyMavenProject.getPackaging())
                                        ? FlexUtils.convertLinkageType(scope, isExported)
                                        : LinkageType.LoadInRuntime;

        if (existingEntry != null) {
          if (existingEntry.getDependencyType().getLinkageType() == LinkageType.Test) {
            existingEntry.getDependencyType().setLinkageType(linkageType);
          }
          continue;
        }

        final ModifiableBuildConfigurationEntry bcEntry =
          myFlexEditor.createBcEntry(bc.getDependencies(), dependencyModuleName, dependencyModuleName);
        bcEntry.getDependencyType().setLinkageType(linkageType);
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
        final Library.ModifiableModel libraryModifiableModel = myModelsProvider.getModifiableLibraryModel(library);
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

      if (libraryName.contains(":swc:") ||
          libraryName.contains(":rb.swc:") ||
          libraryName.contains(":resource-bundle:") ||
          libraryName.contains(":ane:")) {
        playerglobal |= libraryName.contains("playerglobal");
        airglobal |= libraryName.contains("airglobal");
        mobilecomponents |= libraryName.contains("mobilecomponents");
        final boolean ane = libraryName.contains(":ane:") && !libraryName.contains(":swc:");

        final LibraryKind kind = ((LibraryEx)library).getKind();

        if (kind != FlexLibraryType.FLEX_LIBRARY) {
          if (kind == null) {
            final LibraryEx.ModifiableModelEx libraryModel = (LibraryEx.ModifiableModelEx)myModelsProvider.getModifiableLibraryModel(library);
            libraryModel.setKind(FlexLibraryType.FLEX_LIBRARY);
          }
        }

        final ModifiableDependencyEntry sharedLibraryEntry =
          myFlexEditor.createSharedLibraryEntry(bc.getDependencies(), ((LibraryOrderEntry)entry).getLibraryName(),
                                                ((LibraryOrderEntry)entry).getLibraryLevel());
        final LinkageType linkageType = ane ? DependencyType.DEFAULT_LINKAGE
                                            : FlexUtils.convertLinkageType(scope, isExported);
        sharedLibraryEntry.getDependencyType().setLinkageType(linkageType);
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
    final Collection<String> rbSwcPlaceholdersUrls = new ArrayList<>();
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

    final Collection<String> result = new ArrayList<>();
    for (final String locale : myCompiledLocales) {
      result.add(rbSwcUrlCommonPart + "-" + locale + RB_SWC_URL_END);
    }
    return result;
  }

  private void setupSdk(final ModifiableFlexBuildConfiguration bc) {
    final String path = getArtifactFilePath(myMavenProject, getFlexCompilerMavenId(), MavenConstants.TYPE_POM);
    final Sdk flexSdk = FlexSdkUtils.createOrGetSdk(FlexmojosSdkType.getInstance(), path);
    if (flexSdk != null) {
      ensureSdkHasRequiredAdditionalJarPaths(flexSdk);
      bc.getDependencies().setSdkEntry(Factory.createSdkEntry(flexSdk.getName()));
    }
  }

  private MavenId getFlexCompilerMavenId() {
    for (final MavenId mavenId : myFlexmojosPlugin.getDependencies()) {
      if (FLEX_COMPILER_ARTIFACT_ID.equals(mavenId.getArtifactId()) &&
          (FLEX_COMPILER_ADOBE_GROUP_ID.equals(mavenId.getGroupId()) || FLEX_COMPILER_APACHE_GROUP_ID.equals(mavenId.getGroupId()))) {
        return mavenId;
      }
    }

    for (final MavenArtifact mavenArtifact : myMavenProject.getDependencies()) {
      if ("com.adobe.flex".equals(mavenArtifact.getGroupId()) && "framework".equals(mavenArtifact.getArtifactId())
          ||
          "com.adobe.flex.framework".equals(mavenArtifact.getGroupId()) &&
          ("flex-framework".equals(mavenArtifact.getArtifactId()) ||
           "air-framework".equals(mavenArtifact.getArtifactId()))) {
        return new MavenId(FLEX_COMPILER_ADOBE_GROUP_ID, FLEX_COMPILER_ARTIFACT_ID, mavenArtifact.getVersion());
      }
      if ("org.apache.flex".equals(mavenArtifact.getGroupId()) && "framework".equals(mavenArtifact.getArtifactId())
          ||
          "org.apache.flex.framework".equals(mavenArtifact.getGroupId()) &&
          ("flex-framework".equals(mavenArtifact.getArtifactId()) ||
           "common-framework".equals(mavenArtifact.getArtifactId()))) {
        return new MavenId(FLEX_COMPILER_APACHE_GROUP_ID, FLEX_COMPILER_ARTIFACT_ID, mavenArtifact.getVersion());
      }
    }

    // correct flexmojos-maven-plugin resolving and taking version from 'flex.sdk.version' property value is rather expensive, so version is hardcoded
    final String version;
    final String pluginVersion = myFlexmojosPlugin.getVersion();

    if (StringUtil.compareVersionNumbers(pluginVersion, "5") >= 0) {
      version = "4.6.0.23201";
    }
    else if (StringUtil.compareVersionNumbers(pluginVersion, "4.1") >= 0 || pluginVersion != null && pluginVersion.startsWith("4.0-RC")) {
      version = "4.5.1.21328";
    }
    else if (StringUtil.compareVersionNumbers(pluginVersion, "4") >= 0) {
      version = "4.5.0.18623";
    }
    else {
      version = "3.2.0.3958";
    }
    return new MavenId(FLEX_COMPILER_ADOBE_GROUP_ID, FLEX_COMPILER_ARTIFACT_ID, version);
  }

  private static String getArtifactFilePath(final MavenProject mavenProject, final MavenId mavenId, final String type) {
    return FileUtil.toSystemIndependentName(MavenArtifactUtil.getArtifactFile(mavenProject.getLocalRepository(), mavenId, type).getPath());
  }

  private void ensureSdkHasRequiredAdditionalJarPaths(final @NotNull Sdk sdk) {
    assert sdk.getSdkType() instanceof FlexmojosSdkType;
    final FlexmojosSdkAdditionalData additionalData = ((FlexmojosSdkAdditionalData)sdk.getSdkAdditionalData());
    assert additionalData != null;

    for (MavenId dependency : myFlexmojosPlugin.getDependencies()) {
      if (StringUtil.isEmpty(dependency.getArtifactId())) continue;

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

  private void configureRuntimeLoadedModule(final ModifiableFlexBuildConfiguration mainBC,
                                            final RLMInfo info,
                                            final @Nullable ModifiableFlexBuildConfiguration existingRlmBC) {
    final BuildConfigurationNature nature = new BuildConfigurationNature(mainBC.getTargetPlatform(), mainBC.isPureAs(),
                                                                         OutputType.RuntimeLoadedModule);
    final boolean isNewBC = existingRlmBC == null;
    final ModifiableFlexBuildConfiguration rlmBC = isNewBC ? myFlexEditor.copyConfiguration(mainBC, nature) : existingRlmBC;

    rlmBC.setName(info.myRLMName);
    rlmBC.setMainClass(info.myMainClass);
    rlmBC.setOutputFileName(info.myOutputFileName);
    rlmBC.setOutputFolder(info.myOutputFolderPath);
    rlmBC.getCompilerOptions().setAdditionalConfigFilePath(getCompilerConfigFilePath(info.myRLMName));
  }

  protected Collection<RLMInfo> getRLMInfos() {
    final Element configurationElement = myFlexmojosPlugin.getConfigurationElement();
    final Element moduleFilesElement = configurationElement == null
                                       ? null : configurationElement.getChild("moduleFiles", configurationElement.getNamespace());

    if (moduleFilesElement == null) {
      return Collections.emptyList();
    }

    final List<RLMInfo> result = new ArrayList<>();
    for (final Element moduleFilePathElement : moduleFilesElement.getChildren()) {
      final String path = moduleFilePathElement.getTextNormalize();
      if (path.endsWith(".mxml") || path.endsWith(".as")) {
        final String mainClassRelativePath = FileUtil.toSystemIndependentName(path);
        final String mainClass = FileUtilRt.getNameWithoutExtension(mainClassRelativePath.replace('/', '.'));
        final String rlmName = StringUtil.getShortName(mainClass);
        final String outputFileName = myMavenProject.getFinalName() + "-" + rlmName + ".swf";
        final String outputFolderPath = FileUtil.toSystemIndependentName(myMavenProject.getBuildDirectory());
        final String configFilePath = getCompilerConfigFilePath(rlmName);
        result.add(new RLMInfo(rlmName, mainClass, mainClassRelativePath, outputFileName, outputFolderPath, configFilePath));
      }
    }
    return result;
  }

  /*
 private void reimportRuntimeLocalesFacets(final MavenProject project,
                                           final Module module,
                                           final MavenModifiableModelsProvider modelsProvider) {
   FacetModel model = modelsProvider.getModifiableFacetModel(module);

   String packaging = project.getPackaging();
   String extension = "swc".equals(packaging) ? "rb.swc" : packaging;

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
