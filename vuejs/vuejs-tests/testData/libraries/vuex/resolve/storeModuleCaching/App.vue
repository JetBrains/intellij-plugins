<script>
export default {
  computed: {
    trackCounter() {
      return this.$store.getters['counterMod/get<caret>Counter']
    }
  }
}
</script>
