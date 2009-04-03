package com.intellij.tapestry.tests.actions.createnew;

import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.intellij.actions.createnew.AddNewComponentAction;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.view.TapestryProjectViewPane;
import com.intellij.tapestry.tests.actions.ActionMockHelper;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Test;

public class AddNewComponentActionTest extends BaseTestCase {

    @Test(dataProvider = EMPTY_FIXTURE_PROVIDER)
    public void update_not_tapestry_module(IdeaProjectTestFixture fixture) {
        ActionMockHelper actionMockHelper = new ActionMockHelper();
        actionMockHelper.setModuleAsNotTapestryModule();
        Project projectMock = actionMockHelper.getProjectMock();

        TapestryProjectViewPane tapestryProjectViewPaneMock = getTapestryProjectViewPaneMock();
        org.easymock.classextension.EasyMock.expect(tapestryProjectViewPaneMock.getSelectedNode()).andReturn(null);
        expect(projectMock.getComponent(TapestryProjectViewPane.class)).andReturn(tapestryProjectViewPaneMock);
        org.easymock.classextension.EasyMock.replay(tapestryProjectViewPaneMock);

        actionMockHelper.replayAll();
        new AddNewComponentAction().update(actionMockHelper.getEventMock());

        assert !actionMockHelper.getEventMock().getPresentation().isEnabled();
        assert !actionMockHelper.getEventMock().getPresentation().isVisible();
    }

    @Test(dataProvider = EMPTY_FIXTURE_PROVIDER)
    public void update_from_project_view_not_inside_components_package(IdeaProjectTestFixture fixture) throws NotFoundException {
        ActionMockHelper actionMockHelper = new ActionMockHelper();
        actionMockHelper.setModuleAsTapestryModule();
        Project projectMock = actionMockHelper.getProjectMock();
        expect(projectMock.isInitialized()).andReturn(false);

        TapestryProjectViewPane tapestryProjectViewPaneMock = getTapestryProjectViewPaneMock();
        org.easymock.classextension.EasyMock.expect(tapestryProjectViewPaneMock.getSelectedNode()).andReturn(null);
        expect(projectMock.getComponent(TapestryProjectViewPane.class)).andReturn(tapestryProjectViewPaneMock);
        org.easymock.classextension.EasyMock.replay(tapestryProjectViewPaneMock);

        PsiPackage psiPackageMock = createMock(PsiPackage.class);
        expect(psiPackageMock.getQualifiedName()).andReturn("com.app").anyTimes();

        PsiDirectory psiDirectoryMock = createMock(PsiDirectory.class);
        expect(IdeaUtils.getPackage(psiDirectoryMock)).andReturn(psiPackageMock);
        replay(psiDirectoryMock, psiPackageMock);
        actionMockHelper.addDataContext(DataKeys.PSI_ELEMENT.getName(), psiDirectoryMock);

        TapestryProject tapestryProjectMock = actionMockHelper.getTapestryProjectMock();
        org.easymock.classextension.EasyMock.expect(tapestryProjectMock.getApplicationRootPackage()).andReturn("com.app").anyTimes();
        org.easymock.classextension.EasyMock.expect(tapestryProjectMock.getComponentsRootPackage()).andReturn("com.app.components").anyTimes();

        PsiPackage psiComponentsPackageMock = createMock(PsiPackage.class);
        expect(psiComponentsPackageMock.getQualifiedName()).andReturn("com.app.components").anyTimes();
        PsiManager psiManagerMock = actionMockHelper.getPsiManagerMock();
        //org.easymock.classextension.EasyMock.expect(psiManagerMock.findPackage("com.app.components")).andReturn(psiComponentsPackageMock).anyTimes();
        replay(psiComponentsPackageMock);

        actionMockHelper.replayAll();
        new AddNewComponentAction().update(actionMockHelper.getEventMock());

        assert !actionMockHelper.getEventMock().getPresentation().isEnabled();
        assert actionMockHelper.getEventMock().getPresentation().isVisible();
    }

    @Test(dataProvider = EMPTY_FIXTURE_PROVIDER)
    public void update_from_project_view_inside_components_package(IdeaProjectTestFixture fixture) throws NotFoundException {
        ActionMockHelper actionMockHelper = new ActionMockHelper();
        actionMockHelper.setModuleAsTapestryModule();
        Project projectMock = actionMockHelper.getProjectMock();
        expect(projectMock.isInitialized()).andReturn(false).anyTimes();

        TapestryProjectViewPane tapestryProjectViewPaneMock = getTapestryProjectViewPaneMock();
        org.easymock.classextension.EasyMock.expect(tapestryProjectViewPaneMock.getSelectedNode()).andReturn(null);
        expect(projectMock.getComponent(TapestryProjectViewPane.class)).andReturn(tapestryProjectViewPaneMock);
        org.easymock.classextension.EasyMock.replay(tapestryProjectViewPaneMock);

        PsiPackage psiPackageMock = createMock(PsiPackage.class);
        expect(psiPackageMock.getQualifiedName()).andReturn("com.app.components.test").anyTimes();

        PsiDirectory psiDirectoryMock = createMock(PsiDirectory.class);
        expect(IdeaUtils.getPackage(psiDirectoryMock)).andReturn(psiPackageMock);
        replay(psiDirectoryMock, psiPackageMock);
        actionMockHelper.addDataContext(DataKeys.PSI_ELEMENT.getName(), psiDirectoryMock);

        TapestryProject tapestryProjectMock = actionMockHelper.getTapestryProjectMock();
        org.easymock.classextension.EasyMock.expect(tapestryProjectMock.getApplicationRootPackage()).andReturn("com.app").anyTimes();
        org.easymock.classextension.EasyMock.expect(tapestryProjectMock.getComponentsRootPackage()).andReturn("com.app.components").anyTimes();

        PsiPackage psiComponentsPackageMock = createMock(PsiPackage.class);
        expect(psiComponentsPackageMock.getQualifiedName()).andReturn("com.app.components").anyTimes();
        PsiManager psiManagerMock = actionMockHelper.getPsiManagerMock();
        //org.easymock.classextension.EasyMock.expect(psiManagerMock.findPackage("com.app.components")).andReturn(psiComponentsPackageMock).anyTimes();
        replay(psiComponentsPackageMock);

        actionMockHelper.replayAll();
        new AddNewComponentAction().update(actionMockHelper.getEventMock());

        assert actionMockHelper.getEventMock().getPresentation().isEnabled();
        assert actionMockHelper.getEventMock().getPresentation().isVisible();
    }

    private TapestryProjectViewPane getTapestryProjectViewPaneMock() {
        return org.easymock.classextension.EasyMock.createMock(TapestryProjectViewPane.class);
    }
}
