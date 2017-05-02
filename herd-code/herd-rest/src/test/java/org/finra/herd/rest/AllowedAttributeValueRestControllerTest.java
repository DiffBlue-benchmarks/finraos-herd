package org.finra.herd.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.finra.herd.model.api.xml.AllowedAttributeValuesCreateRequest;
import org.finra.herd.model.api.xml.AllowedAttributeValuesDeleteRequest;
import org.finra.herd.model.api.xml.AllowedAttributeValuesInformation;
import org.finra.herd.model.api.xml.AttributeValueListKey;
import org.finra.herd.service.AllowedAttributeValueService;

/**
 * This class tests the functionality of allowed attribute value rest controller
 */
public class AllowedAttributeValueRestControllerTest extends AbstractRestTest
{
    @Mock
    private AllowedAttributeValueService allowedAttributeValueService;

    @InjectMocks
    private AllowedAttributeValueRestController allowedAttributeValueRestController;

    @Before()
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateAllowedAttributeValue()
    {
        // Create attribute value list key.
        AttributeValueListKey attributeValueListKey = new AttributeValueListKey(NAMESPACE_CODE, ATTRIBUTE_VALUE_LIST_NAME);
        AllowedAttributeValuesCreateRequest request = new AllowedAttributeValuesCreateRequest(attributeValueListKey, Arrays.asList(ALLOWED_ATTRIBUTE_VALUE));

        //Allowed Attribute Values Information
        AllowedAttributeValuesInformation allowedAttributeValuesInformation = new AllowedAttributeValuesInformation();
        allowedAttributeValuesInformation.setAttributeValueListKey(attributeValueListKey);
        allowedAttributeValuesInformation.setAllowedAttributeValues(Arrays.asList(ALLOWED_ATTRIBUTE_VALUE));

        //mock calls to external method
        when(allowedAttributeValueService.createAllowedAttributeValues(request)).thenReturn(allowedAttributeValuesInformation);

        //call method under  test
        AllowedAttributeValuesInformation response = allowedAttributeValueRestController.createAllowedAttributeValues(request);

        //verify
        verify(allowedAttributeValueService).createAllowedAttributeValues(request);
        verifyNoMoreInteractions(allowedAttributeValueService);

        //validate
        assertEquals(allowedAttributeValuesInformation, response);
    }

    @Test
    public void testDeleteAllowedAttributeValue()
    {
        // Create attribute value list key.
        AttributeValueListKey attributeValueListKey = new AttributeValueListKey(NAMESPACE_CODE, ATTRIBUTE_VALUE_LIST_NAME);
        AllowedAttributeValuesDeleteRequest request = new AllowedAttributeValuesDeleteRequest(attributeValueListKey, Arrays.asList(ALLOWED_ATTRIBUTE_VALUE));

        //Allowed Attribute Values Information
        AllowedAttributeValuesInformation allowedAttributeValuesInformation = new AllowedAttributeValuesInformation();
        allowedAttributeValuesInformation.setAttributeValueListKey(attributeValueListKey);
        allowedAttributeValuesInformation.setAllowedAttributeValues(Arrays.asList(ALLOWED_ATTRIBUTE_VALUE));

        //mock calls to external method
        when(allowedAttributeValueService.deleteAllowedAttributeValues(request)).thenReturn(allowedAttributeValuesInformation);

        //call method under  test
        AllowedAttributeValuesInformation response = allowedAttributeValueRestController.deleteAllowedAttributeValues(request);

        //verify
        verify(allowedAttributeValueService).deleteAllowedAttributeValues(request);
        verifyNoMoreInteractions(allowedAttributeValueService);

        //validate
        assertEquals(allowedAttributeValuesInformation, response);
    }

    @Test
    public void testGetAllowedAttributeValue()
    {
        // Create attribute value list key.
        AttributeValueListKey attributeValueListKey = new AttributeValueListKey(NAMESPACE_CODE, ATTRIBUTE_VALUE_LIST_NAME);

        //Allowed Attribute Values Information
        AllowedAttributeValuesInformation allowedAttributeValuesInformation = new AllowedAttributeValuesInformation();
        allowedAttributeValuesInformation.setAttributeValueListKey(attributeValueListKey);
        allowedAttributeValuesInformation.setAllowedAttributeValues(Arrays.asList(ALLOWED_ATTRIBUTE_VALUE));

        //mock calls to external method
        when(allowedAttributeValueService.getAllowedAttributeValues(attributeValueListKey)).thenReturn(allowedAttributeValuesInformation);

        //call method under  test
        AllowedAttributeValuesInformation response = allowedAttributeValueRestController.getAllowedAttributeValues(NAMESPACE_CODE, ATTRIBUTE_VALUE_LIST_NAME);

        //verify
        verify(allowedAttributeValueService).getAllowedAttributeValues(attributeValueListKey);
        verifyNoMoreInteractions(allowedAttributeValueService);

        //validate
        assertEquals(allowedAttributeValuesInformation, response);
    }
}
