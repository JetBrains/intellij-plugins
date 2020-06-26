
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
    "type"
})
public class TypedEntity implements DocumentedItem {

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
    /**
     * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.")
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

    /**
     * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
     * 
     */
    @JsonProperty("type")
    public Object getType() {
        return type;
    }

    /**
     * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
     * 
     */
    @JsonProperty("type")
    public void setType(Object type) {
        this.type = type;
    }

}
