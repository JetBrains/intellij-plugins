package com.intellij.tapestry.core.maven;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.containers.ContainerUtil;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class MavenUtilsTest {
    private NamespaceContext myOldContext;

    @BeforeMethod
    public void setUp() {
        myOldContext = XMLUnit.getXpathNamespaceContext();
        XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(ContainerUtil.stringMap("ns", "http://maven.apache.org/POM/4.0.0")));
    }

    @AfterMethod
    public void tearDown() {
        XMLUnit.setXpathNamespaceContext(myOldContext);
    }

    @Test
    public void constructor() {
        new MavenUtils();
    }

    @Test
    public void createMavenSupport_check_dependencies() throws IOException, XpathException, SAXException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(true, false, null, null, null, "group", "artifact", "1.1", null);

        String pom = generatePomXmlText(mavenConfiguration);
        XMLAssert.assertXpathExists("/ns:project/ns:dependencies/ns:dependency/ns:groupId[text()='org.apache.tapestry']", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:dependencies/ns:dependency/ns:artifactId[text()='tapestry-core']", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:dependencies/ns:dependency/ns:version[text()='5']", pom);
    }

    @Test
    public void createMavenSupport_with_remote_repositories() throws IOException, XpathException, SAXException {
        List<RemoteRepositoryDescription> repositories = new ArrayList<>();
        repositories.add(new RemoteRepositoryDescription("url1", "id1", "name1", true, true));

        MavenConfiguration mavenConfiguration = new MavenConfiguration(true, true, null, null, null, "group", "artifact", "1.1", repositories);

        String pom = generatePomXmlText(mavenConfiguration);

        XMLAssert.assertXpathExists("/ns:project/ns:repositories/ns:repository/ns:id[text()='id1']", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:repositories/ns:repository/ns:name[text()='name1']", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:repositories/ns:repository/ns:url[text()='url1']", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:repositories/ns:repository/ns:releases", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:repositories/ns:repository/ns:snapshots", pom);

        repositories.clear();
        repositories.add(new RemoteRepositoryDescription("url2", "id2", "name2", false, false));
        pom = generatePomXmlText(mavenConfiguration);

        XMLAssert.assertXpathExists("/ns:project/ns:repositories/ns:repository/ns:id[text()='id2']", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:repositories/ns:repository/ns:name[text()='name2']", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:repositories/ns:repository/ns:url[text()='url2']", pom);
    }

    @Test
    public void createMavenSupport_with_parent_pom() throws IOException, XpathException, SAXException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(true, false, "parentGroup", "parentArtifact", "1.0", "group", "artifact", "1.1", null);

        String pom = generatePomXmlText(mavenConfiguration);

        XMLAssert.assertXpathExists("/ns:project/ns:parent/ns:groupId[text()='parentGroup']", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:parent/ns:artifactId[text()='parentArtifact']", pom);
        XMLAssert.assertXpathExists("/ns:project/ns:parent/ns:version[text()='1.0']", pom);
    }

    @Test
    public void createMavenSupport_default_version() throws IOException, XpathException, SAXException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", null, null);
        String pom = generatePomXmlText(mavenConfiguration);
        XMLAssert.assertXpathExists("/ns:project/ns:version[text()='1.0-SNAPSHOT']", pom);

        mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", "", null);
        XMLAssert.assertXpathExists("/ns:project/ns:version[text()='1.0-SNAPSHOT']", generatePomXmlText(mavenConfiguration));

        mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", "1.0", null);
        XMLAssert.assertXpathExists("/ns:project/ns:version[text()='1.0']", generatePomXmlText(mavenConfiguration));
    }

    @Test
    public void createMavenSupport_valid_header() throws IOException, XpathException, SAXException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", null, null);

        String pom = generatePomXmlText(mavenConfiguration);
        XMLAssert.assertXpathExists("/ns:project/ns:modelVersion[text()='4.0.0']", pom);
    }

    @NotNull
    private static String generatePomXmlText(MavenConfiguration mavenConfiguration) throws IOException {
        File targetDirectory = FileUtil.createTempDirectory("MavenUtilsTest", null);
        MavenUtils.createMavenSupport(targetDirectory.getAbsolutePath(), mavenConfiguration, "5");
        return FileUtil.loadFile(new File(targetDirectory, "pom.xml"));
    }
}
