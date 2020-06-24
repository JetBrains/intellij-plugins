<template lang="pug">
  p(v-if="value.type === 'A'") qwerty
  p(v-if="asdf.type === 'A'") qwerty
  p(v-if="foo")
  p(v-if="12 <weak_warning descr="Condition is always false since types 'number' and 'string' have no overlap">===</weak_warning> 'A'") asdf
</template>

<script>
export class Thing {
  constructor(name) {
    this.name = name;
    this.type = "A";
  }
}

export default {
  props: {
    value: {
      type: Thing,
      required: true,
    },
    asdf: {
      type: Thing,
      required: true,
    },
  },
};
</script>