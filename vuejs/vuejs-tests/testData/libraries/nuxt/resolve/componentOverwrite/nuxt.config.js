export default {
  // Auto import components (https://go.nuxtjs.dev/config-components)
  components: [
    "level0",
    {path: "node_mods/my-theme/level1", level: 1},
    "node_mods/my-theme/deep",
  ]
}
