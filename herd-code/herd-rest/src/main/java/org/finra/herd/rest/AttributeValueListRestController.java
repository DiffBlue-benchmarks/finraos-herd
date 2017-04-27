package org.finra.herd.rest;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.finra.herd.model.api.xml.AttributeValueList;
import org.finra.herd.model.api.xml.AttributeValueListCreateRequest;
import org.finra.herd.model.api.xml.AttributeValueListKey;
import org.finra.herd.model.api.xml.AttributeValueListKeys;
import org.finra.herd.model.dto.SecurityFunctions;
import org.finra.herd.service.AttributeValueListService;
import org.finra.herd.ui.constants.UiConstants;

@RestController
@RequestMapping(value = UiConstants.REST_URL_BASE, produces = {"application/xml", "application/json"})
@Api(tags = "Attribute Value List")
public class AttributeValueListRestController extends HerdBaseController
{
    public static final String ATTRIBUTE_VALUE_LIST_URI_PREFIX = "/attributeValueLists";

    @Autowired
    private AttributeValueListService attributeValueListService;


    /**
     * Creates a new business object data attribute.
     * <p>Requires WRITE permission on namespace</p>
     *
     * @param request the information needed to create a business object data attribute
     *
     * @return the newly created business object data attribute information
     */
    @RequestMapping(value = ATTRIBUTE_VALUE_LIST_URI_PREFIX, method = RequestMethod.POST, consumes = {"application/xml", "application/json"})
    //@Secured(SecurityFunctions.FN_ATTRIBUTE_VALUE_LISTS_POST)
    public AttributeValueList createAttributeValueList(@RequestBody AttributeValueListCreateRequest request)
    {
        AttributeValueListKey attributeValueListKey = request.getAttributeValueListKey();

        return attributeValueListService.createAttributeValueList(request);
    }

    /**
     * Gets an existing attribute for the business object data without subpartition values.
     * <p>Requires READ permission on namespace</p>
     *
     * @param namespace the namespace
     *
     * @return the business object data attribute information
     */
    @RequestMapping(value = ATTRIBUTE_VALUE_LIST_URI_PREFIX + "/namespaces/{namespace}/attributeValueListNames/{attributeValueListName}",
        method = RequestMethod.GET, consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_ATTRIBUTE_VALUE_LISTS_GET)
    public AttributeValueList getAttributeValueList(@PathVariable("namespace") String namespace
        , @PathVariable("attributeValueListName") String attributeValueListName)
    {
        return attributeValueListService.getAttributeValueList(new AttributeValueListKey(namespace, attributeValueListName));
    }

    /**
     * Deletes an existing attribute for the business object data with 1 subpartition value.
     * <p>Requires WRITE permission on namespace</p>
     *
     * @param namespace the namespace
     * @param attributeValueListName the business object data attribute name
     *
     * @return the business object data attribute that got deleted
     */
    @RequestMapping(value = ATTRIBUTE_VALUE_LIST_URI_PREFIX + "/namespaces/{namespace}/attributeValueListNames/{attributeValueListName}",
        method = RequestMethod.DELETE)
    @Secured(SecurityFunctions.FN_ATTRIBUTE_VALUE_LISTS_DELETE)
    public AttributeValueListKey deleteAttributeValueList(@PathVariable("namespace") String namespace,
        @PathVariable("attributeValueListName") String attributeValueListName)
    {
        return attributeValueListService.deleteAttributeValueList(
            new AttributeValueListKey(namespace, attributeValueListName));
    }

    /**
     * Gets a list of keys for all existing business object data attributes for a specific business object data with 4 subpartition values.
     * <p>Requires READ permission on namespace</p>
     *
     * @return the list of business object data attribute keys
     */
    @RequestMapping(value = ATTRIBUTE_VALUE_LIST_URI_PREFIX, method = RequestMethod.GET)
    @Secured(SecurityFunctions.FN_ATTRIBUTE_VALUE_LISTS_ALL_GET)
    public AttributeValueListKeys getAttributeValueLists()
    {
        return attributeValueListService.getAttributeValueListKeys();
    }


}
