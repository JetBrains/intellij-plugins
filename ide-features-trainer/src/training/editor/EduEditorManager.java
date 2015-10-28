package training.editor;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.containers.ArrayListSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by karashevich on 28/10/15.
 */
public class EduEditorManager {

    private ArrayListSet<EduEditor> eduEditors;

    public static EduEditorManager getInstance() {
        return ServiceManager.getService(EduEditorManager.class);
    }


    public EduEditorManager() {
        eduEditors = new ArrayListSet<EduEditor>();
    }


    public void registerEduEditor(EduEditor eduEditor){
        eduEditors.add(eduEditor);
    }

    public void disposeEduEditor(EduEditor eduEditor){
        eduEditors.remove(eduEditor);
    }

    public EduEditor[] getAllNotDisposedEduEditors(){
        EduEditor[] allNotDisposedEduEditors = new EduEditor[eduEditors.size()];
        return eduEditors.toArray(allNotDisposedEduEditors);
    }
}
