package com.dmarcotte.handlebars.config;

import com.dmarcotte.handlebars.HbBundle;
import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;

public class HbFoldingOptionsProvider
  extends BeanConfigurable<HbFoldingOptionsProvider.HbCodeFoldingOptionsBean> implements CodeFoldingOptionsProvider {

  @SuppressWarnings("UnusedDeclaration") // the properties in this class are accessed using reflection by the parent
  public static class HbCodeFoldingOptionsBean {

    public boolean isAutoCollapseBlocks() {
      return HbConfig.isAutoCollapseBlocksEnabled();
    }

    public void setAutoCollapseBlocks(boolean value) {
      HbConfig.setAutoCollapseBlocks(value);
    }
  }

  public HbFoldingOptionsProvider() {
    super(new HbCodeFoldingOptionsBean());

    checkBox("autoCollapseBlocks", HbBundle.message("hb.pages.folding.auto.collapse.blocks"));
  }
}
