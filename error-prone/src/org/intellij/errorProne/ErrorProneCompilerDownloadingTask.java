package org.intellij.errorProne;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.util.download.DownloadableFileDescription;
import com.intellij.util.download.DownloadableFileService;
import com.intellij.util.download.DownloadableFileSetDescription;
import com.intellij.util.download.DownloadableFileSetVersions;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author nik
 */
public class ErrorProneCompilerDownloadingTask implements CompileTask {
  private static final Logger LOG = Logger.getInstance(ErrorProneCompilerDownloadingTask.class);

  @Override
  public boolean execute(CompileContext context) {
    if (!ErrorProneClasspathProvider.isErrorProneCompilerSelected(context.getProject())) {
      return true;
    }

    DownloadableFileService service = DownloadableFileService.getInstance();
    DownloadableFileSetVersions<DownloadableFileSetDescription> versions = service.createFileSetVersions(null, getClass().getResource("/library/error-prone.xml"));
    List<DownloadableFileSetDescription> descriptions = versions.fetchVersions();
    if (descriptions.isEmpty()) {
      context.addMessage(CompilerMessageCategory.ERROR, "No error-prone compiler versions loaded", null, -1, -1);
      return false;
    }

    DownloadableFileSetDescription latestVersion = descriptions.get(0);
    File cacheDir = ErrorProneClasspathProvider.getCompilerFilesDir(latestVersion.getVersionString());
    if (ErrorProneClasspathProvider.getJarFiles(cacheDir).length > 0) {
      return true;
    }

    try {
      List<Pair<File, DownloadableFileDescription>> pairs = service.createDownloader(latestVersion).download(cacheDir);
      if (pairs.isEmpty() || ErrorProneClasspathProvider.getJarFiles(cacheDir).length == 0) {
        context.addMessage(CompilerMessageCategory.ERROR, "No compiler JARs were downloaded", null, -1, -1);
        return false;
      }
    }
    catch (IOException e) {
      LOG.info(e);
      context.addMessage(CompilerMessageCategory.ERROR, "Failed to download error-prone compiler JARs: " + e.getMessage(), null, -1, -1);
      return false;
    }
    return true;
  }
}
