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

import java.util.List;

import org.finra.herd.model.api.xml.AttributeValueList;
import org.finra.herd.model.api.xml.AttributeValueListKey;
import org.finra.herd.model.jpa.AttributeValueListEntity;

public interface  AttributeValueListDao extends BaseJpaDao
{

    /**
     * Gets a attribute value list by its key.
     *
     * @param attributeValueListKey the attribute value list key (case-insensitive)
     *
     * @return the attribute value list entity for the specified key
     */
    public AttributeValueListEntity getAttributeValueListByKey(AttributeValueListKey attributeValueListKey);

    /**
     * Gets an ordered list of attribute value list keys for all attribute value lists defined in the system.
     *
     * @return the list of attribute value list keys
     */
    public List<AttributeValueListKey> getAttributeValueListKeys();

    /**
     * Gets an ordered list of attribute value list entities for all attribute value lists defined in the system.
     *
     * @return the list of attribute value list entities
     */
    public List<AttributeValueListEntity> getAttributeValueLists();


    /**
     * Gets a attribute value list by its code.
     *
     * @param Namespace the attribute value list display name (case-insensitive)
     *
     * @return the attribute value list entity for the specified display name
     */
    public List<AttributeValueListEntity> getAttributeValueListByNamespace(String Namespace);

}
