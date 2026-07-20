variable "aws_region" {
  description = "AWS region for FitTrack infrastructure"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "fittrack"
}

variable "environment" {
  description = "Deployment environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block for the public subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "availability_zone" {
  description = "Availability zone for the public subnet"
  type        = string
  default     = "us-east-1a"
}

variable "ssh_allowed_cidr" {
  description = "CIDR blocks allowed to SSH into the EC2 instance"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "ec2_ami_id" {
  description = "AMI ID for the application EC2 instance (Amazon Linux 2)"
  type        = string
  default     = "ami-0c55b159cbfafe1f0"
}

variable "ec2_instance_type" {
  description = "EC2 instance type for the application"
  type        = string
  default     = "t3.micro"
}

variable "ec2_key_name" {
  description = "SSH key pair name for EC2 access"
  type        = string
}

variable "db_engine_version" {
  description = "PostgreSQL engine version for RDS"
  type        = string
  default     = "16.3"
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "Allocated storage for RDS in GB"
  type        = number
  default     = 20
}

variable "db_name" {
  description = "Initial database name"
  type        = string
  default     = "fittrack"
}

variable "db_username" {
  description = "Master username for RDS"
  type        = string
  default     = "fittrack_admin"
}

variable "db_password" {
  description = "Master password for RDS"
  type        = string
  sensitive   = true
}
