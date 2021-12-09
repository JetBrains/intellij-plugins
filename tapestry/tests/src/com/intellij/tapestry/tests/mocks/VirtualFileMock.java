package com.intellij.tapestry.tests.mocks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class VirtualFileMock extends VirtualFile {

    private String _url;

    @Override
    @NotNull
    @NonNls
    public String getName() {
      throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public String getUrl() {
        return _url;
    }

    public VirtualFileMock setUrl(String url) {
        _url = url;

        return this;
    }

    @Override
    @NotNull
    public VirtualFileSystem getFileSystem() {
      throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String getPath() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    @Nullable
    public VirtualFile getParent() {
        return null;
    }

    @Override
    public VirtualFile[] getChildren() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    @NotNull
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) {
      throw new UnsupportedOperationException();
    }

    @Override
    public byte @NotNull [] contentsToByteArray() {
        return new byte[0];
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
    }

    @Override
    public @NotNull InputStream getInputStream() {
        throw new UnsupportedOperationException();
    }
}
