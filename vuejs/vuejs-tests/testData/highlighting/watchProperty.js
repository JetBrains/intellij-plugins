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
  },
};
