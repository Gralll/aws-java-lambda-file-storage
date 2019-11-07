package com.gralll.aws.lambda;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

final class S3ClientFactory {

    private static final AmazonS3 S3_CLIENT = AmazonS3Client.builder().build();

    static AmazonS3 s3Client() {
        return S3_CLIENT;
    }
}