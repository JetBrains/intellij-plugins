package com.intellij.tapestry.intellij.facet;

import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.core.maven.MavenConfiguration;
import com.intellij.tapestry.core.maven.MavenUtils;
import com.intellij.tapestry.core.maven.RemoteRepositoryDescription;
import com.intellij.tapestry.core.util.StringUtils;
import com.intellij.tapestry.intellij.util.IntellijWebDescriptorUtils;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class AddTapestrySupportUtil {

  private static final Logger _logger = LoggerFactory.getInstance().getLogger(AddTapestrySupportUtil.class);

  public static void addSupportInWriteCommandAction(@NotNull final ModuleRootModel rootModel,
                                                    @NotNull final TapestryFacetConfiguration configuration,
                                                    final boolean generateStartupApplication,
                                                    final boolean generatePom) {
    if (configuration.getApplicationPackage() == null) return;
    new WriteCommandAction.Simple(rootModel.getModule().getProject()) {
      protected void run() throws Throwable {
        try {
          addSupport(rootModel, configuration, generateStartupApplication, generatePom);
        }
        catch (Exception ex) {
          _logger.error(ex);
        }
      }
    }.execute();
  }

  private static void addSupport(ModuleRootModel rootModel,
                                TapestryFacetConfiguration configuration,
                                boolean generateStartupApplication,
                                boolean generatePom) throws Exception {

    //StartupManager.getInstance(module.getProject()).registerPostStartupActivity(new Runnable() {
    //    public void run() {
    //        try {
    //            // add compiler resources
    //            module.getProject().getComponent(TapestryProjectSupportLoader.class).addCompilerResources();
    //        } catch (Exception ex) {
    //            _logger.warn(ex);
    //        }
    //    }
    //});

    // update web.xml
    //patchWebXml(app, configuration);

    //if (generateStartupApplication && rootModel != null)
    //    generateStartupApplication(rootModel, module, webFacet, configuration);
    //
    if (generatePom && rootModel != null) generatePom(rootModel, configuration);
  }

  private static void generatePom(ModuleRootModel rootModel, TapestryFacetConfiguration configuration) throws IOException {
    VirtualFile[] contentRoots = rootModel.getContentRoots();

    if (contentRoots.length == 0) {
      _logger.warn("Couldn't generate pom because it wasn't possible to determine the module content root.");
      return;
    }
    if (rootModel.getSourceRoots().length == 0) {
      _logger.warn("Coulnd't generate startup application because it wasn't possible to determine module source root");
      return;
    }

    String modulePath = contentRoots[0].getPath();

    try {
      String pomText = StringUtils
          .toString(AddTapestrySupportUtil.class.getResourceAsStream("/fileTemplates/j2ee/" + TapestryConstants.POM_TEMPLATE_NAME + ".ft"));
      pomText = pomText.replace("${GROUP}", configuration.getApplicationPackage());
      pomText = pomText.replace("${ARTIFACT}", configuration.getFilterName());
      pomText = pomText.replace("${NAME}", rootModel.getModule().getName());
      pomText = pomText.replace("${SOURCE_PATH}", rootModel.getSourceRoots()[0].getPath().substring(modulePath.length() + 1));
      pomText = pomText.replace("${TAPESTRY_VERSION}", configuration.getVersion().toString());

      VirtualFileManager.getInstance().findFileByUrl("file://" + modulePath);

      final PsiDirectory psiDirectory = PsiManager.getInstance(rootModel.getModule().getProject())
          .findDirectory(VirtualFileManager.getInstance().findFileByUrl("file://" + modulePath));
      final String pomXml = "pom.xml";
      PsiFile pomFile = psiDirectory.findFile(pomXml);
      if(pomFile == null) pomFile = psiDirectory.createFile(pomXml);
      VfsUtil.saveText(pomFile.getVirtualFile(), pomText);
    }
    catch (Exception ex) {
      _logger.error(ex);
    }

    MavenUtils.createMavenSupport(modulePath, new MavenConfiguration(false, false, null, null, null, configuration.getApplicationPackage(),
                                                                     configuration.getFilterName(), "1.0-SNAPSHOT",
                                                                     new ArrayList<RemoteRepositoryDescription>()),
                                  configuration.getVersion().toString());
  }

  private static void patchWebXml(WebApp app, TapestryFacetConfiguration configuration) {
    IntellijWebDescriptorUtils.updateFilter(app, configuration.getFilterName());

    IntellijWebDescriptorUtils.setApplicationPackage(app, configuration.getApplicationPackage());
  }

  private static void generateStartupApplication(ModuleRootModel rootModel,
                                                 Module module,
                                                 WebFacet webFacet,
                                                 TapestryFacetConfiguration configuration) {
    if (rootModel.getSourceRoots().length == 0) {
      _logger.warn("Coulnd't generate startup application because it wasn't possible to determine module source root");

      return;
    }

    PsiDirectory sourceDirectory = PsiManager.getInstance(module.getProject()).findDirectory(rootModel.getSourceRoots()[0]);

    PsiDirectory pagesDirectory = null;
    PsiDirectory servicesDirectory = null;

    // create packages
    try {
      StringTokenizer tokenizer = new StringTokenizer(configuration.getApplicationPackage(), ".");

      while (tokenizer.hasMoreTokens()) {
        String currentToken = tokenizer.nextToken();

        sourceDirectory = sourceDirectory.createSubdirectory(currentToken);
      }

      sourceDirectory.createSubdirectory(TapestryConstants.COMPONENTS_PACKAGE);

      pagesDirectory = sourceDirectory.createSubdirectory(TapestryConstants.PAGES_PACKAGE);
      servicesDirectory = sourceDirectory.createSubdirectory(TapestryConstants.SERVICES_PACKAGE);
    }
    catch (IncorrectOperationException ex) {
      _logger.error(ex);
    }

    // create Start page
    if (pagesDirectory != null) {
      try {
        String classText = StringUtils.toString(AddTapestrySupportUtil.class.getResourceAsStream(
            "/fileTemplates/j2ee/" + TapestryConstants.START_PAGE_CLASS_TEMPLATE_NAME + ".ft"));
        classText = classText.replace("${PACKAGE_NAME}", configuration.getApplicationPackage() + "." + TapestryConstants.PAGES_PACKAGE);

        VirtualFile startPageClass = pagesDirectory.getVirtualFile().createChildData(pagesDirectory, "Start.java");
        VfsUtil.saveText(startPageClass, classText);

        CodeStyleManager.getInstance(module.getProject()).reformat(pagesDirectory.findFile("Start.java"));


        String templateText = StringUtils.toString(AddTapestrySupportUtil.class.getResourceAsStream(
            "/fileTemplates/j2ee/" + TapestryConstants.START_PAGE_TEMPLATE_TEMPLATE_NAME + ".ft"));

        VirtualFile startPageTemplate = webFacet.getWebRoots(false).get(0).getFile()
            .createChildData(pagesDirectory, "Start." + TapestryConstants.TEMPLATE_FILE_EXTENSION);
        VfsUtil.saveText(startPageTemplate, templateText);
      }
      catch (Exception ex) {
        _logger.error(ex);
      }
    }

    // create module builder
    if (servicesDirectory != null) {
      try {
        String text = StringUtils.toString(AddTapestrySupportUtil.class.getResourceAsStream(
            "/fileTemplates/j2ee/" + TapestryConstants.MODULE_BUILDER_CLASS_TEMPLATE_NAME + ".ft"));
        text = text.replace("${PACKAGE_NAME}", configuration.getApplicationPackage() + "." + TapestryConstants.SERVICES_PACKAGE);
        text = text.replace("${NAME}", StringUtil.capitalize(configuration.getFilterName()) + TapestryConstants.MODULE_BUILDER_SUFIX);

        VirtualFile moduleBuilder = servicesDirectory.getVirtualFile().createChildData(servicesDirectory, StringUtil
            .capitalize(configuration.getFilterName()) + TapestryConstants.MODULE_BUILDER_SUFIX + ".java");
        VfsUtil.saveText(moduleBuilder, text);

        CodeStyleManager.getInstance(module.getProject()).reformat(servicesDirectory.findFile(
            StringUtil.capitalize(configuration.getFilterName()) + TapestryConstants.MODULE_BUILDER_SUFIX + ".java"));
      }
      catch (Exception ex) {
        _logger.error(ex);
      }
    }
  }
}
