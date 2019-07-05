package com.intellij.tapestry.core.events;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.resource.TestableResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class TapestryEventsManagerTest {

    private FileSystemListener _fileEventListenerMock;
    private TapestryModelChangeListener _tapestryListenerMock;
    private TapestryEventsManager _eventsManager;

    @BeforeMethod
    public void initMocks() {
        TapestryLibrary applicationLibraryMock = org.easymock.EasyMock.createMock(TapestryLibrary.class);
        org.easymock.EasyMock.expect(applicationLibraryMock.getBasePackage()).andReturn("com.app").anyTimes();
        org.easymock.EasyMock.replay(applicationLibraryMock);

        TapestryProject tapestryProjectMock = org.easymock.EasyMock.createMock(TapestryProject.class);
        org.easymock.EasyMock.expect(tapestryProjectMock.getApplicationLibrary()).andReturn(applicationLibraryMock).anyTimes();
        org.easymock.EasyMock.expect(tapestryProjectMock.getPagesRootPackage()).andReturn("com.app.pages").anyTimes();
        org.easymock.EasyMock.replay(tapestryProjectMock);

        _fileEventListenerMock = createMock(FileSystemListener.class);

        _tapestryListenerMock = createMock(TapestryModelChangeListener.class);

        _eventsManager = new TapestryEventsManager();
    }

    @Test
    public void modelChanged() {
        _tapestryListenerMock.modelChanged();

        replay(_tapestryListenerMock);

        _eventsManager.addTapestryModelListener(_tapestryListenerMock);
        _eventsManager.modelChanged();

        verify(_tapestryListenerMock);
    }

    @Test
    public void classDeleted() {
        _fileEventListenerMock.classDeleted("com.app.pages.Page1");

        replay(_fileEventListenerMock);

        _eventsManager.addFileSystemListener(_fileEventListenerMock);
        _eventsManager.classDeleted("com.app.pages.Page1");

        verify(_fileEventListenerMock);
    }

    @Test
    public void fileContentsChanged() {
        TestableResource resource = new TestableResource("", "");

        _fileEventListenerMock.fileContentsChanged(resource);

        replay(_fileEventListenerMock);

        _eventsManager.addFileSystemListener(_fileEventListenerMock);
        _eventsManager.fileContentsChanged(resource);

        verify(_fileEventListenerMock);
    }

    @Test
    public void classCreated() {
        _fileEventListenerMock.classCreated("com.app.pages.Page1");

        replay(_fileEventListenerMock);

        _eventsManager.addFileSystemListener(_fileEventListenerMock);
        _eventsManager.classCreated("com.app.pages.Page1");

        verify(_fileEventListenerMock);
    }

    @Test
    public void removeFileSystemListener() {
        _fileEventListenerMock.classDeleted("com.app.pages.Page1");

        replay(_fileEventListenerMock);

        _eventsManager.addFileSystemListener(_fileEventListenerMock);
        _eventsManager.classDeleted("com.app.pages.Page1");

        verify(_fileEventListenerMock);

        _eventsManager.removeFileSystemListener(_fileEventListenerMock);
        _eventsManager.classDeleted("com.app.pages.Page1");
        verify(_fileEventListenerMock);
    }

    @Test
    public void removeTapestryModelListener() {
        _tapestryListenerMock.modelChanged();

        replay(_tapestryListenerMock);

        _eventsManager.addTapestryModelListener(_tapestryListenerMock);
        _eventsManager.modelChanged();

        verify(_tapestryListenerMock);

        _eventsManager.removeTapestryModelListener(_tapestryListenerMock);
        _eventsManager.modelChanged();
        verify(_tapestryListenerMock);
    }

    @Test
    public void fileCreated() {
        _fileEventListenerMock.fileCreated("some/path");

        replay(_fileEventListenerMock);

        _eventsManager.addFileSystemListener(_fileEventListenerMock);
        _eventsManager.fileCreated("some/path");

        verify(_fileEventListenerMock);
    }

    @Test
    public void fileDeleted() {
        _fileEventListenerMock.fileDeleted("some/path");

        replay(_fileEventListenerMock);

        _eventsManager.addFileSystemListener(_fileEventListenerMock);
        _eventsManager.fileDeleted("some/path");

        verify(_fileEventListenerMock);
    }
}
