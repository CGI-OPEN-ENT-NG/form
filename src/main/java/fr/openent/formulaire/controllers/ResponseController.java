package fr.openent.formulaire.controllers;

import fr.openent.formulaire.Formulaire;
import fr.openent.formulaire.security.AccessRight;
import fr.openent.formulaire.security.ShareAndOwner;
import fr.openent.formulaire.service.ResponseService;
import fr.openent.formulaire.service.impl.DefaultResponseService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserUtils;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class ResponseController extends ControllerHelper {
    private static final Logger log = LoggerFactory.getLogger(ResponseController.class);
    private final ResponseService responseService;

    public ResponseController() {
        super();
        this.responseService = new DefaultResponseService();
    }

    @Get("/questions/:questionId/responses")
    @ApiDoc("List all the responses to a specific question")
    @ResourceFilter(ShareAndOwner.class)
    @SecuredAction(value = Formulaire.CONTRIB_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void list(HttpServerRequest request) {
        String questionId = request.getParam("questionId");
        String nbLines = request.params().get("nbLines");
        responseService.list(questionId, nbLines, arrayResponseHandler(request));
    }

    @Get("/questions/:questionId/responses/:distributionId")
    @ApiDoc("List all my responses to a specific question for a specific distribution")
    @ResourceFilter(ShareAndOwner.class)
    @SecuredAction(value = Formulaire.RESPONDER_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void listMineByDistribution(HttpServerRequest request) {
        String questionId = request.getParam("questionId");
        String distributionId = request.getParam("distributionId");
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                responseService.listMineByDistribution(questionId, distributionId, user, arrayResponseHandler(request));
            } else {
                log.error("User not found in session.");
                Renders.unauthorized(request);
            }
        });
    }

    @Get("/responses/:responseId")
    @ApiDoc("Get a specific response by id")
    @ResourceFilter(AccessRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void get(HttpServerRequest request) {
        String responseId = request.getParam("responseId");
        responseService.get(responseId, defaultResponseHandler(request));
    }

    @Post("/questions/:questionId/responses")
    @ApiDoc("Create a response")
    @ResourceFilter(ShareAndOwner.class)
    @SecuredAction(value = Formulaire.RESPONDER_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void create(HttpServerRequest request) {
        String questionId = request.getParam("questionId");
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                RequestUtils.bodyToJson(request, response -> {
                    responseService.create(response, user, questionId, defaultResponseHandler(request));
                });
            } else {
                log.error("User not found in session.");
                Renders.unauthorized(request);
            }
        });
    }

    @Put("/responses/:responseId")
    @ApiDoc("Update a specific response")
    @ResourceFilter(ShareAndOwner.class)
    @SecuredAction(value = Formulaire.RESPONDER_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void update(HttpServerRequest request) {
        String responseId = request.getParam("responseId");
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                RequestUtils.bodyToJson(request, response -> {
                    responseService.update(user, responseId, response, defaultResponseHandler(request));
                });
            } else {
                log.error("User not found in session.");
                Renders.unauthorized(request);
            }
        });
    }

    @Delete("/responses/:responseId")
    @ApiDoc("Delete a specific response")
    @ResourceFilter(ShareAndOwner.class)
    @SecuredAction(value = Formulaire.RESPONDER_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void delete(HttpServerRequest request) {
        String responseId = request.getParam("responseId");
        responseService.delete(responseId, defaultResponseHandler(request));
    }
}