<%@ taglib prefix="s" uri="/struts-tags" %>

<%-- common attributes ============== --%>

<%-- "disabled" --%>
<s:textfield disabled="true"/>
<s:textfield disabled="false"/>
<s:textfield disabled="<error>INVALID_VALUE</error>"/>

<%-- "labelposition" --%>
<s:textfield disabled="true"/>
<s:textfield disabled="false"/>
<s:textfield disabled="<error>INVALID_VALUE</error>"/>

<%-- "requiredposition" --%>
<s:textfield requiredposition="left"/>
<s:textfield requiredposition="right"/>
<s:textfield requiredposition="<error>INVALID_VALUE</error>"/>

<%-- "readonly" --%>
<s:textfield readonly="true"/>
<s:textfield readonly="false"/>
<s:textfield readonly="<error>INVALID_VALUE</error>"/>

<%-- "emptyOption" --%>
<s:doubleselect emptyOption="true"/>
<s:doubleselect emptyOption="false"/>
<s:doubleselect emptyOption="<error>INVALID_VALUE</error>"/>

<%-- "doubleEmptyOption" --%>
<s:doubleselect doubleEmptyOption="true"/>
<s:doubleselect doubleEmptyOption="false"/>
<s:doubleselect doubleEmptyOption="<error>INVALID_VALUE</error>"/>

<%-- "multiple" --%>
<s:doubleselect multiple="true"/>
<s:doubleselect multiple="false"/>
<s:doubleselect multiple="<error>INVALID_VALUE</error>"/>

