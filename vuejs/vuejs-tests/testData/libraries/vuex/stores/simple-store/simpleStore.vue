<script>
export default {
  methods: {
    foo(){
      this.$store.dispatch('action1', {}})
      this.$store.getters['bar']
      this.$store.state.state1.toString()
    }
  }
}
</script>
