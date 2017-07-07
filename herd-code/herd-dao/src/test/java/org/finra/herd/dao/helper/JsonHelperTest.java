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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import com.fasterxml.jackson.core.type.TypeReference;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.finra.herd.dao.AbstractDaoTest;
import org.finra.herd.model.jpa.BusinessObjectDefinitionEntity;
import org.finra.herd.model.jpa.BusinessObjectFormatEntity;

/**
 * This class tests functionality within the JsonHelper class.
 */
public class JsonHelperTest extends AbstractDaoTest
{
    @Autowired
    private JsonHelper jsonHelper;

    @Test
    public void testGetKeyValue() throws Exception
    {
        // Create a JSON object with one key value pair.
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(String.format("{\"%s\" : \"%s\"}", KEY, VALUE));

        // Get and validate the key value.
        assertEquals(VALUE, jsonHelper.getKeyValue(jsonObject, KEY, String.class));

        // Try to get a value for a non-existing key.
        try
        {
            jsonHelper.getKeyValue(jsonObject, I_DO_NOT_EXIST, String.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Failed to get \"%s\" key value from JSON object.", I_DO_NOT_EXIST), e.getMessage());
        }

        // Try to get a value that cannot be cast to the specified class type.
        try
        {
            jsonHelper.getKeyValue(jsonObject, KEY, BigDecimal.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Failed to cast \"%s\" key value to %s.", VALUE, BigDecimal.class.getName()), e.getMessage());
        }
    }

    @Test
    public void testObjectToJson()
    {
        assertEquals(String.format("\"%s\"", STRING_VALUE), jsonHelper.objectToJson(STRING_VALUE));
    }

    @Test
    public void testObjectToJsonValidateNoStackOverflowErrorWithCircularDependency()
    {
        // Create a business object definition entity.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity = businessObjectDefinitionDaoTestHelper
            .createBusinessObjectDefinitionEntity(BDEF_NAMESPACE, BDEF_NAME, DATA_PROVIDER_NAME, BDEF_DESCRIPTION, NO_ATTRIBUTES);

        // Create a business object format entity.
        BusinessObjectFormatEntity businessObjectFormatEntity = businessObjectFormatDaoTestHelper
            .createBusinessObjectFormatEntity(businessObjectDefinitionEntity, FORMAT_USAGE_CODE,
                fileTypeDaoTestHelper.createFileTypeEntity(FORMAT_FILE_TYPE_CODE, null), FORMAT_VERSION, FORMAT_DESCRIPTION, LATEST_VERSION_FLAG_SET,
                PARTITION_KEY, null, NO_ATTRIBUTES, SCHEMA_DELIMITER_COMMA, SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, NO_COLUMNS,
                NO_PARTITION_COLUMNS);

        // Introduce circular dependency to the business object format parent/child relationship.
        businessObjectFormatEntity.setBusinessObjectFormatParents(Arrays.asList(businessObjectFormatEntity));
        businessObjectFormatEntity.setBusinessObjectFormatChildren(Arrays.asList(businessObjectFormatEntity));
        businessObjectFormatDao.saveAndRefresh(businessObjectFormatEntity);

        // Create a JSON object from the business object definition entity.
        assertNotNull(jsonHelper.objectToJson(businessObjectDefinitionEntity));
    }

    @Test
    public void testUnmarshallJsonToObjectUsingClassType() throws IOException
    {
        assertEquals(STRING_VALUE, jsonHelper.unmarshallJsonToObject(String.class, String.format("\"%s\"", STRING_VALUE)));
    }

    @Test
    public void testUnmarshallJsonToObjectUsingTypeReference() throws IOException
    {
        assertEquals(STRING_VALUE, jsonHelper.unmarshallJsonToObject(new TypeReference<String>()
        {
        }, String.format("\"%s\"", STRING_VALUE)));
    }
}
