package com.intellij.tapestry.core.maven;

import java.util.List;

/**
 * Holds the configuration settings given in the wizard.
 */
public class MavenConfiguration {

    private final String _groupId;
    private final String _artifactId;
    private final String _version;
    private final String _artifactIdParentPom;
    private final String _groupIdParentPom;
    private final String _versionParentPom;
    private final boolean _createParentPom;
    private final boolean _addRemoteRepository;
    private final List<RemoteRepositoryDescription> _remoteRepository;

    public MavenConfiguration(
            boolean createParentPom,
            boolean addRemoteRepository,
            String groupIdParentPom,
            String artifactIdParentPom,
            String versionParentPom,
            String groupId,
            String artifactId,
            String version,
            List<RemoteRepositoryDescription> remoteRepository
    ) {
        _createParentPom = createParentPom;
        _addRemoteRepository = addRemoteRepository;
        _groupIdParentPom = groupIdParentPom;
        _artifactIdParentPom = artifactIdParentPom;
        _versionParentPom = versionParentPom;
        _groupId = groupId;
        _artifactId = artifactId;
        _version = version;
        _remoteRepository = remoteRepository;
    }

    public String getGroupId() {
        return _groupId;
    }

    public String getArtifactId() {
        return _artifactId;
    }

    public String getVersion() {
        return _version;
    }

    public String getArtifactIdParentPom() {
        return _artifactIdParentPom;
    }

    public String getGroupIdParentPom() {
        return _groupIdParentPom;
    }

    public String getVersionParentPom() {
        return _versionParentPom;
    }

    public boolean isCreateParentPom() {
        return _createParentPom;
    }

    public boolean isAddRemoteRepository() {
        return _addRemoteRepository;
    }

    public List<RemoteRepositoryDescription> getRemoteRepositoryList() {
        return _remoteRepository;
    }
}
