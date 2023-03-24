// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.BuildConfigurationEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PairConsumer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.importing.MavenExtraArtifactType;
import org.jetbrains.idea.maven.importing.MavenImporter;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.server.NativeMavenProjectHolder;
import org.jetbrains.idea.maven.utils.MavenLog;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.event.HyperlinkEvent;
import java.util.*;

public class FlexmojosImporter extends MavenImporter implements FlexConfigInformer {
  static final String[] SUPPORTED_PACKAGINGS = {"swf", "swc", "air"};
  static final String FLEXMOJOS_GROUP_ID = "org.sonatype.flexmojos";
  static final String FLEXMOJOS_ARTIFACT_ID = "flexmojos-maven-plugin";
  private static final List<String> DEPENDENCY_TYPES_FOR_IMPORT = Arrays.asList("swf", "swc", "ane", "resource-bundle", "rb.swc");
  private static final List<String> DEPENDENCY_TYPES_FOR_COMPLETION = Arrays.asList("swf", "swc", "ane", "resource-bundle", "rb.swc");

  private Notification myFlexConfigNotification;

  // instantiated as an extension
  public FlexmojosImporter() {
    super(FLEXMOJOS_GROUP_ID, FLEXMOJOS_ARTIFACT_ID);
  }

  protected FlexmojosImporter(final String pluginGroupID, final String pluginArtifactID) {
    super(pluginGroupID, pluginArtifactID);
  }

  @Override
  public boolean isApplicable(MavenProject mavenProject) {
    return ArrayUtil.contains(mavenProject.getPackaging(), SUPPORTED_PACKAGINGS) && super.isApplicable(mavenProject);
  }

  @Override
  @NotNull
  public ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  public void getSupportedPackagings(Collection<? super String> result) {
    Collections.addAll(result, SUPPORTED_PACKAGINGS);
  }

  @Override
  public void getSupportedDependencyTypes(Collection<? super String> result, SupportedRequestType type) {
    result.addAll(type == SupportedRequestType.FOR_COMPLETION ? DEPENDENCY_TYPES_FOR_COMPLETION : DEPENDENCY_TYPES_FOR_IMPORT);
  }

  @Override
  public void getSupportedDependencyScopes(Collection<? super String> result) {
    Collections.addAll(result, "merged", "internal", "external", "caching", "rsl");
  }

  @Override
  public void resolve(Project project, MavenProject mavenProject, NativeMavenProjectHolder nativeMavenProject,
                      MavenEmbedderWrapper embedder, ResolveContext context) throws MavenProcessCanceledException {
    final MavenPlugin plugin = getFlexmojosPlugin(mavenProject);
    final String version = plugin.getVersion();
    if (version != null && StringUtil.compareVersionNumbers(version, "4") < 0) {
      embedder.resolvePlugin(plugin, nativeMavenProject);
    }
  }

  @NotNull
  private MavenPlugin getFlexmojosPlugin(final MavenProject mavenProject) {
    final MavenPlugin plugin = mavenProject.findPlugin(myPluginGroupID, myPluginArtifactID);
    assert plugin != null;
    return plugin;
  }

  static boolean isFlexmojos3(final MavenPlugin plugin) {
    final String version = plugin.getVersion();
    return version != null && version.startsWith("3.");
  }

  @Override
  @Nullable
  public Pair<String, String> getExtraArtifactClassifierAndExtension(MavenArtifact artifact, MavenExtraArtifactType type) {
    if (!DEPENDENCY_TYPES_FOR_IMPORT.contains(artifact.getType())) return null;
    if (type == MavenExtraArtifactType.DOCS) return Pair.create("asdoc", "zip");
    return null;
  }

  static boolean isFlexApp(MavenProject project) {
    return "swf".equals(project.getPackaging()) || "air".equals(project.getPackaging());
  }

