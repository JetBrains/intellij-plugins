<script>
  import {Component, Model, Prop, PropSync} from 'vue-property-decorator';

  @Component({
               props: {
                 foo_prop: Number
               },
               data: {
                 foo_data: String
               }
             })
  export default class extends mixins(Fooo) {
    foo = 12

    foo_prop = 23

    @Prop() bar;

    @PropSync('name') syncedName;

    @Model('change') checked

    @Emit()
    addToCount(n) {
      this.count += n
    }

    @Emit('reset')
    resetCount() {
      this.count = 0
    }

    getBar() {
      return 0;
    }

    get computedBar() {
      return 0;
    }

    set computedBar(value) {

    }

    set computedSetter(value) {

    }
  }
</script>
