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
package org.finra.herd.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.get.GetIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.finra.herd.core.helper.ConfigurationHelper;
import org.finra.herd.dao.BusinessObjectDefinitionDao;
import org.finra.herd.dao.SearchIndexDao;
import org.finra.herd.model.api.xml.SearchIndex;
import org.finra.herd.model.api.xml.SearchIndexCreateRequest;
import org.finra.herd.model.api.xml.SearchIndexKey;
import org.finra.herd.model.api.xml.SearchIndexKeys;
import org.finra.herd.model.api.xml.SearchIndexSettings;
import org.finra.herd.model.jpa.SearchIndexEntity;
import org.finra.herd.model.jpa.SearchIndexStatusEntity;
import org.finra.herd.model.jpa.SearchIndexTypeEntity;
import org.finra.herd.service.functional.SearchFunctions;
import org.finra.herd.service.helper.AlternateKeyHelper;
import org.finra.herd.service.helper.BusinessObjectDefinitionHelper;
import org.finra.herd.service.helper.ConfigurationDaoHelper;
import org.finra.herd.service.helper.SearchIndexDaoHelper;
import org.finra.herd.service.helper.SearchIndexStatusDaoHelper;
import org.finra.herd.service.helper.SearchIndexTypeDaoHelper;
import org.finra.herd.service.impl.SearchIndexServiceImpl;

/**
 * This class tests search index functionality within the search index service.
 */
public class SearchIndexServiceTest extends AbstractServiceTest
{
    @Mock
    private AlternateKeyHelper alternateKeyHelper;

    @Mock
    private BusinessObjectDefinitionDao businessObjectDefinitionDao;

    @Mock
    private BusinessObjectDefinitionHelper businessObjectDefinitionHelper;

    @Mock
    private ConfigurationDaoHelper configurationDaoHelper;

    @Mock
    private ConfigurationHelper configurationHelper;

    @Mock
    private SearchFunctions searchFunctions;

    @Mock
    private SearchIndexDao searchIndexDao;

    @Mock
    private SearchIndexDaoHelper searchIndexDaoHelper;

    @Mock
    private SearchIndexHelperService searchIndexHelperService;

    @InjectMocks
    private SearchIndexServiceImpl searchIndexService;

    @Mock
    private SearchIndexStatusDaoHelper searchIndexStatusDaoHelper;

    @Mock
    private SearchIndexTypeDaoHelper searchIndexTypeDaoHelper;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Ignore
    @Test
    public void testCreateSearchIndex()
    {
        // Create a search index create request.
        SearchIndexCreateRequest searchIndexCreateRequest = new SearchIndexCreateRequest(new SearchIndexKey(SEARCH_INDEX_NAME), SEARCH_INDEX_TYPE);

        // Create a search index.
        SearchIndex response = searchIndexService.createSearchIndex(searchIndexCreateRequest);
    }

    @Ignore
    @Test
    public void testDeleteSearchIndex()
    {
        // Create a search index key.
        SearchIndexKey searchIndexKey = new SearchIndexKey(SEARCH_INDEX_NAME);

        // Delete a search index.
        SearchIndex response = searchIndexService.deleteSearchIndex(searchIndexKey);
    }

