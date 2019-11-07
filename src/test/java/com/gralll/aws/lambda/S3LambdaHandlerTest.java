/*
 * Copyright 2018: Thomson Reuters. All Rights Reserved. Proprietary and
 * Confidential information of Thomson Reuters. Disclosure, Use or Reproduction
 * without the written authorization of Thomson Reuters is prohibited.
 */
package com.gralll.aws.lambda;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.s3.AmazonS3Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(S3ClientFactory.class)
@PowerMockIgnore({"javax.script.*","javax.management.*", "javax.net.ssl.*"})
public class S3LambdaHandlerTest {

    private S3LambdaHandler s3LambdaHandler;

    private AmazonS3Client amazonS3Client;

    @Before
    public void setUp() {
        s3LambdaHandler = new S3LambdaHandler();
        amazonS3Client = mock(AmazonS3Client.class);
    }

    @Test
    public void shouldReturnCorrectErrorMessageIfMethodIsUnsupported() {
        // Given
        AwsProxyRequest awsProxyRequest = new AwsProxyRequest();

        // When
        AwsProxyResponse awsProxyResponse = s3LambdaHandler.handleRequest(awsProxyRequest, null);

        // Then
        assertThat(awsProxyResponse.getBody(), is("Only GET, POST, PUT are supported."));
    }

    @Test
    public void shouldReturnContentOfExpectedFile() {
        // Given
        PowerMockito.mockStatic(S3ClientFactory.class);
        when(S3ClientFactory.s3Client()).thenReturn(amazonS3Client);
        when(amazonS3Client.getObjectAsString(anyString(), anyString())).thenReturn("content");

        // When
        AwsProxyResponse awsProxyResponse = s3LambdaHandler.handleRequest(getDummyAwsProxyRequest(), null);

        // Then
        PowerMockito.verifyStatic(S3ClientFactory.class, Mockito.times(1));
        S3ClientFactory.s3Client();
        assertThat(awsProxyResponse.getBody(), is("content"));
    }

    private AwsProxyRequest getDummyAwsProxyRequest() {
        AwsProxyRequest awsProxyRequest = new AwsProxyRequest();
        awsProxyRequest.setHttpMethod("GET");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("proxy", "path");
        awsProxyRequest.setPathParameters(pathParams);
        return awsProxyRequest;
    }
}