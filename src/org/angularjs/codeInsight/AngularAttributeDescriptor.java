package org.angularjs.codeInsight;

import com.intellij.xml.impl.dtd.XmlAttributeDescriptorImpl;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;

/**
 * Created by denofevil on 26/11/13.
 */
public class AngularAttributeDescriptor extends AnyXmlAttributeDescriptor {
    public AngularAttributeDescriptor(String attributeName) {
        super(attributeName);
    }
}
