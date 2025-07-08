<script>
export default {
  name: 'test',
  data() {
    return {testItem: 10}
  },
  props : {
    props1: true
  },
  methods: {
    method1() {}
  },
  computed: {
    dataData() {this.<caret> }
  }
}
</script>