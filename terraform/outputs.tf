output "vpc_id" {
  description = "ID of the FitTrack VPC"
  value       = aws_vpc.main.id
}

output "public_subnet_id" {
  description = "ID of the public subnet"
  value       = aws_subnet.public.id
}

output "app_security_group_id" {
  description = "Security group ID for the application"
  value       = aws_security_group.app.id
}

output "db_security_group_id" {
  description = "Security group ID for the database"
  value       = aws_security_group.db.id
}

output "ec2_instance_id" {
  description = "ID of the application EC2 instance"
  value       = aws_instance.app.id
}

output "ec2_public_ip" {
  description = "Public IP address of the application EC2 instance"
  value       = aws_instance.app.public_ip
}

output "rds_endpoint" {
  description = "Connection endpoint for the RDS PostgreSQL instance"
  value       = aws_db_instance.main.endpoint
}

output "rds_address" {
  description = "Hostname of the RDS PostgreSQL instance"
  value       = aws_db_instance.main.address
}

output "rds_port" {
  description = "Port of the RDS PostgreSQL instance"
  value       = aws_db_instance.main.port
}
