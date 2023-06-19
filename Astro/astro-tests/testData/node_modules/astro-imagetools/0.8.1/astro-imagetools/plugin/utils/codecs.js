// @ts-check
import fs from "node:fs";
import * as codecs from "@astropub/codecs";

const resizedImages = new Map();

export const getLoadedImage = async (src, ext) => {
  const buffer = fs.readFileSync(src);

  const image = await codecs[ext].decode(buffer);

  const { width } = image;

  const resizedImageKey = `${src}@${image.width}`;

  resizedImages.set(resizedImageKey, image);

  return { image, width };
};

export const getTransformedImage = async ({ src, image, config, type }) => {
  const { width, format, quality } = config;

  const resizedImageKey = `${src}@${width}`;

  const resizedImage =
    resizedImages.get(resizedImageKey) ||
    resizedImages
      .set(resizedImageKey, await image.resize({ width }))
      .get(resizedImageKey);

  const encodedImage = quality
    ? await codecs[format].encode(resizedImage, {
        quality: parseInt(quality),
      })
    : await resizedImage.encode(type);

  const buffer = Buffer.from(encodedImage.data);

  return { image, buffer };
};
