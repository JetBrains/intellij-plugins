a = {for i in var.x: i=>i ...}

# from intellij-hcl#298
setting = {
for k, v in local.input :
dirname(k) => { basename(k) = merge(v...) }...
}