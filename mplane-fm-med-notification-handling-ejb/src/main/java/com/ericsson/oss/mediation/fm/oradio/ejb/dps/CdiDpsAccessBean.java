/*
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *******************************************************************************
 * -------------------------------------------------------------------------------
 */

package com.ericsson.oss.mediation.fm.oradio.ejb.dps;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.query.*;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;

/**
 * CdiDpsAccessBean is used as an implementation to DpsAccess interface, It is used for a dps query with a single restriction on nodeType.
 */

@ApplicationScoped
public class CdiDpsAccessBean implements DpsAccess {

    @EServiceRef
    private DataPersistenceService dps;

    /**
     * returns a list of MO's from DPS with a restriction
     * @param namespace - Namespace of the required MO
     * @param type - Type of the MO to return
     * @param restrictionType - Type of the restrictions
     * @param nodeType - Type of the node which will act as a restriction
     * @return {@code List<ManagedObject>} the results of the query
     */
    @Override
    public List<ManagedObject> createRestrictionTypeQuery(final String namespace, final String type, final String restrictionType,
                                                          final String nodeType) {
        final QueryBuilder queryBuilder = dps.getQueryBuilder();
        final Query<TypeRestrictionBuilder> query = queryBuilder.createTypeQuery(namespace, type);
        final Restriction restriction = query.getRestrictionBuilder().equalTo(restrictionType, nodeType);
        query.setRestriction(restriction);
        final DataBucket liveBucket = dps.getLiveBucket();
        final QueryExecutor queryExecutor = liveBucket.getQueryExecutor();
        return queryExecutor.getResultList(query);
    }
}