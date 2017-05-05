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
package org.finra.herd.dao;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.finra.herd.dao.config.DaoSpringModuleConfig;

/**
 * TransportClientController
 * <p>
 * Class to check the health of the transport client connection to the search index. If the health of the transport client or the search index cluster
 */
@Component
public class TransportClientController
{
    /**
     * Logger for the transport client controller.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportClientController.class);

    /**
     * Cache manager used to clear the transport client cache.
     */
    @Autowired
    private CacheManager cacheManager;

    /**
     * Cluster Health Response Factory class used to build a ClusterHealthResponse.
     */
    @Autowired
    private ClusterHealthResponseFactory clusterHealthResponseFactory;

    /**
     * The transport client factory used to get a transport client.
     */
    @Autowired
    private TransportClientFactory transportClientFactory;

    /**
     * The control transport client method will check the health of the transport client and the search index. If the search index or transport client are not
     * operating properly the transport client cache will be cleared. When the transport client cache is cleared it will force the next getTransportClient call
     * to reload the transport client.
     */
    @Scheduled(fixedDelay = 60000)
    public void controlTransportClient()
    {
        // The number of nodes on the cluster, default to zero in case of transport client connection error.
        int numberOfNodes = 0;

        LOGGER.info("Checking the transport client and search index health.");

        try
        {
            // Get a cluster health response.
            ClusterHealthResponse clusterHealthResponse = clusterHealthResponseFactory.getClusterHealthResponse();

            // If the cluster health response is null, or if the number of nodes is not greater than zero
            // then clear the transport client cache.
            if (clusterHealthResponse != null)
            {
                numberOfNodes = clusterHealthResponse.getNumberOfNodes();
            }
        }
        catch (Exception exception)
        {
            LOGGER.warn("Exception caught when getting or using the transport client.", exception);
        }
        finally
        {
            // If there are no live nodes on the cluster, close and clear the transport client.
            if (numberOfNodes <= 0)
            {
                LOGGER.info("Closing the transport client.");

                // Close existing transport client, this should be done before clearing the cache
                try
                {
                    TransportClient transportClient = transportClientFactory.getTransportClient();
                    transportClient.close();
                }
                catch (Exception exception)
                {
                    LOGGER.warn("Failed to close the transport client.");
                }

                LOGGER.info("Clearing the transport client cache.");

                // Clearing the transport client cache
                cacheManager.getCache(DaoSpringModuleConfig.TRANSPORT_CLIENT_CACHE_NAME).clear();
            }
        }
    }
}
