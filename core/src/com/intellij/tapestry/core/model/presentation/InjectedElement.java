package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.resource.xml.XmlAttribute;
import com.intellij.tapestry.core.resource.xml.XmlTag;
import com.intellij.tapestry.core.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * An injected element.
 */
public class InjectedElement implements Comparable {

    private transient IJavaField _field;
    private transient PresentationLibraryElement _element;
    private XmlTag _tag;

    public InjectedElement(IJavaField field, PresentationLibraryElement element) {
        _field = field;
        _element = element;
    }

    public InjectedElement(XmlTag tag, PresentationLibraryElement element) {
        _tag = tag;
        _element = element;
    }

    public IJavaField getField() {
        return _field;
    }

    public XmlTag getTag() {
        return _tag;
    }

    public PresentationLibraryElement getElement() {
        return _element;
    }

    /**
     * Finds all the injected element parameters.
     * This parameters can be found from the tag or from the field of injected element.
     *
     * @return the parameters of the injected element.
     */
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        if (_tag != null) {
            for (XmlAttribute attribute : _tag.getAttributes()) {
                parameters.put(attribute.getLocalName(), attribute.getValue());
            }
        } else if (_field != null) {
            IJavaAnnotation componentAnnotation = _field.getAnnotations().get(TapestryConstants.COMPONENT_ANNOTATION);

            if (componentAnnotation != null) {
                String[] componentParameters = componentAnnotation.getParameters().get("parameters");
                if (componentParameters != null) {
                    for (String parameter : componentParameters) {
                        if ((parameter.split("=").length) == 2)
                            parameters.put(parameter.split("=")[0], parameter.split("=")[1]);
                    }
                }
            }
        }
        return parameters;
    }

    /**
     * Finds the injected element id.
     * This can be taken either from the field name itself or from an annotated value.
     *
     * @return the injected element id.
     */
    public String getElementId() {
        if (_element == null || (_field == null && _tag == null)) {
            return null;
        }

        if (_field != null) {
            if (_element instanceof Component)
                return getFieldId();
        }

        if (_tag != null) {

            if (_element instanceof Component && getParameters() != null && getParameters().containsKey("id"))
                return getParameters().get("id");

            if (!(_tag.getLocalName().toUpperCase().equals(_element.getName().toUpperCase())))
                return getElement().getName();

            return _tag.getLocalName();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        return getElementId().compareTo(((InjectedElement) o).getElementId());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        return !(obj == null || !(obj instanceof InjectedElement)) &&
                getElementId().equals(((InjectedElement) obj).getElementId()) &&
                getElement().getName().equals(((InjectedElement) obj).getElement().getName());
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return getElementId().hashCode();
    }

    private String getFieldId() {
        IJavaAnnotation componentAnnotation = _field.getAnnotations().get(TapestryConstants.COMPONENT_ANNOTATION);

        if (componentAnnotation != null) {
            String[] values = componentAnnotation.getParameters().get("id");
            if (values != null && values.length > 0)
                return values[0];
            else
                return ClassUtils.getName(_field.getName());
        } else
            return ClassUtils.getName(_field.getName());
    }
}
