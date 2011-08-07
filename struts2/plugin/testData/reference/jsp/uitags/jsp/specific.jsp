<%@ taglib prefix="s" uri="/struts-tags" %>

<%-- specific attributes ============== --%>

<%-- <s:action> ======================= --%>
<s:action flush="true"/>
<s:action flush="false"/>
<s:action flush="<error>INVALID_VALUE</error>"/>

<s:action executeResult="true"/>
<s:action executeResult="false"/>
<s:action executeResult="<error>INVALID_VALUE</error>"/>

<s:action ignoreContextParams="true"/>
<s:action ignoreContextParams="false"/>
<s:action ignoreContextParams="<error>INVALID_VALUE</error>"/>


<%-- <s:date> ======================= --%>
<s:date nice="true"/>
<s:date nice="false"/>
<s:date nice="<error>INVALID_VALUE</error>"/>


<%-- <s:form> ======================= --%>
<s:form enctype="application/x-www-form-urlencoded"/>
<s:form enctype="multipart/form-data"/>
<s:form enctype="<error>INVALID_VALUE</error>"/>

<s:form validate="true"/>
<s:form validate="false"/>
<s:form validate="<error>INVALID_VALUE</error>"/>


<%-- <s:property> =================== --%>
<s:property escape="true"/>
<s:property escape="true"/>
<s:property escape="<error>INVALID_VALUE</error>"/>

<s:property escapeJavaScript="true"/>
<s:property escapeJavaScript="true"/>
<s:property escapeJavaScript="<error>INVALID_VALUE</error>"/>


<%-- <s:set> =================== --%>
<s:set scope="action"/>
<s:set scope="application"/>
<s:set scope="page"/>
<s:set scope="request"/>
<s:set scope="session"/>
<s:set scope="<error>INVALID_VALUE</error>"/>


<%-- <s:submit> =================== --%>
<s:submit type="button"/>
<s:submit type="image"/>
<s:submit type="input"/>
<s:submit type="submit"/>
<s:submit type="<error>INVALID_VALUE</error>"/>


<%-- <s:text> =================== --%>
<s:text searchValueStack="true"/>
<s:text searchValueStack="true"/>
<s:text searchValueStack="<error>INVALID_VALUE</error>"/>


<%-- <s:url> =================== --%>
<s:url encode="true"/>
<s:url encode="false"/>
<s:url encode="<error>INVALID_VALUE</error>"/>

<s:url escapeAmp="true"/>
<s:url escapeAmp="false"/>
<s:url escapeAmp="<error>INVALID_VALUE</error>"/>

<s:url forceAddSchemeHostAndPort="true"/>
<s:url forceAddSchemeHostAndPort="false"/>
<s:url forceAddSchemeHostAndPort="<error>INVALID_VALUE</error>"/>

<s:url includeContext="true"/>
<s:url includeContext="false"/>
<s:url includeContext="<error>INVALID_VALUE</error>"/>

<s:url includeParams="all"/>
<s:url includeParams="get"/>
<s:url includeParams="none"/>
<s:url includeParams="<error>INVALID_VALUE</error>"/>
