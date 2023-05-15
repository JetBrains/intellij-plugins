
<script>
export default {
  name: 'DemoWatch',
  data() {
    return {
      question: '',
      answer: 'Questions usually contain a question mark. ;-)'
    }
  },
  computed: {
    doubleAge()
    {
      return {a: 12, b: 12};
    }
  },
  watch: {
    "doubleAge.a<caret>"
  }
};
</script>