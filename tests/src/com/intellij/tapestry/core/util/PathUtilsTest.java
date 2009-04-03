package com.intellij.tapestry.core.util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class PathUtilsTest {

    @BeforeClass
    public void defaultConstructor() {
        new PathUtils();
    }

    @Test
    public void packageIntoPath_empty() throws Exception {
        assert PathUtils.packageIntoPath(null, true).equals("");
        assert PathUtils.packageIntoPath(null, false).equals("");

        assert PathUtils.packageIntoPath("", true).equals("");
        assert PathUtils.packageIntoPath("", false).equals("");
    }

    @Test
    public void packageIntoPath_various() throws Exception {
        assert PathUtils.packageIntoPath("a.b", true).equals("a/b/");
        assert PathUtils.packageIntoPath("a.b", false).equals("a/b");

        assert PathUtils.packageIntoPath("a", true).equals("a/");
        assert PathUtils.packageIntoPath("a", false).equals("a");
    }

    @Test
    public void pathIntoPackage_empty() throws Exception {
        assert PathUtils.pathIntoPackage(null, true).equals("");
        assert PathUtils.pathIntoPackage(null, false).equals("");

        assert PathUtils.pathIntoPackage("", true).equals("");
        assert PathUtils.pathIntoPackage("", false).equals("");
    }

    @Test
    public void pathIntoPackage_various() throws Exception {
        assert PathUtils.pathIntoPackage("a/b", true).equals("a");
        assert PathUtils.pathIntoPackage("a/b", false).equals("a.b");
        assert PathUtils.pathIntoPackage("a/b/", true).equals("a");
        assert PathUtils.pathIntoPackage("a/b/", false).equals("a.b");

        assert PathUtils.pathIntoPackage("a", true).equals("a");
        assert PathUtils.pathIntoPackage("a", false).equals("a");
        assert PathUtils.pathIntoPackage("a/", true).equals("a");
        assert PathUtils.pathIntoPackage("a/", false).equals("a");

        assert PathUtils.pathIntoPackage("a/a.txt", true).equals("a");
        assert PathUtils.pathIntoPackage("a/b/a.txt", true).equals("a.b");
        assert PathUtils.pathIntoPackage("/a/b/a.txt", true).equals("a.b");
    }

    @Test
    public void getFullComponentPackage_empty() throws Exception {
        assert PathUtils.getFullComponentPackage(null, null).equals("");
        assert PathUtils.getFullComponentPackage("", "").equals("");
        assert PathUtils.getFullComponentPackage(null, "").equals("");
        assert PathUtils.getFullComponentPackage("", null).equals("");
    }

    @Test
    public void getFullComponentPackage_various() throws Exception {
        assert PathUtils.getFullComponentPackage("com.myapp.pages", "admin/Login").equals("com.myapp.pages.admin");
        assert PathUtils.getFullComponentPackage("com.myapp.pages", "Login").equals("com.myapp.pages");
    }

    @Test
    public void getLastPathElement_empty() throws Exception {
        assert PathUtils.getLastPathElement(null).equals("");
        assert PathUtils.getLastPathElement("").equals("");
    }

    @Test
    public void getLastPathElement_various() throws Exception {
        assert PathUtils.getLastPathElement("admin/Login").equals("Login");
        assert PathUtils.getLastPathElement("Login").equals("Login");
    }

    @Test
    public void getFirstPathElement_empty() throws Exception {
        assert PathUtils.getFirstPathElement(null).equals("");
        assert PathUtils.getFirstPathElement("").equals("");
    }

    @Test
    public void getFirstPathElement_various() throws Exception {
        assert PathUtils.getFirstPathElement("admin/Login").equals("admin");
        assert PathUtils.getFirstPathElement("/admin/Login").equals("admin");
        assert PathUtils.getFirstPathElement("Login").equals("Login");
    }

    @Test
    public void removeLastPathElement_empty() {
        assert PathUtils.removeLastFilePathElement(null, true).equals("");
        assert PathUtils.removeLastFilePathElement("", true).equals("");
    }

    @Test
    public void removeLastPathElement_various() {
        assert PathUtils.removeLastFilePathElement("admin/Login", true).equals("admin");

        assert PathUtils.removeLastFilePathElement("admin\\Login", true).equals("admin");

        assert PathUtils.removeLastFilePathElement("admin/Login", false).equals("admin");

        assert PathUtils.removeLastFilePathElement("admin\\Login", false).equals("admin");

        assert PathUtils.removeLastFilePathElement("Login", false).equals("Login");

        assert PathUtils.removeLastFilePathElement("Login", true).equals("");
    }

    @Test
    public void getComponentFileName_empty() throws Exception {
        assert PathUtils.getComponentFileName(null).equals("");
        assert PathUtils.getComponentFileName("").equals("");
    }

    @Test
    public void getComponentFileName_various() throws Exception {
        assert PathUtils.getComponentFileName("admin/Login").equals("Login");
        assert PathUtils.getComponentFileName("Login").equals("Login");
    }

    @Test
    public void toUnixPath() {
        assert PathUtils.toUnixPath(null) == null;

        
        assert PathUtils.toUnixPath("/path1/path2").equals("/path1/path2");
    }
}
