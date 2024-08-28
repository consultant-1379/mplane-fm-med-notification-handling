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

package com.ericsson.oss.mediation.fm.oradio.models

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.mediation.fm.oradio.ejb.models.EventNotificationMarker
import spock.lang.Specification

class EventNotificationMarkerSpec extends Specification {

    private static final String NETWORK_ELEMENT_NAME = "TestORadio1"
    private static final String ALARM_ID = "1"


    @ObjectUnderTest
    EventNotificationMarker marker

    def setup(){
        marker = new EventNotificationMarker(NETWORK_ELEMENT_NAME, ALARM_ID)
    }
    def "When equals is called if the two markers match exactly then true should be returned"(){
        expect: "marker to be equal to itself"
        assert marker.equals(marker)
    }

    def "When equals is called with null as the passed in object to compare, should return false"(){
        expect: "Marker to not be equal to null"
        assert !marker.equals(null)
    }

    def "When Equals is called with a new Object then it should return false"() {
        expect: "Marker to not be equal to a new object"
        assert !marker.equals(new Object())
    }

    def "When Equals is called with an object with the same networkElementName and EventNotificationId then it should match" (){
        expect: "Marker to be equal to the new instance"
        assert marker.equals(new EventNotificationMarker(NETWORK_ELEMENT_NAME, ALARM_ID))
    }

    def "When Equals is called with an object with the same networkElementName but differing EventNotificationId then it should NOT match" (){
        expect: "Marker to be equal to the new instance"
        assert !marker.equals(new EventNotificationMarker(NETWORK_ELEMENT_NAME, "2"))
    }

    def "When Equals is called with an object with the same EventNotificationId but differing networkElementName then it should NOT match" (){
        expect: "Marker to be equal to the new instance"
        assert !marker.equals(new EventNotificationMarker(NETWORK_ELEMENT_NAME + "1", ALARM_ID))
    }

}
