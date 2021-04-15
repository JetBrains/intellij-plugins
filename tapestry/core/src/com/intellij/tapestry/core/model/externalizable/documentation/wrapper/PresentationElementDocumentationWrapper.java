package com.intellij.tapestry.core.model.externalizable.documentation.wrapper;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps the XML parsing logic.
 */
public class PresentationElementDocumentationWrapper {

    private Document _document;
    private String _description;
    private final Map<String, String> _parameterDescriptions = new HashMap<>();
    private String _examples;
    private String _notes;

    public PresentationElementDocumentationWrapper() {
    }

    public PresentationElementDocumentationWrapper(URL url) throws Exception {
        _document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(url.openStream());
    }

    /**
     * Return the element description.
     *
     * @return the element description. Never returns {@code null}
     */
    public String getDescription() {
        if (_description != null)
            return _description;

        _description = "";

        if (_document == null)
            return _description;

        NodeList descriptions = _document.getDocumentElement().getElementsByTagName("description");
        if (descriptions.getLength() > 0)
            _description = descriptions.item(0).getTextContent();

        return _description;
    }

    /**
     * Return a parameter description.
     *
     * @param name the name of the parameter to look for.
     * @return the parameter description. Never returns {@code null}
     */
    public String getParameterDescription(String name) {
        if (_parameterDescriptions.containsKey(name))
            return _parameterDescriptions.get(name);

        if (_document == null)
            return "";

        NodeList parameters = _document.getDocumentElement().getElementsByTagName("parameter");
        for (int i = 0; i < parameters.getLength(); i++) {
            Node parameter = parameters.item(i);

            String parameterName = parameter.getAttributes().getNamedItem("name").getTextContent();
            if (parameterName.equals(name)) {
                _parameterDescriptions.put(name, parameter.getTextContent());

                return parameter.getTextContent();
            }
        }

        return "";
    }

    public String getExamples() {
        if (_examples != null)
            return _examples;

        _examples = "";

        if (_document == null)
            return _examples;

        NodeList descriptions = _document.getDocumentElement().getElementsByTagName("examples");
        if (descriptions.getLength() > 0)
            _examples = descriptions.item(0).getTextContent();

        return _examples;
    }

    public String getNotes() {
        if (_notes != null)
            return _notes;

        _notes = "";

        if (_document == null)
            return _notes;

        NodeList descriptions = _document.getDocumentElement().getElementsByTagName("notes");
        if (descriptions.getLength() > 0)
            _notes = descriptions.item(0).getTextContent();

        return _notes;
    }
}
