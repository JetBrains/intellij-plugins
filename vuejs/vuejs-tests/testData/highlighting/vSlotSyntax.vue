<template>
  <div>
    <div v-slot:name="propName">
      {{ propName + <weak_warning descr="Unresolved variable or type wrongName">wrongName</weak_warning> }}
    </div>
    <div v-slot:name="{prop1, prop2}">
      {{ prop1 + <weak_warning descr="Unresolved variable or type wrongName">wrongName</weak_warning>}}
    </div>
    <div v-slot:name></div>
    <div v-slot="propName"></div>
    <div v-slot></div>

    <div #name="propName"></div>
    <div #name></div>

    <div <warning descr="Attribute v-slots:name is not allowed here">v-slots:name</warning>="<weak_warning descr="Unresolved variable or type propName">propName</weak_warning>"></div>
    <div <warning descr="Attribute v-slots:name is not allowed here">v-slots:name</warning>></div>
    <div <warning descr="Attribute v-slots is not allowed here">v-slots</warning>="<weak_warning descr="Unresolved variable or type propName">propName</weak_warning>"></div>
    <div <warning descr="Attribute v-slots is not allowed here">v-slots</warning>></div>

    <div <warning descr="Attribute # is not allowed here">#</warning>="propName"></div>
    <div <warning descr="Attribute # is not allowed here">#</warning>></div>
  </div>
</template>