package fr.openent.formulaire.controller;

import fr.openent.formulaire.service.QuestionTypeService;
import fr.openent.formulaire.service.impl.DefaultQuestionTypeService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.controller.ControllerHelper;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class QuestionTypeController extends ControllerHelper {
    private static final Logger log = LoggerFactory.getLogger(QuestionTypeController.class);
    private QuestionTypeService questionTypeService;

    public QuestionTypeController() {
        super();
        this.questionTypeService = new DefaultQuestionTypeService();
    }

    @Get("/types")
    @ApiDoc("List questions types")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void list(HttpServerRequest request) {
        questionTypeService.list(arrayResponseHandler(request));
    }

    @Get("/types/:code")
    @ApiDoc("Get one questio ntype thanks to its code")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void get(HttpServerRequest request) {
        String id = request.getParam("code");
        questionTypeService.get(id, defaultResponseHandler(request));
    }
}