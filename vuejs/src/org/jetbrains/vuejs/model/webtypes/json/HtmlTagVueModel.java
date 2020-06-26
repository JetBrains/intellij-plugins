
package org.jetbrains.vuejs.model.webtypes.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "prop",
    "event"
})
public class HtmlTagVueModel {

    @JsonProperty("prop")
    private String prop = "value";
    @JsonProperty("event")
    private String event = "input";

    @JsonProperty("prop")
    public String getProp() {
        return prop;
    }

    @JsonProperty("prop")
    public void setProp(String prop) {
        this.prop = prop;
    }

    @JsonProperty("event")
    public String getEvent() {
        return event;
    }

    @JsonProperty("event")
    public void setEvent(String event) {
        this.event = event;
    }

}
