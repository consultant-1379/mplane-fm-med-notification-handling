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

package com.ericsson.oss.mediation.fm.oradio.cache;

import com.ericsson.cds.cdi.support.rule.ImplementationInstance;
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest;
import com.ericsson.cds.cdi.support.spock.SharedCdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectCreatedEvent
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectDeletedEvent;
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.OssPrefixCache;
import com.ericsson.oss.mediation.fm.oradio.ejb.dps.DpsAccess;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import spock.lang.Shared;


class OssPrefixCacheSpec extends SharedCdiSpecification {

    private static final def NETWORK_ELEMENT_ID = "TestORadio"
    private static final def NETWORK_ELEMENT_TYPE = "NetworkElement"
    private static final def TEST_ORADIO_NETWORK_ELEMENT_FDN = NETWORK_ELEMENT_TYPE + "=" + NETWORK_ELEMENT_ID
    private  static final def TEST_ORADIO_2_NETWORK_ELEMENT_FDN = TEST_ORADIO_NETWORK_ELEMENT_FDN + "2"
    private static final def TEST_ORADIO_2_OSS_PREFIX = "SubNetwork=ENM,SubNetwork=Athlone,MeContext=ORadioTest2"


    private static final def INVALID_NETWORK_ELEMENT_FDN = "NetworkElement=SomeInvalidFdn"

    private static final def NS_OSS_NE_FM_DEF = "OSS_NE_FM_DEF"

    private static final def VERSION_1_0_0 = "1.0.0"

    private static final def TRUE = true
    private static final def FALSE = false


    // Create a stub for Supervision - this is because you can't use a mock for a class if there is a @PostConstruct method
    @ImplementationInstance
    @Shared
    private ManagedObject networkElementMo = [ getFdn: { TEST_ORADIO_NETWORK_ELEMENT_FDN }, getAttribute: { String attributeName -> "SubNetwork=ENM,MeContext=TestORadio" } ] as ManagedObject


    // Create a stub for DpsAccess - this is because you can't use a mock for a class if there is a @PostConstruct method
    @ImplementationInstance
    @Shared
    private DpsAccess dpsAccess = new DpsAccess() {
        @Override List<ManagedObject> createRestrictionTypeQuery(String namespace, String type, String restrictionType, String nodeType) {
            return [networkElementMo]
        }
    }

    @ObjectUnderTest
    @Shared
    private OssPrefixCache ossPrefixCache

    void setup() {
        // Nothing to do
    }

    def "initialiseCache should populate the cache with all ORadio nodes and their ossPrefix"() {
        expect: "cache is populated and ossPrefix is available"
        ossPrefixCache.getOssPrefix(TEST_ORADIO_NETWORK_ELEMENT_FDN)
    }

    def "Update cache when a new Node is added to ENM"() {
        given: "Node is not present in the cache"
        def ossPrefix = ossPrefixCache.getOssPrefix(TEST_ORADIO_2_NETWORK_ELEMENT_FDN)
        assert ossPrefix == null

        and: "DPSCreatedEvent is Fired"
        def dpsCreatedEvent = createDpsObjectCreatedEvent(TEST_ORADIO_2_NETWORK_ELEMENT_FDN)

        when: "onNodeCreatedEvent is called"
        ossPrefixCache.onNodeCreatedEvent(dpsCreatedEvent)

        then: "ossPrefixCache should be found in the cache now"
        ossPrefix != ossPrefixCache.getOssPrefix(TEST_ORADIO_2_NETWORK_ELEMENT_FDN)
        def newOssPrefix = ossPrefixCache.getOssPrefix(TEST_ORADIO_2_NETWORK_ELEMENT_FDN)
        assert newOssPrefix == TEST_ORADIO_2_OSS_PREFIX
        assert ossPrefixCache.getCache().size() == 2
    }

    def "Update cache when DpsObjectDeletedEvent is received"() {

        given: "Node is present in the cache"
        def dpsCreatedEvent = createDpsObjectCreatedEvent(TEST_ORADIO_2_NETWORK_ELEMENT_FDN)
        ossPrefixCache.onNodeCreatedEvent(dpsCreatedEvent)

        def startingOssPrefix = ossPrefixCache.getOssPrefix(TEST_ORADIO_2_NETWORK_ELEMENT_FDN)
        assert startingOssPrefix != null
        assert startingOssPrefix == TEST_ORADIO_2_OSS_PREFIX

        and: "DpsObjectDeletedEvent fired"
        def dpsDeletedEvent = createDpsObjectDeletedEvent(TEST_ORADIO_2_NETWORK_ELEMENT_FDN);

        when: "onNodeDeletedEvent is called"
        ossPrefixCache.onNodeDeletedEvent(dpsDeletedEvent)

        then: "Node should be removed from the cache"
        def newStartingPrefix = ossPrefixCache.getOssPrefix(TEST_ORADIO_2_NETWORK_ELEMENT_FDN)
        assert startingOssPrefix != newStartingPrefix
        assert newStartingPrefix == null
        assert ossPrefixCache.getCache().size() == 1
    }

    def "Cache should return null if the node is not found"() {

        // Note: No need to call initialiseCache because the cdi framework calls @PostConstruct on creation
        expect:"Node is not in cache, return null"
        assert ossPrefixCache.getOssPrefix(TEST_ORADIO_2_NETWORK_ELEMENT_FDN) == null
    }

    def createDpsObjectCreatedEvent(def fdn) {
        def createdData = ["ossPrefix": TEST_ORADIO_2_OSS_PREFIX, "neType":"ORadio"]
        return new DpsObjectCreatedEvent(NS_OSS_NE_FM_DEF,NETWORK_ELEMENT_TYPE, VERSION_1_0_0, 1L,fdn,"Live",true, [createdData] as HashMap<String, Object>)
    }

    def createDpsObjectDeletedEvent(def fdn) {
        def createdData = ["ossPrefix": TEST_ORADIO_2_OSS_PREFIX]
        return new DpsObjectDeletedEvent(NS_OSS_NE_FM_DEF,NETWORK_ELEMENT_TYPE,VERSION_1_0_0,1L,fdn,"Live", true, [createdData] as HashMap<String,Object>)
    }

}
