// @ts-check

export default function getBackgroundStyles(
  images,
  className,
  objectFit,
  objectPosition,
  fadeInTransition,
  { isImg = false, isBackgroundPicture = false, containerClassName = "" } = {}
) {
  const sourcesWithFallback = images.filter(({ fallback }) => fallback);

  if (sourcesWithFallback.length === 0) return "";

  const staticStyles = !fadeInTransition
    ? ""
    : `
    ${
      isBackgroundPicture
        ? `
            .${containerClassName} * {
              z-index: 1;
              position: relative;
            }
          `
        : ""
    }

    .${className} {
      --opacity: 1;
      --z-index: 0;
    }

    ${
      !isBackgroundPicture
        ? `
            .${className} img {
              z-index: 1;
              position: relative;
            }
          `
        : ""
    }

    .${className}::after {
      inset: 0;
      content: "";
      left: 0;
      width: 100%;
      height: 100%;
      position: absolute;
      pointer-events: none;
      transition: opacity ${
        typeof fadeInTransition !== "object"
          ? "1s"
          : (() => {
              const {
                delay = "0s",
                duration = "1s",
                timingFunction = "ease",
              } = fadeInTransition;

              return `${duration} ${timingFunction} ${delay}`;
            })()
      };
      opacity: var(--opacity);
      z-index: var(--z-index);
    }
  `;

  const dynamicStyles = images
    .map(({ media, fallback, object }) => {
      const elementSelector = className + (!isImg ? " img" : ""),
        backgroundElementSelector =
          className + (fadeInTransition ? "::after" : "");

      const style = `
        .${elementSelector} {
          object-fit: ${object?.fit || objectFit};
          object-position: ${object?.position || objectPosition};
        }

        .${backgroundElementSelector} {
          background-size: ${object?.fit || objectFit};
          background-image: url("${encodeURI(fallback)}");
          background-position: ${object?.position || objectPosition};
        }
      `;

      return media ? `@media ${media} { ${style} }` : style;
    })
    .reverse();

  const backgroundStyles = [staticStyles, ...dynamicStyles].join("");

  return backgroundStyles;
}
