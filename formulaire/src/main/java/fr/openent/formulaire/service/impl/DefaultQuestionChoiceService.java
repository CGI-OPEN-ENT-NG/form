package fr.openent.formulaire.service.impl;

import fr.openent.form.core.constants.Tables;
import fr.openent.formulaire.service.QuestionChoiceService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

public class DefaultQuestionChoiceService implements QuestionChoiceService {

    @Override
    public void list(String questionId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Tables.QUESTION_CHOICE + " WHERE question_id = ? ORDER BY id;";
        JsonArray params = new JsonArray().add(questionId);
        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void listChoices(JsonArray questionIds, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Tables.QUESTION_CHOICE + " WHERE question_id IN " + Sql.listPrepared(questionIds);
        JsonArray params = new JsonArray().addAll(questionIds);
        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void get(String choiceId, Handler<Either<String, JsonObject>> handler) {
        String query = "SELECT * FROM " + Tables.QUESTION_CHOICE + " WHERE id = ?;";
        JsonArray params = new JsonArray().add(choiceId);
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void create(String questionId, JsonObject choice, Handler<Either<String, JsonObject>> handler) {
        String query = "INSERT INTO " + Tables.QUESTION_CHOICE + " (question_id, value, position, type, next_section_id) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING *;";
        JsonArray params = new JsonArray()
                .add(questionId)
                .add(choice.getString("value", ""))
                .add(choice.getInteger("position", 0))
                .add(choice.getString("type", ""))
                .add(choice.getInteger("next_section_id", null));
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void duplicate(int formId, int questionId, int originalQuestionId, Handler<Either<String, JsonObject>> handler) {
        String query = "INSERT INTO " + Tables.QUESTION_CHOICE + " (question_id, value, type, next_section_id) " +
                "SELECT ?, value, type, (SELECT id FROM " + Tables.SECTION + " WHERE original_section_id = qc.next_section_id AND form_id = ?) " +
                "FROM " + Tables.QUESTION_CHOICE + " qc " +
                "WHERE question_id = ? ORDER BY qc.id;";
        JsonArray params = new JsonArray().add(questionId).add(formId).add(originalQuestionId);
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void update(String choiceId, JsonObject choice, Handler<Either<String, JsonObject>> handler) {
        String query = "UPDATE " + Tables.QUESTION_CHOICE + " SET value = ?, position = ?, type = ?, next_section_id = ? " +
                "WHERE id = ? RETURNING *;";
        JsonArray params = new JsonArray()
                .add(choice.getString("value", ""))
                .add(choice.getInteger("position", 0))
                .add(choice.getString("type", ""))
                .add(choice.getInteger("next_section_id", null))
                .add(choiceId);
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void delete(String choiceId, Handler<Either<String, JsonObject>> handler) {
        String query = "DELETE FROM " + Tables.QUESTION_CHOICE + " WHERE id = ?;";
        JsonArray params = new JsonArray().add(choiceId);
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }
}