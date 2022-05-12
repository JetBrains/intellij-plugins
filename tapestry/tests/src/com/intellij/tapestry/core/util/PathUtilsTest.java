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
    public void packageIntoPath_empty() {
        assert PathUtils.packageIntoPath(null, true).isEmpty();
        assert PathUtils.packageIntoPath(null, false).isEmpty();

        assert PathUtils.packageIntoPath("", true).isEmpty();
        assert PathUtils.packageIntoPath("", false).isEmpty();
    }

    @Test
    public void packageIntoPath_various() {
        assert PathUtils.packageIntoPath("a.b", true).equals("a/b/");
        assert PathUtils.packageIntoPath("a.b", false).equals("a/b");

        assert PathUtils.packageIntoPath("a", true).equals("a/");
        assert PathUtils.packageIntoPath("a", false).equals("a");
    }

    @Test
    public void pathIntoPackage_empty() {
        assert PathUtils.pathIntoPackage(null, true).isEmpty();
        assert PathUtils.pathIntoPackage(null, false).isEmpty();

        assert PathUtils.pathIntoPackage("", true).isEmpty();
        assert PathUtils.pathIntoPackage("", false).isEmpty();
    }

    @Test
    public void pathIntoPackage_various() {
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
    public void getFullComponentPackage_empty() {
        assert PathUtils.getFullComponentPackage(null, null).isEmpty();
        assert PathUtils.getFullComponentPackage("", "").isEmpty();
        assert PathUtils.getFullComponentPackage(null, "").isEmpty();
        assert PathUtils.getFullComponentPackage("", null).isEmpty();
    }

    @Test
    public void getFullComponentPackage_various() {
        assert PathUtils.getFullComponentPackage("com.myapp.pages", "admin/Login").equals("com.myapp.pages.admin");
        assert PathUtils.getFullComponentPackage("com.myapp.pages", "Login").equals("com.myapp.pages");
    }

    @Test
    public void getLastPathElement_empty() {
        assert PathUtils.getLastPathElement(null).isEmpty();
        assert PathUtils.getLastPathElement("").isEmpty();
    }

    @Test
    public void getLastPathElement_various() {
        assert PathUtils.getLastPathElement("admin/Login").equals("Login");
        assert PathUtils.getLastPathElement("Login").equals("Login");
    }

    @Test
    public void getFirstPathElement_empty() {
        assert PathUtils.getFirstPathElement(null).isEmpty();
        assert PathUtils.getFirstPathElement("").isEmpty();
    }

    @Test
    public void getFirstPathElement_various() {
        assert PathUtils.getFirstPathElement("admin/Login").equals("admin");
        assert PathUtils.getFirstPathElement("/admin/Login").equals("admin");
        assert PathUtils.getFirstPathElement("Login").equals("Login");
    }

    @Test
    public void removeLastPathElement_empty() {
        assert PathUtils.removeLastFilePathElement(null, true).isEmpty();
        assert PathUtils.removeLastFilePathElement("", true).isEmpty();
    }

    @Test
    public void removeLastPathElement_various() {
        assert PathUtils.removeLastFilePathElement("admin/Login", true).equals("admin");

        assert PathUtils.removeLastFilePathElement("admin\\Login", true).equals("admin");

        assert PathUtils.removeLastFilePathElement("admin/Login", false).equals("admin");

        assert PathUtils.removeLastFilePathElement("admin\\Login", false).equals("admin");

        assert PathUtils.removeLastFilePathElement("Login", false).equals("Login");

        assert PathUtils.removeLastFilePathElement("Login", true).isEmpty();
    }

    @Test
    public void getComponentFileName_empty() {
        assert PathUtils.getComponentFileName(null).isEmpty();
        assert PathUtils.getComponentFileName("").isEmpty();
    }

    @Test
    public void getComponentFileName_various() {
        assert PathUtils.getComponentFileName("admin/Login").equals("Login");
        assert PathUtils.getComponentFileName("Login").equals("Login");
    }

    @Test
    public void toUnixPath() {
        assert PathUtils.toUnixPath(null) == null;

        
        assert PathUtils.toUnixPath("/path1/path2").equals("/path1/path2");
    }
}
