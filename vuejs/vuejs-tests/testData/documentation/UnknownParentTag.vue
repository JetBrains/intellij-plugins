<template>
  <div >
    <Foo123
      v-f<caret>or="foo of bar"
    >
      {{foo}}
      <span v-if="foo !== 'd'">
        {{boo}}
      </span>
    </Foo123>
  </div>
</template>