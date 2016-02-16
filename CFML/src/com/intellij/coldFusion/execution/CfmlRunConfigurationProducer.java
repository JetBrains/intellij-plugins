package com.intellij.coldFusion.execution;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.UI.runner.CfmlRunConfiguration;
import com.intellij.coldFusion.UI.runner.CfmlRunConfigurationType;
import com.intellij.coldFusion.UI.runner.CfmlRunnerParameters;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

import static com.intellij.coldFusion.UI.runner.CfmlRunnerParameters.WWW_ROOT;

/**
 * Created by karashevich on 25/01/16.
 */
public class CfmlRunConfigurationProducer extends RunConfigurationProducer<CfmlRunConfiguration>{

  public CfmlRunConfigurationProducer(){
    super(CfmlRunConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(CfmlRunConfiguration configuration,
                                                  ConfigurationContext context,
                                                  Ref<PsiElement> sourceElement) {

    final Location location = context.getLocation();
    if (!(location instanceof PsiLocation)) return false;

    final VirtualFile file;
    PsiElement element = location.getPsiElement();
    final PsiFile containingFile = element.getContainingFile();
    if (isValid(containingFile)) {
      file = containingFile.getVirtualFile();
      sourceElement.set(containingFile);
    }
    else {
      return false;
    }

    if (!FileTypeManager.getInstance().isFileOfType(file, ScratchFileType.INSTANCE)) {
      final VirtualFile root = ProjectRootManager.getInstance(element.getProject()).getFileIndex().getContentRootForFile(file);
      if (root == null) return false;
    }


    CfmlRunnerParameters params = configuration.getRunnerParameters();
    String serverUrl = configuration.getRunnerParameters().getUrl();
    if (serverUrl.equals("")) {
      CfmlRunConfiguration templateConfiguration = CfmlRunConfigurationType.getInstance().getTemplateConfiguration();
      if (templateConfiguration != null)
        serverUrl = templateConfiguration.getRunnerParameters().getUrl();
    }
    params.setUrl(serverUrl);
    params.setPageUrl(buildPageUrl(context, file));

    configuration.setName(generateName(containingFile));
    return true;
  }

  @NotNull
  private String generateName(PsiFile containingFile) {
    return StringUtil.isNotEmpty(containingFile.getVirtualFile().getPath()) ? PathUtil.getFileName(containingFile.getVirtualFile().getPath()) : "";
  }

  @NotNull
  private String buildPageUrl(ConfigurationContext context, VirtualFile file ){
    String result;
    String absolutePageUrl = file.getUrl();
    int wwwrootIndex = absolutePageUrl.indexOf(WWW_ROOT);
    if (wwwrootIndex == -1) {
      result = context.getProject().getBaseDir().getName() + "/" +
               FileUtil.getRelativePath(context.getProject().getBaseDir().getPath(), file.getPath(), '/');
    } else {
      String relativePageUrl = absolutePageUrl.substring(wwwrootIndex + WWW_ROOT.length());
      result = relativePageUrl;
    }
    return result;
  }

  @Override
  public boolean isConfigurationFromContext(CfmlRunConfiguration configuration, ConfigurationContext context) {
    final Location location = context.getLocation();
    if (location == null) return false;

    final PsiElement anchor = location.getPsiElement();
    final PsiFile containingFile = anchor.getContainingFile();
    if (isValid(containingFile)) {

      final String path;
      path = buildPageUrl(context, containingFile.getVirtualFile());
      return equalsUrlPaths(path, configuration.getRunnerParameters().getPageUrl());
    }
    return false;
  }

  private static boolean isValid(@Nullable PsiFile containingFile) {
    return containingFile != null && containingFile.getFileType() == CfmlFileType.INSTANCE && containingFile.getVirtualFile() != null;
  }


  /**
   *
   * @return a new URL to file on server. It based on {server path} + {relative path to file in project}
   * @throws MalformedURLException
   */
  @NotNull
  private static String buildNewUrl(CfmlRunConfiguration configuration, ConfigurationContext context, PsiFile containingFile) throws MalformedURLException {
    return configuration.getRunnerParameters().getUrl();
  }

  /**
   * Method cuts from URL query part and path. For example: cutUrl("http://localhost:8500/CFTest/index.cfm") returns "http://localhost:8500"
   *
   * @param stringUrl contains full URL with protocol, host, port, possible path and query
   * @return URL without path and query
   * @throws MalformedURLException
   */
  private static String cutUrl(String stringUrl) throws MalformedURLException {
    URL url = new URL(stringUrl);
    return url.getProtocol() +"://" + url.getAuthority();
  }

  /**
   * compares two generalized (with replaced "localhost") URLs without queries
   *
   * @param strUrl1 - first URL built for file
   * @param strUrl2 - second URL from server preferences. Order is not important
   * @return true if URLs without queries are equaled.
   */
  private static boolean equalsUrlPaths(String strUrl1, String strUrl2){
    try {
      URL url1  = new URL(generalizeLocalhost(strUrl1));
      URL url2  = new URL(generalizeLocalhost(strUrl2));

      return StringUtil.equals(url1.getProtocol() + "://" + url1.getAuthority() + url1.getPath(),
                               url2.getProtocol() + "://" + url2.getAuthority() + url2.getPath());
    }
    catch (MalformedURLException e) {
      return strUrl1.equals(strUrl2);
    }

  }

  /**
   * if URL contains "localhost" it is replaced with 127.0.0.1
   *
   * @param oldUrl may contain "localhost"
   * @return URL without "localhost"
   */
  private static String generalizeLocalhost(String oldUrl){
    String localhostConst = "//localhost";
    if (oldUrl.contains(localhostConst)) {
      int index = oldUrl.indexOf(localhostConst);
      return (oldUrl.substring(0, index) + "//127.0.0.1" + oldUrl.substring(index + localhostConst.length()));
    }
    return oldUrl;
  }
}
