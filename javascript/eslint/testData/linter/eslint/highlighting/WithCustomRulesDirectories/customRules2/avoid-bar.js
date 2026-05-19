module.exports = {
  meta: {
    type: "error",
  },
  create(context) {
    return {
      Identifier(node) {
        var bannedName = "bar";
        if (node.name === bannedName) {
          context.report({
                           node,
                           message: 'Avoid using variables named \'{{ name }}\'',
                           data: {
                             name: bannedName,
                           }
                         });
        }
      }
    };
  }
};
