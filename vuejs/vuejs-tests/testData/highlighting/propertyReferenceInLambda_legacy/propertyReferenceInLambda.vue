<template>
  <button @click="() => foo = 7">Button</button>
  <button @click="() => <weak_warning descr="Unresolved variable or type bar"><weak_warning descr="Variable bar implicitly declared">bar</weak_warning></weak_warning> = 7">Button</button>
</template>

<script>
export default {
  data() {
    return {
      foo: 0,
    };
  },
};
</script>
