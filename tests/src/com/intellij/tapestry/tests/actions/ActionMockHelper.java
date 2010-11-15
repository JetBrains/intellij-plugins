package com.intellij.tapestry.tests.actions;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.facet.TapestryFacet;
import com.intellij.tapestry.intellij.facet.TapestryFacetConfiguration;
import com.intellij.tapestry.intellij.facet.TapestryFacetType;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collection;

public class ActionMockHelper {

    private AnActionEvent _event;
  private InputEvent _inputEventMock;
    private DataContext _dataContextMock;
    private Module _moduleMock;
    private Project _projectMock;
    private PsiManager _psiManagerMock;
    private TapestryModuleSupportLoader _TapestryModuleSupportLoaderMock;
    private TapestryProject _tapestryProjectMock;
    private FacetManager _facetManagerMock = EasyMock.createMock(FacetManager.class);

    public ActionMockHelper() {
        _inputEventMock = org.easymock.classextension.EasyMock.createMock(InputEvent.class);

        _psiManagerMock = org.easymock.classextension.EasyMock.createMock(PsiManager.class);

        _tapestryProjectMock = org.easymock.classextension.EasyMock.createMock(TapestryProject.class);

        _TapestryModuleSupportLoaderMock = org.easymock.classextension.EasyMock.createMock(TapestryModuleSupportLoader.class);
        org.easymock.classextension.EasyMock.expect(_TapestryModuleSupportLoaderMock.getTapestryProject()).andReturn(_tapestryProjectMock);

        _projectMock = createMock(Project.class);
        expect(_projectMock.getComponent(PsiManager.class)).andReturn(_psiManagerMock).anyTimes();

        _moduleMock = createMock(Module.class);
        expect(_moduleMock.getProject()).andReturn(_projectMock).anyTimes();
        expect(_moduleMock.getComponent(TapestryModuleSupportLoader.class)).andReturn(_TapestryModuleSupportLoaderMock).anyTimes();

        _facetManagerMock = org.easymock.classextension.EasyMock.createMock(FacetManager.class);

        expect(_moduleMock.getComponent(FacetManager.class)).andReturn(_facetManagerMock).anyTimes();

        _dataContextMock = createDataContext();

      Presentation _presentation = new Presentation();

        _event = new AnActionEvent(_inputEventMock, _dataContextMock, "", _presentation, null, 0);
    }

    public AnActionEvent getEventMock() {
        return _event;
    }

    public Project getProjectMock() {
        return _projectMock;
    }

    public Module getModuleMock() {
        return _moduleMock;
    }

    public PsiManager getPsiManagerMock() {
        return _psiManagerMock;
    }

    public TapestryModuleSupportLoader getTapestryModuleSupportLoaderMock() {
        return _TapestryModuleSupportLoaderMock;
    }

    public TapestryProject getTapestryProjectMock() {
        return _tapestryProjectMock;
    }

    public void replayAll() {
        replay(_dataContextMock, _moduleMock, _projectMock);

        org.easymock.classextension.EasyMock.replay(_inputEventMock, _psiManagerMock, _TapestryModuleSupportLoaderMock, _tapestryProjectMock, _facetManagerMock);
    }

    public void resetAll() {
        reset(_dataContextMock, _moduleMock, _projectMock);

        org.easymock.classextension.EasyMock.reset(_inputEventMock, _psiManagerMock, _TapestryModuleSupportLoaderMock, _tapestryProjectMock, _facetManagerMock);
    }

    public void addDataContext(String key, Object value) {
        expect(_dataContextMock.getData(key)).andReturn(value).anyTimes();
    }

    public void setModuleAsTapestryModule() {
        Collection<TapestryFacet> facet = new ArrayList<TapestryFacet>();
        facet.add(new TapestryFacet(TapestryFacetType.getInstance(), _moduleMock, null, new TapestryFacetConfiguration(), null));

        org.easymock.classextension.EasyMock.expect(_facetManagerMock.getFacetsByType(TapestryFacetType.ID)).andReturn(facet).anyTimes();
    }

    public void setModuleAsNotTapestryModule() {
        org.easymock.classextension.EasyMock.expect(_facetManagerMock.getFacetsByType(TapestryFacetType.ID)).andReturn(new ArrayList<TapestryFacet>());
    }

    private DataContext createDataContext() {
        DataContext dataContextMock = createMock(DataContext.class);
        expect(dataContextMock.getData(DataKeys.MODULE.getName())).andReturn(_moduleMock).anyTimes();

        return dataContextMock;
    }
}
