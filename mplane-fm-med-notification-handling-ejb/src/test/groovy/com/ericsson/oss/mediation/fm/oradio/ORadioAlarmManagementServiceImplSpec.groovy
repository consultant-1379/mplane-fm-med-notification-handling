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

package com.ericsson.oss.mediation.fm.oradio

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.SharedCdiSpecification
import com.ericsson.oss.mediation.fm.oradio.api.MPlaneAlarmService
import com.ericsson.oss.mediation.fm.oradio.ejb.ORadioAlarmManagementServiceImpl
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.EventNotificationBuffer
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.OssPrefixCache
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.SupervisionStateCache
import com.ericsson.oss.mediation.translator.model.EventNotification
import org.slf4j.Logger
import spock.lang.Shared


class ORadioAlarmManagementServiceImplSpec extends SharedCdiSpecification{

    private static final String TEST_NODE_NAME = "ORadio1"
    private static final String TEST_NODE_NETWORK_ELEMENT_FDN = "NetworkElement=ORadio1"
    private static final String TEST_NODE_OSS_PREFIX = "SubNetwork=ENM,MeContext=ORadio1"
    private static final String TRUE = "True"
    private static final String FALSE = "False"

    @ObjectUnderTest
    @Shared
    ORadioAlarmManagementServiceImpl oRadioAlarmManagementService

    @MockedImplementation
    private Logger logger;

    @MockedImplementation
    private MPlaneAlarmService mplaneAlarmService;

    @MockedImplementation
    private SupervisionStateCache supervisionStateCache;

    @MockedImplementation
    private OssPrefixCache ossPrefixCache;

    @MockedImplementation
    private EventNotificationBuffer eventNotificationBuffer;

    def setup(){

    }

    def "When process Notification is called without the node being DISABLED for FMAlarmSupervision, then Alarm is discarded"(){
        given: "Node is not in the FMSupervisionCache"
        when:"Oradio is called"
        oRadioAlarmManagementService.processNotification("1","ORadio1")
        then:"Nothing is run after and the alarm is not processed"
        0 * ossPrefixCache.getOssPrefix(_)
        0 * eventNotificationBuffer.eventNotificationExists(_,_)
        noExceptionThrown()
    }

    def "When process Notification is called with the node being ENABLED for FMAlarmSupervision, then Alarm is then added to the EventNotificationBuffer"(){
        given: "Node is in the FMSupervisionCache and ENABLED"
        supervisionStateCache.isSupervisionActive(TEST_NODE_NETWORK_ELEMENT_FDN) >> TRUE
        when:"Oradio is called"
        oRadioAlarmManagementService.processNotification("1","ORadio1")
        then:"Nothing is run after and the alarm is not processed"
        1 * ossPrefixCache.getOssPrefix(_)
        1 * eventNotificationBuffer.eventNotificationExists(_,_)
        1 * eventNotificationBuffer.addEventNotification(_,_)
        noExceptionThrown()


    }

    def "When process Notification is called with the node being ENABLED for FMAlarmSupervision and an ossPrefix set, then Alarm is then added to the EventNotificationBuffer"(){
        given: "Node is in the FMSupervisionCache and ENABLED"
        supervisionStateCache.isSupervisionActive(TEST_NODE_NETWORK_ELEMENT_FDN) >> TRUE
        ossPrefixCache.getOssPrefix(TEST_NODE_NETWORK_ELEMENT_FDN) >> TEST_NODE_OSS_PREFIX
        when:"Oradio is called"
        oRadioAlarmManagementService.processNotification("","ORadio1")
        then:"Nothing is run after and the alarm is added to the eventNotificationBuffer"
        1 * ossPrefixCache.getOssPrefix(_)
        1 * eventNotificationBuffer.eventNotificationExists(_,_)
        1 * eventNotificationBuffer.addEventNotification(_,_)
        noExceptionThrown()
    }

    def "When process Notification is called with the node being ENABLED for FMAlarmSupervision and an ossPrefix set to an empty string, then Alarm is then added to the EventNotificationBuffer"(){
        given: "Node is in the FMSupervisionCache and ENABLED"
        supervisionStateCache.isSupervisionActive(TEST_NODE_NETWORK_ELEMENT_FDN) >> TRUE
        ossPrefixCache.getOssPrefix(TEST_NODE_NETWORK_ELEMENT_FDN) >> ""
        when:"Oradio is called"
        oRadioAlarmManagementService.processNotification("","ORadio1")
        then:"Nothing is run after and the alarm is added to the eventNotificationBuffer"
        1 * ossPrefixCache.getOssPrefix(_)
        1 * eventNotificationBuffer.eventNotificationExists(_,_)
        1 * eventNotificationBuffer.addEventNotification(_,_)
        noExceptionThrown()
    }

