package com.intellij.tapestry.intellij.facet;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.maven.MavenConfiguration;
import com.intellij.tapestry.core.maven.MavenUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

public final class AddTapestrySupportUtil {

  private static final Logger _logger = Logger.getInstance(AddTapestrySupportUtil.class);

  public static void addSupportInWriteCommandAction(@NotNull final Module module,
                                                    @NotNull final TapestryFacetConfiguration configuration,
                                                    final boolean generateStartupApplication,
                                                    final boolean generatePom) {
    if (configuration.getApplicationPackage() == null) return;
    WriteCommandAction.writeCommandAction(module.getProject()).run(() -> {
      try {
        addSupport(module, configuration, generateStartupApplication, generatePom);
      }
      catch (Exception ex) {
        _logger.error(ex);
      }
    });
  }

  private static void addSupport(final Module module,
                                 final TapestryFacetConfiguration configuration,
                                 boolean generateStartupApplication,  // TODO !!!!
                                 boolean generatePom) throws Exception {

    if (generatePom) {
      generatePom(module, configuration);
    }
  }

  private static void generatePom(final Module module, TapestryFacetConfiguration configuration) throws IOException {
    ModuleRootManager rootModel = ModuleRootManager.getInstance(module);
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
      String pomText = FileUtil.loadTextAndClose(
        AddTapestrySupportUtil.class.getResourceAsStream("/fileTemplates/j2ee/" + TapestryConstants.POM_TEMPLATE_NAME + ".ft"));
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
      if (pomFile == null) pomFile = psiDirectory.createFile(pomXml);
      VfsUtil.saveText(pomFile.getVirtualFile(), pomText);
    }
    catch (Exception ex) {
      _logger.error(ex);
    }

    MavenUtils.createMavenSupport(modulePath, new MavenConfiguration(false, false, null, null, null, configuration.getApplicationPackage(),
                                                                     configuration.getFilterName(), "1.0-SNAPSHOT",
                                                                     new ArrayList<>()),
                                  configuration.getVersion().toString());
  }

}
