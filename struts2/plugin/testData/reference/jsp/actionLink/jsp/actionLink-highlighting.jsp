<%-- "normal" links --%>
<a href="actionLink-highlighting.jsp"/>
<a href="/"/>
<a href="/index.jsp"></a>

<%-- VALID Action Links --%>
<a href="rootActionLink.action"/>
<a href="/rootActionLink.action"/> 
<a href="/actionLink/actionLink1.action"/>
<a href="/actionLink/actionLink2.action"/>

<%-- INVALID Action Links --%>
<a href="/actionLink/<warning descr="Cannot resolve file 'INVALID_VALUE.action'">INVALID_VALUE.action</warning>"/>
<a href="<warning descr="Cannot resolve file 'INVALID_VALUE.action'">INVALID_VALUE.action</warning>"/>
<a href="/<warning descr="Cannot resolve file 'INVALID_VALUE.action'">INVALID_VALUE.action</warning>"/>


<%-- Action Links with dynamic context --%>
<a href="<%=request.getContextPath()%>/actionLink/actionLink1.action"/>
<a href="${pageContext.request.contextPath}/actionLink/actionLink2.action"/>