    @Test
    public void testGetSearchIndex()
    {
        // Create a search index key.
        SearchIndexKey searchIndexKey = new SearchIndexKey(SEARCH_INDEX_NAME);

        // Create the relative search index entities.
        SearchIndexTypeEntity searchIndexTypeEntity = new SearchIndexTypeEntity();
        searchIndexTypeEntity.setCode(SEARCH_INDEX_TYPE);
        SearchIndexStatusEntity searchIndexStatusEntity = new SearchIndexStatusEntity();
        searchIndexStatusEntity.setCode(SEARCH_INDEX_STATUS);
        SearchIndexEntity searchIndexEntity = new SearchIndexEntity();
        searchIndexEntity.setName(SEARCH_INDEX_NAME);
        searchIndexEntity.setType(searchIndexTypeEntity);
        searchIndexEntity.setStatus(searchIndexStatusEntity);
        searchIndexEntity.setCreatedBy(USER_ID);
        searchIndexEntity.setCreatedOn(new Timestamp(CREATED_ON.toGregorianCalendar().getTimeInMillis()));
        searchIndexEntity.setUpdatedOn(new Timestamp(UPDATED_ON.toGregorianCalendar().getTimeInMillis()));

        // Mock some of the external call responses.
        AdminClient mockedAdminClient = mock(AdminClient.class);
        IndicesAdminClient mockedIndiciesAdminClient = mock(IndicesAdminClient.class);
        GetIndexRequestBuilder mockedGetIndexRequestBuilder = mock(GetIndexRequestBuilder.class);
        @SuppressWarnings("unchecked")
        ListenableActionFuture<GetIndexResponse> mockedListenableActionFutureActionResponse = mock(ListenableActionFuture.class);
        GetIndexResponse mockedGetIndexResponse = mock(GetIndexResponse.class);

        // Create a search index get settings response.
        ImmutableOpenMap<String, Settings> getIndexResponseSettings = ImmutableOpenMap.<String, Settings>builder().fPut(SEARCH_INDEX_NAME,
            Settings.builder().put(IndexMetaData.SETTING_CREATION_DATE, SEARCH_INDEX_SETTING_CREATION_DATE)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, SEARCH_INDEX_SETTING_NUMBER_OF_REPLICAS)
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, SEARCH_INDEX_SETTING_NUMBER_OF_SHARDS)
                .put(IndexMetaData.SETTING_INDEX_PROVIDED_NAME, SEARCH_INDEX_SETTING_INDEX_PROVIDED_NAME)
                .put(IndexMetaData.SETTING_INDEX_UUID, SEARCH_INDEX_SETTING_INDEX_UUID).build()).build();

        // Mock the external calls.
        when(alternateKeyHelper.validateStringParameter("Search index name", SEARCH_INDEX_NAME)).thenReturn(SEARCH_INDEX_NAME);
        when(searchIndexDaoHelper.getSearchIndexEntity(searchIndexKey)).thenReturn(searchIndexEntity);
        when(searchIndexHelperService.getAdminClient()).thenReturn(mockedAdminClient);
        when(mockedAdminClient.indices()).thenReturn(mockedIndiciesAdminClient);
        when(mockedIndiciesAdminClient.prepareGetIndex()).thenReturn(mockedGetIndexRequestBuilder);
        when(mockedGetIndexRequestBuilder.setIndices(SEARCH_INDEX_NAME)).thenReturn(mockedGetIndexRequestBuilder);
        when(mockedGetIndexRequestBuilder.execute()).thenReturn(mockedListenableActionFutureActionResponse);
        when(mockedListenableActionFutureActionResponse.actionGet()).thenReturn(mockedGetIndexResponse);
        when(mockedGetIndexResponse.getSettings()).thenReturn(getIndexResponseSettings);

        // Get a search index.
        SearchIndex response = searchIndexService.getSearchIndex(searchIndexKey);

        // Verify the external calls.
        verify(alternateKeyHelper).validateStringParameter("Search index name", SEARCH_INDEX_NAME);
        verify(searchIndexDaoHelper).getSearchIndexEntity(searchIndexKey);
        verify(searchIndexHelperService).getAdminClient();
        verifyNoMoreInteractions(alternateKeyHelper, businessObjectDefinitionDao, businessObjectDefinitionHelper, configurationDaoHelper, configurationHelper,
            searchFunctions, searchIndexDao, searchIndexDaoHelper, searchIndexHelperService, searchIndexStatusDaoHelper, searchIndexTypeDaoHelper);

        // Validate the returned object.
        assertEquals(new SearchIndex(searchIndexKey, SEARCH_INDEX_TYPE, SEARCH_INDEX_STATUS,
            new SearchIndexSettings(SEARCH_INDEX_SETTING_CREATION_DATE, SEARCH_INDEX_SETTING_NUMBER_OF_REPLICAS, SEARCH_INDEX_SETTING_NUMBER_OF_SHARDS,
                SEARCH_INDEX_SETTING_INDEX_PROVIDED_NAME, SEARCH_INDEX_SETTING_INDEX_UUID), USER_ID, CREATED_ON, UPDATED_ON), response);
    }

    @Test
    public void testGetSearchIndexes()
    {
        // Create a list of search index keys.
        List<SearchIndexKey> searchIndexKeys = Arrays.asList(new SearchIndexKey(SEARCH_INDEX_NAME), new SearchIndexKey(SEARCH_INDEX_NAME_2));

        // Mock the external calls.
        when(searchIndexDao.getSearchIndexes()).thenReturn(searchIndexKeys);

        // Get search indexes.
        SearchIndexKeys response = searchIndexService.getSearchIndexes();

        // Verify the external calls.
        verify(searchIndexDao).getSearchIndexes();
        verifyNoMoreInteractions(alternateKeyHelper, businessObjectDefinitionDao, businessObjectDefinitionHelper, configurationDaoHelper, configurationHelper,
            searchFunctions, searchIndexDao, searchIndexDaoHelper, searchIndexHelperService, searchIndexStatusDaoHelper, searchIndexTypeDaoHelper);

        // Validate the returned object.
        assertEquals(new SearchIndexKeys(searchIndexKeys), response);
    }
}
