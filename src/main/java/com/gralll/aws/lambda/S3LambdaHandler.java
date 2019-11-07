package com.gralll.aws.lambda;

import com.amazonaws.AmazonClientException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.amazonaws.HttpMethod.GET;
import static com.amazonaws.HttpMethod.POST;
import static com.amazonaws.HttpMethod.PUT;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNoneEmpty;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

@Slf4j
public class S3LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {

    private static final String BUCKET_NAME = "s3-file-service-storage-dev-euw1";

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest input, Context context) {
        // Just to calculate handling time
        Instant start = Instant.now();
        log.info("Handling of a request has been started at: {}", start);

        final String httpMethod = input.getHttpMethod();
        AwsProxyResponse awsProxyResponse;
        try {
            if (equalsIgnoreCase(GET.name(), httpMethod)) {
                awsProxyResponse = handleGetRequest(input);
            } else if (equalsIgnoreCase(POST.name(), httpMethod)) {
                awsProxyResponse = handlePostRequest(input);
            } else if (equalsIgnoreCase(PUT.name(), httpMethod)) {
                awsProxyResponse = handlePutRequest(input);
            } else {
                log.error("Unsupported http method {}", httpMethod);
                awsProxyResponse = buildAwsProxyResponse(SC_BAD_REQUEST, "Only GET, POST, PUT are supported.");
            }
        } catch (AmazonClientException e) {
            log.error("Request failed.", e);
            awsProxyResponse = buildAwsProxyResponse(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    format("Request failed. Details: %s", e.getMessage()));
        }

        Instant end = Instant.now();
        log.info("Handling of request has been finished at: {}", end);
        log.info("Handling took: {} millisecond", Duration.between(start, end).toMillis());

        return awsProxyResponse;
    }

    private AwsProxyResponse handleGetRequest(AwsProxyRequest input) {
        String fileName = getProxyParam(input);
        if (isNoneEmpty(fileName)) {
            return getFile(input.getPathParameters().get("proxy"));
        }
        return getFilesList();
    }

    private AwsProxyResponse handlePostRequest(AwsProxyRequest input) {
        if (isNoneEmpty(getProxyParam(input))) {
            return buildAwsProxyResponse(
                    SC_BAD_REQUEST,
                    "Unsupported method POST with fileName");
        }
        String fileName = format("File-%s", UUID.randomUUID());
        return sendFile(fileName, input.getBody());
    }

    private AwsProxyResponse handlePutRequest(AwsProxyRequest input) {
        String fileName = getProxyParam(input);
        if (isEmpty(fileName)) {
            return buildAwsProxyResponse(SC_BAD_REQUEST, "Unsupported method PUT without fileName");
        }
        return sendFile(fileName, input.getBody());
    }

    private AwsProxyResponse getFilesList() {
        List<String> keyList =
                S3ClientFactory.s3Client().listObjects(BUCKET_NAME)
                         .getObjectSummaries()
                         .stream()
                         .map(S3ObjectSummary::getKey)
                         .collect(Collectors.toList());
        log.info("Files list for bucket {} was received.", BUCKET_NAME);
        return buildAwsProxyResponse(SC_OK, keyList.toString());
    }

    private AwsProxyResponse getFile(String fileName) {
        String content = S3ClientFactory.s3Client().getObjectAsString(BUCKET_NAME, fileName);
        log.info("File {} was received.", fileName);
        return buildAwsProxyResponse(SC_OK, content);
    }

    private AwsProxyResponse sendFile(String fileName, String content) {
        S3ClientFactory.s3Client().putObject(BUCKET_NAME, fileName, content);
        log.info("File {} was uploaded", fileName);
        return buildAwsProxyResponse(
                SC_CREATED,
                format("File was uploaded: %s", fileName));
    }

    private AwsProxyResponse buildAwsProxyResponse(int status, String body) {
        AwsProxyResponse response = new AwsProxyResponse(status, null, body);
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    private String getProxyParam(AwsProxyRequest request) {
        if (request.getPathParameters() != null && request.getPathParameters().get("proxy") != null) {
            return request.getPathParameters().get("proxy");
        }
        return StringUtils.EMPTY;
    }
}