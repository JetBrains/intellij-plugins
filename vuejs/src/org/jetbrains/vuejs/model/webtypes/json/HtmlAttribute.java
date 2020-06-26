
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
    "default",
    "required",
    "value",
    "source",
    "vue-argument",
    "vue-modifiers"
})
public class HtmlAttribute implements SourceEntity {

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
    @JsonProperty("default")
    private String _default;
    @JsonProperty("required")
    private Boolean required;
    @JsonProperty("value")
    private Object value;
    /**
     * Allows to specify the source of the entity. For Vue.js component this may be for instance a class.
     * 
     */
    @JsonProperty("source")
    @JsonPropertyDescription("Allows to specify the source of the entity. For Vue.js component this may be for instance a class.")
    private Source source;
    /**
     * Provide information about directive argument
     * 
     */
    @JsonProperty("vue-argument")
    @JsonPropertyDescription("Provide information about directive argument")
    private HtmlAttributeVueArgument vueArgument;
    @JsonProperty("vue-modifiers")
    private List<HtmlAttributeVueModifier> vueModifiers = new ArrayList<HtmlAttributeVueModifier>();

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
     * Provide information about directive argument
     * 
     */
    @JsonProperty("vue-argument")
    public HtmlAttributeVueArgument getVueArgument() {
        return vueArgument;
    }

    /**
     * Provide information about directive argument
     * 
     */
    @JsonProperty("vue-argument")
    public void setVueArgument(HtmlAttributeVueArgument vueArgument) {
        this.vueArgument = vueArgument;
    }

    @JsonProperty("vue-modifiers")
    public List<HtmlAttributeVueModifier> getVueModifiers() {
        return vueModifiers;
    }

    @JsonProperty("vue-modifiers")
    public void setVueModifiers(List<HtmlAttributeVueModifier> vueModifiers) {
        this.vueModifiers = vueModifiers;
    }

}
