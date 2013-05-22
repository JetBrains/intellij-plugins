package com.jetbrains.lang.dart;

import com.intellij.ide.highlighter.HtmlFileHighlighter;
import com.intellij.ide.highlighter.XmlFileHighlighter;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DartSupportLoader implements ProjectComponent {
  @Override
  public void projectOpened() {
    initDartSupport();
  }

  @Override
  public void projectClosed() {
  }

  @Override
  public void initComponent() {
    initDartSupport();
  }

  @Override
  public void disposeComponent() {
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "Dart";
  }

  private static void initDartSupport() {
    // add dart keys to html highlighter
    final Map<IElementType, TextAttributesKey> keys = DartSyntaxHighlighter.getKeys();
    HtmlFileHighlighter.registerEmbeddedTokenAttributes(keys, null);
    XmlFileHighlighter.registerEmbeddedTokenAttributes(keys, null);
  }
}
