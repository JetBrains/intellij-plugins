package com.intellij.flex.uiDesigner.preview;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MxmlPreviewProgressIndicator extends ProgressIndicatorBase {
  private final Object lock = new Object();
  private final int delay;
  private final MxmlPreviewToolWindowForm form;

  public MxmlPreviewProgressIndicator(@NotNull MxmlPreviewToolWindowForm form, int delay) {
    this.form = form;
    this.delay = delay;
  }

  @Override
    public void start() {
      super.start();
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          final Timer timer = UIUtil.createNamedTimer("Android rendering progress timer", delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              synchronized (lock) {
                if (isRunning() && form != null) {
                  form.getPreviewPanel().registerIndicator(MxmlPreviewProgressIndicator.this);
                }
              }
            }
          });
          timer.setRepeats(false);
          timer.start();
        }
      });
    }

    @Override
    public void stop() {
      synchronized (lock) {
        super.stop();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            if (form != null) {
              form.getPreviewPanel().unregisterIndicator(MxmlPreviewProgressIndicator.this);
            }
          }
        });
      }
    }
}
