echo "****************Upload lambdas to S3****************"
aws lambda update-function-code --function-name  arn:aws:lambda:eu-west-1:686094824109:function:S3FileService-dev-euw1-S3FileServiceFunctionJava-15XI9WOZVTB3F --zip-file fileb://../build/distributions/aws-sam-lambda-1.0-SNAPSHOT.zip
echo "****************FINISH****************"
PAUSE