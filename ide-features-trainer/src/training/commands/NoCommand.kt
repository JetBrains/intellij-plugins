package training.commands


class NoCommand : Command(Command.CommandType.NOCOMMAND) {

  override fun execute(executionList: ExecutionList) {
    //do nothing
  }
}
