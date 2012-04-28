/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.angularjs;

import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import com.intellij.xml.util.HtmlUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eugene.Kudelevsky
 */
public class AngularJSCustomAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
    @Override
    public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag tag) {
        if (tag == null) {
            return XmlAttributeDescriptor.EMPTY;
        }
        final List<XmlAttributeDescriptor> result = new ArrayList<XmlAttributeDescriptor>();
        //todo: refactor to loading a file
        //todo: explore making the loaded file a DTD/schema that would also make these "valid" attributes for inspections
        result.add(new AnyXmlAttributeDescriptor("ng-app"));
        result.add(new AnyXmlAttributeDescriptor("ng-bind"));
        result.add(new AnyXmlAttributeDescriptor("ng-bind-html-unsafe"));
        result.add(new AnyXmlAttributeDescriptor("ng-bind-template"));
        result.add(new AnyXmlAttributeDescriptor("ng-class"));
        result.add(new AnyXmlAttributeDescriptor("ng-class-even"));
        result.add(new AnyXmlAttributeDescriptor("ng-class-odd"));
        result.add(new AnyXmlAttributeDescriptor("ng-cloak"));
        result.add(new AnyXmlAttributeDescriptor("ng-controller"));
        result.add(new AnyXmlAttributeDescriptor("ng-form"));
        result.add(new AnyXmlAttributeDescriptor("ng-hide"));
        result.add(new AnyXmlAttributeDescriptor("ng-include"));
        result.add(new AnyXmlAttributeDescriptor("ng-init"));
        result.add(new AnyXmlAttributeDescriptor("ng-non-bindable"));
        result.add(new AnyXmlAttributeDescriptor("ng-pluralize"));
        result.add(new AnyXmlAttributeDescriptor("ng-repeat"));
        result.add(new AnyXmlAttributeDescriptor("ng-show"));
        result.add(new AnyXmlAttributeDescriptor("ng-submit"));
        result.add(new AnyXmlAttributeDescriptor("ng-style"));
        result.add(new AnyXmlAttributeDescriptor("ng-switch"));
        result.add(new AnyXmlAttributeDescriptor("ng-switch-when"));
        result.add(new AnyXmlAttributeDescriptor("ng-switch-default"));
        result.add(new AnyXmlAttributeDescriptor("ng-options"));
        result.add(new AnyXmlAttributeDescriptor("ng-view"));
        result.add(new AnyXmlAttributeDescriptor("ng-transclude"));
        result.add(new AnyXmlAttributeDescriptor("ng-model"));
        result.add(new AnyXmlAttributeDescriptor("ng-list"));
        result.add(new AnyXmlAttributeDescriptor("ng-change"));
        result.add(new AnyXmlAttributeDescriptor("ng-value"));
        result.add(new AnyXmlAttributeDescriptor("ng-required"));
        result.add(new AnyXmlAttributeDescriptor("required"));
        return result.toArray(new XmlAttributeDescriptor[result.size()]);
    }

    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, XmlTag context) {
        if (context != null) {
            return new AnyXmlAttributeDescriptor(attributeName);
        }
        return null;
    }

}
