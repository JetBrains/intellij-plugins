<cfquery>
<cfinclude />
SELECT * FROM Bar inner join (select C1, C2 from tab where Cc = 
  <cfqueryparam cfsqltype="cf_sql_integer" value="#test#"/>) where 1=1
</cfquery>