<table width="100%" class="table">

    <tr>
        <th style="width: 200px;">Field</th>
        <th>Old Value</th>
        <th>New Value</th>
    </tr>


<cfloop from="1" to="#arraylen( arguments.changedFields )#" index="a">
    <tr>
        <td>#arguments.changedFields[a].field#</td>
        <td>#arguments.changedFields[a].oldValue#</td>
        <td>#arguments.changedFields[a].newValue#</td>
    </tr>
<<caret>



</table>
