<%@ taglib prefix="s" uri="/struts-tags" %>

<%--
  ~ Copyright 2011 The authors
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%-- <s:param> --%>
<s:action name="myAction">
  <b>something else between</b>
  <s:param name="myField"/>
  <s:param name="myBooleanField"/>
  <s:param name="mySetterOnlyField"/>
  <s:param name="<error>INVALID_VALUE</error>"/>
</s:action>

<s:a action="myAction">
  <s:param name="myField"/>
</s:a>

<s:action name="%{dynamicUrl}">
  <s:param name="INVALID_VALUE_WONT_BE_HIGHLIGHTED"/>
</s:action>