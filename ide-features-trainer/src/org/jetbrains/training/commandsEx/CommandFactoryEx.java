package org.jetbrains.training.commandsEx;

import org.jdom.Element;

/**
 * Created by karashevich on 30/01/15.
 */
public class CommandFactoryEx {

    public static CommandEx buildCommand(Element element){

        CommandEx.CommandType cmdType = null;
        String tagName = element.getName();

        if (tagName == null) return new NoCommandEx(); //Potential exception leak here

        for (CommandEx.CommandType ct: CommandEx.CommandType.values()){
            if (tagName.toUpperCase().equals(ct.toString().toUpperCase())) {
                cmdType = ct;
                break;
            }
        }

        if (cmdType == null) return new NoCommandEx();

        switch (cmdType) {
            case START:
                return new StartCommandEx();

            case TEXT:
                return new TextCommandEx();

            case MOVECARET:
                return new MoveCaretCommandEx();

            case NOCOMMAND:
                return new NoCommandEx();

            case TRY:
                return new TryCommandEx();

            case TRYBLOCK:
                return new TryBlockCommandEx();

            case COPYTEXT:
                return new CopyTextCommandEx();

            case ACTION:
                return new ActionCommandEx();

            case REPLAY:
                return new ReplayCommandEx();

            case TYPETEXT:
                return new TypeTextCommandRx();

            case TRAVERSECARET:
                return new TraverseCaretCommandEx();

            case WAIT:
                return new WaitCommandEx();

            case WIN:
                return new WinCommandEx();

            case MOUSEBLOCK:
                return new MouseBlockCommandEx();

            case MOUSEUNBLOCK:
                return new MouseUnblockCommandEx();
        }

        return new NoCommandEx();

    }

}
