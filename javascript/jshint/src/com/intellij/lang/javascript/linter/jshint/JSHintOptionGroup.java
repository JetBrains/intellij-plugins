package com.intellij.lang.javascript.linter.jshint;

import com.intellij.openapi.util.NlsContexts.HintText;
import com.intellij.openapi.util.NlsContexts.Label;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public class JSHintOptionGroup {

  private final @Label String myTitle;
  private final @HintText String myDescription;
  private final Map<JSHintOption, @Nls String> myHtmlDescriptionByOptionMap;

  public JSHintOptionGroup(@NotNull @Label String title,
                           @NotNull @HintText String description) {
    myTitle = title;
    myDescription = htmlWrap(description);
    myHtmlDescriptionByOptionMap = new EnumMap<>(JSHintOption.class);
  }

  public @NotNull @Label String getTitle() {
    return myTitle;
  }

  public @NotNull @HintText String getDescription() {
    return myDescription;
  }

  public @NotNull Collection<JSHintOption> getOptions() {
    return myHtmlDescriptionByOptionMap.keySet();
  }

  public @Nullable @Nls String getHtmlDescriptionByOption(@NotNull JSHintOption option) {
    return myHtmlDescriptionByOptionMap.get(option);
  }

  @Override
  public String toString() {
    return "(title: " + myTitle + ")";
  }

  public void add(@NotNull JSHintOption option, @HintText @NotNull String description) {
    myHtmlDescriptionByOptionMap.put(option, htmlWrap(description));
  }

  private static @HintText String htmlWrap(@HintText @NotNull String bodyHtml) {
    String openingTags = "<html><body>";
    if (bodyHtml.startsWith(openingTags)) {
      return bodyHtml;
    }
    return  openingTags + bodyHtml + "</body></html>";
  }

}
