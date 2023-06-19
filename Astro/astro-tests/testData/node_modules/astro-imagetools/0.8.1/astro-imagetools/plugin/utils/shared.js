// @ts-check

export function getConfigOptions(config, ext, imageWidth) {
  const { w, width = w, format = ext, base64, raw, inline, ...rest } = config;

  const imageFormat = format === "jpeg" ? "jpg" : format;

  const widths = width
    ? width.split(";").map((w) => parseInt(w))
    : [imageWidth];

  const extension = format === "jpg" ? "jpeg" : format;
  const type = `image/${extension}`;

  const options = {
    format: imageFormat,
    ...rest,
  };

  return {
    type,
    widths,
    options,
    extension,
    raw: typeof raw === "string",
    inline: typeof base64 === "string" || typeof inline === "string",
  };
}

export function getAssetPath(base, assetFileNames, ext, width, hash) {
  const regexExecArray = /(?<=\[hash:)\d+(?=\])/g.exec(assetFileNames),
    hashLength = regexExecArray ? regexExecArray[0] : 8,
    extname = `.${ext}`,
    name = base;

  width = width + "w";
  hash = hash.slice(0, hashLength);

  const assetPath = assetFileNames
    .replace("[name]", name)
    .replace("[width]", width)
    .replace(regexExecArray ? `[hash:${hashLength}]` : "[hash]", hash)
    .replace("[ext]", ext)
    .replace("[extname]", extname);

  return assetPath;
}
