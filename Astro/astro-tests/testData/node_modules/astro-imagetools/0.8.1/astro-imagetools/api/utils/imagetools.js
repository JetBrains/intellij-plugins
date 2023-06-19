// @ts-check
import {
  builtins,
  loadImage,
  applyTransforms,
  generateTransforms,
} from "imagetools-core";

export async function getImageDetails(path, width, height, aspect) {
  const loadedImage = loadImage(path);

  if (aspect && !width && !height) {
    if (!width && !height) {
      ({ width } = await loadedImage.metadata());
    }

    if (width) {
      height = width / aspect;
    }

    if (height) {
      width = height * aspect;
    }
  }

  const { image, metadata } = await applyTransforms(
    generateTransforms({ width, height }, builtins).transforms,
    loadedImage
  );

  const {
    width: imageWidth,
    height: imageHeight,
    format: imageFormat,
  } = metadata;

  return { image, imageWidth, imageHeight, imageFormat };
}
