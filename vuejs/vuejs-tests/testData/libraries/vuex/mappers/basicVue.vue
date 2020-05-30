<template>
  {{ <caret> }}
</template>
<script>
  export default {
    computed: {
      comp1: () => "foo",
      ...mapState("foo", ["a", "b"]),
      ...mapState(["c"]),
      ...mapState({
                    count: state => state.count,
                    countAlias: 'count',
                    countPlusLocalState(state) {
                      return state.count + this.localCount
                    }
                  }),
      ...mapGetters("bar", [
        'doneTodosCount',
        'anotherGetter',
      ]),
      ...mapGetters({
                      doneCount: 'doneTodosCount2'
                    }),
    },
    methods: {
      ...mapMutations([
                        'increment',
                        'incrementBy'
                      ]),
      ...mapMutations({
                        add: 'increment'
                      }),
      ...mapActions([
                      'incrementAction',
                      'incrementByAction'
                    ]),
      ...mapActions({
                      addAction: 'increment'
                    })
    }
  }
</script>
