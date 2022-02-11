<template>
  {{ <caret> }}
</template>
<script>
  @Component({
   computed: {
     ...mapState("foo", ["a", "b"])
   },
   methods: {
     ...mapMutations([
       'increment',
       'incrementBy'
     ])
   }
 })
  export default class MyComp extends Vue {
    @Prop("foo2") propFoo
    @State('foo') stateFoo
    @State(state => state.bar) stateBar
    @Getter('foo') getterFoo
    @Action('foo') actionFoo
    @Mutation('foo') mutationFoo
    @someModule.Getter('foo') moduleGetterFoo

    // If the argument is omitted, use the property name
    // for each state/getter/action/mutation type
    @State fru
    @Getter bar
    @Action baz
    @Mutation qux

  }
</script>
