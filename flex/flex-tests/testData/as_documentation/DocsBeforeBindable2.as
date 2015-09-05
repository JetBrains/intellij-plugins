package {
public class DocsBeforeBindable2 {

  /**
   *  Gets the value of a specified resource as a Class.
   *  @return The resource value, as a <code>Class</code>,
   *  or <code>null</code> if it is not found.
   */
  [Bindable("change")]
  
      function getC<caret>lass(bundleName:String, resourceName:String,
                        locale:String = null):Class;


}
}