package com.intellij.tapestry.intellij.toolwindow;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.model.externalizable.documentation.Home;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import icons.TapestryCoreIcons;
import org.apache.log4j.Logger;
//import org.lobobrowser.html.UserAgentContext;
//import org.lobobrowser.html.gui.HtmlPanel;
//import org.lobobrowser.html.parser.DocumentBuilderImpl;
//import org.lobobrowser.html.parser.InputSourceImpl;
//import org.lobobrowser.html.test.SimpleHtmlRendererContext;
//import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Description Class
 */
public class DocumentationTab {

    private JButton _homeButton;
    private JButton _reloadButton;
    private JButton _backButton;
    private JButton _forwardButton;
    private JButton _goButton;
    private JTextField _text;
    private JButton _docButton;
    private JPanel _mainPanel;

    private static final Logger _logger = Logger.getLogger(DocumentationTab.class);

//    private DocumentBuilderImpl _documentBuilder;
//    private HtmlPanel _htmlPanel;
    private JButton _classButton;
//    private SimpleHtmlRendererContext _renderContext;
    private Project _project;
    private Object _element;

    public DocumentationTab(Project project) {
        _project = project;

        _homeButton.setIcon(TapestryCoreIcons.House);
        _backButton.setIcon(TapestryCoreIcons.Arrow_left);
        _forwardButton.setIcon(TapestryCoreIcons.Arrow_right);
        _reloadButton.setIcon(TapestryCoreIcons.Arrow_refresh);
        _goButton.setIcon(TapestryCoreIcons.Bullet_go);

        _backButton.setVisible(false);
        _forwardButton.setVisible(false);
        _reloadButton.setVisible(false);
        _goButton.setVisible(false);
        _docButton.setVisible(false);

//        UserAgentContext ucontext = new SimpleUserAgentContext();

        _text.setText("ldp:Home");

//        _renderContext = new SimpleHtmlRendererContext(_htmlPanel, ucontext);
//        _documentBuilder = new DocumentBuilderImpl(ucontext, _renderContext);

        try {
            showDocumentation(null, project);
        } catch (Exception ex) {
            _logger.error("Error parsing documentation HTML with cobra", ex);
        }

        _homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDocumentation(null, _project);
            }
        });

        _classButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                navigateToClass();
            }
        });
    }

    public JComponent getMainPanel() {
        return _mainPanel;
    }

    /**
     * Navigate to the class of the component
     */
    protected void navigateToClass() {

        PresentationLibraryElement elementType = (PresentationLibraryElement) _element;

        if (_element != null) {
            FileEditorManager.getInstance(_project).openFile(
                    ((IntellijJavaClassType) elementType.getElementClass()).getPsiClass().getContainingFile().getVirtualFile(),
                    true);
        }
    }

    protected void setElement(Object element) {
        _element = element;
    }

    /**
     * Show the documentation.
     *
     * @param element the element to show the documentation of.
     * @param project the project of the element selected
     */
    protected void showDocumentation(Object element, Project project) {
        String text;

        if (element == null) {
            _classButton.setEnabled(false);

            _text.setText("ldp://Home");

            try {
                List<String> moduleNames = new ArrayList<String>();
                for (Module module : TapestryUtils.getAllTapestryModules(project))
                    moduleNames.add(module.getName());

                text = new Home(moduleNames).getDocumentation();
            } catch (Exception ex) {
                text = null;
            }
        } else {
            _classButton.setEnabled(true);

            PresentationLibraryElement elementType = (PresentationLibraryElement) element;
            String library = elementType.getLibrary().getId();

            if (library.equals(TapestryProject.APPLICATION_LIBRARY_ID))
                _text.setText("ldp://App : " + elementType.getElementClass().getFullyQualifiedName());
            else
                _text.setText("ldp://Lib : " + library + " : " + elementType.getElementClass().getFullyQualifiedName());

            try {
                text = elementType.getDocumentation();
            } catch (Exception e) {
                text = null;
            }
        }

        if (text != null) {
            Document document = null;
//            try {
//                document = _documentBuilder.parse(new InputSourceImpl(new StringReader(text), "http://"));
//            } catch (Exception ex) {
//                _logger.error("Error parsing documentation HTML with cobra", ex);
//            }
//
//            _htmlPanel.setDocument(document, _renderContext);
        } else
            clear();
    }

    /**
     * Clears the content.
     */
    protected void clear() {
//        _htmlPanel.clearDocument();
    }

    /**
     * Provides all the Tapestry Project modules
     *
     * @param project the tapestry project
     * @return the names of all modules
     */
    protected List<String> getModules(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        List<String> nameOfModules = new ArrayList<String>();

        for (Module module : modules)
            nameOfModules.add(module.getName());

        return nameOfModules;
    }
}
