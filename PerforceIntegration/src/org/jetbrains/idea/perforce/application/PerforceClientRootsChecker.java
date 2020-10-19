package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.PerforceAuthenticationException;
import org.jetbrains.idea.perforce.perforce.PerforceServerUnavailable;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;
import java.util.*;

/**
 * @author irengrig
 */
public class PerforceClientRootsChecker implements P4RootsInformation {
  private static final Logger LOG = Logger.getInstance(PerforceClientRootsChecker.class);
  private final Map<P4Connection, WrongRoots> myMap = new HashMap<>();
  private final MultiMap<P4Connection, VcsException> myErrors = new MultiMap<>();
  private final Set<P4Connection> myNotAuthorized = new HashSet<>();

  public PerforceClientRootsChecker() {}

  public PerforceClientRootsChecker(Map<P4Connection, ConnectionInfo> infoAndClient, final Map<VirtualFile, P4Connection> map) {
    if (map.containsKey(null)) {
      LOG.info("Null root: " + new LinkedHashMap<>(map));
    }

    final MultiMap<P4Connection, VirtualFile> inverse = invertConnectionMap(map);
    for (P4Connection connection : inverse.keySet()) {
      final Collection<VirtualFile> roots = inverse.get(connection);
      final List<String> clientRoots;
      try {
        clientRoots = getClientRoots(connection, infoAndClient);
      } catch (PerforceAuthenticationException e) {
        myNotAuthorized.add(connection);
        continue;
      } catch (VcsException e) {
        myErrors.putValue(connection, e);
        continue;
      }

      ContainerUtil.putIfNotNull(connection, checkRoots(roots, clientRoots), myMap);
    }
  }

  @Nullable
  private static WrongRoots checkRoots(Collection<VirtualFile> roots, List<String> clientRoots) {
    WrongRoots wrongRoots = null;

    for (VirtualFile root : roots) {
      if (root == null) {
        continue; // logged already at the recalculate method start
      }
      boolean checkedOk = false;
      final File ioFile = new File(root.getPath());
      for (String clientRoot : clientRoots) {
        if ("null".equals(clientRoot)) {
          checkedOk = true;
          break;
        }

        final File ioRoot = new File(clientRoot);
        if (isDirectory(ioRoot) && FileUtil.isAncestor(ioRoot, ioFile, false)) {
          checkedOk = true;
          break;
        }
      }
      if (!checkedOk) {
        if (wrongRoots == null) {
          wrongRoots = new WrongRoots(clientRoots);
        }
        wrongRoots.addWrong(root);
      }
    }
    return wrongRoots;
  }

  private static MultiMap<P4Connection, VirtualFile> invertConnectionMap(Map<VirtualFile, P4Connection> map) {
    final MultiMap<P4Connection, VirtualFile> inverse = new MultiMap<>();
    for (Map.Entry<VirtualFile, P4Connection> entry : map.entrySet()) {
      inverse.putValue(entry.getValue(), entry.getKey());
    }
    return inverse;
  }

  public boolean isServerUnavailable() {
    return ContainerUtil.findInstance(new ArrayList<>(myErrors.values()), PerforceServerUnavailable.class) != null;
  }

  public static boolean isDirectory(File file) {
    if (file.isDirectory()) return true;
    if (SystemInfo.isWindows) {
      String path = file.getPath();
      if (path.length() == 2 && Character.isLetter(path.charAt(0)) && ':' == path.charAt(1)) {
        return true;
      }
    }
    return false;
  }

  private static List<String> getClientRoots(P4Connection connection, Map<P4Connection, ConnectionInfo> infoMap) throws VcsException {
    ConnectionInfo info = infoMap.get(connection);
    ClientData clientSpec = info == null ? null : info.getClient();
    if (clientSpec == null) {
      throw new VcsException(PerforceBundle.message("error.no.client.specifications.loaded"));
    }
    return clientSpec.getAllRoots();
  }

  @Override
  public MultiMap<P4Connection, VcsException> getErrors() {
    return myErrors;
  }

  @Override
  public boolean hasAnyErrors() {
    return ! myMap.isEmpty() || ! myErrors.isEmpty();
  }

  @Override
  public boolean hasNotAuthorized() {
    return ! myNotAuthorized.isEmpty();
  }

  @Override
  public Set<P4Connection> getNotAuthorized() {
    return myNotAuthorized;
  }

  @Override
  public Map<P4Connection, WrongRoots> getMap() {
    return myMap;
  }

  // needed only for reporting
  public static class WrongRoots {
    private final List<String> myActualInClientSpec;
    private final List<VirtualFile> myWrong;

    public WrongRoots(List<String> actualInClientSpec) {
      myActualInClientSpec = actualInClientSpec;
      myWrong = new ArrayList<>();
    }

    public void addWrong(final VirtualFile file) {
      myWrong.add(file);
    }

    public List<String> getActualInClientSpec() {
      return myActualInClientSpec;
    }

    public List<VirtualFile> getWrong() {
      return myWrong;
    }
  }
}
