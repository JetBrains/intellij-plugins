<%@ taglib prefix="s" uri="/struts-tags" %>

<%-- valid --%>
<s:url action="namespace1Action"/>
<s:url action="namespace1Action" namespace="/namespace1"/>
<s:url action="namespace2Action" namespace="/namespace2"/>
<s:a action="namespace2Action" namespace="/namespace2"/>

<%-- invalid --%>
<s:url action="<error>namespace1Action</error>" namespace="<error></error>"/>
<s:url action="<error>namespace1Action</error>" namespace="<error>INVALID_VALUE</error>"/>
<s:a action="<error>namespace1Action</error>" namespace="<error>INVALID_VALUE</error>"/>