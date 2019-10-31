package training.commands

class BadCommandException : Exception {

  constructor(command: Command) : super("exception in command " + command.commandType.toString())
  constructor(s: String) : super(s)
}
