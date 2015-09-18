package training.commands;

/**
 * Created by karashevich on 18/09/15.
 */
public class BadCommandException extends Exception {

    public BadCommandException(Command command) {
        super("exception in command " + command.getCommandType().toString());
    }

    public BadCommandException(String s) {
        super(s);
    }
}
