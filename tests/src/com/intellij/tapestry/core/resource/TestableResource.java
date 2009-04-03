package com.intellij.tapestry.core.resource;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class TestableResource implements IResource {

    private String _name;
    private String _fileName;

    public TestableResource(String name, String fileName) {
        _name = name;
        _fileName = fileName;
    }

    public String getName() {
        return _name;
    }

    public File getFile() {
        try {
            URL url = TestableResource.class.getResource("/web/" + _fileName);
            return url != null ? new File(url.toURI()) : null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getExtension() {
        return _fileName.substring(_fileName.lastIndexOf('.') + 1, _fileName.length() - 1);
    }

    public void accept(CoreXmlRecursiveElementVisitor visitor) {
    }
}
