package icons;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * NOTE THIS FILE IS AUTO-GENERATED
 * DO NOT EDIT IT BY HAND, run "Generate icon classes" configuration instead
 */
public class IdetalkCoreIcons {
  private static Icon load(String path) {
    return IconLoader.getIcon(path, IdetalkCoreIcons.class);
  }

  public static final Icon CodePointer = AllIcons.Nodes.Tag;
  public static final Icon EditSource = AllIcons.Actions.EditSource;

  public static class IdeTalk {
    public static final Icon Away = load("/ideTalk/away.svg"); // 16x16
    public static final Icon Jabber = load("/ideTalk/jabber.svg"); // 16x16
    public static final Icon Jabber_dnd = load("/ideTalk/jabber_dnd.svg"); // 16x16
    public static final Icon Notavailable = load("/ideTalk/notavailable.svg"); // 16x16
    public static final Icon Offline = load("/ideTalk/offline.svg"); // 16x16
    public static final Icon User = load("/ideTalk/user.svg"); // 16x16
    public static final Icon User_dnd = load("/ideTalk/user_dnd.svg"); // 16x16
    public static final Icon User_toolwindow = load("/ideTalk/user_toolwindow.svg"); // 13x13
  }

  public static final Icon Message = load("/message.svg"); // 16x16

  public static class Nodes {
    public static final Icon Group_close = load("/nodes/group_close.svg"); // 16x16
    public static final Icon Group_open = Group_close;
    public static final Icon Unknown = AllIcons.Nodes.Unknown;
  }

  public static final Icon Stacktrace = load("/stacktrace.svg"); // 16x16
}
