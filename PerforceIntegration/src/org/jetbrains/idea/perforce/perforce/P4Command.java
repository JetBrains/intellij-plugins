package org.jetbrains.idea.perforce.perforce;

import org.jetbrains.annotations.NotNull;

public enum P4Command {
  add("add"),
  ignores("ignores"),
  edit("edit"),
  revert("revert"),
  delete("delete"),
  integrate("integrate"),
  reopen("reopen"),
  clients("clients"),
  users("users"),
  user("user"),
  describe("describe"),
  change("change"),
  changes("changes"),
  opened("opened"),
  submit("submit"),
  filelog("filelog"),
  sync("sync"),
  dirs("dirs"),
  files("files"),
  jobs("jobs"),
  resolved("resolved"),
  resolve("resolve"),
  move("move"),
  unknown("unknown");

  private final String myName;

  P4Command(String name) {
    myName = name;
  }

  public String getName() {
    return myName;
  }

  @NotNull
  public static P4Command getInstance(final String command) {
    final String trimmed = command.trim();
    final P4Command[] p4Commands = values();
    for (P4Command p4Command : p4Commands) {
      if (p4Command.getName().equals(trimmed)) {
        return p4Command;
      }
    }
    return unknown;
  }
}
