// @ts-check

export default function config() {
  return {
    optimizeDeps: {
      exclude: ["@astropub/codecs", "imagetools-core", "sharp"],
    },
    ssr: {
      external: [
        "sharp",
        "potrace",
        "file-type",
        "object-hash",
        "find-cache-dir",
        "@astropub/codecs",
      ],
    },
  };
}
