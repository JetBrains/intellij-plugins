<%@ taglib prefix="x" uri="/customTag" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:url var="test_url" action="test"/>
<x:something href="${test_url}">Link</x:something>