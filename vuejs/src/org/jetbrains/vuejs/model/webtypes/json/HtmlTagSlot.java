
package org.jetbrains.vuejs.model.webtypes.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "pattern",
    "description",
    "doc-url",
    "vue-properties"
})
public class HtmlTagSlot implements DocumentedItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
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
     * Specify properties of the slot scope
     * 
     */
    @JsonProperty("vue-properties")
    @JsonPropertyDescription("Specify properties of the slot scope")
    private List<TypedEntity> vueProperties = new ArrayList<TypedEntity>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

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
     * Specify properties of the slot scope
     * 
     */
    @JsonProperty("vue-properties")
    public List<TypedEntity> getVueProperties() {
        return vueProperties;
    }

    /**
     * Specify properties of the slot scope
     * 
     */
    @JsonProperty("vue-properties")
    public void setVueProperties(List<TypedEntity> vueProperties) {
        this.vueProperties = vueProperties;
    }

}
