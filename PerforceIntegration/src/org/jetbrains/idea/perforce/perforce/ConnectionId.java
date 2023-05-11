package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

@Tag("ID")
public class ConnectionId {
  public boolean myUseP4Config;
  public String myP4ConfigFileName;
  public String myWorkingDir;

  public ConnectionId(@Nullable final String p4ConfigFileName, @NotNull final String workingDir) {
    myP4ConfigFileName = p4ConfigFileName;
    myWorkingDir = workingDir;
    myUseP4Config = true;
  }

  // singleton connection constructor
  public ConnectionId() {
    myUseP4Config = false;
    myP4ConfigFileName = null;
    myWorkingDir = null;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final ConnectionId that = (ConnectionId)o;

    if (!myUseP4Config) {
      return !that.myUseP4Config;
    } else {
      if (myP4ConfigFileName != null ? !myP4ConfigFileName.equals(that.myP4ConfigFileName) : that.myP4ConfigFileName != null) return false;
      return !(myWorkingDir != null ? !myWorkingDir.equals(that.myWorkingDir) : that.myWorkingDir != null);
    }


  }

  public int hashCode() {
    if (!myUseP4Config) {
      return 0;
    } else {
      int result = 0;
      result = 29 * result + (myP4ConfigFileName != null ? myP4ConfigFileName.hashCode() : 0);
      result = 29 * result + (myWorkingDir != null ? myWorkingDir.hashCode() : 0);
      return result;
    }
  }

  public void writeToStream(final DataOutput stream) throws IOException {
    stream.writeByte(!myUseP4Config ? 0 : 1);
    if (myUseP4Config) {
      stream.writeUTF(StringUtil.notNullize(myP4ConfigFileName));
      stream.writeUTF(Objects.requireNonNull(myWorkingDir));
    }
  }

  public static ConnectionId readFromStream(final DataInput stream) throws IOException {
    byte useP4Config = stream.readByte();
    if (useP4Config == 0) {
      return new ConnectionId();
    }
    String configFileName = stream.readUTF();
    String workingDir = stream.readUTF();
    return new ConnectionId(configFileName.isEmpty() ? null : configFileName, workingDir);
  }
}
