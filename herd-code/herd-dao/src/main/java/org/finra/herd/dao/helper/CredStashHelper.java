/*
* Copyright 2015 herd contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.finra.herd.dao.helper;

import java.util.Map;

import com.amazonaws.ClientConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import org.finra.herd.core.helper.ConfigurationHelper;
import org.finra.herd.dao.CredStashFactory;
import org.finra.herd.dao.credstash.CredStash;
import org.finra.herd.dao.exception.CredStashGetCredentialFailedException;
import org.finra.herd.model.dto.ConfigurationValue;

@Component
public class CredStashHelper
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CredStashHelper.class);

    @Autowired
    private AwsHelper awsHelper;

    @Autowired
    private ConfigurationHelper configurationHelper;

    @Autowired
    private CredStashFactory credStashFactory;

    @Autowired
    private JsonHelper jsonHelper;

    /**
     * Gets a password from the credstash.
     *
     * @param credStashEncryptionContext the encryption context
     * @param credentialName the credential name
     *
     * @return the password
     * @throws CredStashGetCredentialFailedException if CredStash fails to get a credential
     */
    @Retryable(maxAttempts = 3, value = CredStashGetCredentialFailedException.class, backoff = @Backoff(delay = 5000, multiplier = 2))
    public String getCredentialFromCredStash(String credStashEncryptionContext, String credentialName) throws CredStashGetCredentialFailedException
    {
        // Get the credstash table name and credential names for the keystore and truststore.
        String credStashAwsRegion = configurationHelper.getProperty(ConfigurationValue.CREDSTASH_AWS_REGION_NAME);
        String credStashTableName = configurationHelper.getProperty(ConfigurationValue.CREDSTASH_TABLE_NAME);

        // Log configuration values and input parameters.
        LOGGER.info("credStashAwsRegion={} credStashTableName={} credStashEncryptionContext={} credentialName={}", credStashAwsRegion, credStashTableName,
            credStashEncryptionContext, credentialName);

        // Get the AWS client configuration.
        ClientConfiguration clientConfiguration = awsHelper.getClientConfiguration(awsHelper.getAwsParamsDto());

        // Get the keystore and truststore passwords from Credstash.
        CredStash credstash = credStashFactory.getCredStash(credStashAwsRegion, credStashTableName, clientConfiguration);

        // Try to obtain the credentials from cred stash.
        String password = null;
        try
        {
            // Convert the JSON config file version of the encryption context to a Java Map class.
            @SuppressWarnings("unchecked")
            Map<String, String> credstashEncryptionContextMap = jsonHelper.unmarshallJsonToObject(Map.class, credStashEncryptionContext);
            // Get the keystore and truststore passwords from credstash.
            password = credstash.getCredential(credentialName, credstashEncryptionContextMap);
        }
        catch (Exception exception)
        {
            LOGGER.error("Caught exception when attempting to get a credential value from CredStash.", exception);
        }

        // If either the keystorePassword or truststorePassword values are empty and could not be obtained
        // as credentials from cred stash, then throw a CredStashGetCredentialFailedException.
        if (StringUtils.isEmpty(password))
        {
            throw new CredStashGetCredentialFailedException("Failed to obtain the keystore or truststore credential from cred stash.");
        }

        // Return the keystore and truststore passwords in a map.
        return password;
    }
}