  @Override
  public void process(final @NotNull IdeModifiableModelsProvider modelsProvider,
                      final @NotNull Module module,
                      final @NotNull MavenRootModelAdapter modelAdapter,
                      final @NotNull MavenProjectsTree mavenTree,
                      final @NotNull MavenProject mavenProject,
                      final @NotNull MavenProjectChanges changes,
                      final @NotNull Map<MavenProject, String> mavenProjectToModuleName,
                      final @NotNull List<MavenProjectsProcessorTask> postTasks) {
    expireNotification();

    final FlexProjectConfigurationEditor currentEditor = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();
    final boolean needToCommit = currentEditor == null;
    final LibraryTable.ModifiableModel projectLibrariesModel = modelsProvider.getModifiableProjectLibrariesModel();
    final Map<Module, ModifiableRootModel> moduleToModifiableModel = Collections.singletonMap(module, modelAdapter.getRootModel());

    final FlexProjectConfigurationEditor flexEditor =
      currentEditor != null
      ? currentEditor
      : new FlexProjectConfigurationEditor(module.getProject(),
                                           FlexProjectConfigurationEditor
                                             .createModelProvider(moduleToModifiableModel, projectLibrariesModel, null)) {
        @Override
        @Nullable
        protected Module findModuleWithBC(final BuildConfigurationEntry bcEntry) {
          // don't check BC presence here because corresponding BC may appear later in next import cycle
          return modelAdapter.findModuleByName(bcEntry.getModuleName());
        }
      };

    final MavenPlugin flexmojosPlugin = getFlexmojosPlugin(mavenProject);

    final Flexmojos3Configurator configurator =
      StringUtil.compareVersionNumbers(flexmojosPlugin.getVersion(), "5") >= 0
      ? new Flexmojos5Configurator(module, modelsProvider, flexEditor, mavenTree, mavenProjectToModuleName, mavenProject, flexmojosPlugin,
                                   getCompiledLocales(mavenProject), getRuntimeLocales(mavenProject), this)
      : StringUtil.compareVersionNumbers(flexmojosPlugin.getVersion(), "4") >= 0
        ? new Flexmojos4Configurator(module, modelsProvider, flexEditor, mavenTree, mavenProjectToModuleName, mavenProject, flexmojosPlugin,
                                     getCompiledLocales(mavenProject), getRuntimeLocales(mavenProject), this)
        : new Flexmojos3Configurator(module, modelsProvider, flexEditor, mavenTree, mavenProjectToModuleName, mavenProject, flexmojosPlugin,
                                     getCompiledLocales(mavenProject), getRuntimeLocales(mavenProject), this);
    configurator.configureAndAppendTasks(postTasks);

    if (needToCommit) {
      try {
        flexEditor.commit();
      }
      catch (ConfigurationException e) {
        MavenLog.LOG.error(e); // can't happen
      }
    }
  }

  @Override
  public void collectSourceRoots(MavenProject mavenProject, PairConsumer<String, JpsModuleSourceRootType<?>> result) {
    final String localesDir = findConfigValue(mavenProject, "resourceBundlePath", "src/main/locales/{locale}");
    assert localesDir != null;

    for (String locale : getCompiledLocales(mavenProject)) {
      result.consume(localesDir.replace("{locale}", locale), JavaSourceRootType.SOURCE);
    }

    for (String locale : getRuntimeLocales(mavenProject)) {
      result.consume(localesDir.replace("{locale}", locale), JavaSourceRootType.SOURCE);
    }
  }

  private List<String> getRuntimeLocales(final MavenProject mavenProject) {
    final String elementName = isFlexmojos3(getFlexmojosPlugin(mavenProject)) ? "runtimeLocales" : "localesRuntime";
    final Element localesElement = getConfig(mavenProject, elementName);
    return localesElement == null ? Collections.emptyList() : getChildrenValues(localesElement);
  }

