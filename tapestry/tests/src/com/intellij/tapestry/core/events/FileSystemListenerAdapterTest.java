package com.intellij.tapestry.core.events;

import org.testng.annotations.Test;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class FileSystemListenerAdapterTest {

    @Test
    public void testAll() {
        FileSystemListenerAdapter adapter = new TestableFileSystemListenerAdapter();
        adapter.classCreated(null);
        adapter.classDeleted(null);
        adapter.fileCreated(null);
        adapter.fileDeleted(null);
    }
}

class TestableFileSystemListenerAdapter extends FileSystemListenerAdapter {

}
