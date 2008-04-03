<%@ taglib prefix="s" uri="/struts-tags" %>

<%-- relative paths ============== --%>

<s:include value="paths.jsp"/>
<%-- TODO IDEA-bug ? reference provider does not even get called here --%>
<s:url value="<error>INVALID_VALUE</error>"/>

<s:url value="paths.jsp"/>
<s:url value="<error>INVALID_VALUE</error>"/>

<s:submit src="paths.jsp"/>
<s:submit src="<error>INVALID_VALUE</error>"/>