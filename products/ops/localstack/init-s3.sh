#!/bin/bash
# Creates the S3 bucket in LocalStack for local development
awslocal s3 mb s3://magenta-properties-media
awslocal s3api put-bucket-cors --bucket magenta-properties-media --cors-configuration '{
  "CORSRules": [{
    "AllowedOrigins": ["*"],
    "AllowedMethods": ["GET","PUT","POST"],
    "AllowedHeaders": ["*"],
    "MaxAgeSeconds": 3000
  }]
}'
echo "S3 bucket created: magenta-properties-media"
