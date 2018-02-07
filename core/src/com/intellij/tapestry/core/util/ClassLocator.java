package com.intellij.tapestry.core.util;

import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassLocator {

    private final String[] _packageNames;
    private final ClassLoader _classLoader;
    private String _packageName;
    private final List<ClassLocation> _classLocations = new LinkedList<>();

    public ClassLocator(String... packageNames) throws ClassNotFoundException {
        this(Thread.currentThread().getContextClassLoader(), packageNames);
    }

    public ClassLocator(ClassLoader classLoader, String... packageNames) throws ClassNotFoundException {
        _classLoader = classLoader;
        _packageNames = packageNames;

        if (classLoader == null)
            throw new ClassNotFoundException("Can't get class loader.");
    }

    public List<ClassLocation> getAllClassLocations() throws ClassNotFoundException, IOException {
        synchronized (this) {
            _classLocations.clear();

            for (String packageName : _packageNames) {
                _packageName = packageName;

                String path = packageName.replace('.', '/');
                Enumeration<URL> resources = _classLoader.getResources(path);
                if (resources == null) {
                    throw new ClassNotFoundException("No resource for " + path);
                }

                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    if (resource.getProtocol().equalsIgnoreCase("FILE")) {
                        loadDirectory(resource);
                    } else if (resource.getProtocol().equalsIgnoreCase("JAR")) {
                        loadJar(resource);
                    } else {
                        throw new ClassNotFoundException("Unknown protocol on class resource: " + resource.toExternalForm());
                    }
                }
            }


            return _classLocations;
        }
    }

    private void loadJar(URL resource) throws IOException {
        JarURLConnection conn = (JarURLConnection) resource.openConnection();
        JarFile jarFile = conn.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        String packagePath = _packageName.replace('.', '/');

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if ((entry.getName().startsWith(packagePath)) && !entry.getName().endsWith("/")) {
                URL url = new URL("jar:" + new URL("file", null, slashify(jarFile.getName())).toExternalForm() + "!/" + entry.getName());

                String className = entry.getName();
                className = StringUtil.trimStart(className, "/");

                className = PathUtils.toUnixPath(className);
                className = PathUtils.getLastPathElement(className);

                className = className.substring(0, className.lastIndexOf('.'));

                ClassLocation classLocation = new ClassLocation(_classLoader, className, url);
                addClassLocation(classLocation);
            }
        }
    }

    private void loadDirectory(URL resource) throws IOException {
        loadDirectory(_packageName, resource.getFile());

    }

    private void loadDirectory(String packageName, String fullPath) throws IOException {
        File directory = new File(fullPath);
        if (!directory.isDirectory())
            throw new IOException("Invalid directory " + directory.getAbsolutePath());

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory())
                loadDirectory(packageName + '.' + file.getName(), file.getAbsolutePath());
            else {
                String simpleName = file.getName();
                simpleName = simpleName.substring(0, simpleName.lastIndexOf('.'));

                ClassLocation location = new ClassLocation(_classLoader, simpleName, new URL("file", null, file.getAbsolutePath()));
                addClassLocation(location);
            }
        }
    }


    private void addClassLocation(ClassLocation classLocation) throws IOException {
        if (_classLocations.contains(classLocation)) {
            throw new IOException("Duplicate location found for: " + classLocation.getClassName());
        }

        _classLocations.add(classLocation);
    }

    private static String slashify(String string) {
        if (string.endsWith("/")) {
            return string;
        } else {
            return string + "/";
        }
    }

    public static class ClassLocation {

        private String className;

        private URL url;

        private ClassLoader classLoader;

        public ClassLocation(ClassLoader classLoader, String className, URL url) {
            this.className = className;
            this.url = url;
            this.classLoader = classLoader;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public URL getUrl() {
            return url;
        }

        public void setUrl(URL url) {
            this.url = url;
        }


        @Override
        public int hashCode() {
            return (className == null) ? 0 : className.hashCode();
        }

        public boolean equals(ClassLocation classLocation) {
            if (classLocation == null)
                return false;

            return (className.equals(classLocation.className));
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ClassLocation)
                return equals((ClassLocation) o);
            else
                return false;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public void setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }
    }
}
