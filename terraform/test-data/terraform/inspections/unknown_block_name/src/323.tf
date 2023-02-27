//noinspection MissingProperty
resource "aws_appmesh_virtual_service" "admin_service" {
  spec {
    provider { // note that name matches one in TypeModel.RootBlocksMap
      virtual_node {
        virtual_node_name = "test"
      }
    }
  }
}