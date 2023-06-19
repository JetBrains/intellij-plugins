// @ts-check
import fs from "node:fs";
import { extname } from "node:path";
import * as codecs from "@astropub/codecs";

export async function getImageDetails(path, width, height, aspect) {
  const extension = extname(path).slice(1);

  const imageFormat = extension === "jpeg" ? "jpg" : extension;

  const buffer = fs.readFileSync(path);
  const decodedImage = await codecs.jpg.decode(buffer);

  if (aspect && !width && !height) {
    if (!width && !height) {
      ({ width } = decodedImage);
    }

    if (width) {
      height = width / aspect;
    }

    if (height) {
      width = height * aspect;
    }
  }

  const image = await decodedImage.resize({ width, height });

  const { width: imageWidth, height: imageHeight } = image;

  return {
    image,
    imageWidth,
    imageHeight,
    imageFormat,
  };
}
