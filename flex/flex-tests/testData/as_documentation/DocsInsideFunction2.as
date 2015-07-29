package {
public class DocsInsideFunction2 {

  [Bindable("change")]

      /**
       *  Gets the value of a specified resource as a Class.
       *  @return The resource value, as a <code>Class</code>,
       *  or <code>null</code> if it is not found.
       */
      function getC<caret>lass(bundleName:String, resourceName:String,
                        locale:String = null):Class;


}
}