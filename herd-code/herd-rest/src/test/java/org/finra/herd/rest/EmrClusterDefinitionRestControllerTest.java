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
package org.finra.herd.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import org.finra.herd.model.AlreadyExistsException;
import org.finra.herd.model.ObjectNotFoundException;
import org.finra.herd.model.api.xml.EmrClusterDefinition;
import org.finra.herd.model.api.xml.EmrClusterDefinitionCreateRequest;
import org.finra.herd.model.api.xml.EmrClusterDefinitionInformation;
import org.finra.herd.model.api.xml.EmrClusterDefinitionKey;
import org.finra.herd.model.api.xml.EmrClusterDefinitionKeys;
import org.finra.herd.model.api.xml.EmrClusterDefinitionUpdateRequest;
import org.finra.herd.model.api.xml.NodeTag;
import org.finra.herd.model.dto.ConfigurationValue;
import org.finra.herd.model.jpa.EmrClusterDefinitionEntity;
import org.finra.herd.model.jpa.NamespaceEntity;
import org.finra.herd.service.impl.EmrClusterDefinitionServiceImpl;

/**
 * This class tests various functionality within the EMR Cluster Definition REST controller.
 */
public class EmrClusterDefinitionRestControllerTest extends AbstractRestTest
{
    @Test
    public void testCreateEmrClusterDefinition() throws Exception
    {
        // Create and persist the namespace entity.
        namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Create an EMR cluster definition create request.
        EmrClusterDefinitionCreateRequest request = createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Create an EMR cluster definition.
        EmrClusterDefinitionInformation resultEmrClusterDefinition = emrClusterDefinitionRestController.createEmrClusterDefinition(request);

        // Validate the returned object.
        validateEmrClusterDefinition(null, NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), resultEmrClusterDefinition);
    }

    @Test
    public void testCreateEmrClusterDefinitionMissingRequiredParameters() throws Exception
    {
        // Try to perform a create without specifying a namespace.
        try
        {
            emrClusterDefinitionRestController.createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(BLANK_TEXT, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));
            fail("Should throw an IllegalArgumentException when namespace is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A namespace must be specified.", e.getMessage());
        }

        // Try to perform a create without specifying an EMR cluster definition name.
        try
        {
            emrClusterDefinitionRestController.createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, BLANK_TEXT,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));
            fail("Should throw an IllegalArgumentException when EMR cluster definition name is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An EMR cluster definition name must be specified.", e.getMessage());
        }

        // Try to perform a create without specifying an EMR cluster definition configuration.
        try
        {
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, null));
            fail("Should throw an IllegalArgumentException when EMR cluster definition configuration is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An EMR cluster definition configuration must be specified.", e.getMessage());
        }

        // Try to perform a create without specifying both instance definitions and instance fleets.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.setInstanceDefinitions(null);
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Instance group definitions or instance fleets must be specified.", e.getMessage());
        }

        // Try to perform a create without specifying master instances.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.getInstanceDefinitions().setMasterInstances(null);
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when master instances are not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Master instances must be specified.", e.getMessage());
        }

        // Try to perform a create with instance count less than one for master instances.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.getInstanceDefinitions().getMasterInstances().setInstanceCount(0);
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when instance count is less than one for master instances.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("At least 1 master instance must be specified.", e.getMessage());
        }

        // Try to perform a create without specifying instance type for master instances.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.getInstanceDefinitions().getMasterInstances().setInstanceType(BLANK_TEXT);
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when instance type for master instances is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An instance type for master instances must be specified.", e.getMessage());
        }

        // Try to perform a create with instance count less than one for core instances.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.getInstanceDefinitions().getCoreInstances().setInstanceCount(-1);
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when instance count is less than one for core instances.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("At least 0 core instance must be specified.", e.getMessage());
        }

        // Try to perform a create without specifying instance type for core instances.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.getInstanceDefinitions().getCoreInstances().setInstanceType(BLANK_TEXT);
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when instance type for core instances is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An instance type for core instances must be specified.", e.getMessage());
        }

        // Try to perform a create without specifying node tags.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.setNodeTags(new ArrayList<NodeTag>());
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when node tags are not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Node tags must be specified.", e.getMessage());
        }

        // Try to perform a create without specifying node tag name.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            NodeTag nodeTag = new NodeTag();
            nodeTag.setTagName(BLANK_TEXT);
            nodeTag.setTagValue(ATTRIBUTE_VALUE_1);
            emrClusterDefinitionConfiguration.getNodeTags().add(nodeTag);
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when node tag name is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A node tag name must be specified.", e.getMessage());
        }

        // Try to perform a create without specifying node tag value.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            NodeTag nodeTag = new NodeTag();
            nodeTag.setTagName(ATTRIBUTE_NAME_1_MIXED_CASE);
            nodeTag.setTagValue(BLANK_TEXT);
            emrClusterDefinitionConfiguration.getNodeTags().add(nodeTag);
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when node tag value is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A node tag value must be specified.", e.getMessage());
        }
    }

    @Test
    public void testCreateEmrClusterDefinitionTrimParameters() throws Exception
    {
        // Create and persist the namespace entity.
        namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Create an EMR cluster definition create request by passing namespace and EMR cluster definition name with leading and trailing whitespace characters.
        EmrClusterDefinitionCreateRequest request =
            createEmrClusterDefinitionCreateRequest(addWhitespace(NAMESPACE), addWhitespace(EMR_CLUSTER_DEFINITION_NAME),
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Create an EMR cluster definition.
        EmrClusterDefinitionInformation resultEmrClusterDefinition = emrClusterDefinitionRestController.createEmrClusterDefinition(request);

        // Validate the returned object.
        validateEmrClusterDefinition(null, NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), resultEmrClusterDefinition);
    }

    @Test
    public void testCreateEmrClusterDefinitionUpperCaseParameters() throws Exception
    {
        // Create and persist the namespace entity with a lowercase name.
        namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE.toLowerCase());

        // Create an EMR cluster definition create request by passing the EMR cluster definition name key parameters in upper case.
        EmrClusterDefinitionCreateRequest request = createEmrClusterDefinitionCreateRequest(NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase(),
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Create an EMR cluster definition.
        EmrClusterDefinitionInformation resultEmrClusterDefinition = emrClusterDefinitionRestController.createEmrClusterDefinition(request);

        // Validate the returned object.
        validateEmrClusterDefinition(null, NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase(),
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), resultEmrClusterDefinition);
    }

    @Test
    public void testCreateEmrClusterDefinitionLowerCaseParameters() throws Exception
    {
        // Create and persist the namespace entity with an uppercase name.
        namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE.toUpperCase());

        // Create an EMR cluster definition create request by passing the EMR cluster definition name key parameters in lower case.
        EmrClusterDefinitionCreateRequest request = createEmrClusterDefinitionCreateRequest(NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase(),
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Create an EMR cluster definition.
        EmrClusterDefinitionInformation resultEmrClusterDefinition = emrClusterDefinitionRestController.createEmrClusterDefinition(request);

        // Validate the returned object.
        validateEmrClusterDefinition(null, NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase(),
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), resultEmrClusterDefinition);
    }

    @Test
    public void testCreateEmrClusterDefinitionNamespaceNoExists() throws Exception
    {
        // Try to perform a create using a non-existing namespace.
        String testNamespace = "I_DO_NOT_EXIST";
        try
        {
            emrClusterDefinitionRestController.createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(testNamespace, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));
            fail("Should throw an ObjectNotFoundException when namespace is not specified.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("Namespace \"%s\" doesn't exist.", testNamespace), e.getMessage());
        }
    }

    @Test
    public void testCreateEmrClusterDefinitionEmrClusterDefinitionAlreadyExists() throws Exception
    {
        // Create and persist the namespace entity.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Create and persist the EMR cluster definition entity.
        emrClusterDefinitionDaoTestHelper.createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME,
            getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Try to perform a create using an already existing EMR cluster definition name.
        try
        {
            emrClusterDefinitionRestController.createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));
            fail("Should throw an AlreadyExistsException when EMR cluster definition already exists.");
        }
        catch (AlreadyExistsException e)
        {
            assertEquals(String
                .format("Unable to create EMR cluster definition with name \"%s\" for namespace \"%s\" because it already exists.", EMR_CLUSTER_DEFINITION_NAME,
                    NAMESPACE), e.getMessage());
        }
    }

    @Test
    public void testCreateEmrClusterDefinitionMaxInstancesSetToZero() throws Exception
    {
        // Create and persist the namespace entity.
        namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Override configuration to set the maximum allowed number of EMR instances to zero.
        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put(ConfigurationValue.MAX_EMR_INSTANCES_COUNT.getKey(), "0");
        modifyPropertySourceInEnvironment(overrideMap);

        try
        {
            // Create an EMR cluster definition create request.
            EmrClusterDefinitionCreateRequest request = createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

            // Create an EMR cluster definition.
            EmrClusterDefinitionInformation resultEmrClusterDefinition = emrClusterDefinitionRestController.createEmrClusterDefinition(request);

            // Validate the returned object.
            validateEmrClusterDefinition(null, NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), resultEmrClusterDefinition);
        }
        finally
        {
            // Restore the property sources so we don't affect other tests.
            restorePropertySourceInEnvironment();
        }
    }

    @Test
    public void testCreateEmrClusterDefinitionMaxInstancesExceeded() throws Exception
    {
        final Integer TEST_MAX_EMR_INSTANCES_COUNT = 10;

        // Override configuration.
        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put(ConfigurationValue.MAX_EMR_INSTANCES_COUNT.getKey(), TEST_MAX_EMR_INSTANCES_COUNT.toString());
        modifyPropertySourceInEnvironment(overrideMap);

        // Try to perform a create by specifying too many instances (TEST_MAX_EMR_INSTANCES_COUNT + 1).
        EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
        emrClusterDefinitionConfiguration.getInstanceDefinitions().getMasterInstances().setInstanceCount(TEST_MAX_EMR_INSTANCES_COUNT);
        emrClusterDefinitionConfiguration.getInstanceDefinitions().getCoreInstances().setInstanceCount(1);
        try
        {
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when total number of instances exceeds maximum allowed.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Total number of instances requested can not exceed : %d", TEST_MAX_EMR_INSTANCES_COUNT), e.getMessage());
        }
        finally
        {
            // Restore the property sources so we don't affect other tests.
            restorePropertySourceInEnvironment();
        }
    }

    @Test
    public void testCreateEmrClusterDefinitionDuplicateNodeTags() throws Exception
    {
        // Try to perform a create with duplicate node tag names.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            for (int i = 0; i < 2; i++)
            {
                NodeTag nodeTag = new NodeTag();
                nodeTag.setTagName(ATTRIBUTE_NAME_1_MIXED_CASE);
                nodeTag.setTagValue(ATTRIBUTE_VALUE_1);
                emrClusterDefinitionConfiguration.getNodeTags().add(nodeTag);
            }
            emrClusterDefinitionRestController
                .createEmrClusterDefinition(createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when duplicate node tag names are specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Duplicate node tag \"%s\" is found.", ATTRIBUTE_NAME_1_MIXED_CASE), e.getMessage());
        }
    }

    @Test
    public void testCreateEmrClusterDefinitionMissingMandatoryNodeTags() throws Exception
    {
        // Set a list of test mandatory AWS tags that are present in EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH XML file.
        List<String> testMandatoryAwsTagNames = Arrays.asList(ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_NAME_2_MIXED_CASE, ATTRIBUTE_NAME_3_MIXED_CASE);

        // Override configuration to add max partition values.
        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put(ConfigurationValue.MANDATORY_AWS_TAGS.getKey(), StringUtils.join(testMandatoryAwsTagNames, "|"));
        modifyPropertySourceInEnvironment(overrideMap);

        try
        {
            // Test each of the AWS mandatory node tags.
            for (String mandatoryAwsTagName : testMandatoryAwsTagNames)
            {
                EmrClusterDefinition emrClusterDefinitionConfiguration =
                    getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
                // Change EMR cluster definition configuration to have a new list of the test node tags that does not have the relative AWS mandatory node tag.
                emrClusterDefinitionConfiguration.setNodeTags(getTestNodeTags(testMandatoryAwsTagNames, mandatoryAwsTagName));
                try
                {
                    emrClusterDefinitionRestController.createEmrClusterDefinition(
                        createEmrClusterDefinitionCreateRequest(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, emrClusterDefinitionConfiguration));
                    fail(String.format("Should throw an IllegalArgumentException when \"%s\" required AWS node tag is not specified.", mandatoryAwsTagName));
                }
                catch (IllegalArgumentException e)
                {
                    assertEquals(String.format("Mandatory AWS tag not specified: \"%s\"", mandatoryAwsTagName), e.getMessage());
                }
            }
        }
        finally
        {
            // Restore the property sources so we don't affect other tests.
            restorePropertySourceInEnvironment();
        }
    }

    @Test
    public void testGetEmrClusterDefinition() throws Exception
    {
        // Create and persist the namespace entity.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Create and persist the EMR cluster definition entity.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Get the EMR cluster definition.
        EmrClusterDefinitionInformation resultEmrClusterDefinition =
            emrClusterDefinitionRestController.getEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME);

        // Validate the returned object.
        validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), resultEmrClusterDefinition);
    }

    @Test
    public void testGetEmrClusterDefinitionMissingRequiredParameters() throws Exception
    {
        // Try to perform a get without specifying a namespace.
        try
        {
            emrClusterDefinitionRestController.getEmrClusterDefinition(BLANK_TEXT, EMR_CLUSTER_DEFINITION_NAME);
            fail("Should throw an IllegalArgumentException when namespace is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A namespace must be specified.", e.getMessage());
        }

        // Try to perform a get without specifying an EMR cluster definition name.
        try
        {
            emrClusterDefinitionRestController.getEmrClusterDefinition(NAMESPACE, BLANK_TEXT);
            fail("Should throw an IllegalArgumentException when EMR cluster definition name is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An EMR cluster definition name must be specified.", e.getMessage());
        }
    }

    @Test
    public void testGetEmrClusterDefinitionTrimParameters() throws Exception
    {
        // Create and persist the namespace entity.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Create and persist the EMR cluster definition entity.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Get an EMR cluster definition by passing namespace and EMR cluster definition name with leading and trailing whitespace characters.
        EmrClusterDefinitionInformation resultEmrClusterDefinition =
            emrClusterDefinitionRestController.getEmrClusterDefinition(addWhitespace(NAMESPACE), addWhitespace(EMR_CLUSTER_DEFINITION_NAME));

        // Validate the returned object.
        validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), resultEmrClusterDefinition);
    }

    @Test
    public void testGetEmrClusterDefinitionUpperCaseParameters() throws Exception
    {
        // Create and persist the namespace entity with a lowercase name.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE.toLowerCase());

        // Create and persist the EMR cluster definition entity with a lowercase name.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME.toLowerCase(),
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Get an EMR cluster definition by passing the EMR cluster definition name key parameters in upper case.
        EmrClusterDefinitionInformation resultEmrClusterDefinition =
            emrClusterDefinitionRestController.getEmrClusterDefinition(NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase());

        // Validate the returned object.
        validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase(),
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), resultEmrClusterDefinition);
    }

    @Test
    public void testGetEmrClusterDefinitionLowerCaseParameters() throws Exception
    {
        // Create and persist the namespace entity with an uppercase name.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE.toUpperCase());

        // Create and persist the EMR cluster definition entity with an uppercase name.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME.toUpperCase(),
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Get an EMR cluster definition by passing the EMR cluster definition name key parameters in lower case.
        EmrClusterDefinitionInformation resultEmrClusterDefinition =
            emrClusterDefinitionRestController.getEmrClusterDefinition(NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase());

        // Validate the returned object.
        validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase(),
            getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), resultEmrClusterDefinition);
    }

    @Test
    public void testGetEmrClusterDefinitionEmrClusterDefinitionNoExists() throws Exception
    {
        // Try to perform a get using a non-existing EMR cluster definition name.
        String testEmrClusterDefinitionName = "I_DO_NOT_EXIST";
        try
        {
            emrClusterDefinitionRestController.getEmrClusterDefinition(NAMESPACE, testEmrClusterDefinitionName);
            fail("Should throw an ObjectNotFoundException when EMR cluster definition does not exist.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("EMR cluster definition with name \"%s\" doesn't exist for namespace \"%s\".", testEmrClusterDefinitionName, NAMESPACE),
                e.getMessage());
        }
    }

    @Test
    public void testUpdateEmrClusterDefinition() throws Exception
    {
        // Create and persist the namespace entity.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Create and persist the EMR cluster definition entity using minimal test XML configuration.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_MINIMAL_CLASSPATH));

        // Create an EMR cluster definition update request using normal test XML configuration.
        EmrClusterDefinitionUpdateRequest request =
            createEmrClusterDefinitionUpdateRequest(getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        executeWithoutLogging(EmrClusterDefinitionServiceImpl.class, () -> {
            // Update the EMR cluster definition.
            EmrClusterDefinitionInformation updatedEmrClusterDefinition =
                emrClusterDefinitionRestController.updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, request);

            // Validate the returned object.
            validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), updatedEmrClusterDefinition);
        });
    }

    @Test
    public void testUpdateEmrClusterDefinitionMissingRequiredParameters() throws Exception
    {
        // Try to perform an update without specifying a namespace.
        try
        {
            emrClusterDefinitionRestController.updateEmrClusterDefinition(BLANK_TEXT, EMR_CLUSTER_DEFINITION_NAME,
                createEmrClusterDefinitionUpdateRequest(getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));
            fail("Should throw an IllegalArgumentException when namespace is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A namespace must be specified.", e.getMessage());
        }

        // Try to perform an update without specifying an EMR cluster definition name.
        try
        {
            emrClusterDefinitionRestController.updateEmrClusterDefinition(NAMESPACE, BLANK_TEXT,
                createEmrClusterDefinitionUpdateRequest(getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));
            fail("Should throw an IllegalArgumentException when EMR cluster definition name is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An EMR cluster definition name must be specified.", e.getMessage());
        }

        // Try to perform an update without specifying an EMR cluster definition configuration.
        try
        {
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(null));
            fail("Should throw an IllegalArgumentException when EMR cluster definition configuration is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An EMR cluster definition configuration must be specified.", e.getMessage());
        }

        // Try to perform an update without specifying both instance definitions and instance fleets.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.setInstanceDefinitions(null);
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Instance group definitions or instance fleets must be specified.", e.getMessage());
        }

        // Try to perform an update without specifying master instances.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.getInstanceDefinitions().setMasterInstances(null);
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when master instances are not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Master instances must be specified.", e.getMessage());
        }

        // Try to perform an update with instance count less than one for master instances.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.getInstanceDefinitions().getMasterInstances().setInstanceCount(0);
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when instance count is less than one for master instances.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("At least 1 master instance must be specified.", e.getMessage());
        }

        // Try to perform an update without specifying instance type for master instances.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.getInstanceDefinitions().getMasterInstances().setInstanceType(BLANK_TEXT);
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when instance type for master instances is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An instance type for master instances must be specified.", e.getMessage());
        }

        // Try to perform an update without specifying instance type for core instances.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.getInstanceDefinitions().getCoreInstances().setInstanceType(BLANK_TEXT);
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when instance type for core instances is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An instance type for core instances must be specified.", e.getMessage());
        }

        // Try to perform an update without specifying node tags.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            emrClusterDefinitionConfiguration.setNodeTags(new ArrayList<NodeTag>());
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when node tags are not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Node tags must be specified.", e.getMessage());
        }

        // Try to perform an update without specifying node tag name.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            NodeTag nodeTag = new NodeTag();
            nodeTag.setTagName(BLANK_TEXT);
            nodeTag.setTagValue(ATTRIBUTE_VALUE_1);
            emrClusterDefinitionConfiguration.getNodeTags().add(nodeTag);
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when node tag name is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A node tag name must be specified.", e.getMessage());
        }

        // Try to perform an update without specifying node tag value.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            NodeTag nodeTag = new NodeTag();
            nodeTag.setTagName(ATTRIBUTE_NAME_1_MIXED_CASE);
            nodeTag.setTagValue(BLANK_TEXT);
            emrClusterDefinitionConfiguration.getNodeTags().add(nodeTag);
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when node tag value is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A node tag value must be specified.", e.getMessage());
        }
    }

    @Test
    public void testUpdateEmrClusterDefinitionTrimParameters() throws Exception
    {
        // Create and persist the namespace entity.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Create and persist the EMR cluster definition entity using minimal test XML configuration.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_MINIMAL_CLASSPATH));

        executeWithoutLogging(EmrClusterDefinitionServiceImpl.class, () -> {
            // Update an EMR cluster definition with the normal test XML configuration by passing namespace
            // and EMR cluster definition name with leading and trailing whitespace characters.
            EmrClusterDefinitionInformation updatedEmrClusterDefinition = emrClusterDefinitionRestController
                .updateEmrClusterDefinition(addWhitespace(NAMESPACE), addWhitespace(EMR_CLUSTER_DEFINITION_NAME),
                    createEmrClusterDefinitionUpdateRequest(getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));

            // Validate the returned object.
            validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), updatedEmrClusterDefinition);
        });
    }

    @Test
    public void testUpdateEmrClusterDefinitionUpperCaseParameters() throws Exception
    {
        // Create and persist the namespace entity with a lowercase name.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE.toLowerCase());

        // Create and persist the EMR cluster definition entity with a lowercase name using minimal test XML configuration.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME.toLowerCase(),
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_MINIMAL_CLASSPATH));

        executeWithoutLogging(EmrClusterDefinitionServiceImpl.class, () -> {
            // Update an EMR cluster definition with the normal test XML configuration by passing the EMR cluster definition name key parameters in upper case.
            EmrClusterDefinitionInformation updatedEmrClusterDefinition = emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase(),
                    createEmrClusterDefinitionUpdateRequest(getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));

            // Validate the returned object.
            validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase(),
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), updatedEmrClusterDefinition);
        });
    }

    @Test
    public void testUpdateEmrClusterDefinitionLowerCaseParameters() throws Exception
    {
        // Create and persist the namespace entity with an uppercase name.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE.toUpperCase());

        // Create and persist the EMR cluster definition entity with an uppercase name using minimal test XML configuration.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME.toUpperCase(),
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_MINIMAL_CLASSPATH));

        executeWithoutLogging(EmrClusterDefinitionServiceImpl.class, () -> {
            // Update an EMR cluster definition with the normal test XML configuration by passing the EMR cluster definition name key parameters in lower case.
            EmrClusterDefinitionInformation updatedEmrClusterDefinition = emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase(),
                    createEmrClusterDefinitionUpdateRequest(getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));

            // Validate the returned object.
            validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase(),
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), updatedEmrClusterDefinition);
        });
    }

    @Test
    public void testUpdateEmrClusterDefinitionEmrClusterDefinitionNoExists() throws Exception
    {
        // Try to perform an update using a non-existing EMR cluster definition name.
        String testEmrClusterDefinitionName = "I_DO_NOT_EXIST";
        try
        {
            emrClusterDefinitionRestController.updateEmrClusterDefinition(NAMESPACE, testEmrClusterDefinitionName,
                createEmrClusterDefinitionUpdateRequest(getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH)));
            fail("Should throw an ObjectNotFoundException when EMR cluster definition does not exist.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("EMR cluster definition with name \"%s\" doesn't exist for namespace \"%s\".", testEmrClusterDefinitionName, NAMESPACE),
                e.getMessage());
        }
    }

    @Test
    public void testUpdateEmrClusterDefinitionMaxInstancesExceeded() throws Exception
    {
        final Integer TEST_MAX_EMR_INSTANCES_COUNT = 10;

        // Override configuration.
        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put(ConfigurationValue.MAX_EMR_INSTANCES_COUNT.getKey(), TEST_MAX_EMR_INSTANCES_COUNT.toString());
        modifyPropertySourceInEnvironment(overrideMap);

        // Try to perform an update by specifying too many instances (TEST_MAX_EMR_INSTANCES_COUNT + 1).
        EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
        emrClusterDefinitionConfiguration.getInstanceDefinitions().getMasterInstances().setInstanceCount(TEST_MAX_EMR_INSTANCES_COUNT);
        emrClusterDefinitionConfiguration.getInstanceDefinitions().getCoreInstances().setInstanceCount(1);
        try
        {
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when total number of instances exceeds maximum allowed.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Total number of instances requested can not exceed : %d", TEST_MAX_EMR_INSTANCES_COUNT), e.getMessage());
        }
        finally
        {
            // Restore the property sources so we don't affect other tests.
            restorePropertySourceInEnvironment();
        }
    }

    @Test
    public void testUpdateEmrClusterDefinitionDuplicateNodeTags() throws Exception
    {
        // Try to perform an update by passing duplicate node tag names.
        try
        {
            EmrClusterDefinition emrClusterDefinitionConfiguration = getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
            for (int i = 0; i < 2; i++)
            {
                NodeTag nodeTag = new NodeTag();
                nodeTag.setTagName(ATTRIBUTE_NAME_1_MIXED_CASE);
                nodeTag.setTagValue(ATTRIBUTE_VALUE_1);
                emrClusterDefinitionConfiguration.getNodeTags().add(nodeTag);
            }
            emrClusterDefinitionRestController
                .updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME, createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
            fail("Should throw an IllegalArgumentException when duplicate node tag names are specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Duplicate node tag \"%s\" is found.", ATTRIBUTE_NAME_1_MIXED_CASE), e.getMessage());
        }
    }

    @Test
    public void testUpdateEmrClusterDefinitionMissingMandatoryNodeTags() throws Exception
    {
        // Set a list of test mandatory AWS tags that are present in EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH XML file.
        List<String> testMandatoryAwsTagNames = Arrays.asList(ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_NAME_2_MIXED_CASE, ATTRIBUTE_NAME_3_MIXED_CASE);

        // Override configuration to add max partition values.
        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put(ConfigurationValue.MANDATORY_AWS_TAGS.getKey(), StringUtils.join(testMandatoryAwsTagNames, "|"));
        modifyPropertySourceInEnvironment(overrideMap);

        try
        {
            // Test each of the AWS mandatory node tags.
            for (String mandatoryAwsTagName : testMandatoryAwsTagNames)
            {
                EmrClusterDefinition emrClusterDefinitionConfiguration =
                    getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH);
                // Change EMR cluster definition configuration to have a new list of the test node tags that does not have the relative AWS mandatory node tag.
                emrClusterDefinitionConfiguration.setNodeTags(getTestNodeTags(testMandatoryAwsTagNames, mandatoryAwsTagName));
                try
                {
                    emrClusterDefinitionRestController.updateEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
                        createEmrClusterDefinitionUpdateRequest(emrClusterDefinitionConfiguration));
                    fail(String.format("Should throw an IllegalArgumentException when \"%s\" required AWS node tag is not specified.", mandatoryAwsTagName));
                }
                catch (IllegalArgumentException e)
                {
                    assertEquals(String.format("Mandatory AWS tag not specified: \"%s\"", mandatoryAwsTagName), e.getMessage());
                }
            }
        }
        finally
        {
            // Restore the property sources so we don't affect other tests.
            restorePropertySourceInEnvironment();
        }
    }

    @Test
    public void testDeleteEmrClusterDefinition() throws Exception
    {
        // Create and persist the namespace entity.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Create and persist the EMR cluster definition entity.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Validate that this EMR cluster definition exists.
        assertNotNull(emrClusterDefinitionDao.getEmrClusterDefinitionByAltKey(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME));

        executeWithoutLogging(EmrClusterDefinitionServiceImpl.class, () -> {
            // Delete this EMR cluster definition.
            EmrClusterDefinitionInformation deletedEmrClusterDefinition =
                emrClusterDefinitionRestController.deleteEmrClusterDefinition(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME);

            // Validate the returned object.
            validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), deletedEmrClusterDefinition);
        });

        // Ensure that this EMR cluster definition is no longer there.
        assertNull(emrClusterDefinitionDao.getEmrClusterDefinitionByAltKey(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME));
    }

    @Test
    public void testDeleteEmrClusterDefinitionMissingRequiredParameters() throws Exception
    {
        // Try to perform a delete without specifying a namespace.
        try
        {
            emrClusterDefinitionRestController.deleteEmrClusterDefinition(BLANK_TEXT, EMR_CLUSTER_DEFINITION_NAME);
            fail("Should throw an IllegalArgumentException when namespace is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A namespace must be specified.", e.getMessage());
        }

        // Try to perform a delete without specifying an EMR cluster definition name.
        try
        {
            emrClusterDefinitionRestController.deleteEmrClusterDefinition(NAMESPACE, BLANK_TEXT);
            fail("Should throw an IllegalArgumentException when EMR cluster definition name is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An EMR cluster definition name must be specified.", e.getMessage());
        }
    }

    @Test
    public void testDeleteEmrClusterDefinitionTrimParameters() throws Exception
    {
        // Create and persist the namespace entity.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE);

        // Create and persist the EMR cluster definition entity.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Validate that this EMR cluster definition exists.
        assertNotNull(emrClusterDefinitionDao.getEmrClusterDefinitionByAltKey(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME));

        executeWithoutLogging(EmrClusterDefinitionServiceImpl.class, () -> {
            // Delete this EMR cluster definition by passing namespace and EMR cluster definition name with leading and trailing whitespace characters.
            EmrClusterDefinitionInformation deletedEmrClusterDefinition =
                emrClusterDefinitionRestController.deleteEmrClusterDefinition(addWhitespace(NAMESPACE), addWhitespace(EMR_CLUSTER_DEFINITION_NAME));

            // Validate the returned object.
            validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE, EMR_CLUSTER_DEFINITION_NAME,
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), deletedEmrClusterDefinition);
        });

        // Ensure that this EMR cluster definition is no longer there.
        assertNull(emrClusterDefinitionDao.getEmrClusterDefinitionByAltKey(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME));
    }

    @Test
    public void testDeleteEmrClusterDefinitionUpperCaseParameters() throws Exception
    {
        // Create and persist the namespace entity with a lowercase name.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE.toLowerCase());

        // Create and persist the EMR cluster definition entity with a lowercase name.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME.toLowerCase(),
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Validate that this EMR cluster definition exists.
        assertNotNull(emrClusterDefinitionDao.getEmrClusterDefinitionByAltKey(NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase()));

        executeWithoutLogging(EmrClusterDefinitionServiceImpl.class, () -> {
            // Delete this EMR cluster definition by passing the EMR cluster definition name key parameters in upper case.
            EmrClusterDefinitionInformation deletedEmrClusterDefinition =
                emrClusterDefinitionRestController.deleteEmrClusterDefinition(NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase());

            // Validate the returned object.
            validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase(),
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), deletedEmrClusterDefinition);
        });

        // Ensure that this EMR cluster definition is no longer there.
        assertNull(emrClusterDefinitionDao.getEmrClusterDefinitionByAltKey(NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase()));
    }

    @Test
    public void testDeleteEmrClusterDefinitionLowerCaseParameters() throws Exception
    {
        // Create and persist the namespace entity with an uppercase name.
        NamespaceEntity namespaceEntity = namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE.toUpperCase());

        // Create and persist the EMR cluster definition entity with an uppercase name.
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = emrClusterDefinitionDaoTestHelper
            .createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME.toUpperCase(),
                getTestEmrClusterDefinitionConfigurationXml(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH));

        // Validate that this EMR cluster definition exists.
        assertNotNull(emrClusterDefinitionDao.getEmrClusterDefinitionByAltKey(NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase()));

        executeWithoutLogging(EmrClusterDefinitionServiceImpl.class, () -> {
            // Delete this EMR cluster definition by passing the EMR cluster definition name key parameters in lower case.
            EmrClusterDefinitionInformation deletedEmrClusterDefinition =
                emrClusterDefinitionRestController.deleteEmrClusterDefinition(NAMESPACE.toLowerCase(), EMR_CLUSTER_DEFINITION_NAME.toLowerCase());

            // Validate the returned object.
            validateEmrClusterDefinition(emrClusterDefinitionEntity.getId(), NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase(),
                getTestEmrClusterDefinitionConfiguration(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH), deletedEmrClusterDefinition);
        });

        // Ensure that this EMR cluster definition is no longer there.
        assertNull(emrClusterDefinitionDao.getEmrClusterDefinitionByAltKey(NAMESPACE.toUpperCase(), EMR_CLUSTER_DEFINITION_NAME.toUpperCase()));
    }

    @Test
    public void testDeleteEmrClusterDefinitionEmrClusterDefinitionNoExists() throws Exception
    {
        // Try to perform a delete using a non-existing EMR cluster definition name.
        String testEmrClusterDefinitionName = "I_DO_NOT_EXIST";
        try
        {
            emrClusterDefinitionRestController.deleteEmrClusterDefinition(NAMESPACE, testEmrClusterDefinitionName);
            fail("Should throw an ObjectNotFoundException when EMR cluster definition does not exist.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("EMR cluster definition with name \"%s\" doesn't exist for namespace \"%s\".", testEmrClusterDefinitionName, NAMESPACE),
                e.getMessage());
        }
    }

    @Test
    public void testGetEmrClusterDefinitions() throws Exception
    {
        // Create and persist an EMR cluster definition entity.
        emrClusterDefinitionDaoTestHelper.createEmrClusterDefinitionEntity(namespaceDaoTestHelper.createNamespaceEntity(NAMESPACE), EMR_CLUSTER_DEFINITION_NAME,
            IOUtils.toString(resourceLoader.getResource(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH).getInputStream()));

        // Create an EMR cluster definition key.
        EmrClusterDefinitionKey emrClusterDefinitionKey = new EmrClusterDefinitionKey(NAMESPACE, EMR_CLUSTER_DEFINITION_NAME);

        // Retrieve and validate EMR cluster definition keys.
        assertEquals(new EmrClusterDefinitionKeys(Arrays.asList(emrClusterDefinitionKey)),
            emrClusterDefinitionRestController.getEmrClusterDefinitions(NAMESPACE));
    }

    /**
     * Returns the contents loaded from the specified XML file.
     *
     * @param xmlFileClasspath the XML file classpath to be loaded
     *
     * @return the XML contents loaded from the specified XML file
     */
    private String getTestEmrClusterDefinitionConfigurationXml(String xmlFileClasspath) throws Exception
    {
        return IOUtils.toString(resourceLoader.getResource(xmlFileClasspath).getInputStream());
    }

    /**
     * Loads from the specified XML file and returns a test EMR cluster definition configuration.
     *
     * @param xmlFileClasspath the XML file classpath to be loaded
     *
     * @return the newly created EMR cluster definition configuration instance
     */
    private EmrClusterDefinition getTestEmrClusterDefinitionConfiguration(String xmlFileClasspath) throws Exception
    {
        String xmlString = getTestEmrClusterDefinitionConfigurationXml(xmlFileClasspath);
        return xmlHelper.unmarshallXmlToObject(EmrClusterDefinition.class, xmlString);
    }

    /**
     * Creates a new EMR cluster definition create request.
     *
     * @param namespace the namespace
     * @param emrClusterDefinitionName the EMR cluster definition name
     * @param emrClusterDefinitionConfiguration the EMR cluster definition configuration
     *
     * @return the newly created EMR cluster definition create request
     */
    private EmrClusterDefinitionCreateRequest createEmrClusterDefinitionCreateRequest(String namespace, String emrClusterDefinitionName,
        EmrClusterDefinition emrClusterDefinitionConfiguration)
    {
        // Create a new ENR cluster definition create request.
        EmrClusterDefinitionCreateRequest request = new EmrClusterDefinitionCreateRequest();

        // Fill in the parameters.
        request.setEmrClusterDefinitionKey(new EmrClusterDefinitionKey(namespace, emrClusterDefinitionName));
        request.setEmrClusterDefinition(emrClusterDefinitionConfiguration);

        return request;
    }

    /**
     * Creates a new EMR cluster definition update request.
     *
     * @param emrClusterDefinitionConfiguration the EMR cluster definition configuration
     *
     * @return the newly created EMR cluster definition update request
     */
    private EmrClusterDefinitionUpdateRequest createEmrClusterDefinitionUpdateRequest(EmrClusterDefinition emrClusterDefinitionConfiguration)
    {
        // Create a new ENR cluster definition create request.
        EmrClusterDefinitionUpdateRequest request = new EmrClusterDefinitionUpdateRequest();

        // Fill in the parameters.
        request.setEmrClusterDefinition(emrClusterDefinitionConfiguration);

        return request;
    }

    /**
     * Validates EMR cluster definition contents against specified arguments.
     *
     * @param expectedEmrClusterDefinitionId the expected EMR cluster definition ID
     * @param expectedNamespace the expected namespace
     * @param expectedEmrClusterDefinitionName the expected EMR cluster definition name
     * @param expectedEmrClusterConfiguration the expected EMR cluster configuration
     * @param actualEmrClusterDefinition the EMR cluster definition object instance to be validated
     */
    private void validateEmrClusterDefinition(Integer expectedEmrClusterDefinitionId, String expectedNamespace, String expectedEmrClusterDefinitionName,
        EmrClusterDefinition expectedEmrClusterConfiguration, EmrClusterDefinitionInformation actualEmrClusterDefinition)
    {
        assertNotNull(actualEmrClusterDefinition);
        if (expectedEmrClusterDefinitionId != null)
        {
            assertEquals(expectedEmrClusterDefinitionId, Integer.valueOf(actualEmrClusterDefinition.getId()));
        }
        assertEquals(expectedNamespace, actualEmrClusterDefinition.getEmrClusterDefinitionKey().getNamespace());
        assertEquals(expectedEmrClusterDefinitionName, actualEmrClusterDefinition.getEmrClusterDefinitionKey().getEmrClusterDefinitionName());
        assertEquals(expectedEmrClusterConfiguration, actualEmrClusterDefinition.getEmrClusterDefinition());
    }

    /**
     * Builds a list of all specified node tags, except for the excluded one.
     *
     * @param nodeTagNames the list of the node tag names
     * @param excludedNodeTagName the name of the node tag that should be excluded from the list
     *
     * @return the list of the node tags
     */
    private List<NodeTag> getTestNodeTags(List<String> nodeTagNames, String excludedNodeTagName)
    {
        // Build a  list of the node tags that would have all specified node tags, except for the excluded one.
        List<NodeTag> nodeTags = new ArrayList<>();
        for (String nodeTagName : nodeTagNames)
        {
            if (!nodeTagName.equals(excludedNodeTagName))
            {
                NodeTag nodeTag = new NodeTag();
                nodeTag.setTagName(nodeTagName);
                nodeTag.setTagValue(ATTRIBUTE_VALUE_1);
                nodeTags.add(nodeTag);
            }
        }
        return nodeTags;
    }
}
