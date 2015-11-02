if (window.__IntelliJTools === undefined) {
  window.__IntelliJTools = {}
}

window.__IntelliJTools.scrollToOffset = (function () {

  var scrollToSrcOffset = function (offsetToScroll, attributeName) {
    var best = [null, Infinity]

    var score = function (fromTo) {
      if (!fromTo) {
        return Infinity
      }
      return Math.min(Math.abs(fromTo[0] - offsetToScroll),
                      Math.abs(fromTo[1] - 1 - offsetToScroll))
    }

    var dfs = function (node) {
      for (var child = node.firstChild; child != null; child = child.nextSibling) {
        if (!('getAttribute' in child)) {
          continue
        }
        var attrValue = child.getAttribute(attributeName);

        var fromTo = null
        if (attrValue) {
          fromTo = attrValue.split('..')
          var curScore = score(fromTo)
          if (curScore < best[1]) {
            best = [child, curScore]
          }
        }

        if (!fromTo || fromTo[1] <= offsetToScroll) {
          continue
        }

        if (fromTo[0] <= offsetToScroll) {
          dfs(child)
        }
        break
      }
    }


    dfs(document.body)
    if (best[0]) {
      best[0].scrollIntoView()
    }
  }

  return scrollToSrcOffset
})()

