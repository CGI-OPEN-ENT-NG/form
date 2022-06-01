package fr.openent.formulaire_public.service.impl;

import fr.openent.form.core.constants.Tables;
import fr.openent.formulaire_public.service.ResponseService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;

import java.util.ArrayList;

public class DefaultResponseService implements ResponseService {

    @Override
    public void createResponses(JsonArray responses, JsonObject distribution, Handler<Either<String, JsonArray>> handler) {
        ArrayList<JsonObject> responsesList = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            if (responses.getJsonObject(i) != null) {
                JsonObject response = responses.getJsonObject(i);
                responsesList.add(response);
            }
        }

        if (!responsesList.isEmpty()) {
            SqlStatementsBuilder s = new SqlStatementsBuilder();
            String query = "INSERT INTO " + Tables.RESPONSE + " (question_id, choice_id, answer, responder_id, distribution_id) " +
                    "VALUES (?, ?, ?, ?, ?);";

            s.raw("BEGIN;");
            for (JsonObject response : responsesList) {
                JsonArray params = new JsonArray()
                        .add(response.getInteger("question_id", null))
                        .add(response.getInteger("choice_id", null))
                        .add(response.getString("answer", ""))
                        .add("")
                        .add(distribution.getInteger("id", null));
                s.prepared(query, params);
            }
            s.raw("COMMIT;");

            Sql.getInstance().transaction(s.build(), SqlResult.validResultHandler(handler));
        }
        else {
            handler.handle(new Either.Right<>(new JsonArray()));
        }
    }
}
