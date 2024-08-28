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

package com.ericsson.oss.mediation.fm.oradio.ejb.exception;

/**
 * This class is thrown if the system is unable to create a timer within the EventNotificationBufferTimer class.
 * General Exception to throw if there is an issue creating the timer.
 */
public class CreateEventBufferTimerException extends Exception{

    /**
     * CreateEventBufferTimerException General exception constructor
     */
    public CreateEventBufferTimerException(){
        super();
    }

    /**
     * CreateEventBufferTimerException Exception constructor
     * @param message - message to add to the exception with details of the failure
     */
    public CreateEventBufferTimerException(final String message){
        super(message);
    }
}
