<%@ taglib prefix="s" uri="/struts-tags" %>

<%-- valid --%>
<s:url action="namespace1Action"/>
<s:a action="namespace1Action"/>
<s:url action="namespace2Action"/>
<s:url action="namespace1Action" namespace="/namespace1"/>
<s:url action="namespace2Action" namespace="/namespace2"/>

<s:url action="myWildCard" namespace="/wildcard"/>
<s:url action="myWildCardAnythingGoesHere" namespace="/wildcard"/>

<%-- "method" --%>
<s:url action="bangAction" namespace="/bang" method="methodB"/>
<s:url action="bangAction" namespace="/bang" method="<error>INVALID_VALUE</error>"/>


<%-- bang-notation --%>
<s:url action="bangAction!methodA" namespace="/bang"/>
<s:url action="bangAction!methodB" namespace="/bang"/>

<s:url action="myWildCard%{anythingDynamic}"/>
<s:url action="%{anythingDynamic}"/>

<s:form action="namespace1Action"/>
<s:form action="<error>INVALID_VALUE</error>"/>
<s:submit action="namespace1Action"/>
<s:submit action="<error>INVALID_VALUE</error>"/>

<%-- invalid --%>
<s:url action="<error></error>"/>
<s:url action="<error>INVALID_VALUE</error>"/>
<s:a action="<error>INVALID_VALUE</error>"/>
<s:url action="<error>namespace2Action</error>" namespace="/namespace1"/>

<s:url action="bangAction!<error>INVALID_VALUE</error>" namespace="/bang"/>
<s:url action="namespace2Action!<error>INVALID_VALUE</error>" namespace="/namespace2"/>
