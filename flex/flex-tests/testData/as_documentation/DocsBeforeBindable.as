package {
public class DocsBeforeBindable {

  /**
   *  Gets the value of a specified resource as a Class.
   *  @return The resource value, as a <code>Class</code>,
   *  or <code>null</code> if it is not found.
   */
  [Bindable("change")]
  
      public function getC<caret>lass(bundleName:String, resourceName:String,
                        locale:String = null):Class;


}
}