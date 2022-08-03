<script setup>
import {useUserStore} from './user'

const store = useUserStore()
store.init()
console.log(store.userInfo);

store.<weak_warning descr="Unresolved function or method otherInit()">otherInit</weak_warning>();
console.log(store.<weak_warning descr="Unresolved variable otherStateField">otherStateField</weak_warning>);
</script>

<template>
  <p>
    <button @click="store.init()">
      Click me!
    </button>
    User info: {{ store.userInfo }}
  </p>
</template>
