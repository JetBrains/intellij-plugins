package com.intellij.flex.maven;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;

class WorkspaceReaderImpl implements WorkspaceReader {
  private static final String POM_EXTENSION = "pom";

  private final WorkspaceRepository repository = new WorkspaceRepository("ide", WorkspaceReaderImpl.class);

  private final HashMap<ArtifactKey, ArtifactData> map;
  private final GeneratorServer generatorServer;

  public WorkspaceReaderImpl(DataInputStream in, GeneratorServer generatorServer) throws IOException {
    this.generatorServer = generatorServer;
    
    final int mapSize = in.readUnsignedShort();
    map = new HashMap<ArtifactKey, ArtifactData>(mapSize);
    ObjectInputStream objectInputStream = new ObjectInputStream(in);
    try {
      for (int i = 0; i < mapSize; i++) {
        final ArtifactKey key = new ArtifactKey((String)objectInputStream.readObject(), (String)objectInputStream.readObject(),
                                                ((String)objectInputStream.readObject()));
        map.put(key, new ArtifactData(key, (String)objectInputStream.readObject()));
      }
    }
    catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  @Override
  public WorkspaceRepository getRepository() {
    return repository;
  }

  @Override
  public File findArtifact(Artifact artifact) {
    final ArtifactData data = map.get(new ArtifactKey(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
    if (data == null) {
       return null;
    }

    if (data.file == null) {
      data.file = new File(data.filePath);
    }

    if (!artifact.getExtension().equals(POM_EXTENSION)) {
      if (!(artifact.getExtension().equals("swc") || artifact.getExtension().equals("swf"))) {
        throw new IllegalStateException("Found artifact must be flash artifact (swc or swf)");
      }

      if (data.outputFile == null) {
        try {
          data.outputFile = generatorServer.getOutputFile(data.file);
        }
        catch (Exception e) {
          generatorServer.getLogger().warn("Error while resolve output file for " + artifact.toString(), e);
          return null;
        }
      }

      return data.outputFile;
    }

    return data.file;
  }

  @Override
  public List<String> findVersions(Artifact artifact) {
    return null;
  }

  private static class ArtifactKey {
    private final String groupId;
    private final String artifactId;
    private final String version;

    private ArtifactKey(String groupId, String artifactId, String version) {
      this.groupId = emptify(groupId);
      this.artifactId = emptify(artifactId);
      this.version = emptify(version);
    }

    private static String emptify(String str) {
      return str == null ? "" : str;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o instanceof ArtifactKey) {
        ArtifactKey other = (ArtifactKey)o;
        return groupId.equals(other.groupId) && artifactId.equals(other.artifactId) && version.equals(other.version);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return 31 * (31 * groupId.hashCode() + artifactId.hashCode()) + version.hashCode();
    }
  }

  private static class ArtifactData {
    public final ArtifactKey key;
    private final String filePath;

    private File file;
    private File outputFile;

    private ArtifactData(ArtifactKey key, String filePath) {
      this.filePath = filePath;
      this.key = key;
    }
  }
}
