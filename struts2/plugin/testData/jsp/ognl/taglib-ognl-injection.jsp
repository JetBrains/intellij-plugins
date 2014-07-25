<%@ taglib prefix="s" uri="/struts-tags" %>

<%-- generic attribute --%>
<<info descr="null">s</info>:url action="<inject descr="null">%{1 + 2}</inject>"/>

<%-- no prefix necessary --%>
<<info descr="null">s</info>:iterator value="<inject descr="null">1 + 2</inject>"/>

<%-- list expression --%>
<<info descr="null">s</info>:select label="label" name="name" list="<inject descr="null">{1, 2, 3}</inject>" />

<%-- map expression --%>
<<info descr="null">s</info>:select label="label" name="name" list="<inject descr="null">#{'foo':'foovalue', 'bar':'barvalue'}</inject>" />

