package com.intellij.tapestry.core.maven;

import com.intellij.openapi.util.io.FileUtil;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;
import org.xmlunit.matchers.HasXPathMatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class MavenUtilsTest {
    private static final Map<String, String> NAMESPACE_CONTEXT = Collections.singletonMap("ns", "http://maven.apache.org/POM/4.0.0");

    @Test
    public void constructor() {
        new MavenUtils();
    }

    @Test
    public void createMavenSupport_check_dependencies() throws IOException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(true, false, null, null, null, "group", "artifact", "1.1", null);

        String pom = generatePomXmlText(mavenConfiguration);
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:dependencies/ns:dependency/ns:groupId[text()='org.apache.tapestry']"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:dependencies/ns:dependency/ns:artifactId[text()='tapestry-core']"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:dependencies/ns:dependency/ns:version[text()='5']"));
    }

    @NotNull
    private static HasXPathMatcher hasXPathWithNs(@NotNull String xPath) {
        return HasXPathMatcher.hasXPath(xPath).withNamespaceContext(NAMESPACE_CONTEXT);
    }

    @Test
    public void createMavenSupport_with_remote_repositories() throws IOException {
        List<RemoteRepositoryDescription> repositories = new ArrayList<>();
        repositories.add(new RemoteRepositoryDescription("url1", "id1", "name1", true, true));

        MavenConfiguration mavenConfiguration = new MavenConfiguration(true, true, null, null, null, "group", "artifact", "1.1", repositories);

        String pom = generatePomXmlText(mavenConfiguration);

        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:repositories/ns:repository/ns:id[text()='id1']"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:repositories/ns:repository/ns:name[text()='name1']"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:repositories/ns:repository/ns:url[text()='url1']"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:repositories/ns:repository/ns:releases"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:repositories/ns:repository/ns:snapshots"));

        repositories.clear();
        repositories.add(new RemoteRepositoryDescription("url2", "id2", "name2", false, false));
        pom = generatePomXmlText(mavenConfiguration);

        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:repositories/ns:repository/ns:id[text()='id2']"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:repositories/ns:repository/ns:name[text()='name2']"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:repositories/ns:repository/ns:url[text()='url2']"));
    }

    @Test
    public void createMavenSupport_with_parent_pom() throws IOException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(true, false, "parentGroup", "parentArtifact", "1.0", "group", "artifact", "1.1", null);

        String pom = generatePomXmlText(mavenConfiguration);

        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:parent/ns:groupId[text()='parentGroup']"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:parent/ns:artifactId[text()='parentArtifact']"));
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:parent/ns:version[text()='1.0']"));
    }

    @Test
    public void createMavenSupport_default_version() throws IOException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", null, null);
        String pom = generatePomXmlText(mavenConfiguration);
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:version[text()='1.0-SNAPSHOT']"));

        mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", "", null);
        MatcherAssert.assertThat(generatePomXmlText(mavenConfiguration), hasXPathWithNs("/ns:project/ns:version[text()='1.0-SNAPSHOT']"));

        mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", "1.0", null);
        MatcherAssert.assertThat(generatePomXmlText(mavenConfiguration), hasXPathWithNs("/ns:project/ns:version[text()='1.0']"));
    }

    @Test
    public void createMavenSupport_valid_header() throws IOException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", null, null);

        String pom = generatePomXmlText(mavenConfiguration);
        MatcherAssert.assertThat(pom, hasXPathWithNs("/ns:project/ns:modelVersion[text()='4.0.0']"));
    }

    @NotNull
    private static String generatePomXmlText(MavenConfiguration mavenConfiguration) throws IOException {
        File targetDirectory = FileUtil.createTempDirectory("MavenUtilsTest", null);
        MavenUtils.createMavenSupport(targetDirectory.getAbsolutePath(), mavenConfiguration, "5");
        return FileUtil.loadFile(new File(targetDirectory, "pom.xml"));
    }
}
