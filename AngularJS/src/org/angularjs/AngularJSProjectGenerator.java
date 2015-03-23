package org.angularjs;

import com.intellij.lang.javascript.boilerplate.AbstractGithubTagDownloadedProjectGenerator;
import com.intellij.platform.templates.github.GithubTagInfo;
import icons.AngularJSIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSProjectGenerator extends AbstractGithubTagDownloadedProjectGenerator {
  @NotNull
  @Override
  protected String getDisplayName() {
    return "AngularJS";
  }

  @NotNull
  @Override
  public String getGithubUserName() {
    return "angular";
  }

  @NotNull
  @Override
  public String getGithubRepositoryName() {
    return "angular-seed";
  }

  @Nullable
  @Override
  public String getDescription() {
    return "<html>This project is an application skeleton for a typical <a href=\"https://angularjs.org\">AngularJS</a> web app.<br>" +
           "Don't forget to install dependencies by running<pre>npm install</pre></html>";
  }

  @Override
  public Icon getIcon() {
    return AngularJSIcons.AngularJS;
  }

  @Nullable
  @Override
  public String getPrimaryZipArchiveUrlForDownload(@NotNull GithubTagInfo tag) {
    return null;
  }
}
