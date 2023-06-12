<script>
export default {
  inject: {
    message: {}, // shouldn't resolve here from the alias
    localMessage: {
      from: 'message'
    }
  }
}
</script>