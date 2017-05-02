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
package org.finra.herd.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import org.finra.herd.dao.AttributeValueListDao;
import org.finra.herd.model.api.xml.AttributeValueListKey;
import org.finra.herd.model.api.xml.AttributeValueListKeys;
import org.finra.herd.model.jpa.AttributeValueListEntity;
import org.finra.herd.model.jpa.AttributeValueListEntity_;

@Repository
public class AttributeValueListDaoImpl extends AbstractHerdDao implements AttributeValueListDao
{
    @Override
    public AttributeValueListEntity getAttributeValueListByKey(AttributeValueListKey attributeValueListKey)
    {
        // Create the criteria builder and the criteria.
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AttributeValueListEntity> criteria = builder.createQuery(AttributeValueListEntity.class);

        // The criteria root is the tag type code.
        Root<AttributeValueListEntity> attributeValueListEntityRoot = criteria.from(AttributeValueListEntity.class);

        // Create the standard restrictions.
        Predicate queryRestriction = builder.equal(builder.upper(attributeValueListEntityRoot.get(AttributeValueListEntity_.attributeValueListName)),
            attributeValueListKey.getAttributeValueListName().toUpperCase());

        // Add all clauses to the query.
        criteria.select(attributeValueListEntityRoot).where(queryRestriction);

        // Run the query and return the results.
        return entityManager.createQuery(criteria).getSingleResult();
    }

    @Override
    public AttributeValueListKeys getAttributeValueListKeys()
    {
        // Create the criteria builder and the criteria.
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AttributeValueListEntity> criteria = builder.createQuery(AttributeValueListEntity.class);

        // The criteria root is the tag type entity.
        Root<AttributeValueListEntity> attributeValueListEntityRoot = criteria.from(AttributeValueListEntity.class);

        // Get the columns.
        Path<String> nameColumn = attributeValueListEntityRoot.get(AttributeValueListEntity_.attributeValueListName);

        // Order the results by tag type's order and display name.
        List<Order> orderBy = new ArrayList<>();
        orderBy.add(builder.asc(nameColumn));

        // Add all clauses to the query.
        criteria.select(attributeValueListEntityRoot).orderBy(orderBy);

        List<AttributeValueListKey> attributeValueListKeys = new ArrayList<>();
        for (AttributeValueListEntity entity : entityManager.createQuery(criteria).getResultList())
        {
            AttributeValueListKey attributeValueListKey = new AttributeValueListKey();
            attributeValueListKey.setNamespace(entity.getNamespace().getCode());
            attributeValueListKey.setAttributeValueListName(entity.getAttributeValueListName());
            attributeValueListKeys.add(attributeValueListKey);
        }

        return new AttributeValueListKeys(attributeValueListKeys);
    }

    @Override
    public List<AttributeValueListEntity> getAttributeValueLists()
    {
        // Create the criteria builder and the criteria.
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AttributeValueListEntity> criteria = builder.createQuery(AttributeValueListEntity.class);

        // The criteria root is the tag type entity.
        Root<AttributeValueListEntity> attributeValueListEntityRoot = criteria.from(AttributeValueListEntity.class);

        // Get the columns.
        Path<String> nameColumn = attributeValueListEntityRoot.get(AttributeValueListEntity_.attributeValueListName);

        // Order the results by tag type's order and display name.
        List<Order> orderBy = new ArrayList<>();
        orderBy.add(builder.asc(nameColumn));

        // Add all clauses to the query.
        criteria.select(attributeValueListEntityRoot).orderBy(orderBy);

        // Run the query and return the results.
        return entityManager.createQuery(criteria).getResultList();
    }
}
