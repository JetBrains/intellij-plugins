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
  color: <error descr="Unknown function 'v-bind'">v-bind</error>(color);
}
.foo {
  color: <error descr="Unknown function 'v-binds'">v-binds</error>(color);
}
</style>
