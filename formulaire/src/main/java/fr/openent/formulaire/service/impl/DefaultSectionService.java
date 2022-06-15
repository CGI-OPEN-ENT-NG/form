package fr.openent.formulaire.service.impl;

import fr.openent.form.core.constants.Tables;
import fr.openent.formulaire.service.SectionService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;

import static fr.openent.form.helpers.SqlHelper.getParamsForUpdateDateModifFormRequest;
import static fr.openent.form.helpers.SqlHelper.getUpdateDateModifFormRequest;

public class DefaultSectionService implements SectionService {
    private final Sql sql = Sql.getInstance();

    @Override
    public void list(String formId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Tables.SECTION + " WHERE form_id = ? ORDER BY position;";
        JsonArray params = new JsonArray().add(formId);
        sql.prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void get(String sectionId, Handler<Either<String, JsonObject>> handler) {
        String query = "SELECT * FROM " + Tables.SECTION + " WHERE id = ?;";
        JsonArray params = new JsonArray().add(sectionId);
        sql.prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void create(JsonObject section, String formId, Handler<Either<String, JsonObject>> handler) {
        String query = "INSERT INTO " + Tables.SECTION + " (form_id, title, description, position) " +
                "VALUES (?, ?, ?, ?) RETURNING *;";
        JsonArray params = new JsonArray()
                .add(formId)
                .add(section.getString("title", ""))
                .add(section.getString("description", ""))
                .add(section.getInteger("position", null));

        query += getUpdateDateModifFormRequest();
        params.addAll(getParamsForUpdateDateModifFormRequest(formId));

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void update(String formId, JsonArray sections, Handler<Either<String, JsonArray>> handler) {
        if (!sections.isEmpty()) {
            SqlStatementsBuilder s = new SqlStatementsBuilder();
            String query = "UPDATE " + Tables.SECTION + " SET title = ?, description = ?, position = ? WHERE id = ? RETURNING *;";

            s.raw("BEGIN;");
            for (int i = 0; i < sections.size(); i++) {
                JsonObject section = sections.getJsonObject(i);
                JsonArray params = new JsonArray()
                        .add(section.getString("title", ""))
                        .add(section.getString("description", ""))
                        .add(section.getInteger("position", null))
                        .add(section.getInteger("id", null));
                s.prepared(query, params);
            }

            s.prepared(getUpdateDateModifFormRequest(), getParamsForUpdateDateModifFormRequest(formId));
            s.raw("COMMIT;");

            sql.transaction(s.build(), SqlResult.validResultsHandler(handler));
        }
        else {
            handler.handle(new Either.Right<>(new JsonArray()));
        }
    }

    @Override
    public void delete(JsonObject section, Handler<Either<String, JsonObject>> handler) {
        String query = "DELETE FROM " + Tables.SECTION + " WHERE id = ?;";
        JsonArray params = new JsonArray().add(section.getInteger("id").toString());

        query += getUpdateDateModifFormRequest();
        params.addAll(getParamsForUpdateDateModifFormRequest(section.getInteger("form_id").toString()));

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }
}