    def "When processNotification is called with a CLEARED notification in the cache, then the new Notification should be merged with the cached and sent to APS"(){
        given: "Node is in the FMSupervisionCache and ENABLED"
        supervisionStateCache.isSupervisionActive(TEST_NODE_NETWORK_ELEMENT_FDN) >> TRUE
        ossPrefixCache.getOssPrefix(TEST_NODE_NETWORK_ELEMENT_FDN) >> ""
        and: "EventNotification is already in the EventNotificationBuffer"
        eventNotificationBuffer.eventNotificationExists(_,_) >> true
        eventNotificationBuffer.getEventNotification(_,_) >> createTestEventNotification("1","ORadio1", "CLEARED")

        when:"Oradio is called"
        oRadioAlarmManagementService.processNotification("1","ORadio1")
        then:"Nothing is run after and the alarm is added to the eventNotificationBuffer"
        1 * ossPrefixCache.getOssPrefix(_)
        1 * eventNotificationBuffer.removeEventNotification(_,_)
        1 * mplaneAlarmService.sendAlarm(_)

        noExceptionThrown()
    }

    def "When processNotification is called with an ALARM notification in the cache, then the cached Notification should be merged with the new and sent to APS"(){
        given: "Node is in the FMSupervisionCache and ENABLED"
        supervisionStateCache.isSupervisionActive(TEST_NODE_NETWORK_ELEMENT_FDN) >> TRUE
        ossPrefixCache.getOssPrefix(TEST_NODE_NETWORK_ELEMENT_FDN) >> ""
        and: "EventNotification is already in the EventNotificationBuffer"
        eventNotificationBuffer.eventNotificationExists(_,_) >> true
        eventNotificationBuffer.getEventNotification(_,_) >> createTestEventNotification("1","ORadio1", "ALARM")
        when:"Oradio is called"
        oRadioAlarmManagementService.processNotification("1","ORadio1")
        then:"Nothing is run after and the alarm is added to the eventNotificationBuffer"
        1 * ossPrefixCache.getOssPrefix(_)
        1 * eventNotificationBuffer.removeEventNotification(_,_)
        1 * mplaneAlarmService.sendAlarm(_)

        noExceptionThrown()
    }

    def "Should be able to get the EventNotificationBuffer cache"(){
        given: "there is a cache to get"
        eventNotificationBuffer.getEventNotificationBuffer() >> new HashMap<String, String>()
        when: "getCurrentEventNotificationBuffer is called"
        def cache = oRadioAlarmManagementService.getCurrentEventNotificationBuffer()
        then:
        cache != null
        cache == new HashMap<String,String>()
        noExceptionThrown()
    }

    def "Should be able to get the ossPrefixCache"(){
        given: "there is a cache to get"
        ossPrefixCache.getCache() >> new HashMap<String, String>()
        when: "getOssPrefixCache is called"
        def cache = oRadioAlarmManagementService.getOssPrefixCache()
        then:
        cache != null
        cache == new HashMap<String,String>()
        noExceptionThrown()
    }

    def "Should be able to get the SupervisionCache"(){
        given: "there is a cache to get"
        supervisionStateCache.getSupervisionStateCache() >> new HashMap<String, String>()
        when: "getSupervisionCache is called"
        def cache = oRadioAlarmManagementService.getSupervisionCache()
        then:
        cache != null
        cache == new HashMap<String,String>()
        noExceptionThrown()
    }

    private static EventNotification createTestEventNotification(final String alarmId, final String networkElementName, final String perceivedSeverity) {
        EventNotification notif =  new EventNotification()
        notif.setExternalEventId(alarmId)
        notif.setPerceivedSeverity(perceivedSeverity)
        notif.setProbableCause("Unit Testing");
        notif.addAdditionalAttribute("targetAttributeInfo", alarmId)
        notif.setManagedObjectInstance("NetworkElement=" + networkElementName)
        return notif
    }

}


