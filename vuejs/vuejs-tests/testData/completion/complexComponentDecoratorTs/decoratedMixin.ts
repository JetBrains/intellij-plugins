import decoratedMixin2 from "./decoratedMixin2";
import {Component, Prop} from "vue-property-descriptor"

@Component
export default class Foo extends decoratedMixin2 {
  @Prop() private decoratedMixinProp!: boolean
}
