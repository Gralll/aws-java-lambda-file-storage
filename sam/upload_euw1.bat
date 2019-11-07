echo "****************Upload lambdas to S3****************"
aws s3 cp ../build/distributions/aws-sam-lambda-1.0-SNAPSHOT.zip s3://file-service-function-dev-euw1
echo "****************FINISH****************"
PAUSE