variable "alert_error_set" {
  type = object({
    alert_list = list(object({
      metric_name = string
      alarm_name  = string
    }))
  })
  default = {
    alert_list = [
      {
        metric_name = "ExecutionsFailed"
        alarm_name  = "StepFunction"
      },
      {
        metric_name = "ExecutionsAborted"
        alarm_name  = "StepFunction"
      },
      {
        metric_name = "ExecutionsTimedOut"
        alarm_name  = "StepFunction"
      }
    ]
  }
}

variable "alert_error_unset" {
  type = object({
    alert_list = list(object({
      metric_name = string
      alarm_name  = string
    }))
  })
}

//noinspection MissingProperty
resource "aws_cloudwatch_metric_alarm" "alert_error_1" {
  count               = length(var.alert_error_set.alert_list)
  alarm_name          = "${var.alert_error_set.alert_list[count.index].alarm_name}-${var.alert_error_set.alert_list[count.index].metric_name}"
  comparison_operator = var.alert_error_set.alert_list[count.index].metric_name
}

//noinspection MissingProperty
resource "aws_cloudwatch_metric_alarm" "alert_error_2" {
  count               = length(var.alert_error_unset.alert_list)
  alarm_name          = "${var.alert_error_unset.alert_list[count.index].alarm_name}-${var.alert_error_unset.alert_list[count.index].metric_name}"
  comparison_operator = var.alert_error_unset.alert_list[count.index].metric_name
}