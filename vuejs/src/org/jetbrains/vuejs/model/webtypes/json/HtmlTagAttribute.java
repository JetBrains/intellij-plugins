
package org.jetbrains.vuejs.model.webtypes.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "description",
    "doc-url",
    "default",
    "required",
    "value",
    "type"
})
public class HtmlTagAttribute implements DocumentedItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
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
    @JsonProperty("default")
    private String _default;
    @JsonProperty("required")
    private Boolean required;
    @JsonProperty("value")
    private Object value;
    /**
     * Deprecated. Use 'value' property instead. Specify only if type is 'boolean' for compatibility with WebStorm 2019.2.
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Deprecated. Use 'value' property instead. Specify only if type is 'boolean' for compatibility with WebStorm 2019.2.")
    private Object type;

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

    @JsonProperty("default")
    public String getDefault() {
        return _default;
    }

    @JsonProperty("default")
    public void setDefault(String _default) {
        this._default = _default;
    }

    @JsonProperty("required")
    public Boolean getRequired() {
        return required;
    }

    @JsonProperty("required")
    public void setRequired(Boolean required) {
        this.required = required;
    }

    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Deprecated. Use 'value' property instead. Specify only if type is 'boolean' for compatibility with WebStorm 2019.2.
     * 
     */
    @JsonProperty("type")
    public Object getType() {
        return type;
    }

    /**
     * Deprecated. Use 'value' property instead. Specify only if type is 'boolean' for compatibility with WebStorm 2019.2.
     * 
     */
    @JsonProperty("type")
    public void setType(Object type) {
        this.type = type;
    }

}
