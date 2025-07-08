import OneMoreComponent from './OneMoreComponent.vue'
import DComponent from './d-component.vue'
Vue.mixin({
  components: { OneMoreComponent },
  mixins: [ DComponent ]
})