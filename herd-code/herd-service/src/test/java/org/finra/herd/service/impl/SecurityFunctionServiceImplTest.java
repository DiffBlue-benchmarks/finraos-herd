package org.finra.herd.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.finra.herd.dao.SecurityFunctionDao;
import org.finra.herd.model.AlreadyExistsException;
import org.finra.herd.model.api.xml.SecurityFunction;
import org.finra.herd.model.api.xml.SecurityFunctionCreateRequest;
import org.finra.herd.model.api.xml.SecurityFunctionKey;
import org.finra.herd.model.api.xml.SecurityFunctionKeys;
import org.finra.herd.model.jpa.SecurityFunctionEntity;
import org.finra.herd.service.AbstractServiceTest;
import org.finra.herd.service.SecurityFunctionService;
import org.finra.herd.service.helper.AlternateKeyHelper;
import org.finra.herd.service.helper.SecurityFunctionDaoHelper;

/**
 * This class tests functionality within the security function service implementation.
 */
public class SecurityFunctionServiceImplTest extends AbstractServiceTest
{
    private static final String SECURITY_FUNCTION_NAME_WITH_EXTRA_SPACES = SECURITY_FUNCTION + "    ";

    private static final SecurityFunctionCreateRequest SECURITY_FUNCTION_CREATE_REQUEST = new SecurityFunctionCreateRequest()
    {{
        setSecurityFunctionName(SECURITY_FUNCTION);
    }};

    private static final SecurityFunctionCreateRequest SECURITY_FUNCTION_CREATE_REQUEST_WITH_EXTRA_SPACES_IN_NAME = new SecurityFunctionCreateRequest()
    {{
        setSecurityFunctionName(SECURITY_FUNCTION_NAME_WITH_EXTRA_SPACES);
    }};

    private static final SecurityFunctionEntity SECURITY_FUNCTION_ENTITY = new SecurityFunctionEntity()
    {{
        setCode(SECURITY_FUNCTION);
        setCreatedBy(CREATED_BY);
        setUpdatedBy(CREATED_BY);
        setCreatedOn(new Timestamp(CREATED_ON.getMillisecond()));
    }};

    private static final List<String> ALL_SECURITY_FUNCTION_NAMES = Arrays.asList(SECURITY_FUNCTION, SECURITY_FUNCTION_2, SECURITY_FUNCTION_3);

    @InjectMocks
    private SecurityFunctionService securityFunctionService = new SecurityFunctionServiceImpl();

    @Mock
    private AlternateKeyHelper alternateKeyHelper;

    @Mock
    private SecurityFunctionDao securityFunctionDao;

    @Mock
    private SecurityFunctionDaoHelper securityFunctionDaoHelper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateSecurityFunction()
    {
        validateCreateSecurityFunction(SECURITY_FUNCTION_CREATE_REQUEST, SECURITY_FUNCTION);
    }

    @Test
    public void testCreateSecurityFunctionWithExtraSpacesInName()
    {
        validateCreateSecurityFunction(SECURITY_FUNCTION_CREATE_REQUEST_WITH_EXTRA_SPACES_IN_NAME, SECURITY_FUNCTION_NAME_WITH_EXTRA_SPACES);
    }

    @Test
    public void testCreateSecurityFunctionAlreadyExists()
    {
        expectedException.expect(AlreadyExistsException.class);
        expectedException.expectMessage(String.format("Unable to create security function \"%s\" because it already exists.", SECURITY_FUNCTION));

        when(securityFunctionDao.getSecurityFunctionByName(SECURITY_FUNCTION)).thenReturn(SECURITY_FUNCTION_ENTITY);
        when(alternateKeyHelper.validateStringParameter(anyString(), anyString())).thenReturn(SECURITY_FUNCTION);
        securityFunctionService.createSecurityFunction(SECURITY_FUNCTION_CREATE_REQUEST);
    }

    @Test
    public void testGetSecurityFunction()
    {
        validateGetSecurityFunctionByName(SECURITY_FUNCTION);
    }


    @Test
    public void testGetSecurityFunctionWithExtraSpacesInName()
    {
        validateGetSecurityFunctionByName(SECURITY_FUNCTION_NAME_WITH_EXTRA_SPACES);
    }

    @Test
    public void testDeleteSecurityFunction()
    {
        validateDeleteSecurityFunctionByName(SECURITY_FUNCTION);
    }

    @Test
    public void testDeleteSecurityFunctionWithExtraSpacesInName()
    {
        validateDeleteSecurityFunctionByName(SECURITY_FUNCTION_NAME_WITH_EXTRA_SPACES);
    }

