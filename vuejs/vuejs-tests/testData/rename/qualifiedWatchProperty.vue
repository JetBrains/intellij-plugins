
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
      return {<caret>a: 12, b: 12};
    }
  },
  watch: {
    "doubleAge.a" () {

    }
  }
};
</script>