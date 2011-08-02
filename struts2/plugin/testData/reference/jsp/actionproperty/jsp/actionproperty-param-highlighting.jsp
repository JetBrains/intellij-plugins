<%@ taglib prefix="s" uri="/struts-tags" %>

<%-- <s:param> --%>
<s:action name="myAction">
  <b>something else between</b>
  <s:param name="myField"/>
  <s:param name="myBooleanField"/>
  <s:param name="<error>INVALID_VALUE</error>"/>
</s:action>


<s:action name="%{dynamicUrl}">
  <s:param name="INVALID_VALUE_WONT_BE_HIGHLIGHTED"/>
</s:action>