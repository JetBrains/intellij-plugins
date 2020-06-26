
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
    "attributes",
    "source",
    "events",
    "slots",
    "vue-scoped-slots",
    "vue-model"
})
public class HtmlTag implements SourceEntity {

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
    @JsonProperty("attributes")
    private List<HtmlTagAttribute> attributes = new ArrayList<HtmlTagAttribute>();
    /**
     * Allows to specify the source of the entity. For Vue.js component this may be for instance a class.
     * 
     */
    @JsonProperty("source")
    @JsonPropertyDescription("Allows to specify the source of the entity. For Vue.js component this may be for instance a class.")
    private Source source;
    /**
     * 
     */
    @JsonProperty("events")
    @JsonPropertyDescription("")
    private List<HtmlTagEvent> events = new ArrayList<HtmlTagEvent>();
    @JsonProperty("slots")
    private List<HtmlTagSlot> slots = new ArrayList<HtmlTagSlot>();
    /**
     * Deprecated. Use regular 'slot' property instead and specify 'vue-properties' to provide slot scope information.
     * 
     */
    @JsonProperty("vue-scoped-slots")
    @JsonPropertyDescription("Deprecated. Use regular 'slot' property instead and specify 'vue-properties' to provide slot scope information.")
    private List<HtmlTagSlot> vueScopedSlots = new ArrayList<HtmlTagSlot>();
    @JsonProperty("vue-model")
    private HtmlTagVueModel vueModel;

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

    @JsonProperty("attributes")
    public List<HtmlTagAttribute> getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(List<HtmlTagAttribute> attributes) {
        this.attributes = attributes;
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
     * 
     */
    @JsonProperty("events")
    public List<HtmlTagEvent> getEvents() {
        return events;
    }

    /**
     * 
     */
    @JsonProperty("events")
    public void setEvents(List<HtmlTagEvent> events) {
        this.events = events;
    }

    @JsonProperty("slots")
    public List<HtmlTagSlot> getSlots() {
        return slots;
    }

    @JsonProperty("slots")
    public void setSlots(List<HtmlTagSlot> slots) {
        this.slots = slots;
    }

    /**
     * Deprecated. Use regular 'slot' property instead and specify 'vue-properties' to provide slot scope information.
     * 
     */
    @JsonProperty("vue-scoped-slots")
    public List<HtmlTagSlot> getVueScopedSlots() {
        return vueScopedSlots;
    }

    /**
     * Deprecated. Use regular 'slot' property instead and specify 'vue-properties' to provide slot scope information.
     * 
     */
    @JsonProperty("vue-scoped-slots")
    public void setVueScopedSlots(List<HtmlTagSlot> vueScopedSlots) {
        this.vueScopedSlots = vueScopedSlots;
    }

    @JsonProperty("vue-model")
    public HtmlTagVueModel getVueModel() {
        return vueModel;
    }

    @JsonProperty("vue-model")
    public void setVueModel(HtmlTagVueModel vueModel) {
        this.vueModel = vueModel;
    }

}
