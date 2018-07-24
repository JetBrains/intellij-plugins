package com.intellij.tapestry.core.maven;

/**
 * Description of an Remote Repository
 */
public class RemoteRepositoryDescription {

    private final String _name;
    private final String _url;
    private final String _id;
    private final boolean _createSnapshots;
    private final boolean _createReleases;

    public RemoteRepositoryDescription(String url, String id, String name, boolean createSnapshots, boolean createReleases) {
        _name = name;
        _url = url;
        _id = id;
        _createSnapshots = createSnapshots;
        _createReleases = createReleases;
    }

    public String getName() {
        return _name;
    }

    public String getId() {
        return _id;
    }

    public String getUrl() {
        return _url;
    }

    public boolean isCreatingSnapshots() {
        return _createSnapshots;
    }

    public boolean isCreatingReleases() {
        return _createReleases;
    }
}
