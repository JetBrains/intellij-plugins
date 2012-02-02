package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.BuildConfigurationEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.importing.MavenExtraArtifactType;
import org.jetbrains.idea.maven.importing.MavenImporter;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.server.NativeMavenProjectHolder;
import org.jetbrains.idea.maven.utils.MavenLog;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;

import javax.swing.event.HyperlinkEvent;
import java.util.*;

public class FlexmojosImporter extends MavenImporter implements FlexConfigInformer {
  private static final String[] SUPPORTED_PACKAGINGS = {"swf", "swc"};
  static final String FLEXMOJOS_GROUP_ID = "org.sonatype.flexmojos";
  static final String FLEXMOJOS_ARTIFACT_ID = "flexmojos-maven-plugin";
  private static final List<String> DEPENDENCY_TYPES_FOR_IMPORT = Arrays.asList("swf", "swc", "resource-bundle", "rb.swc");
  private static final List<String> DEPENDENCY_TYPES_FOR_COMPLETION = Arrays.asList("swf", "swc", "resource-bundle", "rb.swc");

  private Notification myFlexConfigNotification;

  public FlexmojosImporter() {
    super(FLEXMOJOS_GROUP_ID, FLEXMOJOS_ARTIFACT_ID);
  }

  public boolean isApplicable(MavenProject mavenProject) {
    return ArrayUtil.contains(mavenProject.getPackaging(), SUPPORTED_PACKAGINGS) && super.isApplicable(mavenProject);
  }

  @NotNull
  public ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  public void getSupportedPackagings(Collection<String> result) {
    Collections.addAll(result, SUPPORTED_PACKAGINGS);
  }

  public void getSupportedDependencyTypes(Collection<String> result, SupportedRequestType type) {
    result.addAll(type == SupportedRequestType.FOR_COMPLETION ? DEPENDENCY_TYPES_FOR_COMPLETION : DEPENDENCY_TYPES_FOR_IMPORT);
  }

  public void getSupportedDependencyScopes(Collection<String> result) {
    Collections.addAll(result, "merged", "internal", "external", "caching", "rsl");
  }

  public void resolve(Project project, MavenProject mavenProject, NativeMavenProjectHolder nativeMavenProject,
                      MavenEmbedderWrapper embedder) throws MavenProcessCanceledException {
    final MavenPlugin plugin = getFlexmojosPlugin(mavenProject);
    final String version = plugin.getVersion();
    if (version != null && StringUtil.compareVersionNumbers(version, "4") < 0) {
      embedder.resolvePlugin(plugin, mavenProject.getRemoteRepositories(), nativeMavenProject, true);
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

  @Nullable
  public Pair<String, String> getExtraArtifactClassifierAndExtension(MavenArtifact artifact, MavenExtraArtifactType type) {
    if (!DEPENDENCY_TYPES_FOR_IMPORT.contains(artifact.getType())) return null;
    if (type == MavenExtraArtifactType.DOCS) return Pair.create("asdoc", "zip");
    return null;
  }

  static boolean isFlexApp(MavenProject project) {
    return "swf".equalsIgnoreCase(project.getPackaging());
  }

  public void preProcess(Module module, MavenProject mavenProject, MavenProjectChanges changes,
                         MavenModifiableModelsProvider modifiableModelsProvider) {
  }

  public void process(final MavenModifiableModelsProvider modelsProvider,
                      final Module module,
                      final MavenRootModelAdapter modelAdapter,
                      final MavenProjectsTree mavenTree,
                      final MavenProject mavenProject,
                      final MavenProjectChanges changes,
                      final Map<MavenProject, String> mavenProjectToModuleName,
                      final List<MavenProjectsProcessorTask> postTasks) {
    expireNotification();

    final FlexProjectConfigurationEditor currentEditor = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();
    final boolean needToCommit = currentEditor == null;
    final LibraryTableBase.ModifiableModelEx projectLibrariesModel =
      (LibraryTableBase.ModifiableModelEx)modelsProvider.getProjectLibrariesModel();
    final Map<Module, ModifiableRootModel> moduleToModifiableModel = Collections.singletonMap(module, modelAdapter.getRootModel());

    final FlexProjectConfigurationEditor flexEditor =
      currentEditor != null
      ? currentEditor
      : new FlexProjectConfigurationEditor(module.getProject(),
                                           FlexProjectConfigurationEditor
                                             .createModelProvider(moduleToModifiableModel, projectLibrariesModel, null)) {
        protected Module findModule(final BuildConfigurationEntry bcEntry) {
          return modelAdapter.findModuleByName(bcEntry.getModuleName());
        }
      };

    final MavenPlugin flexmojosPlugin = getFlexmojosPlugin(mavenProject);

    final Flexmojos3Configurator configurator =
      StringUtil.compareVersionNumbers(flexmojosPlugin.getVersion(), "4") >= 0
      ? new Flexmojos4Configurator(module, modelsProvider, flexEditor, mavenTree, mavenProject, flexmojosPlugin,
                                   getCompiledLocales(mavenProject), getRuntimeLocales(mavenProject), this)
      : new Flexmojos3Configurator(module, modelsProvider, flexEditor, mavenTree, mavenProject, flexmojosPlugin,
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

  public void collectSourceFolders(final MavenProject mavenProject, final List<String> result) {
    final String localesDir = findConfigValue(mavenProject, "resourceBundlePath", "src/main/locales/{locale}");
    assert localesDir != null;

    for (String locale : getCompiledLocales(mavenProject)) {
      result.add(localesDir.replace("{locale}", locale));
    }

    for (String locale : getRuntimeLocales(mavenProject)) {
      result.add(localesDir.replace("{locale}", locale));
    }
  }

  private List<String> getRuntimeLocales(final MavenProject mavenProject) {
    final String elementName = isFlexmojos3(getFlexmojosPlugin(mavenProject)) ? "runtimeLocales" : "localesRuntime";
    final Element localesElement = getConfig(mavenProject, elementName);
    return localesElement == null ? Collections.<String>emptyList() : getChildrenValues(localesElement);
  }

  private List<String> getCompiledLocales(final MavenProject mavenProject) {
    final String elementName = isFlexmojos3(getFlexmojosPlugin(mavenProject)) ? "compiledLocales" : "localesCompiled";
    final Element localesElement = getConfig(mavenProject, elementName);
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
    final List<String> result = new ArrayList<String>();
    //noinspection unchecked
    for (Element child : (Iterable<Element>)element.getChildren()) {
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

  public synchronized void showFlexConfigWarningIfNeeded(final Project project) {
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


  protected synchronized void expireNotification() {
    if (myFlexConfigNotification != null && !myFlexConfigNotification.isExpired()) {
      myFlexConfigNotification.expire();
    }
    myFlexConfigNotification = null;
  }
}
