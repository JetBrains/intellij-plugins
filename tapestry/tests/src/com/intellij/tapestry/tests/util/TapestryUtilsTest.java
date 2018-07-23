package com.intellij.tapestry.tests.util;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.util.ComponentUtils;
import com.intellij.tapestry.intellij.core.resource.xml.IntellijXmlTag;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import static org.easymock.EasyMock.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TapestryUtilsTest extends BaseTestCase {

    private Module _moduleMock;

    @BeforeClass
    public void defaultConstructor() {
        new TapestryUtils();
    }

    @BeforeMethod
    public void initMocks() {
        _moduleMock = createMock(Module.class);
    }

    @AfterMethod
    public void releaseMocks() {
        _moduleMock = null;
    }

    @Test(dataProvider = EMPTY_FIXTURE_PROVIDER)
    public void isComponentTag_is_tag(IdeaProjectTestFixture fixture) {
        XmlTag tapestryTagMock = createMock(XmlTag.class);
        expect(tapestryTagMock.getNamespace()).andReturn(TapestryConstants.TEMPLATE_NAMESPACE);
        replay(tapestryTagMock);

        assert ComponentUtils._isComponentTag(new IntellijXmlTag(tapestryTagMock));


        reset(tapestryTagMock);
        XmlAttribute attributeMock = createMock(XmlAttribute.class);
        expect(attributeMock.getLocalName()).andReturn("att1");
        expect(attributeMock.getNamespace()).andReturn(TapestryConstants.TEMPLATE_NAMESPACE);

        expect(tapestryTagMock.getNamespace()).andReturn("");
        expect(tapestryTagMock.getAttributes()).andReturn(new XmlAttribute[]{attributeMock});
        replay(tapestryTagMock, attributeMock);

        assert ComponentUtils._isComponentTag(new IntellijXmlTag(tapestryTagMock));
    }

    @Test(dataProvider = EMPTY_FIXTURE_PROVIDER)
    public void isComponentTag_is_not_tag(IdeaProjectTestFixture fixture) {
        XmlTag tapestryTagMock = createMock(XmlTag.class);

        XmlAttribute attributeMock = createMock(XmlAttribute.class);
        expect(attributeMock.getLocalName()).andReturn("att1");
        expect(attributeMock.getNamespace()).andReturn("");

        expect(tapestryTagMock.getNamespace()).andReturn("");
        expect(tapestryTagMock.getAttributes()).andReturn(new XmlAttribute[]{attributeMock});
        replay(tapestryTagMock, attributeMock);

        assert !ComponentUtils._isComponentTag(new IntellijXmlTag(tapestryTagMock));
    }

    @Test(dataProvider = EMPTY_FIXTURE_PROVIDER)
    public void getComponentIdentifier_not_component_tag(IdeaProjectTestFixture fixture) {
        XmlTag tapestryTagMock = createMock(XmlTag.class);

        XmlAttribute attributeMock = createMock(XmlAttribute.class);
        expect(attributeMock.getLocalName()).andReturn("att1");
        expect(attributeMock.getNamespace()).andReturn("");

        expect(tapestryTagMock.getNamespace()).andReturn("");
        expect(tapestryTagMock.getAttributes()).andReturn(new XmlAttribute[]{attributeMock});
        replay(tapestryTagMock, attributeMock);

        assert TapestryUtils.getComponentIdentifier(tapestryTagMock) == null;
    }
}
