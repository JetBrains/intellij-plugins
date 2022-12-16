/*
 * Copyright 2016 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.facet;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.framework.library.DownloadableLibraryService;
import com.intellij.framework.library.FrameworkSupportWithLibrary;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurableBase;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportProviderBase;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.jam.model.util.JamCommonUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.model.xml.Filter;
import com.intellij.javaee.web.model.xml.FilterMapping;
import com.intellij.javaee.web.model.xml.WebApp;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.StrutsFileTemplateProvider;
import com.intellij.struts2.facet.ui.StrutsConfigsSearcher;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.descriptors.ConfigFile;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * "Add Framework" support.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<StrutsFacet> {

  private static final Logger LOG = Logger.getInstance(StrutsFrameworkSupportProvider.class);

  public StrutsFrameworkSupportProvider() {
    super(StrutsFacetType.getInstance());
  }

  @Override
  public String getTitle() {
    return UIUtil.replaceMnemonicAmpersand("Struts &2");
  }

  @NotNull
  @Override
  public FrameworkSupportConfigurableBase createConfigurable(@NotNull final FrameworkSupportModel model) {
    return new Struts2FrameworkSupportConfigurable(this, model, getVersions(), getVersionLabelText());
  }

  @Override
  protected void setupConfiguration(final StrutsFacet strutsFacet,
                                    final ModifiableRootModel modifiableRootModel, final FrameworkVersion version) {
  }

  @Override
  public boolean isEnabledForModuleBuilder(@NotNull ModuleBuilder builder) {
    return false;
  }

  @Override
  protected void onFacetCreated(final StrutsFacet strutsFacet,
                                final ModifiableRootModel modifiableRootModel,
                                final FrameworkVersion version) {
    final Module module = strutsFacet.getModule();
    StartupManager.getInstance(module.getProject()).runAfterOpened(() -> {
      DumbService.getInstance(module.getProject()).runWhenSmart(() -> {
          final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
          if (sourceRoots.length <= 0) {
            return;
          }

          final PsiDirectory directory = PsiManager.getInstance(module.getProject()).findDirectory(sourceRoots[0]);
          if (directory == null ||
              directory.findFile(StrutsConstants.STRUTS_XML_DEFAULT_FILENAME) != null) {
            return;
          }

          final StrutsFileTemplateProvider templateProvider = new StrutsFileTemplateProvider(module);
          final FileTemplate strutsXmlTemplate = templateProvider.determineFileTemplate(directory.getProject());

          try {
            final StrutsFacetConfiguration strutsFacetConfiguration = strutsFacet.getConfiguration();

            // create empty struts.xml & fileset with all found struts-*.xml files (struts2.jar, plugins)
            final PsiElement psiElement = FileTemplateUtil.createFromTemplate(strutsXmlTemplate,
                                                                              StrutsConstants.STRUTS_XML_DEFAULT_FILENAME,
                                                                              null,
                                                                              directory);
            final Set<StrutsFileSet> empty = Collections.emptySet();
            final StrutsFileSet fileSet = new StrutsFileSet(StrutsFileSet.getUniqueId(empty),
                                                            StrutsFileSet.getUniqueName("Default File Set", empty),
                                                            strutsFacetConfiguration);
            fileSet.addFile(((XmlFile)psiElement).getVirtualFile());

            final StrutsConfigsSearcher searcher = new StrutsConfigsSearcher(module);
            DumbService.getInstance(module.getProject()).runWhenSmart(() -> searcher.search());

            final MultiMap<VirtualFile, PsiFile> jarConfigFiles = searcher.getJars();
            for (final VirtualFile virtualFile : jarConfigFiles.keySet()) {
              final Collection<PsiFile> psiFiles = jarConfigFiles.get(virtualFile);
              for (final PsiFile psiFile : psiFiles) {
                fileSet.addFile(psiFile.getVirtualFile());
              }
            }
            strutsFacetConfiguration.getFileSets().add(fileSet);


            // create filter & mapping in web.xml (if present)
            WriteCommandAction.writeCommandAction(modifiableRootModel.getProject()).run(() -> {
              final WebFacet webFacet = strutsFacet.getWebFacet();
              if (null == webFacet) return;

              final ConfigFile configFile = webFacet.getWebXmlDescriptor();
              if (configFile == null) return;

              final XmlFile webXmlFile = configFile.getXmlFile();
              final WebApp webApp = JamCommonUtil.getRootElement(webXmlFile, WebApp.class);
              if (webApp == null) return;
              if (!FileModificationService.getInstance().prepareFileForWrite(webXmlFile)) return;

              final Filter strutsFilter = webApp.addFilter();
              strutsFilter.getFilterName().setStringValue("struts2");

              @NonNls final String filterClass = templateProvider.is21orNewer() ?
                                                 StrutsConstants.STRUTS_2_1_FILTER_CLASS :
                                                 StrutsConstants.STRUTS_2_0_FILTER_CLASS;
              strutsFilter.getFilterClass().setStringValue(filterClass);

              final FilterMapping filterMapping = webApp.addFilterMapping();
              filterMapping.getFilterName().setValue(strutsFilter);
              filterMapping.addUrlPattern().setStringValue("/*");
            });


            final NotificationListener showFacetSettingsListener = new NotificationListener() {
              @Override
              public void hyperlinkUpdate(@NotNull final Notification notification,
                                          @NotNull final HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                  notification.expire();
                  ModulesConfigurator.showFacetSettingsDialog(strutsFacet, null);
                }
              }
            };

            new Notification("Struts 2", "Struts 2 Setup",
                             "Struts 2 Facet has been created, please check <a href=\"more\">created fileset</a>",
                             NotificationType.INFORMATION)
              .setListener(showFacetSettingsListener)
              .notify(module.getProject());
          }
          catch (Exception e) {
            LOG.error("error creating struts.xml from template", e);
          }
        });
    });
  }

  private static final class Struts2FrameworkSupportConfigurable extends FrameworkSupportConfigurableBase
    implements FrameworkSupportWithLibrary {

    private Struts2FrameworkSupportConfigurable(FrameworkSupportProviderBase frameworkSupportProvider,
                                                FrameworkSupportModel model,
                                                @NotNull List<FrameworkVersion> versions,
                                                @Nullable String versionLabelText) {
      super(frameworkSupportProvider, model, versions, versionLabelText);
    }

    @NotNull
    @Override
    public CustomLibraryDescription createLibraryDescription() {
      return DownloadableLibraryService.getInstance().createDescriptionForType(Struts2LibraryType.class);
    }

    @Override
    public boolean isLibraryOnly() {
      return false;
    }
  }
}
