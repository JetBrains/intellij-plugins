# Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

# See https://github.com/joelittlejohn/jsonschema2pojo/wiki/Getting-Started#the-command-line-interface for installation instructions
SCRIPT_PATH=$(dirname "$0")
DEST_DIR="$SCRIPT_PATH/../src/org/jetbrains/vuejs/model/webtypes/json/"
rm -R "$DEST_DIR"
jsonschema2pojo --source "$SCRIPT_PATH/web-types.json" \
                --annotation-style JACKSON2 \
                --omit-tostring \
                --omit-hashcode-and-equals \
                --enable-additional-properties \
                --package org.jetbrains.vuejs.model.webtypes.json \
                --source-type JSONSCHEMA \
                --target "$SCRIPT_PATH/../src/"

cp -R "$SCRIPT_PATH"/src/* "$DEST_DIR"

# Add implements for SourceEntities
for cls in HtmlAttribute HtmlTag HtmlVueFilter; do
  sed -i '' "s/class $cls/class $cls implements SourceEntity/g" "$DEST_DIR/$cls.java"
done

# Add implements for DocumentedItems
for cls in HtmlAttributeVueArgument HtmlAttributeVueModifier HtmlTagAttribute HtmlTagEvent HtmlTagSlot HtmlVueFilterArgument TypedEntity; do
  sed -i '' "s/class $cls/class $cls implements DocumentedItem/g" "$DEST_DIR/$cls.java"
done

# Patch HtmlTag#vueScopedSlots
sed -i '' "s/private Object vueScopedSlots;/private List<HtmlTagSlot> vueScopedSlots = new ArrayList<HtmlTagSlot>();/g" "$DEST_DIR/HtmlTag.java"
sed -i '' "s/public Object getVueScopedSlots()/public List<HtmlTagSlot> getVueScopedSlots()/g" "$DEST_DIR/HtmlTag.java"
sed -i '' "s/setVueScopedSlots(Object vueScopedSlots)/setVueScopedSlots(List<HtmlTagSlot> vueScopedSlots)/g" "$DEST_DIR/HtmlTag.java"