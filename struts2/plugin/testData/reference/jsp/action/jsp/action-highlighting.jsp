<%@ taglib prefix="s" uri="/struts-tags" %>

<%-- valid --%>
<s:url action="namespace1Action"/>
<s:url action="namespace2Action"/>
<s:url action="namespace1Action" namespace="/namespace1"/>
<s:url action="namespace2Action" namespace="/namespace2"/>

<s:url action="myWildCard" namespace="/wildcard"/>
<s:url action="myWildCardAnythingGoesHere" namespace="/wildcard"/>

<%-- invalid --%>
<s:url action="<error></error>"/>
<s:url action="<error>INVALID_VALUE</error>"/>