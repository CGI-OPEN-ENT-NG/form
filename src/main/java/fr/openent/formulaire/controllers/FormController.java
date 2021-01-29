package fr.openent.formulaire.controllers;

import fr.openent.formulaire.Formulaire;
import fr.openent.formulaire.export.FormResponsesExport;
import fr.openent.formulaire.security.AccessRight;
import fr.openent.formulaire.security.CreationRight;
import fr.openent.formulaire.security.canShareResourceFilter;
import fr.openent.formulaire.service.DistributionService;
import fr.openent.formulaire.service.FormService;
import fr.openent.formulaire.service.NeoService;
import fr.openent.formulaire.service.impl.DefaultDistributionService;
import fr.openent.formulaire.service.impl.DefaultFormService;
import fr.openent.formulaire.service.impl.DefaultNeoService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class FormController extends ControllerHelper {
    private static final Logger log = LoggerFactory.getLogger(FormController.class);
    private final Storage storage;
    private FormService formService;
    private DistributionService distributionService;
    private NeoService neoService;

    public FormController(final Storage storage) {
        super();
        this.storage = storage;
        this.formService = new DefaultFormService();
        this.distributionService = new DefaultDistributionService();
        this.neoService = new DefaultNeoService();
    }


    @ResourceFilter(canShareResourceFilter.class)
    @SecuredAction(value = Formulaire.READ_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void initReadResourceRight(final HttpServerRequest request) {
    }


    @Get("/forms")
    @ApiDoc("List all the forms created by me")
    @ResourceFilter(AccessRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void list(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                formService.list(user, arrayResponseHandler(request));
            } else {
                log.debug("User not found in session.");
                Renders.unauthorized(request);
            }
        });
    }

    @Get("/sentForms")
    @ApiDoc("List all the forms sent to me")
    @ResourceFilter(AccessRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void listSentForms(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                formService.listSentForms(user, arrayResponseHandler(request));
            } else {
                log.debug("User not found in session.");
                Renders.unauthorized(request);
            }
        });
    }

    @Get("/forms/:id")
    @ApiDoc("Get form thanks to the id")
    @ResourceFilter(AccessRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void get(HttpServerRequest request) {
        String id = request.getParam("id");
        formService.get(id, defaultResponseHandler(request));
    }

    @Put("/forms/:id")
    @ApiDoc("Update given form")
    @SecuredAction(Formulaire.CREATION_RIGHT)
    public void update(HttpServerRequest request) {
        String id = request.getParam("id");
        RequestUtils.bodyToJson(request, form -> {
            formService.update(id, form, defaultResponseHandler(request));
        });
    }

    @Post("/forms")
    @ApiDoc("Create a form")
    @ResourceFilter(CreationRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void create(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                RequestUtils.bodyToJson(request, form -> {
                    formService.create(form, user, defaultResponseHandler(request));
                });
            } else {
                log.debug("User not found in session.");
                Renders.unauthorized(request);
            }
        });
    }

    @Delete("/forms/:id")
    @ApiDoc("Delete given form")
    @ResourceFilter(CreationRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void delete(HttpServerRequest request) {
        String id = request.getParam("id");
        formService.delete(id, defaultResponseHandler(request));
    }

    // Export

    @Get("/export/:formId")
    @ApiDoc("Export given form")
    @ResourceFilter(CreationRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void export(HttpServerRequest request) {
        new FormResponsesExport(request).launch();
    }


    // Share/Sending functions

    @Get("/share/json/:id")
    @ApiDoc("Lists rights for a given form.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void shareJson(final HttpServerRequest request) {
        super.shareJson(request, false);
    }

    @Put("/share/json/:id")
    @ApiDoc("Adds rights for a given form.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void shareSubmit(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                request.pause();
                final String formId = request.params().get("id");
                formService.get(formId, getFormHandler -> {
                    request.resume();
                    final String formName = getFormHandler.right().getValue().getString("title");
                    JsonObject params = new fr.wseduc.webutils.collections.JsonObject();
//                    params.put("username", user.getUsername());
//                    params.put("uri", pathPrefix + "#/subject/copy/preview/perform/"+formId);
//                    params.put("userUri", "/userbook/annuaire#" + user.getUserId() + "#" + user.getType());
//                    params.put("subjectName", formName);
//                    params.put("resourceUri", params.getString("uri"));
//                    JsonObject pushNotif = new JsonObject()
//                            .put("title", "exercizer.share")
//                            .put("body", I18n.getInstance().translate(
//                                    "exercizer.push.notif.share.body",
//                                    getHost(request),
//                                    I18n.acceptLanguage(request),
//                                    user.getUsername(),
//                                    formName
//                            ));
//
//                    params.put("pushNotif", pushNotif);
                    FormController.super.shareJsonSubmit(request, "formulaire.share", false, params, null);
                });
            }
            else {
                log.debug("User not found in session.");
                unauthorized(request);
            }
        });
    }

    @Put("/share/resource/:id")
    @ApiDoc("Adds rights for a given form.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void shareResource(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "share", shareFormObject -> {
            UserUtils.getUserInfos(eb, request, user -> {
                if (user != null) {
                    final String formId = request.params().get("id");
                    Map<String, Object> idUsers = shareFormObject.getJsonObject("users").getMap();
                    Map<String, Object> idGroups = shareFormObject.getJsonObject("groups").getMap();
                    Map<String, Object> idBookmarks = shareFormObject.getJsonObject("bookmarks").getMap();

                    JsonArray usersIds = new JsonArray(new ArrayList<>(idUsers.keySet()));
                    JsonArray groupsIds = new JsonArray(new ArrayList<>(idGroups.keySet()));
                    JsonArray bookmarksIds = new JsonArray(new ArrayList<>(idBookmarks.keySet()));

                    neoService.getUsersInfosFromIds(usersIds, groupsIds, eventUsers -> {
                        if (eventUsers.isRight()) {
                            JsonArray infos = eventUsers.right().getValue();
                            removeDeletedDistributions(formId, infos);
                            addNewDistributions(formId, user, infos);
                        } else {
                            log.error("[Formulaire@getUserIds] Fail to get users' ids from groups' ids");
                        }
                    });

                    super.shareResource(request, "formulaire.share", false, null, null);
                } else {
                    log.debug("User not found in session.");
                    unauthorized(request);
                }
            });
        });
    }


    @Put("/share/remove/:id")
    @ApiDoc("Removes rights for a given form.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void shareRemove(final HttpServerRequest request) {
        super.removeShare(request, false);
    }

    @Get("/publish")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void sharePublish(final HttpServerRequest request) {
        // This route is used to create publish Workflow right, nothing to do
        return;
    }

    private void removeDeletedDistributions(String formId, JsonArray users) {
        distributionService.getRemoved(formId, users, filteringEvent -> {
            if (filteringEvent.isRight()) {
                JsonArray removed = filteringEvent.right().getValue();
                if (!removed.isEmpty()) {
                    distributionService.removeMultiple(formId, removed, event -> {
                        if (event.isRight()) {
                            log.info("[Formulaire@removeDeletedDistributions] Successful remove in distribution table");
                        } else {
                            log.error("[Formulaire@removeDeletedDistributions] Fail to remove distributions : " + event.left().getValue());
                        }
                    });
                }
            } else {
                log.error("[Formulaire@removeDeletedDistributions] Fail to filter distributions to remove : " + filteringEvent.left().getValue());
            }
        });
    }

    private void addNewDistributions(String formId, UserInfos user, JsonArray users) {
        if (!users.isEmpty()) {
            distributionService.getDuplicates(formId, users, filteringEvent -> {
                if (filteringEvent.isRight()) {
                    JsonArray duplicates = filteringEvent.right().getValue();
                    distributionService.createMultiple(formId, user, users, duplicates, event -> {
                        if (event.isRight()) {
                            log.info("[Formulaire@addNewDistributions] Successful adding in distribution table");
                        } else {
                            log.error("[Formulaire@addNewDistributions] Fail to add distributions : " + event.left().getValue());
                        }
                    });
                } else {
                    log.error("[Formulaire@addNewDistributions] Fail to filter existing distributions : " + filteringEvent.left().getValue());
                }
            });
        }
    }

}