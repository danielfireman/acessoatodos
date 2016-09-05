package com.acessoatodos.aws;

import com.amazonaws.auth.AWSCredentials;

/**
 * Provides null AWS credentials. Created to use in cases like
 * local DynamoDB.
 */
class EmptyAwsCredentials implements AWSCredentials {
    @Override
    public String getAWSAccessKeyId() {
        return "";
    }

    @Override
    public String getAWSSecretKey() {
        return "";
    }
}
