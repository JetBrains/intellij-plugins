package org.jetbrains.training;

import org.jdom.Element;
import org.jetbrains.training.commands.*;
/**
 * Created by karashevich on 30/01/15.
 */
public class CommandFactory {

    public Command buildCommand(Element element){

        Command.CommandType cmdType = null;
        String tagName = element.getName();

        if (tagName == null) return new NoCommand(); //Potential exception leak here

        for (Command.CommandType ct: Command.CommandType.values()){
            if (tagName.toUpperCase().equals(ct.toString().toUpperCase())) {
                cmdType = ct;
                break;
            }
        }

        if (cmdType == null) return new NoCommand();

        switch (cmdType) {
            case START:
                return new StartCommand();

            case TEXT:
                return new StartCommand();

            case MOVECARET:
                return new MoveCaretCommand();

            case NOCOMMAND:
                return new NoCommand();

            case TRY:
                return new TryCommand();

            case COPYTEXT:
                return new CopyTextCommand();

            case ACTION:
                return new ActionCommand();

        }

        return new NoCommand();

    }

}
