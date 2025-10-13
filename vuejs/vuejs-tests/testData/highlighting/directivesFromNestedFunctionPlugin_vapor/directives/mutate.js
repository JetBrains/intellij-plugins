export function mutate(el, value, _, { attr = false, char = false, child = false, sub = false, once = false, immediate = false }) {
  console.log("Intersect: ", el, value, " modifiers: ", { attr, char, child, sub, once, immediate })
}
