<%-- "normal" links --%>
<a href="actionLink-highlighting.jsp"/>
<a href="/"/>

<%-- VALID Action Links --%>
<a href="rootActionLink.action"/>
<a href="/rootActionLink.action"/> 
<a href="/actionLink/actionLink1.action"/>
<a href="/actionLink/actionLink2.action"/>

<%-- INVALID Action Links --%>
<a href="<warning descr="Cannot resolve Struts 2 package '/INVALID_VALUE'">/INVALID_VALUE/</warning><warning></warning>"/>
<a href="/actionLink/<warning>INVALID_VALUE.action</warning>"/>


<%-- Action Links with dynamic context --%>
<a href="<%=request.getContextPath()%>/actionLink/actionLink1.action"/>
<%-- TODO slash after EL breaks test --%>
<a href="${pageContext.request.contextPath}actionLink/actionLink2.action"/>