  private List<String> getCompiledLocales(final MavenProject mavenProject) {
    final boolean flexmojos3 = isFlexmojos3(getFlexmojosPlugin(mavenProject));
    final String elementName = flexmojos3 ? "compiledLocales" : "localesCompiled";
    Element localesElement = getConfig(mavenProject, elementName);
    if (flexmojos3 && localesElement == null) localesElement = getConfig(mavenProject, "locales");
    if (localesElement != null) {
      return getChildrenValues(localesElement);
    }

    if (isFlexApp(mavenProject)) {
      final String defaultLocale = getDefaultLocale(mavenProject);
      if (defaultLocale != null) {
        return Collections.singletonList(defaultLocale);
      }
    }

    return Collections.emptyList();
  }

  private static List<String> getChildrenValues(final Element element) {
    final List<String> result = new ArrayList<>();
    for (Element child : element.getChildren()) {
      final String childValue = child.getTextNormalize();
      if (!StringUtil.isEmptyOrSpaces(childValue)) {
        result.add(childValue);
      }
    }
    return result;
  }

  @Nullable
  private String getDefaultLocale(final MavenProject mavenProject) {
    return "true".equals(findConfigValue(mavenProject, "useDefaultLocale", "true"))
           ? findConfigValue(mavenProject, "defaultLocale", "en_US")
           : null;
  }

  @Override
  public synchronized void showFlexConfigWarningIfNeeded(final Project project) {
    if (myFlexConfigNotification != null) return; // already shown
    doShowFlexConfigWarning(project);
  }

  private synchronized void doShowFlexConfigWarning(final Project project) {
    final NotificationListener listener = new NotificationListener() {
      @Override
      public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
        Messages
          .showWarningDialog(project, FlexBundle.message("flexmojos.warning.detailed"), FlexBundle.message("flexmojos.project.import"));
        notification.expire();
      }
    };
    myFlexConfigNotification =
      new Notification("Maven", FlexBundle.message("flexmojos.project.import"), FlexBundle.message("flexmojos.warning.short"),
                       NotificationType.WARNING).setListener(listener);

    myFlexConfigNotification.notify(project);
  }


  protected synchronized void expireNotification() {
    if (myFlexConfigNotification != null && !myFlexConfigNotification.isExpired()) {
      myFlexConfigNotification.expire();
    }
    myFlexConfigNotification = null;
  }

  public static String getOutputFilePath(final MavenProject mavenProject) {
    MavenPlugin flexmojosPlugin = mavenProject.findPlugin(FLEXMOJOS_GROUP_ID, FLEXMOJOS_ARTIFACT_ID);
    if (flexmojosPlugin == null) flexmojosPlugin = mavenProject.findPlugin(Flexmojos5Importer.FLEXMOJOS_5_GROUP_ID, FLEXMOJOS_ARTIFACT_ID);

    final Element configurationElement = flexmojosPlugin == null ? null : flexmojosPlugin.getConfigurationElement();

    final String overriddenTargetFilePath =
      configurationElement == null || StringUtil.compareVersionNumbers(flexmojosPlugin.getVersion(), "4") >= 0
      ? null
      : configurationElement.getChildText("output", configurationElement.getNamespace());

    if (overriddenTargetFilePath != null && !overriddenTargetFilePath.isEmpty()) {
      return FileUtil.isAbsolute(overriddenTargetFilePath)
             ? FileUtil.toSystemIndependentName(overriddenTargetFilePath)
             : FileUtil.toSystemIndependentName(mavenProject.getDirectory() + "/" + overriddenTargetFilePath);
    }

    final String classifier =
      configurationElement == null ? null : configurationElement.getChildText("classifier", configurationElement.getNamespace());
    final String suffix = classifier == null ? "" : "-" + classifier;
    final String fileExtension = "swc".equals(mavenProject.getPackaging()) ? "swc" : "swf";
    return FileUtil.toSystemIndependentName(mavenProject.getBuildDirectory())
           + "/" + mavenProject.getFinalName() + suffix + "." + fileExtension;
  }
}
