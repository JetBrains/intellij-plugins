package com.intellij.tapestry.core.resource;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class TestableResource implements IResource {

    private final String _name;
    private final String _fileName;

    public TestableResource(String name, String fileName) {
        _name = name;
        _fileName = fileName;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public File getFile() {
        try {
            URL url = TestableResource.class.getResource("/web/" + _fileName);
            return url != null ? new File(url.toURI()) : null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getExtension() {
        return _fileName.substring(_fileName.lastIndexOf('.') + 1, _fileName.length() - 1);
    }

    @Override
    public void accept(CoreXmlRecursiveElementVisitor visitor) {
    }
}
