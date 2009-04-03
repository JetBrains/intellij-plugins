package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.ui.libraries.LibraryInfo;
import static com.intellij.facet.ui.libraries.MavenLibraryUtil.createSubMavenJarInfo;

public enum TapestryVersion {

    TAPESTRY_5_0_11("5.0.11", new LibraryInfo[]{
            createSubMavenJarInfo("org/apache/tapestry", "tapestry-core", "5.0.11", "org.apache.tapestry.corelib.components.Loop"),
            createSubMavenJarInfo("org/apache/tapestry", "tapestry-ioc", "5.0.11", "org.apache.tapestry.ioc.Registry"),
            createSubMavenJarInfo("org/apache/tapestry", "tapestry-annotations", "5.0.11", "org.apache.tapestry.beaneditor.Validate"),
            createSubMavenJarInfo("commons-codec", "commons-codec", "1.3", "org.apache.commons.codec.Decoder"),
            createSubMavenJarInfo("jboss", "javassist", "3.7.ga", "javassist.ClassPath"),
            createSubMavenJarInfo("log4j", "log4j", "1.2.14", "org.apache.log4j.Logger"),
            createSubMavenJarInfo("org/slf4j", "slf4j-api", "1.4.3", "org.slf4j.Logger"),
            createSubMavenJarInfo("org/slf4j", "slf4j-log4j12", "1.4.3", "org.slf4j.impl.Log4jLoggerFactory")
    });

    private final String _name;
    private final LibraryInfo[] _jars;

    private TapestryVersion(String name, LibraryInfo[] jars) {
        _name = name;
        _jars = jars;
    }

    public LibraryInfo[] getJars() {
        return _jars;
    }

    public String toString() {
        return _name;
    }

    public static TapestryVersion fromString(String name) {
        for (TapestryVersion version : TapestryVersion.values())
            if (version.toString().equals(name))
                return version;

        return null;
    }
}
