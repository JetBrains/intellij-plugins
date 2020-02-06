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
    return AngularJSBundle.message("angularjs.new.project.name");
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
    return AngularJSBundle.message("angularjs.new.project.description");
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
