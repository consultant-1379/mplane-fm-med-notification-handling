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

package com.ericsson.oss.mediation.fm.oradio.timer

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.SharedCdiSpecification
import com.ericsson.oss.mediation.fm.oradio.api.MPlaneAlarmService
import com.ericsson.oss.mediation.fm.oradio.ejb.cache.EventNotificationBuffer
import com.ericsson.oss.mediation.fm.oradio.ejb.models.EventNotificationMarker
import com.ericsson.oss.mediation.fm.oradio.ejb.timer.EventNotificationBufferTimer
import com.ericsson.oss.mediation.translator.model.EventNotification
import org.slf4j.Logger
import spock.lang.Shared

import javax.ejb.TimerConfig
import javax.ejb.TimerService

class EventNotificationBufferTimeSpec extends SharedCdiSpecification{

    private static final String NODE_NAME = "TestORadio1"
    private static final String ALARM_ID = "1"

    @ObjectUnderTest
    @Shared
    EventNotificationBufferTimer eventNotificationBufferTimer

    @MockedImplementation
    private TimerService timerService;

    @MockedImplementation
    EventNotificationBuffer eventNotificationBuffer;

    @MockedImplementation
    MPlaneAlarmService mPlaneAlarmService

    @MockedImplementation
    Logger log;

    def setup(){
    }


    def "When the timeout occurs and the is no list of Events to send then the function should exit"(){
        given: "the buffer has no notifications to process"
        eventNotificationBuffer.getEventNotificationsfromTime() >> createEmptyEventNotificationBufferMap()
        when: "the timeout occurs"
        eventNotificationBufferTimer.timeout(timerService.createIntervalTimer(1000,1000,new TimerConfig()))
        then: "Nothing to process so nothing should be removed from the buffer"
        noExceptionThrown()
        0 * eventNotificationBuffer.removeEventNotificationList(_)
        0 * mPlaneAlarmService.sendAlarms(_)
    }

    def "When the timeout occurs and the is a list of Events to send then the function process and send the events"(){
        given: "the buffer has no notifications to process"
        eventNotificationBuffer.getEventNotificationsfromTime() >> createEventNotificationBufferMap(NODE_NAME,ALARM_ID)
        when: "the timeout occurs"
        eventNotificationBufferTimer.timeout(timerService.createIntervalTimer(1000,1000,new TimerConfig()))
        then: "Nothing to process so nothing should be removed from the buffer"
        noExceptionThrown()
        1 * eventNotificationBuffer.removeEventNotificationList(_)
        1 * mPlaneAlarmService.sendAlarms(_)
    }

    private static Map<EventNotificationMarker, EventNotification> createEmptyEventNotificationBufferMap() {
        return new HashMap<EventNotificationMarker, EventNotification>();
    }

    private static Map<EventNotificationMarker,EventNotification> createEventNotificationBufferMap(final String networkElementName, final String alarmId) {
        Map<EventNotificationMarker, EventNotification> map = new HashMap<>()
        map.put(new EventNotificationMarker(networkElementName, alarmId),createDummyEventNotification(networkElementName,alarmId))
        return map
    }

    private static EventNotification createDummyEventNotification(final String networkElementName, final String alarmId) {
        EventNotification notif =  new EventNotification()
        notif.setExternalEventId(alarmId)
        notif.setManagedObjectInstance("NetworkElement=" + networkElementName)
        return notif
    }
}
