package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.Computable;
import com.intellij.util.net.NetUtils;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public class OpenDartObservatoryUrlAction extends DumbAwareAction {
  private final int myObservatoryPort;
  private final Computable<Boolean> myIsApplicable;

  public OpenDartObservatoryUrlAction(final int observatoryPort, final Computable<Boolean> isApplicable) {
    super(DartBundle.message("open.observatory.action.text"), DartBundle.message("open.observatory.action.description"), DartIcons.Dart_16);
    myObservatoryPort = observatoryPort;
    myIsApplicable = isApplicable;
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabled(myIsApplicable.compute());
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    BrowserUtil.browse("http://" + NetUtils.getLocalHostString() + ":" + myObservatoryPort);
  }
}
