<template>
    <div>
        {{this.$options.name}}
        {{this.$data.name}}
        {{this.$props.name}}
        {{$options.name}}
        {{$data.name}}
        {{$props.name}}
    </div>
</template>
<script>
import Vue from "vue";

export const Mixins = {
    customStuff: 12,
    props: {
        mixinProp: {
            type: String,
            required: true
        }
    },
    data: () => {
        return {mixinData: 'foo'}
    }
}

export default Vue.extend({
                              name: "Test",
                              customOption: "Foo",
                              mixins: [Mixins],
                              data: () => {
                                  return {
                                      foo: 12,
                                      $foo: 34
                                  }
                              },
                              props: {
                                  aProp: Number
                              },
                              computed: {
                                  check() {
                                      this.$options.foo
                                      this.$data.foo
                                      this.$props.foo
                                      return 12
                                  }
                              }
                          });
</script>
