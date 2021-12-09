export default {
  // Auto import components (https://go.nuxtjs.dev/config-components)
  components: [
    "~/components",
    {path: "components/special//", extensions: ["vue"]},
    {path: "@/prefixed/", prefix: "prefixed"}
  ]
}
