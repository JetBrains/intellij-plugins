
package org.jetbrains.vuejs.model.webtypes.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Provide information about directive argument
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pattern",
    "description",
    "doc-url",
    "required"
})
public class HtmlAttributeVueArgument implements DocumentedItem {

    /**
     * A RegEx pattern to match whole content. Syntax should work with at least ECMA, Java and Python implementations.
     * 
     */
    @JsonProperty("pattern")
    @JsonPropertyDescription("A RegEx pattern to match whole content. Syntax should work with at least ECMA, Java and Python implementations.")
    private Object pattern;
    /**
     * Short description to be rendered in documentation popup. It will be rendered according to description-markup setting.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Short description to be rendered in documentation popup. It will be rendered according to description-markup setting.")
    private String description;
    /**
     * Link to online documentation.
     * 
     */
    @JsonProperty("doc-url")
    @JsonPropertyDescription("Link to online documentation.")
    private String docUrl;
    /**
     * Whether directive requires an argument
     * 
     */
    @JsonProperty("required")
    @JsonPropertyDescription("Whether directive requires an argument")
    private Boolean required = false;

    /**
     * A RegEx pattern to match whole content. Syntax should work with at least ECMA, Java and Python implementations.
     * 
     */
    @JsonProperty("pattern")
    public Object getPattern() {
        return pattern;
    }

    /**
     * A RegEx pattern to match whole content. Syntax should work with at least ECMA, Java and Python implementations.
     * 
     */
    @JsonProperty("pattern")
    public void setPattern(Object pattern) {
        this.pattern = pattern;
    }

    /**
     * Short description to be rendered in documentation popup. It will be rendered according to description-markup setting.
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * Short description to be rendered in documentation popup. It will be rendered according to description-markup setting.
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Link to online documentation.
     * 
     */
    @JsonProperty("doc-url")
    public String getDocUrl() {
        return docUrl;
    }

    /**
     * Link to online documentation.
     * 
     */
    @JsonProperty("doc-url")
    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    /**
     * Whether directive requires an argument
     * 
     */
    @JsonProperty("required")
    public Boolean getRequired() {
        return required;
    }

    /**
     * Whether directive requires an argument
     * 
     */
    @JsonProperty("required")
    public void setRequired(Boolean required) {
        this.required = required;
    }

}
