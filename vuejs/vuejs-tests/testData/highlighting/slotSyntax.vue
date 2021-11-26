<template>
  <div>
    <div slot="name" slot-scope="propName">
      {{ propName + <weak_warning descr="Unresolved variable or type wrongName">wrongName</weak_warning> }}
    </div>
    <div slot="name" slot-scope="{prop1, prop2}">
      {{ prop1 }}
    </div>
    <div slot="name"></div>
    <div slot-scope="propName"></div>
    <div slot <warning descr="slot-scope requires value">slot-scope</warning>></div>
    <div <warning descr="Attribute scope is not allowed here">scope</warning>="foo"></div>
    <template scope="foo"></template>
  </div>
</template>