import fs from "node:fs";
import path from "node:path";

// To strip off params when checking for file on disk.
const paramPattern = /\?.*/;

/**
 * getSrcPath allows the use of `src` attributes relative to either the public folder or project root.
 *
 * It first checks to see if the src is a file relative to the project root.
 * If the file isn't found, it will look in the public folder.
 * Finally, if it still can't be found, the original input will be returned.
 */
export async function getSrcPath(src) {
  const { default: astroViteConfigs } = await import(
    "../../astroViteConfigs.js"
  );

  // If this is already resolved to a file, return it.
  if (fs.existsSync(src.replace(paramPattern, ""))) return src;

  const rootPath = path.join(astroViteConfigs.rootDir, src);
  const rootTest = rootPath.replace(paramPattern, "");
  if (fs.existsSync(rootTest)) return rootPath;

  const publicPath = path.join(astroViteConfigs.publicDir, src);
  const publicTest = publicPath.replace(paramPattern, "");
  if (fs.existsSync(publicTest)) return publicPath;

  // Fallback
  return src;
}
