<script>
export default {
  mounted() {
    this.$store.dispatch('changeData', 'new data is nice');
    this.$store.dispatch('foo/changeData', 'foo data is nice');
  }
}
</script>

