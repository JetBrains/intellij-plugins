@Component({
             template: `
              <div class="checkbox-wrapper" @click="check">
                  <div :class="{ checkbox: true, checked: checked }"></div>
                  <div class="title">{{ title + foo }}</div>
              </div>
             `
           })
export default class {
  checked = false
  @Prop() title = 'Check me'

  check() {
    this.checked = !this.checked;
  }
}
