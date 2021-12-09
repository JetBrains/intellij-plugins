import decoratedMixin2 from "./decoratedMixin2";
import {Options, Prop} from "vue-class-component"

@Options
export default class Foo extends decoratedMixin2 {
  @Prop() private decoratedMixinProp!: boolean
}
