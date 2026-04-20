<script>
export default {
  name: "Foo",
  computed: {
    todos () {
      const count = this.$store.state.counter //  Can be found
      const list = this.$store.state.todos.list //　Not found
      return list
    }
  }
}
</script>