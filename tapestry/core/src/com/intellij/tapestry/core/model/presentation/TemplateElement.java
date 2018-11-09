package com.intellij.tapestry.core.model.presentation;

import org.jetbrains.annotations.NotNull;

/**
 * An template element.
 */
public class TemplateElement implements Comparable {

    @NotNull
    private InjectedElement _element;
    private String _template;

    public TemplateElement(@NotNull InjectedElement element, String template) {
        _element = element;
        _template = template;
    }

    public InjectedElement getElement() {
        return _element;
    }

    public void setElement(InjectedElement element) {
        _element = element;
    }

    public String getTemplate() {
        return _template;
    }

    public void setTemplate(String template) {
        _template = template;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object o) {
        return getTemplate().compareTo(((TemplateElement) o).getTemplate());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        return obj instanceof TemplateElement &&
               getElement().equals(((TemplateElement)obj).getElement()) &&
               getTemplate().equals(((TemplateElement)obj).getTemplate());
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return getTemplate().hashCode();
    }
}
