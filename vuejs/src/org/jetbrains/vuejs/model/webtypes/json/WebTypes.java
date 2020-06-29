
package org.jetbrains.vuejs.model.webtypes.json;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;


/**
 * JSON schema for web-types
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "framework",
    "name",
    "version",
    "contributions"
})
public class WebTypes {

    /**
     * Framework, for which the components are provided by the library
     * (Required)
     * 
     */
    @JsonProperty("framework")
    @JsonPropertyDescription("Framework, for which the components are provided by the library")
    private WebTypes.Framework framework;
    /**
     * Name of the library
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the library")
    private String name;
    /**
     * Version of the library, for which web-types are provided
     * (Required)
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("Version of the library, for which web-types are provided")
    private String version;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("contributions")
    private Contributions contributions;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Framework, for which the components are provided by the library
     * (Required)
     * 
     */
    @JsonProperty("framework")
    public WebTypes.Framework getFramework() {
        return framework;
    }

    /**
     * Framework, for which the components are provided by the library
     * (Required)
     * 
     */
    @JsonProperty("framework")
    public void setFramework(WebTypes.Framework framework) {
        this.framework = framework;
    }

    /**
     * Name of the library
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Name of the library
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Version of the library, for which web-types are provided
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * Version of the library, for which web-types are provided
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("contributions")
    public Contributions getContributions() {
        return contributions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("contributions")
    public void setContributions(Contributions contributions) {
        this.contributions = contributions;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


    /**
     * Framework, for which the components are provided by the library
     * 
     */
    public enum Framework {

        VUE("vue");
        private final String value;
        private final static Map<String, WebTypes.Framework> CONSTANTS = new HashMap<>();

        static {
            for (WebTypes.Framework c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Framework(String value) {
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
        public static WebTypes.Framework fromValue(String value) {
            WebTypes.Framework constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
