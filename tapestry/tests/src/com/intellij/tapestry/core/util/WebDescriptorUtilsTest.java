package com.intellij.tapestry.core.util;

import com.intellij.tapestry.core.resource.TestableResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class WebDescriptorUtilsTest {

    private static DocumentBuilder _documentBuilder;
    private Document _document1;
    private Document _document2;
    private Document _document5;
    private Document _document6;

    @BeforeTest
    public void init() {
        if (_documentBuilder == null) {
            try {
                _documentBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                ex.printStackTrace();
            }
        }

        try {
            _document1 = _documentBuilder.parse(new File(TestableResource.class.getResource("/web/web1.xml").toURI()));
            _document2 = _documentBuilder.parse(new File(TestableResource.class.getResource("/web/web2.xml").toURI()));
            _document5 = _documentBuilder.parse(new File(TestableResource.class.getResource("/web/web5.xml").toURI()));
            _document6 = _documentBuilder.parse(new File(TestableResource.class.getResource("/web/web6.xml").toURI()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void constructor() {
        new WebDescriptorUtils();
    }

    @Test
    public void getTapestryFilterName_not_found() {
        assert WebDescriptorUtils.getTapestryFilterName(_document5) == null;

        assert WebDescriptorUtils.getTapestryFilterName(_document6) == null;
    }

    @Test
    public void getTapestryFilterName_found() {
        assert WebDescriptorUtils.getTapestryFilterName(_document1).equals("app");
    }

    @Test
    public void getApplicationPackage_not_found() {
        assert WebDescriptorUtils.getApplicationPackage(_document2) == null;

        assert WebDescriptorUtils.getApplicationPackage(_document6) == null;
    }

    @Test
    public void getApplicationPackage_found() {
        assert WebDescriptorUtils.getApplicationPackage(_document1).equals("org.example.myapp");
    }
}
