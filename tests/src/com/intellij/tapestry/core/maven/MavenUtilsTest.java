package com.intellij.tapestry.core.maven;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class MavenUtilsTest {

    @Test
    public void constructor() {
        new MavenUtils();
    }

    @Test
    public void createMavenSupport_check_dependencies() throws IOException, XpathException, SAXException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(true, false, null, null, null, "group", "artifact", "1.1", null);

        MavenUtils.createMavenSupport("target", mavenConfiguration, "5");
        String pom = getResultXml();

        XMLAssert.assertXpathExists("/project/dependencies/dependency/groupId[text()='org.apache.tapestry']", pom);
        XMLAssert.assertXpathExists("/project/dependencies/dependency/artifactId[text()='tapestry-core']", pom);
        XMLAssert.assertXpathExists("/project/dependencies/dependency/version[text()='5']", pom);
    }

    @Test
    public void createMavenSupport_with_remote_repositories() throws IOException, XpathException, SAXException {
        List<RemoteRepositoryDescription> repositories = new ArrayList<>();
        repositories.add(new RemoteRepositoryDescription("url1", "id1", "name1", true, true));

        MavenConfiguration mavenConfiguration = new MavenConfiguration(true, true, null, null, null, "group", "artifact", "1.1", repositories);

        MavenUtils.createMavenSupport("target", mavenConfiguration, "5");
        String pom = getResultXml();

        XMLAssert.assertXpathExists("/project/repositories/repository/id[text()='id1']", pom);
        XMLAssert.assertXpathExists("/project/repositories/repository/name[text()='name1']", pom);
        XMLAssert.assertXpathExists("/project/repositories/repository/url[text()='url1']", pom);
        XMLAssert.assertXpathExists("/project/repositories/repository/releases", pom);
        XMLAssert.assertXpathExists("/project/repositories/repository/snapshots", pom);

        repositories.clear();
        repositories.add(new RemoteRepositoryDescription("url2", "id2", "name2", false, false));
        MavenUtils.createMavenSupport("target", mavenConfiguration, "5");
        pom = getResultXml();

        XMLAssert.assertXpathExists("/project/repositories/repository/id[text()='id2']", pom);
        XMLAssert.assertXpathExists("/project/repositories/repository/name[text()='name2']", pom);
        XMLAssert.assertXpathExists("/project/repositories/repository/url[text()='url2']", pom);
    }

    @Test
    public void createMavenSupport_with_parent_pom() throws IOException, XpathException, SAXException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(true, false, "parentGroup", "parentArtifact", "1.0", "group", "artifact", "1.1", null);

        MavenUtils.createMavenSupport("target", mavenConfiguration, "5");
        String pom = getResultXml();

        XMLAssert.assertXpathExists("/project/parent/groupId[text()='parentGroup']", pom);
        XMLAssert.assertXpathExists("/project/parent/artifactId[text()='parentArtifact']", pom);
        XMLAssert.assertXpathExists("/project/parent/version[text()='1.0']", pom);
    }

    @Test
    public void createMavenSupport_default_version() throws IOException, XpathException, SAXException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", null, null);
        MavenUtils.createMavenSupport("target", mavenConfiguration, "5");
        String pom = getResultXml();
        XMLAssert.assertXpathExists("/project/version[text()='1.0-SNAPSHOT']", pom);

        mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", "", null);
        MavenUtils.createMavenSupport("target", mavenConfiguration, "5");
        pom = getResultXml();
        XMLAssert.assertXpathExists("/project/version[text()='1.0-SNAPSHOT']", pom);

        mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", "1.0", null);
        MavenUtils.createMavenSupport("target", mavenConfiguration, "5");
        pom = getResultXml();
        XMLAssert.assertXpathExists("/project/version[text()='1.0']", pom);
    }

    @Test
    public void createMavenSupport_valid_header() throws IOException, XpathException, SAXException {
        MavenConfiguration mavenConfiguration = new MavenConfiguration(false, false, null, null, null, "group", "artifact", null, null);

        MavenUtils.createMavenSupport("target", mavenConfiguration, "5");
        String pom = getResultXml();

        XMLAssert.assertXpathExists("/project/modelVersion[text()='4.0.0']", pom);
    }

    private String getResultXml() {
        try {
            InputStream stream = new FileInputStream("target/pom.xml");
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);

            return new String(bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
    }
}
