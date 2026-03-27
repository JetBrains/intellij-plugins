export default {
  data() {
    return {
      galleryLimit: 3,
      activeMedia: {
        foo: 12,
      },
    };
  },

  computed: {
    currentDeviceView() {
      return {
        foo: 2,
        bar: 3,
      };
    },
    product() {
      return <weak_warning descr="Unresolved variable or type foo">foo</weak_warning>;
    },
  },

  watch: {
    'currentDeviceView.<warning descr="Unrecognized name">fodo</warning>': {

    },

    galleryLimit: {
      deep: true,
      handler() {},
    },

    'element.foo': {
      handler() {},
    },

    'product.fooBar': {
      handler() {},
    },
  },
};
