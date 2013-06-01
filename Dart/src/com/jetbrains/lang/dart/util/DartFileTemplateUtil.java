package com.jetbrains.lang.dart.util;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.util.Condition;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartFileTemplateUtil {
  private final static String DART_TEMPLATE_PREFIX = "Dart ";

  public static List<FileTemplate> getApplicableTemplates() {
    return getApplicableTemplates(new Condition<FileTemplate>() {
      @Override
      public boolean value(FileTemplate fileTemplate) {
        return DartFileType.DEFAULT_EXTENSION.equals(fileTemplate.getExtension());
      }
    });
  }

  public static List<FileTemplate> getApplicableTemplates(Condition<FileTemplate> filter) {
    final List<FileTemplate> applicableTemplates = new SmartList<FileTemplate>();
    applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance().getInternalTemplates(), filter));
    applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance().getAllTemplates(), filter));
    return applicableTemplates;
  }

  public static String getTemplateShortName(String templateName) {
    if (templateName.startsWith(DART_TEMPLATE_PREFIX)) {
      return templateName.substring(DART_TEMPLATE_PREFIX.length());
    }
    return templateName;
  }

  @NotNull
  public static Icon getTemplateIcon(String name) {
    name = getTemplateShortName(name);
    if ("Class".equals(name)) {
      return icons.DartIcons.Class_dart;
    }
    else if ("Interface".equals(name)) {
      return icons.DartIcons.Interface_dart;
    }
    return icons.DartIcons.Dart_16;
  }
}
