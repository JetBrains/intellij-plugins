<cfcomponent><fold text='...' expand='true'>
  <cffunction><fold text='...' expand='false'>
    <cfset a = 0>
    <cfset a = 1>
  </fold></cffunction>

  <cffunction><fold text='...' expand='false'>
    <cfset a = 0>
    <cfset a = 1>
  </fold></cffunction>

  <cfscript><fold text='...' expand='true'>
    function MyFunction() <fold text='{...}' expand='false'>{
      a = 10;
    }</fold>
  </fold></cfscript>
</fold></cfcomponent>