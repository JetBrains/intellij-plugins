package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AppUIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.gem.module.ModuleGemInfrastructure;
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil;
import org.jetbrains.plugins.ruby.tasks.rake.RakeUtilBase;

import java.io.File;
import java.io.IOException;

/**
 * @author Dennis.Ushakov
 */
public class MotionCalabashUtil {
  private static Logger LOG = Logger.getInstance(MotionCalabashUtil.class);

  public static void addCalabashSupport(final Project project, final Module module, final String applicationHomePath) {
    AppUIUtil.invokeLaterIfProjectAlive(project, () -> {
      try {
        final boolean hadGemfile = addToGemfile(applicationHomePath);
        if (!hadGemfile) {
          addToRakefile(applicationHomePath);
        }
        ModuleGemInfrastructure.getInstance(module).updateModuleGemset();
      } catch (IOException e) {
        LOG.error(e);
      }
    });
  }

  private static void addToRakefile(String applicationHomePath) throws IOException {
    final File file = new File(applicationHomePath, RakeUtilBase.RAKE_FILE);
    final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    if (virtualFile == null) {
      LOG.warn("Unable add motion-calabash support: no " + RakeUtilBase.RAKE_FILE + " in " + applicationHomePath);
      return;
    }
    String text = VfsUtilCore.loadText(virtualFile);
    int pos = text.indexOf("require");
    pos = text.indexOf("\n", pos);
    text = text.substring(0, pos) + "require 'motion-calabash'\n" + text.substring(pos + 1);
    final String finalText = text;
    new WriteAction() {
      @Override
      protected void run(@NotNull Result result) throws Throwable {
        VfsUtil.saveText(virtualFile, finalText);
      }
    }.execute().throwException();
  }

  private static boolean addToGemfile(String applicationHomePath) throws IOException {
    final File file = new File(applicationHomePath, BundlerUtil.GEMFILE);
    VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    String text = "source 'https://rubygems.org'\n";
    final boolean hasGemfile = virtualFile != null && virtualFile.isValid();

    if (hasGemfile) {
        text = VfsUtilCore.loadText(virtualFile);
    } else {
        file.createNewFile();
        virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
    text += "\ngem 'motion-calabash'\n";
    assert virtualFile != null;
    final String finalText = text;
    final VirtualFile finalVirtualFile = virtualFile;
    new WriteAction() {
      @Override
      protected void run(@NotNull Result result) throws Throwable {
        VfsUtil.saveText(finalVirtualFile, finalText);
      }
    }.execute().throwException();
    return hasGemfile;
  }
}
