module.exports = {
  meta: {
    type: "error",
  },
  create(context) {
    return {
      Identifier(node) {
        var bannedName = "foo";
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
