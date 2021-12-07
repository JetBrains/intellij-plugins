<script>
export default {
  data() {
    return {
      color: 'red'
    }
  },
  props: {
    bar: Boolean
  }
}
</script>

<style>
.text {
  color: v-bind(<caret>);
}
</style>
