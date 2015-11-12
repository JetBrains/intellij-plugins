package training.editor;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.BidirectionalMap;

import java.util.List;

/**
 * Created by karashevich on 28/10/15.
 */
public class EduEditorManager {

    BidirectionalMap<VirtualFile, EduEditor> fileEduEditorMap;

    public static EduEditorManager getInstance() {
        return ServiceManager.getService(EduEditorManager.class);
    }


    public EduEditorManager() {
        fileEduEditorMap = new BidirectionalMap<VirtualFile, EduEditor>();
    }


    public void registerEduEditor(EduEditor eduEditor, VirtualFile vf) {
        fileEduEditorMap.put(vf, eduEditor);
    }

    public void disposeEduEditor(EduEditor eduEditor) {
        final List<VirtualFile> keysByValue = fileEduEditorMap.getKeysByValue(eduEditor);
        if (keysByValue != null)
            for (VirtualFile virtualFile : keysByValue)
                fileEduEditorMap.remove(virtualFile);
    }

    public EduEditor[] getAllNotDisposedEduEditors() {
        EduEditor[] allNotDisposedEduEditors = new EduEditor[fileEduEditorMap.size()];
        return fileEduEditorMap.values().toArray(allNotDisposedEduEditors);
    }

}
