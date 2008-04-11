<%-- "normal" links --%>
<a href="actionLink-highlighting.jsp"/>
<a href="/"/>

<%-- Action Links --%>
<a href="/actionLink/actionLink1.action"/>
<a href="/actionLink/actionLink2.action"/>
<a href="<warning>/actionLink/INVALID_VALUE.action</warning>"/>


<%-- Action Links with dynamic context --%>
<a href="<%=request.getContextPath()%>/actionLink/actionLink1.action"/>
<a href="${pageContext.request.contextPath}/actionLink/actionLink2.action"/>
