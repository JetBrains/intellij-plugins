<cfif regexp.len[1] EQ 0 OR SCRIPT_NAME EQ "/adm/index.cfm">
  <cflocation url="/adm/login.cfm">
  <cfelse>
  <cfset dir = mid(SCRIPT_NAME, regexp.pos[2], regexp.len[2])>
</cfif>