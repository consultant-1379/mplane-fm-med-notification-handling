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

package com.ericsson.oss.mediation.fm.rest.resources;

import com.ericsson.oss.mediation.fm.oradio.api.ORadioAlarmManagementService;
import com.ericsson.oss.mediation.fm.rest.models.ORadioAlarmTestModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("ORadioAlarmManagementTest")
public class ORadioAlarmManagementServiceTest {

    @Inject
    private ORadioAlarmManagementService oRadioAlarmManagementService;

    @GET
    public Response checkIsWorking(){
        return Response.ok("Is Working, Good Job").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkProcessNotification(final List<ORadioAlarmTestModel> model) {
        HashMap<String,List<String>> response = new HashMap<>();
        for (ORadioAlarmTestModel mod: model) {
            if(!response.containsKey(mod.getNetworkElementName())) {
                response.put(mod.getNetworkElementName(),new ArrayList<>());
            }
            response.replace(mod.getNetworkElementName(),
                    oRadioAlarmManagementService.processNotification(mod.getNetconfString(), mod.getNetworkElementName()));
        }
        return Response.ok(response).build();
    }

    @GET
    @Path("/checkOssPrefixCache")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkOssPrefixCache(){
        return Response.ok(oRadioAlarmManagementService.getOssPrefixCache()).build();
    }

    @GET
    @Path("/checkSupervisionStateCache")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkSupervisionStateCache(){
        return Response.ok(oRadioAlarmManagementService.getSupervisionCache()).build();
    }

    @GET
    @Path("/checkEventNotificationBuffer")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkEventNotificationBuffer(){
        return Response.ok(oRadioAlarmManagementService.getCurrentEventNotificationBuffer()).build();
    }

}
