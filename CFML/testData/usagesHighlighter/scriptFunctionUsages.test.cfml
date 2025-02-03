<cfscript>
function myFun<caret>ction() {
  myFunction();
}
</cfscript>

<cfinvoke method="myFunction">

<cffunction name="myFunction2">
  <cfinvoke method="myFunction">
</cffunction>