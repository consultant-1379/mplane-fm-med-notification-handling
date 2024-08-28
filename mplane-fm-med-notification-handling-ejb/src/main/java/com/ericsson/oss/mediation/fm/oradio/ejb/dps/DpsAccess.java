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

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;

import java.util.List;

/**
 * Used to provide an override for DPS queries, CdiDpsAccessBean is the implementation class and provides a specific implementation
 * of a restricted query.
 */
public interface DpsAccess {

    List<ManagedObject> createRestrictionTypeQuery(final String namespace, final String type, final String restrictionType, final String nodeType);
}
