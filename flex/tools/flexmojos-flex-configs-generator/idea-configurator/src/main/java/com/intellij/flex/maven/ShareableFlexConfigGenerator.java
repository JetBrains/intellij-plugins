package com.intellij.flex.maven;

import org.apache.maven.project.MavenProject;

import java.io.IOException;
public class ShareableFlexConfigGenerator extends IdeaConfigurator {
  private static final String SRC_MAIN = "src/main";
  private static final String TARGET_WITH_FRONT_SLASH = "/target";

  private final String localRepositoryBasedir;
  private final int localRepositoryBasedirLength;

  public ShareableFlexConfigGenerator(String localRepositoryBasedir) {
    this.localRepositoryBasedir = localRepositoryBasedir;
    localRepositoryBasedirLength = localRepositoryBasedir.length();
  }

  @Override
  protected String getConfigFilePath(MavenProject project, String classifier) {
    return "build-gant/flex-configs/--" + project.getArtifactId() + ".xml";
  }

  @Override
  protected void writeTag(String indent, String name, String value, String parentName) throws IOException {
    if (name.equals(PATH_ELEMENT) || name.equals("library")) {
      out.append(indent).append("\t<").append(name).append(">");

      if (parentName.equals(FILE_SPECS) || parentName.equals("source-path") || parentName.equals("include-sources")) {
        int sIndex = value.indexOf(SRC_MAIN);
        if (sIndex != -1) {
          out.append("@@source@@").append(value, sIndex + SRC_MAIN.length(), value.length());
        }
        else if ((sIndex = value.indexOf(TARGET_WITH_FRONT_SLASH)) != -1) {
          out.append("@@target@@").append(value, sIndex + TARGET_WITH_FRONT_SLASH.length(), value.length());
        }
        else {
          throw new IllegalArgumentException(value);
        }
      }
      else {
        if (value.startsWith(localRepositoryBasedir)) {
          out.append("@@repo@@").append(value, localRepositoryBasedirLength, value.length());
        }
        else {
          int index = value.lastIndexOf('/');
          if (index == -1) {
            index = value.lastIndexOf('\\');
          }
          out.append("@@target@@").append(value, index, value.length());
        }
      }
      
      out.append("</").append(name).append('>');
    }
    else {
      super.writeTag(indent, name, value, parentName);
    }
  }
}