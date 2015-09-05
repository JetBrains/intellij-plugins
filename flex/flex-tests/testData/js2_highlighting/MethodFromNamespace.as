package {
import mx.controls.treeClasses.TreeItemRenderer;
import mx.core.mx_internal;

use namespace mx_internal;

public class MethodFromNamespace {

    public function test() {
        var treeRenderer:TreeItemRenderer;
        treeRenderer.getLabel();
    }
}
}