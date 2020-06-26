
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
    "aliases",
    "description",
    "doc-url",
    "source",
    "accepts",
    "returns",
    "arguments"
})
public class HtmlVueFilter implements SourceEntity {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * List of symbol aliases
     * 
     */
    @JsonProperty("aliases")
    @JsonPropertyDescription("List of symbol aliases")
    private List<String> aliases = new ArrayList<String>();
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
     * Allows to specify the source of the entity. For Vue.js component this may be for instance a class.
     * 
     */
    @JsonProperty("source")
    @JsonPropertyDescription("Allows to specify the source of the entity. For Vue.js component this may be for instance a class.")
    private Source source;
    /**
     * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
     * 
     */
    @JsonProperty("accepts")
    @JsonPropertyDescription("Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.")
    private Object accepts;
    /**
     * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
     * 
     */
    @JsonProperty("returns")
    @JsonPropertyDescription("Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.")
    private Object returns;
    /**
     * List of arguments accepted by the filter. All arguments are non-optional by default.
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("List of arguments accepted by the filter. All arguments are non-optional by default.")
    private List<HtmlVueFilterArgument> arguments = new ArrayList<HtmlVueFilterArgument>();

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
     * List of symbol aliases
     * 
     */
    @JsonProperty("aliases")
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * List of symbol aliases
     * 
     */
    @JsonProperty("aliases")
    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
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
     * Allows to specify the source of the entity. For Vue.js component this may be for instance a class.
     * 
     */
    @JsonProperty("source")
    public Source getSource() {
        return source;
    }

    /**
     * Allows to specify the source of the entity. For Vue.js component this may be for instance a class.
     * 
     */
    @JsonProperty("source")
    public void setSource(Source source) {
        this.source = source;
    }

    /**
     * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
     * 
     */
    @JsonProperty("accepts")
    public Object getAccepts() {
        return accepts;
    }

    /**
     * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
     * 
     */
    @JsonProperty("accepts")
    public void setAccepts(Object accepts) {
        this.accepts = accepts;
    }

    /**
     * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
     * 
     */
    @JsonProperty("returns")
    public Object getReturns() {
        return returns;
    }

    /**
     * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
     * 
     */
    @JsonProperty("returns")
    public void setReturns(Object returns) {
        this.returns = returns;
    }

    /**
     * List of arguments accepted by the filter. All arguments are non-optional by default.
     * 
     */
    @JsonProperty("arguments")
    public List<HtmlVueFilterArgument> getArguments() {
        return arguments;
    }

    /**
     * List of arguments accepted by the filter. All arguments are non-optional by default.
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(List<HtmlVueFilterArgument> arguments) {
        this.arguments = arguments;
    }

}
