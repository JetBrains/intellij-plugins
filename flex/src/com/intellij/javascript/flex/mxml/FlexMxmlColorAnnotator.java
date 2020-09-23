// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.daemon.LineMarkerSettings;
import com.intellij.javascript.flex.css.FlexCssPropertyDescriptor;
import com.intellij.javascript.flex.mxml.schema.AnnotationBackedDescriptorImpl;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.impl.util.CssPsiColorUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.ui.ColorChooser;
import com.intellij.ui.ColorLineMarkerProvider;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.ColorIcon;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.util.ColorMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexMxmlColorAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof XmlAttribute) || !JavaScriptSupportLoader.isFlexMxmFile(element.getContainingFile())) {
      return;
    }
    if (!LineMarkerSettings.getSettings().isEnabled(ColorLineMarkerProvider.INSTANCE)) {
      return;
    }
    XmlAttribute attribute = (XmlAttribute)element;
    XmlAttributeDescriptor descriptor = attribute.getDescriptor();
    if (!(descriptor instanceof AnnotationBackedDescriptorImpl)) {
      return;
    }
    AnnotationBackedDescriptorImpl annotationBackedDescriptor = (AnnotationBackedDescriptorImpl)descriptor;
    String format = annotationBackedDescriptor.getFormat();
    if (!FlexCssPropertyDescriptor.COLOR_FORMAT.equals(format)) {
      return;
    }

    final String value = attribute.getValue();

    if (value == null || value.length() == 0) {
      return;
    }

    if (!JSCommonTypeNames.ARRAY_CLASS_NAME.equals(annotationBackedDescriptor.getType())) {
      XmlAttributeValue valueElement = attribute.getValueElement();
      if (valueElement != null) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(valueElement).gutterIconRenderer(new MyRenderer(value, attribute)).create();
      }
    }
  }

  @Nullable
  private static Color getColor(@NotNull String colorValue) {
    try {
      int num = Integer.parseInt(colorValue);
      //noinspection UseJBColor
      return new Color(num);
    }
    catch (NumberFormatException ignored) {
    }
    String hex = toCanonicalHex(colorValue, false);
    if (hex == null) {
      return null;
    }
    try {
      return Color.decode(hex);
    }
    catch (NumberFormatException ignored) {
    }
    return null;
  }

  @Nullable
  private static String toCanonicalHex(String colorValue, boolean cssStyle) {
    if (colorValue.startsWith("#")) {
      if (cssStyle) return colorValue;
      return "0x" + colorValue.substring(1);
    }
    if (colorValue.startsWith("0x")) {
      if (!cssStyle) return colorValue;
      return "#" + colorValue.substring(2);
    }
    colorValue = StringUtil.toLowerCase(colorValue);
    if (ColorMap.isStandardColor(colorValue)) {
      String hex = ColorMap.getHexCodeForColorName(colorValue);
      if (hex != null) {
        return toCanonicalHex(hex, cssStyle);
      }
    }
    return null;
  }

  public static final class MyRenderer extends GutterIconRenderer {
    private static final int ICON_SIZE = 8;

    private final String myColorValue;
    private final XmlAttribute myAttribute;

    private MyRenderer(String colorValue, XmlAttribute attribute) {
      myColorValue = colorValue;
      myAttribute = attribute;
    }

    @NotNull
    @Override
    public Icon getIcon() {
      Color color = getColor(myColorValue);
      if (color != null) {
        return JBUIScale.scaleIcon(new ColorIcon(ICON_SIZE, color));
      }
      return JBUIScale.scaleIcon(EmptyIcon.create(ICON_SIZE));
    }

    @Override
    public String getTooltipText() {
      String hex = toCanonicalHex(myColorValue, true);
      if (hex == null) {
        return null;
      }
      Color color = null;
      try {
        color = Color.decode(hex);
      }
      catch (NumberFormatException ignored) {
      }
      if (color == null) {
        return null;
      }

      StringBuilder builder = new StringBuilder("<html><body><table style=\"padding: 2px 0;\" cellspacing=\"2\">");
      String attributeName = myAttribute.getName();
      builder.append("<tr><td valign=\"bottom\">").append(attributeName).append(":</td><td style=\"background-color:");
      final float[] hsb = new float[3];
      Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);

      final String textColor = hsb[2] > 0.7f ? "black" : "white";
      builder.append(hex);
      builder.append("; color: ").append(textColor).append("; font-family: monospaced;\" valign=\"bottom\">").append(hex)
        .append("</td></tr></table></body></html>");

      return builder.toString();
    }

    @Override
    public AnAction getClickAction() {
      return new AnAction() {
        @Override
        public void actionPerformed(@NotNull final AnActionEvent e) {
          final Editor editor = e.getData(CommonDataKeys.EDITOR);
          if (editor != null) {
            Color currentColor = getColor(myColorValue);
            final Color color = ColorChooser
              .chooseColor(editor.getProject(), editor.getComponent(), FlexBundle.message("flex.choose.color.dialog.title"), currentColor);
            if (color != null && !color.equals(currentColor)) {
              final PsiFile psiFile = myAttribute.getContainingFile();
              if (!FileModificationService.getInstance().prepareFileForWrite(psiFile)) return;

              final String hex = CssPsiColorUtil.toHexColor(color);
              final String mxmlStyleHex = toCanonicalHex(hex, false);
              ApplicationManager.getApplication().runWriteAction(() -> myAttribute.setValue(mxmlStyleHex));
            }
          }
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MyRenderer that = (MyRenderer)o;

      if (myAttribute != null ? !myAttribute.equals(that.myAttribute) : that.myAttribute != null) return false;
      if (myColorValue != null ? !myColorValue.equals(that.myColorValue) : that.myColorValue != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = myColorValue != null ? myColorValue.hashCode() : 0;
      result = 31 * result + (myAttribute != null ? myAttribute.hashCode() : 0);
      return result;
    }
  }
}
