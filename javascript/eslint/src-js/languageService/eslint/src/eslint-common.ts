export function containsString(src: string | null | undefined, toFind: string): boolean {
  return src != null && src.indexOf(toFind) >= 0
}

export function normalizePath(eslintPackagePath: string): string | undefined {
  if (eslintPackagePath === undefined) return undefined
  if (eslintPackagePath.charAt(eslintPackagePath.length - 1) !== '/' &&
    eslintPackagePath.charAt(eslintPackagePath.length - 1) !== '\\') {
    eslintPackagePath = eslintPackagePath + '/';
  }
  return toUnixPathSeparators(eslintPackagePath);
}

export function toUnixPathSeparators(path: string) {
  return path.split("\\").join("/");
}

export function requireInContext(modulePathToRequire: string, contextPath?: string): any {
  const contextRequire = getContextRequire(contextPath);
  return contextRequire(modulePathToRequire);
}

export function requireResolveInContext(modulePathToRequire: string, contextPath?: string): string {
  const contextRequire = getContextRequire(contextPath);
  return contextRequire.resolve(modulePathToRequire);
}

function getContextRequire(contextPath?: string): NodeRequire {
  if (contextPath != null) {
    const module = require('module')
    if (typeof module.createRequire === 'function') {
      // https://nodejs.org/api/module.html#module_module_createrequire_filename
      // Implemented in Yarn PnP: https://next.yarnpkg.com/advanced/pnpapi/#requiremodule
      return module.createRequire(contextPath);
    }
    // noinspection JSDeprecatedSymbols
    if (typeof module.createRequireFromPath === 'function') {
      // Use createRequireFromPath (a deprecated version of createRequire) to support Node.js 10.x
      // noinspection JSDeprecatedSymbols
      return module.createRequireFromPath(contextPath);
    }
    throw Error('Function module.createRequire is unavailable in Node.js ' + process.version +
      ', Node.js >= 12.2.0 is required')
  }
  return require;
}
