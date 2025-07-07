let mixin = {
  props: {
    hi2dden: {}
  }
};

Vue.mixin(mixin);

Vue.mixin({
  props: {
    interestingProp: {},
    requiredMixinProp: {
      required: true
    }
  }
});