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
  @Override
  protected @NotNull String getDisplayName() {
    return AngularJSBundle.message("angularjs.new.project.name");
  }

  @Override
  public @NotNull String getGithubUserName() {
    return "angular";
  }

  @Override
  public @NotNull String getGithubRepositoryName() {
    return "angular-seed";
  }

  @Override
  public @Nullable String getDescription() {
    return AngularJSBundle.message("angularjs.new.project.description");
  }

  @Override
  public Icon getIcon() {
    return AngularJSIcons.AngularJS;
  }

  @Override
  public @Nullable String getPrimaryZipArchiveUrlForDownload(@NotNull GithubTagInfo tag) {
    return null;
  }
}
