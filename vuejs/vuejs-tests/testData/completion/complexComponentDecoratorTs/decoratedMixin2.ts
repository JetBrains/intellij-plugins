import {Component, Prop} from "vue-property-descriptor"

@Component()
export default class {
  @Prop() protected decoratedMixinProp2
}
