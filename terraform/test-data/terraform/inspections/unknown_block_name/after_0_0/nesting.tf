# intention: "TfUnknownBlockType"
# fix: "Add closing braces before an element"
# position: 3: "resource"
#
resource "aws_alb" "x" {
  subnets = []

}
resource "aws_cloudtrail" "y" {
  name = ""
  s3_bucket_name = ""
}
