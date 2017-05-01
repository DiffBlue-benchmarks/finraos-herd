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
package org.finra.herd.service.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import org.finra.herd.model.api.xml.ExpectedPartitionValueKey;

/**
 * A helper class for Allowed Attribute Value related code.
 */
@Component
public class AllowedAttributeValueHelper
{
    @Autowired
    private AlternateKeyHelper alternateKeyHelper;

    /**
     * Validates the allowed attribute value key. This method also trims the key parameters.
     *
     * @param key the allowed attribute value key
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    public void validateAllowedAttributeValueKey(ExpectedPartitionValueKey key) throws IllegalArgumentException
    {
        Assert.notNull(key, "An allowed attribute value key must be specified.");
        key.setPartitionKeyGroupName(alternateKeyHelper.validateStringParameter("partition key group name", key.getPartitionKeyGroupName()));
        Assert.hasText(key.getAllowedAttributeValue(), "An allowed attribute value must be specified.");
        key.setAllowedAttributeValue(key.getAllowedAttributeValue().trim());
    }
}
