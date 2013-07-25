<cfscript>
component {
  public void function someFunction(required string param="defaultValue") {
    var a = par<caret>am; 
  }
}
</cfscript>