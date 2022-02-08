package com.intellij.tapestry.core.maven;

import com.intellij.tapestry.core.TapestryConstants;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Maven related utilities.
 */
public final class MavenUtils {

  /**
     * Creates a Maven pom.xml.
     *
     * @param path               to path to the directory where the pom.xml will be created.
     * @param mavenConfiguration all maven configurations.
     * @param tapestryVersion    the selected Tapestry version.
     * @throws IOException if an error occurs creating the pom.xml file.
     */
    public static void createMavenSupport(String path, MavenConfiguration mavenConfiguration, String tapestryVersion) throws IOException {
        File file = new File(path + "/pom.xml");
        MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
        Model model = new Model();

        model.setModelVersion("4.0.0");
        model.setPackaging("war");
        model.setGroupId(mavenConfiguration.getGroupId());
        model.setArtifactId(mavenConfiguration.getArtifactId());

        if (mavenConfiguration.getVersion() != null && mavenConfiguration.getVersion().length() > 0) {
            model.setVersion(mavenConfiguration.getVersion());
        } else {
            model.setVersion("1.0-SNAPSHOT");
        }

        // Add dependencies of tapestry
        Dependency dependency = new Dependency();
        dependency.setGroupId("org.apache.tapestry");
        dependency.setArtifactId("tapestry-core");
        dependency.setVersion(tapestryVersion);

        model.addDependency(dependency);

        // Add resources build configuration
        Build build = new Build();
        Resource resource = new Resource();
        resource.setDirectory("src/main/java");
        resource.addInclude("**/*."+ TapestryConstants.TEMPLATE_FILE_EXTENSION);
        resource.addInclude("**/*.properties");
        build.addResource(resource);

        model.setBuild(build);

        if (mavenConfiguration.isCreateParentPom()) {
            Parent parent = new Parent();
            parent.setArtifactId(mavenConfiguration.getArtifactIdParentPom());
            parent.setGroupId(mavenConfiguration.getGroupIdParentPom());
            parent.setVersion(mavenConfiguration.getVersionParentPom());

            model.setParent(parent);
        }

        if (mavenConfiguration.isAddRemoteRepository()) {
            for (RemoteRepositoryDescription repositoryDescription : mavenConfiguration.getRemoteRepositoryList()) {
                Repository repository = new Repository();
                RepositoryPolicy repositoryPolicyReleases = new RepositoryPolicy();
                repositoryPolicyReleases.setEnabled(true);
                RepositoryPolicy repositoryPolicySnapshots = new RepositoryPolicy();
                repositoryPolicySnapshots.setEnabled(true);

                repository.setName(repositoryDescription.getName());
                repository.setId(repositoryDescription.getId());
                repository.setUrl(repositoryDescription.getUrl());

                repositoryPolicyReleases.setEnabled(repositoryDescription.isCreatingReleases());
                repository.setReleases(repositoryPolicyReleases);

                repositoryPolicySnapshots.setEnabled(repositoryDescription.isCreatingSnapshots());
                repository.setSnapshots(repositoryPolicySnapshots);

                model.addRepository(repository);
            }
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
          mavenXpp3Writer.write(fileWriter, model);
        }
    }
}
