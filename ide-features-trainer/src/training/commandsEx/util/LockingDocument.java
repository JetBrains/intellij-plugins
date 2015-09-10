package training.commandsEx.util;

import com.intellij.openapi.editor.impl.*;
import org.jetbrains.annotations.NotNull;


/**
 * Created by karashevich on 05/02/15.
 */
public class LockingDocument extends DocumentImpl{

    public LockingDocument(@NotNull CharSequence chars) {
        super(chars);
    }

    public LockingDocument(@NotNull CharSequence chars, boolean acceptSlashR, boolean forUseInNonAWTThread) {
        super(chars, acceptSlashR, forUseInNonAWTThread);
    }

    public LockingDocument(@NotNull CharSequence chars, boolean forUseInNonAWTThread) {
        super(chars, forUseInNonAWTThread);
    }

    public LockingDocument(@NotNull String text) {
        super(text);
    }
}