    @Test
    public void testGetSecurityFunctions()
    {
        when(securityFunctionDao.getUnrestrictedSecurityFunctions()).thenReturn(ALL_SECURITY_FUNCTION_NAMES);

        SecurityFunctionKeys securityFunctionKeys = securityFunctionService.getSecurityFunctions();

        assertNotNull(securityFunctionKeys);
        List<SecurityFunctionKey> securityFunctionKeyList = securityFunctionKeys.getSecurityFunctionKeys();
        assertEquals(ALL_SECURITY_FUNCTION_NAMES.size(), securityFunctionKeyList.size());

        // verify the order is reserved
        assertEquals(SECURITY_FUNCTION, securityFunctionKeyList.get(0).getSecurityFunctionName());
        assertEquals(SECURITY_FUNCTION_2, securityFunctionKeyList.get(1).getSecurityFunctionName());
        assertEquals(SECURITY_FUNCTION_3, securityFunctionKeyList.get(2).getSecurityFunctionName());

        verify(securityFunctionDao, times(1)).getUnrestrictedSecurityFunctions();
    }

    @Test
    public void testGetSecurityFunctionsEmptyList()
    {
        when(securityFunctionDao.getUnrestrictedSecurityFunctions()).thenReturn(Collections.emptyList());
        SecurityFunctionKeys securityFunctionKeys = securityFunctionService.getSecurityFunctions();

        assertNotNull(securityFunctionKeys);
        assertEquals(0, securityFunctionKeys.getSecurityFunctionKeys().size());

        verify(securityFunctionDao, times(1)).getUnrestrictedSecurityFunctions();
    }

    private void validateCreateSecurityFunction(SecurityFunctionCreateRequest securityFunctionCreateRequest, String securityFunctionName)
    {
        when(securityFunctionDao.getSecurityFunctionByName(SECURITY_FUNCTION)).thenReturn(null);
        when(alternateKeyHelper.validateStringParameter(anyString(), anyString())).thenReturn(SECURITY_FUNCTION);
        when(securityFunctionDao.saveAndRefresh(any(SecurityFunctionEntity.class))).thenReturn(SECURITY_FUNCTION_ENTITY);

        SecurityFunction securityFunction = securityFunctionService.createSecurityFunction(securityFunctionCreateRequest);
        assertEquals(SECURITY_FUNCTION, securityFunction.getSecurityFunctionName());

        verify(alternateKeyHelper, times(1)).validateStringParameter("security function name", securityFunctionName);
        verify(securityFunctionDao, times(1)).getSecurityFunctionByName(SECURITY_FUNCTION);
        verify(securityFunctionDao, times(1)).saveAndRefresh(any(SecurityFunctionEntity.class));
    }

    private void validateGetSecurityFunctionByName(String securityFunctionName)
    {
        when(securityFunctionDaoHelper.getSecurityFunctionEntityByName(SECURITY_FUNCTION)).thenReturn(SECURITY_FUNCTION_ENTITY);
        when(alternateKeyHelper.validateStringParameter(anyString(), anyString())).thenReturn(SECURITY_FUNCTION);

        SecurityFunction securityFunction = securityFunctionService.getSecurityFunction(securityFunctionName);
        assertEquals(SECURITY_FUNCTION, securityFunction.getSecurityFunctionName());
        verify(alternateKeyHelper, times(1)).validateStringParameter("security function name", securityFunctionName);
        verify(securityFunctionDaoHelper, times(1)).getSecurityFunctionEntityByName(SECURITY_FUNCTION);
    }

    private void validateDeleteSecurityFunctionByName(String securityFunctionName)
    {
        when(securityFunctionDaoHelper.getSecurityFunctionEntityByName(SECURITY_FUNCTION)).thenReturn(SECURITY_FUNCTION_ENTITY);
        when(alternateKeyHelper.validateStringParameter(anyString(), anyString())).thenReturn(SECURITY_FUNCTION);

        SecurityFunction securityFunction = securityFunctionService.deleteSecurityFunction(securityFunctionName);
        assertEquals(SECURITY_FUNCTION, securityFunction.getSecurityFunctionName());
        verify(alternateKeyHelper, times(1)).validateStringParameter("security function name", securityFunctionName);
        verify(securityFunctionDaoHelper, times(1)).getSecurityFunctionEntityByName(SECURITY_FUNCTION);
        verify(securityFunctionDao, times(1)).delete(SECURITY_FUNCTION_ENTITY);
    }
}
