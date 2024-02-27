# ------------------------------------------------------------------------------
# Environment
# ------------------------------------------------------------------------------
variable "environment" {
  type        = string
  description = "The environment name, defined in envrionment vars."
}
variable "aws_region" {
  default     = "eu-west-2"
  type        = string
  description = "The AWS region for deployment."
}
variable "aws_profile" {
  default     = "development-eu-west-2"
  type        = string
  description = "The AWS profile to use for deployment."
}
variable "human_log" {
  default     = 0
  type        = number
  description = "Where human-friendly logging is enabled or not."
}
# Kafka consumer configuration
variable "backoff_delay" {
  default   = 30000
  type      = number
  description = "The delay in milliseconds between message republish attempts."
}
variable "bootstrap_server_url" {
  default   = ""
  type      = string
  description = "The URLs of the Kafka brokers that the consumers will connect to."
}
variable "concurrent_listener_instances" {
  default   = 1
  type      = number
  description = "The number of consumers that should participate in the consumer group. Must be equal to the number of main topic partitions."
}
variable "group_id" {
  default   = "advanced-company-search-consumer-group"
  type      = string
  description = "The group ID of the main consumer."
}
variable "max_attempts" {
  default   = 4
  type      = number
  description = "The maximum number of times messages will be processed before they are sent to the dead letter topic."
}
variable "server_port" {
    default   = 8080
    type      = number
    description = "Port this application runs on."
}
variable "topic" {
  default   = "stream-company-profile"
  type      = string
  description = "The topic from which the main consumer will consume messages."
}
# ------------------------------------------------------------------------------
# Docker Container
# ------------------------------------------------------------------------------
variable "docker_registry" {
  type        = string
  description = "The FQDN of the Docker registry."
}

# ------------------------------------------------------------------------------
# Service performance and scaling configs
# ------------------------------------------------------------------------------
variable "desired_task_count" {
  type        = number
  description = "The desired ECS task count for this service"
  default     = 1 # defaulted low for dev environments, override for production if required
}
variable "max_task_count" {
  type        = number
  description = "The maximum number of tasks for this service."
  default     = 3
}
variable "required_cpus" {
  type        = number
  description = "The required cpu resource for this service. 1024 here is 1 vCPU"
  default     = 256 # defaulted minimum required for fargate services, override for production
}
variable "required_memory" {
  type        = number
  description = "The required memory for this service"
  default     = 512 # defaulted minimum required for fargate services, override for production
}

variable "use_fargate" {
  type        = bool
  description = "If true, sets the required capabilities for all containers in the task definition to use FARGATE, false uses EC2"
  default     = true
}

variable "use_capacity_provider" {
  type        = bool
  description = "Whether to use a capacity provider instead of setting a launch type for the service"
  default     = true
}

variable "service_autoscale_enabled" {
  type        = bool
  description = "Whether to enable service autoscaling, including scheduled autoscaling"
  default     = true
}

variable "service_autoscale_target_value_cpu" {
  type        = number
  description = "Target CPU percentage for the ECS Service to autoscale on"
  default     = 50 # 100 disables autoscaling using CPU as a metric
}

variable "service_scaledown_schedule" {
  type        = string
  description = "The schedule to use when scaling down the number of tasks to zero."
  default     = ""
}

variable "service_scaleup_schedule" {
  type        = string
  description = "The schedule to use when scaling up the number of tasks to their normal desired level."
  default     = ""
}

# ----------------------------------------------------------------------
# Cloudwatch alerts
# ----------------------------------------------------------------------
variable "cloudwatch_alarms_enabled" {
  description = "Whether to create a standard set of cloudwatch alarms for the service.  Requires an SNS topic to have already been created for the stack."
  type        = bool
  default     = true
}

# ------------------------------------------------------------------------------
# Service environment variable configs
# ------------------------------------------------------------------------------
variable "ssm_version_prefix" {
  type        = string
  description = "String to use as a prefix to the names of the variables containing variables and secrets version."
  default     = "SSM_VERSION_"
}

variable "use_set_environment_files" {
  type        = bool
  default     = false
  description = "Toggle default global and shared  environment files"
}

variable "advanced_company_search_consumer_version" {
  type        = string
  description = "The version of the advanced-company-search-consumer container to run."
}

variable "log_level" {
  default     = "info"
  type        = string
  description = "The log level for services to use: trace, debug, info or error"
}