package org.jetbrains.training;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.popup.ComponentPopupBuilderImpl;

import javax.swing.*;
import java.awt.*;

/**
 * Created by karashevich on 04/01/15.
 */
public class InfoPanel extends NonOpaquePanel {

    private JPanel labelsPanel;
    private JBPopup hint;

    public InfoPanel(Project project) {
        super(new BorderLayout());


        labelsPanel = new NonOpaquePanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
        Color bckColor = new Color(128,128,128,128);

        setBackground(bckColor);
        setOpaque(true);

        add(labelsPanel, BorderLayout.CENTER);

        ComponentPopupBuilderImpl componentPopupBuilder = (ComponentPopupBuilderImpl) JBPopupFactory.getInstance().createComponentPopupBuilder(this, this);
        componentPopupBuilder.setFocusable(false);
        componentPopupBuilder.setBelongsToGlobalPopupStack(false);
        componentPopupBuilder.setCancelKeyEnabled(false);
        componentPopupBuilder.createPopup();

        hint = (JBPopup) componentPopupBuilder;
        hint.show(ideFrame.getComponent());
    }
}
