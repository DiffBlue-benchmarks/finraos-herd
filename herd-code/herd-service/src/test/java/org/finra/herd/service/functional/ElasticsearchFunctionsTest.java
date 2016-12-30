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
package org.finra.herd.service.functional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * ElasticsearchFunctionsTest
 */
public class ElasticsearchFunctionsTest
{
    @InjectMocks
    private ElasticsearchFunctions searchFunctions;

    @Mock
    private TransportClient transportClient;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIndexFunction()
    {
        QuadConsumer<String, String, String, String> indexFunction = searchFunctions.getIndexFunction();
        assertThat("Function is null.", indexFunction, not(nullValue()));
        assertThat("Index function not an instance of QuadConsumer.", indexFunction, instanceOf(QuadConsumer.class));

        IndexRequestBuilder indexRequestBuilder = mock(IndexRequestBuilder.class);
        @SuppressWarnings("unchecked")
        ListenableActionFuture<IndexResponse> listenableActionFuture = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(indexRequestBuilder);
        when(indexRequestBuilder.execute()).thenReturn(listenableActionFuture);

        // Call the method under test
        indexFunction.accept("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(indexRequestBuilder, times(1)).execute();
    }

    @Test
    public void testValidateFunctionIndex()
    {
        QuadConsumer<String, String, String, String> validateFunction = searchFunctions.getValidateFunction();
        assertThat("Function is null.", validateFunction, not(nullValue()));
        assertThat("Validate function not an instance of QuadConsumer.", validateFunction, instanceOf(QuadConsumer.class));

        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        GetResponse getResponse = mock(GetResponse.class);
        IndexRequestBuilder indexRequestBuilder = mock(IndexRequestBuilder.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<GetResponse> listenableActionFutureGetResponse = mock(ListenableActionFuture.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<IndexResponse> listenableActionFutureIndexResponse = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(getRequestBuilder);
        when(getRequestBuilder.execute()).thenReturn(listenableActionFutureGetResponse);
        when(listenableActionFutureGetResponse.actionGet()).thenReturn(getResponse);
        when(getResponse.getSourceAsString()).thenReturn(null);
        when(transportClient.prepareIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(indexRequestBuilder);
        when(indexRequestBuilder.execute()).thenReturn(listenableActionFutureIndexResponse);

        // Call the method under test
        validateFunction.accept("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(getRequestBuilder, times(1)).execute();
        verify(listenableActionFutureGetResponse, times(1)).actionGet();
        verify(getResponse, times(1)).getSourceAsString();
        verify(transportClient, times(1)).prepareIndex("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(indexRequestBuilder, times(1)).execute();
    }

    @Test
    public void testValidateFunctionUpdate()
    {
        QuadConsumer<String, String, String, String> validateFunction = searchFunctions.getValidateFunction();
        assertThat("Function is null.", validateFunction, not(nullValue()));
        assertThat("Validate function not an instance of QuadConsumer.", validateFunction, instanceOf(QuadConsumer.class));

        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        GetResponse getResponse = mock(GetResponse.class);
        UpdateRequestBuilder updateRequestBuilder = mock(UpdateRequestBuilder.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<GetResponse> listenableActionFutureGetResponse = mock(ListenableActionFuture.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<UpdateResponse> listenableActionFutureUpdateResponse = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(getRequestBuilder);
        when(getRequestBuilder.execute()).thenReturn(listenableActionFutureGetResponse);
        when(listenableActionFutureGetResponse.actionGet()).thenReturn(getResponse);
        when(getResponse.getSourceAsString()).thenReturn("JSON_UPDATE");
        when(transportClient.prepareUpdate("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(updateRequestBuilder);
        when(updateRequestBuilder.execute()).thenReturn(listenableActionFutureUpdateResponse);

        // Call the method under test
        validateFunction.accept("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(getRequestBuilder, times(1)).execute();
        verify(listenableActionFutureGetResponse, times(1)).actionGet();
        verify(getResponse, times(1)).getSourceAsString();
        verify(transportClient, times(1)).prepareUpdate("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(updateRequestBuilder, times(1)).execute();
    }

    @Test
    public void testValidateFunctionNoActionRequiredValidDocument()
    {
        QuadConsumer<String, String, String, String> validateFunction = searchFunctions.getValidateFunction();
        assertThat("Function is null.", validateFunction, not(nullValue()));
        assertThat("Validate function not an instance of QuadConsumer.", validateFunction, instanceOf(QuadConsumer.class));

        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        GetResponse getResponse = mock(GetResponse.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<GetResponse> listenableActionFutureGetResponse = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(getRequestBuilder);
        when(getRequestBuilder.execute()).thenReturn(listenableActionFutureGetResponse);
        when(listenableActionFutureGetResponse.actionGet()).thenReturn(getResponse);
        when(getResponse.getSourceAsString()).thenReturn("JSON");

        // Call the method under test
        validateFunction.accept("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(getRequestBuilder, times(1)).execute();
        verify(listenableActionFutureGetResponse, times(1)).actionGet();
        verify(getResponse, times(1)).getSourceAsString();
    }

    @Test
    public void testIsValidFunction()
    {
        QuadPredicate<String, String, String, String> isValidFunction = searchFunctions.getIsValidFunction();
        assertThat("Function is null.", isValidFunction, not(nullValue()));
        assertThat("Is valid function not an instance of QuadPredicate.", isValidFunction, instanceOf(QuadPredicate.class));

        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        GetResponse getResponse = mock(GetResponse.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<GetResponse> listenableActionFutureGetResponse = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(getRequestBuilder);
        when(getRequestBuilder.execute()).thenReturn(listenableActionFutureGetResponse);
        when(listenableActionFutureGetResponse.actionGet()).thenReturn(getResponse);
        when(getResponse.getSourceAsString()).thenReturn("JSON");

        // Call the method under test
        boolean isValid = isValidFunction.test("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");

        assertThat("IsValid is false when it should have been true.", isValid, is(true));

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(getRequestBuilder, times(1)).execute();
        verify(listenableActionFutureGetResponse, times(1)).actionGet();
        verify(getResponse, times(1)).getSourceAsString();
    }

    @Test
    public void testIsValidFunctionEmpty()
    {
        QuadPredicate<String, String, String, String> isValidFunction = searchFunctions.getIsValidFunction();
        assertThat("Function is null.", isValidFunction, not(nullValue()));
        assertThat("Is valid function not an instance of QuadPredicate.", isValidFunction, instanceOf(QuadPredicate.class));

        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        GetResponse getResponse = mock(GetResponse.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<GetResponse> listenableActionFutureGetResponse = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(getRequestBuilder);
        when(getRequestBuilder.execute()).thenReturn(listenableActionFutureGetResponse);
        when(listenableActionFutureGetResponse.actionGet()).thenReturn(getResponse);
        when(getResponse.getSourceAsString()).thenReturn("");

        // Call the method under test
        boolean isValid = isValidFunction.test("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");

        assertThat("IsValid is true when it should have been false.", isValid, is(false));

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(getRequestBuilder, times(1)).execute();
        verify(listenableActionFutureGetResponse, times(1)).actionGet();
        verify(getResponse, times(1)).getSourceAsString();
    }

    @Test
    public void testIsValidFunctionNull()
    {
        QuadPredicate<String, String, String, String> isValidFunction = searchFunctions.getIsValidFunction();
        assertThat("Function is null.", isValidFunction, not(nullValue()));
        assertThat("Is valid function not an instance of QuadPredicate.", isValidFunction, instanceOf(QuadPredicate.class));

        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        GetResponse getResponse = mock(GetResponse.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<GetResponse> listenableActionFutureGetResponse = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(getRequestBuilder);
        when(getRequestBuilder.execute()).thenReturn(listenableActionFutureGetResponse);
        when(listenableActionFutureGetResponse.actionGet()).thenReturn(getResponse);
        when(getResponse.getSourceAsString()).thenReturn(null);

        // Call the method under test
        boolean isValid = isValidFunction.test("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");

        assertThat("IsValid is true when it should have been false.", isValid, is(false));

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(getRequestBuilder, times(1)).execute();
        verify(listenableActionFutureGetResponse, times(1)).actionGet();
        verify(getResponse, times(1)).getSourceAsString();
    }

    @Test
    public void testIsValidFunctionNotEqual()
    {
        QuadPredicate<String, String, String, String> isValidFunction = searchFunctions.getIsValidFunction();
        assertThat("Function is null.", isValidFunction, not(nullValue()));
        assertThat("Is valid function not an instance of QuadPredicate.", isValidFunction, instanceOf(QuadPredicate.class));

        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        GetResponse getResponse = mock(GetResponse.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<GetResponse> listenableActionFutureGetResponse = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(getRequestBuilder);
        when(getRequestBuilder.execute()).thenReturn(listenableActionFutureGetResponse);
        when(listenableActionFutureGetResponse.actionGet()).thenReturn(getResponse);
        when(getResponse.getSourceAsString()).thenReturn("JSON_NOT_EQUAL");

        // Call the method under test
        boolean isValid = isValidFunction.test("INDEX_NAME", "DOCUMENT_TYPE", "ID", "JSON");

        assertThat("IsValid is true when it should have been false.", isValid, is(false));

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareGet("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(getRequestBuilder, times(1)).execute();
        verify(listenableActionFutureGetResponse, times(1)).actionGet();
        verify(getResponse, times(1)).getSourceAsString();
    }

    @Test
    public void testIndexExistsFunction()
    {
        Predicate<String> indexExistsFunction = searchFunctions.getIndexExistsFunction();
        assertThat("Function is null.", indexExistsFunction, not(nullValue()));
        assertThat("Index exists function not an instance of Predicate.", indexExistsFunction, instanceOf(Predicate.class));
    }

    @Test
    public void testDeleteIndexFunction()
    {
        Consumer<String> deleteIndexFunction = searchFunctions.getDeleteIndexFunction();
        assertThat("Function is null.", deleteIndexFunction, not(nullValue()));
        assertThat("Delete index function not an instance of Consumer.", deleteIndexFunction, instanceOf(Consumer.class));
    }

    @Test
    public void testCreateIndexFunction()
    {
        TriConsumer<String, String, String> createIndexFunction = searchFunctions.getCreateIndexFunction();
        assertThat("Function is null.", createIndexFunction, not(nullValue()));
        assertThat("Create index function not an instance of TriConsumer.", createIndexFunction, instanceOf(TriConsumer.class));

    }

    @Test
    public void testDeleteDocumentByIdFunction()
    {
        TriConsumer<String, String, String> deleteDocumentByIdFunction = searchFunctions.getDeleteDocumentByIdFunction();
        assertThat("Function is null.", deleteDocumentByIdFunction, not(nullValue()));
        assertThat("Delete document by id function not an instance of TriConsumer.", deleteDocumentByIdFunction, instanceOf(TriConsumer.class));

        DeleteRequestBuilder deleteRequestBuilder = mock(DeleteRequestBuilder.class);
        @SuppressWarnings("unchecked")
        ListenableActionFuture<DeleteResponse> listenableActionFuture = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareDelete("INDEX_NAME", "DOCUMENT_TYPE", "ID")).thenReturn(deleteRequestBuilder);
        when(deleteRequestBuilder.execute()).thenReturn(listenableActionFuture);

        // Call the method under test
        deleteDocumentByIdFunction.accept("INDEX_NAME", "DOCUMENT_TYPE", "ID");

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareDelete("INDEX_NAME", "DOCUMENT_TYPE", "ID");
        verify(deleteRequestBuilder, times(1)).execute();
    }

    @Test
    public void testNumberOfTypesInIndexFunction()
    {
        BiFunction<String, String, Long> numberOfTypesInIndexFunction = searchFunctions.getNumberOfTypesInIndexFunction();
        assertThat("Function is null.", numberOfTypesInIndexFunction, not(nullValue()));
        assertThat("Number of types in index function not an instance of BiFunction.", numberOfTypesInIndexFunction, instanceOf(BiFunction.class));

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        SearchRequestBuilder searchRequestBuilderWithTypes = mock(SearchRequestBuilder.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);

        @SuppressWarnings("unchecked")
        ListenableActionFuture<SearchResponse> listenableActionFuture = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareSearch("INDEX_NAME")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setTypes("DOCUMENT_TYPE")).thenReturn(searchRequestBuilderWithTypes);
        when(searchRequestBuilderWithTypes.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.getTotalHits()).thenReturn(100L);

        // Call the method under test
        long numberOfTypesInIndex = numberOfTypesInIndexFunction.apply("INDEX_NAME", "DOCUMENT_TYPE");

        assertThat("Number of types in index is incorrect.", numberOfTypesInIndex, is(100L));

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareSearch("INDEX_NAME");
        verify(searchRequestBuilder, times(1)).setTypes("DOCUMENT_TYPE");
        verify(searchRequestBuilderWithTypes, times(1)).execute();
        verify(listenableActionFuture, times(1)).actionGet();
        verify(searchResponse, times(1)).getHits();
        verify(searchHits, times(1)).getTotalHits();
    }

    @Test
    public void testIdsInIndexFunction()
    {
        BiFunction<String, String, List<String>> idsInIndexFunction = searchFunctions.getIdsInIndexFunction();
        assertThat("Function is null.", idsInIndexFunction, not(nullValue()));
        assertThat("Ids in index function not an instance of BiFunction.", idsInIndexFunction, instanceOf(BiFunction.class));

        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        SearchRequestBuilder searchRequestBuilderWithTypes = mock(SearchRequestBuilder.class);
        SearchRequestBuilder searchRequestBuilderWithQuery = mock(SearchRequestBuilder.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);
        SearchHit searchHit1 = mock(SearchHit.class);
        SearchHit searchHit2 = mock(SearchHit.class);
        SearchHit[] searchHitArray = new SearchHit[2];
        searchHitArray[0] = searchHit1;
        searchHitArray[1] = searchHit2;

        @SuppressWarnings("unchecked")
        ListenableActionFuture<SearchResponse> listenableActionFuture = mock(ListenableActionFuture.class);

        // Mock the call to external methods
        when(transportClient.prepareSearch("INDEX_NAME")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setTypes("DOCUMENT_TYPE")).thenReturn(searchRequestBuilderWithTypes);
        when(searchRequestBuilderWithTypes.setQuery(any())).thenReturn(searchRequestBuilderWithQuery);
        when(searchRequestBuilderWithQuery.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.hits()).thenReturn(searchHitArray);

        // Call the method under test
        List<String> idsInIndex = idsInIndexFunction.apply("INDEX_NAME", "DOCUMENT_TYPE");

        assertThat("Ids in index list is null.", idsInIndex, not(nullValue()));

        // Verify the calls to external methods
        verify(transportClient, times(1)).prepareSearch("INDEX_NAME");
        verify(searchRequestBuilder, times(1)).setTypes("DOCUMENT_TYPE");
        verify(searchRequestBuilderWithTypes, times(1)).setQuery(any());
        verify(searchRequestBuilderWithQuery, times(1)).execute();
        verify(listenableActionFuture, times(1)).actionGet();
        verify(searchResponse, times(1)).getHits();
    }
}
