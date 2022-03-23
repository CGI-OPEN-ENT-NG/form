package fr.openent.formulaire.controllers;

import fr.openent.formulaire.Formulaire;
import fr.openent.formulaire.security.AccessRight;
import fr.openent.formulaire.security.ShareAndOwner;
import fr.openent.formulaire.service.SectionService;
import fr.openent.formulaire.service.impl.DefaultSectionService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class SectionController extends ControllerHelper {
    private static final Logger log = LoggerFactory.getLogger(SectionController.class);
    private final SectionService sectionService;

    public SectionController() {
        super();
        this.sectionService = new DefaultSectionService();
    }

    @Get("/forms/:formId/sections")
    @ApiDoc("List all the sections of a specific form")
    @ResourceFilter(AccessRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void list(HttpServerRequest request) {
        String formId = request.getParam("formId");
        sectionService.list(formId, arrayResponseHandler(request));
    }

    @Get("/sections/:sectionId")
    @ApiDoc("Get a specific section by id")
    @ResourceFilter(ShareAndOwner.class)
    @SecuredAction(value = Formulaire.CONTRIB_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void get(HttpServerRequest request) {
        String sectionId = request.getParam("sectionId");
        sectionService.get(sectionId, defaultResponseHandler(request));
    }

    @Post("/forms/:formId/sections")
    @ApiDoc("Create a section in a specific form")
    @ResourceFilter(ShareAndOwner.class)
    @SecuredAction(value = Formulaire.CONTRIB_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void create(HttpServerRequest request) {
        String formId = request.getParam("formId");
        RequestUtils.bodyToJson(request, question -> {
            sectionService.create(question, formId, defaultResponseHandler(request));
        });
    }

    @Put("/sections/:sectionId")
    @ApiDoc("Update a specific section")
    @ResourceFilter(ShareAndOwner.class)
    @SecuredAction(value = Formulaire.CONTRIB_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void update(HttpServerRequest request) {
        String sectionId = request.getParam("sectionId");
        RequestUtils.bodyToJson(request, question -> {
            sectionService.update(sectionId, question, defaultResponseHandler(request));
        });
    }

    @Delete("/sections/:sectionId")
    @ApiDoc("Delete a specific section")
    @ResourceFilter(ShareAndOwner.class)
    @SecuredAction(value = Formulaire.CONTRIB_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void delete(HttpServerRequest request) {
        String sectionId = request.getParam("sectionId");
        sectionService.delete(sectionId, defaultResponseHandler(request));
    }
}