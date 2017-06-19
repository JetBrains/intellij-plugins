package org.dartlang.analysis.server.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.util.text.StringUtil;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostfixCompletionTemplate {
  public static final PostfixCompletionTemplate[] EMPTY_ARRAY = new PostfixCompletionTemplate[0];
  private final String key;
  private final String name;
  private final String example;

  public PostfixCompletionTemplate(String key, String name, String example) {
    this.key = key;
    this.name = name;
    this.example = example;
  }

  public static PostfixCompletionTemplate fromJson(JsonObject jsonObject) {
    String name = jsonObject.get("name") == null ? null : jsonObject.get("name").getAsString();
    String key = jsonObject.get("key") == null ? null : jsonObject.get("key").getAsString();
    String example = jsonObject.get("example") == null ? null : jsonObject.get("example").getAsString();
    return new PostfixCompletionTemplate(key, name, example);
  }

  public static PostfixCompletionTemplate[] fromStringArray(String[][] strings) {
    if (strings == null) {
      return EMPTY_ARRAY;
    }
    ArrayList<PostfixCompletionTemplate> list = new ArrayList<>(strings.length);
    PostfixCompletionTemplate[] templates = new PostfixCompletionTemplate[strings.length];
    for (int i = 0; i < strings.length; i++) {
      templates[i] = new PostfixCompletionTemplate(strings[i][1], strings[i][0], strings[i][2]);
    }
    return templates;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PostfixCompletionTemplate) {
      PostfixCompletionTemplate other = (PostfixCompletionTemplate)obj;
      return StringUtil.equals(key, other.key) && StringUtil.equals(name, other.name) && StringUtil.equals(example, other.example);
    }
    return false;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(name);
    builder.append(key);
    builder.append(example);
    return builder.toHashCode();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    builder.append("name=");
    builder.append(name);
    builder.append(", key=");
    builder.append(key);
    builder.append(", example=");
    builder.append(example);
    builder.append("]");
    return builder.toString();
  }

  public String getName() {
    return name;
  }

  public String getExample() {
    return example;
  }

  public String getKey() {
    return key;
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    if (name != null) {
      jsonObject.addProperty("name", name);
    }
    if (key != null) {
      jsonObject.addProperty("key", key);
    }
    if (example != null) {
      jsonObject.addProperty("example", example);
    }
    return jsonObject;
  }
}
