<template>
  <div class="hello">

    <!--  Fixme: msg should have an error, because its not being returned from setup and its not on the props-->
    <h1>{{ msg }}</h1>

    {{ list }}

  </div>
</template>

<script>
import { computed } from 'vue';

let foo = {
  msg: 12
}

export default {
  name: 'HelloWorld',
  props: {
    filterString: String,
    whatever: String,
    anotherFilter: String,
    longMessage: String,
  },
  setup (props) {

    const list = computed(() => {

      if (props.filterString.trim().length === 0) return;
      if (props.whatever.trim().length === 0) return;
      if (props.whatever.<weak_warning descr="Unresolved function or method trm()">trm</weak_warning>().length === 0) return;

      if (props.<weak_warning descr="Unresolved variable msg">msg</weak_warning>.trim().length === 0) return;
      if (props.<weak_warning descr="Unresolved variable filter">filter</weak_warning>.trim().length === 0) return;

      return 'xx';
    });

    return {
      list,
    };
  }

};
</script>
