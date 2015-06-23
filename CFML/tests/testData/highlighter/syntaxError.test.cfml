<cfquery name="origData" datasource="Request.Site.DataSource">
    SELECT *
      FROM Whatever
     WHERE ID = <cfqueryparam value="1" cfsqltype="CF_SQL_INTEGER">
</cfquery>

<cfscript>
// #123: Don't blink
  foo = 1;
</cfscript>