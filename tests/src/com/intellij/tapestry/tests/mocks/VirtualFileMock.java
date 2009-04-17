package com.intellij.tapestry.tests.mocks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class VirtualFileMock extends VirtualFile {

    private String _url;

    @NotNull
    @NonNls
    public String getName() {
        return null;
    }

    @NotNull
    public String getUrl() {
        return _url;
    }

    public VirtualFileMock setUrl(String url) {
        _url = url;

        return this;
    }

    @NotNull
    public VirtualFileSystem getFileSystem() {
        return null;
    }

    public String getPath() {
        return null;
    }

    public boolean isWritable() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public boolean isValid() {
        return false;
    }

    @Nullable
    public VirtualFile getParent() {
        return null;
    }

    public VirtualFile[] getChildren() {
        return new VirtualFile[0];
    }

    @NotNull
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
        return null;
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        return new byte[0];
    }

    public long getTimeStamp() {
        return 0;
    }

    public long getLength() {
        return 0;
    }

    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
    }

    public InputStream getInputStream() throws IOException {
        return null;
    }
}
