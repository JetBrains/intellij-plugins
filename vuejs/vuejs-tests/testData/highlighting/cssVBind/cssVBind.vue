<script>
export default {
  data() {
    return {
      color: 'red'
    }
  }
}
</script>

<style>
.text {
  color: v-bind(color);
}
.foo {
  color: <error descr="Unknown function 'v-binds'">v-binds</error>(color);
}
</style>
