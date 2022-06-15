package fr.openent.formulaire.service.impl;

import fr.openent.form.core.constants.Tables;
import fr.openent.formulaire.service.QuestionService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;

import static fr.openent.form.helpers.SqlHelper.getParamsForUpdateDateModifFormRequest;
import static fr.openent.form.helpers.SqlHelper.getUpdateDateModifFormRequest;

public class DefaultQuestionService implements QuestionService {
    private final Sql sql = Sql.getInstance();

    @Override
    public void listForForm(String formId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Tables.QUESTION + " WHERE form_id = ? AND section_id IS NULL ORDER BY position;";
        JsonArray params = new JsonArray().add(formId);
        sql.prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void listForSection(String sectionId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Tables.QUESTION + " WHERE section_id = ? ORDER BY section_position;";
        JsonArray params = new JsonArray().add(sectionId);
        sql.prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void listForFormAndSection(String formId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Tables.QUESTION + " WHERE form_id = ? " +
                "ORDER BY position, section_id, section_position;";
        JsonArray params = new JsonArray().add(formId);
        sql.prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void export(String formId, boolean isPdf, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT q.*, (CASE WHEN q.position ISNULL THEN s.position WHEN s.position ISNULL THEN q.position END) AS element_position " +
                "FROM " + Tables.QUESTION + " q " +
                "LEFT JOIN " + Tables.SECTION + " s ON q.section_id = s.id " +
                "WHERE q.form_id = ? " + (isPdf ? "" : "AND question_type != 1 ") +
                "ORDER BY element_position, q.section_position;";
        JsonArray params = new JsonArray().add(formId);
        sql.prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void get(String questionId, Handler<Either<String, JsonObject>> handler) {
        String query = "SELECT * FROM " + Tables.QUESTION + " WHERE id = ?;";
        JsonArray params = new JsonArray().add(questionId);
        sql.prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getSectionIdsWithConditionalQuestions(String formId, JsonArray questionIds, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT DISTINCT section_id FROM " + Tables.QUESTION + " WHERE form_id = ? AND conditional = ? " +
                "AND section_id IS NOT NULL ";
        JsonArray params = new JsonArray().add(formId).add(true);

        if (questionIds.size() > 0) {
            query += "AND id NOT IN " + Sql.listPrepared(questionIds);
            params.addAll(questionIds);
        }

        query += ";";

        sql.prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getSectionIdsByForm(String questionId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Tables.SECTION  +
                " WHERE form_id = (SELECT form_id FROM " + Tables.QUESTION + " WHERE id = ?);";
        JsonArray params = new JsonArray().add(questionId);
        sql.prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getFormPosition(String questionId, Handler<Either<String, JsonObject>> handler) {
        String query = "SELECT DISTINCT (SELECT MAX(pos) as position FROM (VALUES (q.position), (s.position)) AS value(pos)) " +
                "FROM " + Tables.QUESTION + " q " +
                "LEFT JOIN " + Tables.SECTION + " s ON s.id = q.section_id " +
                "WHERE q.id = ?;";
        JsonArray params = new JsonArray().add(questionId);
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void create(JsonObject question, String formId, Handler<Either<String, JsonObject>> handler) {
        String query = "INSERT INTO " + Tables.QUESTION + " (form_id, title, position, question_type, statement, " +
                "mandatory, section_id, section_position, conditional) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *;";
        JsonArray params = new JsonArray()
                .add(formId)
                .add(question.getString("title", ""))
                .add(question.getInteger("section_position", null) != null ? null : question.getInteger("position", null))
                .add(question.getInteger("question_type", 1))
                .add(question.getString("statement", ""))
                .add(question.getBoolean("conditional", false) || question.getBoolean("mandatory", false))
                .add(question.getInteger("section_id", null))
                .add(question.getInteger("section_position", null))
                .add(question.getBoolean("conditional", false));

        query += getUpdateDateModifFormRequest();
        params.addAll(getParamsForUpdateDateModifFormRequest(formId));

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void update(String formId, JsonArray questions, Handler<Either<String, JsonArray>> handler) {
        if (!questions.isEmpty()) {
            SqlStatementsBuilder s = new SqlStatementsBuilder();
            String query = "UPDATE " + Tables.QUESTION + " SET title = ?, position = ?, question_type = ?, " +
                    "statement = ?, mandatory = ?, section_id = ?, section_position = ?, conditional = ?  WHERE id = ? RETURNING *;";

            s.raw("BEGIN;");
            for (int i = 0; i < questions.size(); i++) {
                JsonObject question = questions.getJsonObject(i);
                JsonArray params = new JsonArray()
                        .add(question.getString("title", ""))
                        .add(question.getInteger("section_position", null) != null ? null : question.getInteger("position", null))
                        .add(question.getInteger("question_type", 1))
                        .add(question.getString("statement", ""))
                        .add(question.getBoolean("conditional", false) || question.getBoolean("mandatory", false))
                        .add(question.getInteger("section_id", null))
                        .add(question.getInteger("section_position", null))
                        .add(question.getBoolean("conditional", false))
                        .add(question.getInteger("id", null));
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
    public void delete(JsonObject question, Handler<Either<String, JsonObject>> handler) {
        String query = "DELETE FROM " + Tables.QUESTION + " WHERE id = ?;";
        JsonArray params = new JsonArray().add(question.getInteger("id"));

        query += getUpdateDateModifFormRequest();
        params.addAll(getParamsForUpdateDateModifFormRequest(question.getInteger("form_id").toString()));

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }
}