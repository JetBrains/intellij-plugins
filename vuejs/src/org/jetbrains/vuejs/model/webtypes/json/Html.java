
package org.jetbrains.vuejs.model.webtypes.json;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "types-syntax",
    "description-markup",
    "tags",
    "attributes",
    "vue-filters"
})
public class Html {

    /**
     * Language in which types as specified.
     * 
     */
    @JsonProperty("types-syntax")
    @JsonPropertyDescription("Language in which types as specified.")
    private Html.TypesSyntax typesSyntax;
    /**
     * Markup language in which descriptions are formatted
     * 
     */
    @JsonProperty("description-markup")
    @JsonPropertyDescription("Markup language in which descriptions are formatted")
    private Html.DescriptionMarkup descriptionMarkup = Html.DescriptionMarkup.fromValue("none");
    @JsonProperty("tags")
    private List<HtmlTag> tags = new ArrayList<>();
    @JsonProperty("attributes")
    private List<HtmlAttribute> attributes = new ArrayList<>();
    @JsonProperty("vue-filters")
    private List<HtmlVueFilter> vueFilters = new ArrayList<>();

    /**
     * Language in which types as specified.
     * 
     */
    @JsonProperty("types-syntax")
    public Html.TypesSyntax getTypesSyntax() {
        return typesSyntax;
    }

    /**
     * Language in which types as specified.
     * 
     */
    @JsonProperty("types-syntax")
    public void setTypesSyntax(Html.TypesSyntax typesSyntax) {
        this.typesSyntax = typesSyntax;
    }

    /**
     * Markup language in which descriptions are formatted
     * 
     */
    @JsonProperty("description-markup")
    public Html.DescriptionMarkup getDescriptionMarkup() {
        return descriptionMarkup;
    }

    /**
     * Markup language in which descriptions are formatted
     * 
     */
    @JsonProperty("description-markup")
    public void setDescriptionMarkup(Html.DescriptionMarkup descriptionMarkup) {
        this.descriptionMarkup = descriptionMarkup;
    }

    @JsonProperty("tags")
    public List<HtmlTag> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(List<HtmlTag> tags) {
        this.tags = tags;
    }

    @JsonProperty("attributes")
    public List<HtmlAttribute> getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(List<HtmlAttribute> attributes) {
        this.attributes = attributes;
    }

    @JsonProperty("vue-filters")
    public List<HtmlVueFilter> getVueFilters() {
        return vueFilters;
    }

    @JsonProperty("vue-filters")
    public void setVueFilters(List<HtmlVueFilter> vueFilters) {
        this.vueFilters = vueFilters;
    }


    /**
     * Markup language in which descriptions are formatted
     * 
     */
    public enum DescriptionMarkup {

        HTML("html"),
        MARKDOWN("markdown"),
        NONE("none");
        private final String value;
        private final static Map<String, Html.DescriptionMarkup> CONSTANTS = new HashMap<>();

        static {
            for (Html.DescriptionMarkup c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        DescriptionMarkup(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Html.DescriptionMarkup fromValue(String value) {
            Html.DescriptionMarkup constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * Language in which types as specified.
     * 
     */
    public enum TypesSyntax {

        TYPESCRIPT("typescript");
        private final String value;
        private final static Map<String, Html.TypesSyntax> CONSTANTS = new HashMap<>();

        static {
            for (Html.TypesSyntax c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TypesSyntax(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Html.TypesSyntax fromValue(String value) {
            Html.TypesSyntax constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
