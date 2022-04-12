it('should work for a constructor mixin', () => {
  const spy = jasmine.createSpy('global mixin')
  const Mixin = Vue.extend({
    created() {
      spy(this.$options.myOption)
    }
  })

  Vue.mixin(Mixin)
})
