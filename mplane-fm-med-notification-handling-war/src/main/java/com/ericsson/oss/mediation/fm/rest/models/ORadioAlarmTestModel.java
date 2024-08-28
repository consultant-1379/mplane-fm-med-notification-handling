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

package com.ericsson.oss.mediation.fm.rest.models;

import java.io.Serializable;

public class ORadioAlarmTestModel implements Serializable {

    private String networkElementName;

    private String netconfString;

    public void setNetconfString(String netconfString) {
        this.netconfString = netconfString;
    }

    public String getNetconfString() {
        return netconfString;
    }

    public void setNetworkElementName(String networkElementName) {
        this.networkElementName = networkElementName;
    }

    public String getNetworkElementName() {
        return networkElementName;
    }


}
