package com.intellij.tapestry.core.util;

import com.intellij.tapestry.core.TapestryConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public final class WebDescriptorUtils {

    public final static String APPLICATION_PACKAGE_PARAMETER_NAME = "tapestry.app-package";
    private final static String CONTEXT_PARAM_NAME = "param-name";
    private final static String CONTEXT_PARAM_VALUE = "param-value";

    /**
     * Finds the Tapestry filter name.
     *
     * @param document the document to parse.
     * @return the defined Tapestry filter name, {@code null} if it's not found.
     */
    public static String getTapestryFilterName(Document document) {
        NodeList nodeList = document.getElementsByTagName("filter-class");
        for (int i = 0; i < nodeList.getLength(); i++)
            if (nodeList.item(i).getTextContent().equals(TapestryConstants.FILTER_CLASS)) {
                NodeList paramNodeList = nodeList.item(i).getParentNode().getChildNodes();
                for (int j = 0; j < paramNodeList.getLength(); j++)
                    if (paramNodeList.item(j).getNodeName().equals("filter-name")) {
                        return paramNodeList.item(j).getTextContent();
                    }
            }

        return null;
    }

    /**
     * Finds the defined application root package.
     *
     * @param document the document to parse.
     * @return the defined application root package, {@code null} if it's not found.
     */
    public static String getApplicationPackage(Document document) {
        return getContextParam(document, APPLICATION_PACKAGE_PARAMETER_NAME);
    }

    /**
     * Finds the value of a defined context parameter.
     *
     * @param document  the documento to parse.
     * @param paramName the context parameter name.
     * @return the context parameter name value, {@code null} if the parameter isn't found.
     */
    private static String getContextParam(@Nullable Document document, @NotNull String paramName) {
        if (document == null) return null;

        NodeList nodeList = document.getElementsByTagName(CONTEXT_PARAM_NAME);

        for (int i = 0; i < nodeList.getLength(); i++)
            if (paramName.equals(nodeList.item(i).getTextContent())) {
                NodeList paramNodeList = nodeList.item(i).getParentNode().getChildNodes();
                for (int j = 0; j < paramNodeList.getLength(); j++)
                    if (paramNodeList.item(j).getNodeName().equals(CONTEXT_PARAM_VALUE)) {
                        return paramNodeList.item(j).getTextContent();
                    }
            }

        return null;
    }
}
