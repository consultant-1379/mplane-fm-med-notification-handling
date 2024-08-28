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

package com.ericsson.oss.mediation.fm.oradio.cache

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.SharedCdiSpecification
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.EventNotificationBuffer
import com.ericsson.oss.mediation.fm.oradio.ejb.models.EventNotificationMarker
import com.ericsson.oss.mediation.translator.model.EventNotification

class EventNotificationBufferSpec extends SharedCdiSpecification{

    private static final def NODE_NAME = "ORadioTest1"
    private static final def NETWORK_ELEMENT_TYPE = "NetworkElement"
    private static final def ORADIO_TEST_1_FDN = NETWORK_ELEMENT_TYPE + "=" + NODE_NAME
    private static final def CRITICAL_PERCEIVED_SEVERITY = "CRITICAL"
    private static final def MAJOR_PERCEIVED_SEVERITY = "MAJOR"

    @ObjectUnderTest
    EventNotificationBuffer eventNotificationBuffer;

    def "Should be able to add to the EventNotificationBuffer"() {
        given: "that the EventBuffer is created"
        eventNotificationBuffer = new EventNotificationBuffer()
        when:"a user tries to add an EventNotification to the buffer"
        final String alarmId = 1
        EventNotification notification = createEventNotification(ORADIO_TEST_1_FDN, CRITICAL_PERCEIVED_SEVERITY,alarmId)
        eventNotificationBuffer.addEventNotification(notification, ORADIO_TEST_1_FDN)
        then:"EventNotification should have a time associated with it"
        EventNotification fromBuffer = eventNotificationBuffer.getEventNotification(alarmId, ORADIO_TEST_1_FDN)
        assert fromBuffer == notification;
    }

    def "Should be able to remove from the EventNotificationBuffer"(){
        given:"the Eventbuffer is created"
        eventNotificationBuffer = new EventNotificationBuffer()
        and:"There is an eventNotification in the buffer"
        final String alarmId = 1
        eventNotificationBuffer.addEventNotification(createEventNotification(ORADIO_TEST_1_FDN,CRITICAL_PERCEIVED_SEVERITY,alarmId), ORADIO_TEST_1_FDN)
        assert eventNotificationBuffer.getEventNotification(alarmId, ORADIO_TEST_1_FDN) != null
        when:"remove is called"
        eventNotificationBuffer.removeEventNotification(alarmId,ORADIO_TEST_1_FDN)
        then:"Notification should be removed"
        assert !eventNotificationBuffer.eventNotificationExists(alarmId, ORADIO_TEST_1_FDN)
        assert eventNotificationBuffer.getEventNotificationBuffer().size() == 0
    }

    def "Should be able to remove a List of items from the EventNotificationBuffer"(){
        given:"the Eventbuffer is created"
        eventNotificationBuffer = new EventNotificationBuffer()
        and:"there is 5 Notifications in the buffer"
        addToEventNotificationBuffer(5,ORADIO_TEST_1_FDN,MAJOR_PERCEIVED_SEVERITY)
        def markerList = createEventNotificationMarkerList(ORADIO_TEST_1_FDN,3)
        when:"remove is called"
        eventNotificationBuffer.removeEventNotificationList(markerList as Set)
        then:"Notification should be removed"
        assert !eventNotificationBuffer.eventNotificationExists("2", ORADIO_TEST_1_FDN)
        assert eventNotificationBuffer.eventNotificationExists("4", ORADIO_TEST_1_FDN)
        assert eventNotificationBuffer.getEventNotificationBuffer().size() == 2
    }

    def "Should return a list of applicable EventNotifications that have elapsed from the timeout"(){
        given:"the Eventbuffer is created"
        eventNotificationBuffer = new EventNotificationBuffer()
        and:"there is 5 Notifications in the buffer"
        addToEventNotificationBuffer(5,ORADIO_TEST_1_FDN,MAJOR_PERCEIVED_SEVERITY)
        when:"getEventNotificationsfromTime is called"
        def currentEventNotificationsBeforeTimeout = eventNotificationBuffer.getEventNotificationsfromTime()
        assert currentEventNotificationsBeforeTimeout == null || currentEventNotificationsBeforeTimeout.size() == 0
        and: "wait for 2 seconds to elapse"
        sleep(2000)
        then:"Notification should now be available"
        def currentEventNotificationsAfterTimeout = eventNotificationBuffer.getEventNotificationsfromTime()
        assert currentEventNotificationsAfterTimeout.size() == 5
    }


    private static EventNotification createEventNotification(final String fdn, final String severity, final String alarmId){
        EventNotification notif = new EventNotification()
        notif.setManagedObjectInstance(fdn)
        notif.setPerceivedSeverity(severity)
        notif.setExternalEventId(alarmId)
        return notif
    }

    private addToEventNotificationBuffer(final int amountOfRecords, final String fdn, final String severity) {
        for (int i=0; i< amountOfRecords; i++){
            eventNotificationBuffer.addEventNotification(createEventNotification(fdn,severity,i.toString()),ORADIO_TEST_1_FDN)
        }

    }

    private static List<EventNotificationMarker> createEventNotificationMarkerList(final String fdn, final int amountOfRecords)
    {
        List<EventNotificationMarker> markerList = new ArrayList<>();
        for (int i=0;i<amountOfRecords;i++){
            markerList.add(new EventNotificationMarker(fdn,i.toString()))
        }
        return markerList
    }

}
