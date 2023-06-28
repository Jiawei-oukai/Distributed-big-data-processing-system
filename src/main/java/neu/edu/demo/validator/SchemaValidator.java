package neu.edu.demo.validator;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;


public class SchemaValidator {

    public boolean planValidate(String plan) {
        boolean flag = true;

        try {
            JSONObject planJSON = new JSONObject(plan);
            InputStream inputStreamJsonSchema = getClass().getResourceAsStream("/PlanJSONSchema.json");
            JSONObject Schema = new JSONObject(new JSONTokener(inputStreamJsonSchema));
            Schema schema = SchemaLoader.load(Schema);
            schema.validate(planJSON);

        } catch (ValidationException | org.json.JSONException e) {
            flag = false;
        }
        return flag;
    }

    public boolean planPatchValidate(String plan) {
        boolean flag = true;

        try {
            JSONObject planJSON = new JSONObject(plan);
            InputStream inputStreamJsonSchema = getClass().getResourceAsStream("/PlanJSONSchemaPatch.json");
            JSONObject Schema = new JSONObject(new JSONTokener(inputStreamJsonSchema));
            Schema schema = SchemaLoader.load(Schema);
            schema.validate(planJSON);

        } catch (ValidationException | org.json.JSONException e) {
            flag = false;
        }
        return flag;
    }
}
