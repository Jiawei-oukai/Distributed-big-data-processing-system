package neu.edu.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minidev.json.JSONObject;
import neu.edu.demo.repository.PlanRepository;
import neu.edu.demo.validator.SchemaValidator;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

@Service
public class PlanService {

    @Autowired
    PlanRepository pr;
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public ResponseEntity patch (String inputStr, String id, String patchEtag) throws JsonProcessingException {


        ObjectNode plan = (ObjectNode) pr.findByKey(id);
        //if not exist
        if (plan == null){
            JSONObject response = new JSONObject();
            response.put("response","Invalid plan id");
            return ResponseEntity.status(404).body(response);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String nodeJson = objectMapper.writeValueAsString(plan);
        String etag = DigestUtils.md5DigestAsHex(nodeJson.getBytes());

        patchEtag = patchEtag.replace("\"","");

        if (!patchEtag.equals(etag)) { // Etag does not match
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        }
        //if exist
        SchemaValidator validator = new SchemaValidator();

        if (!validator.planPatchValidate(inputStr)){
            JSONObject invalid = new JSONObject();
            invalid.put("message","Invalid JSON schema.");
            return ResponseEntity.badRequest().body(invalid);
        }


        JsonNode inputPlan = null;
        try {
            inputPlan = objectMapper.readTree((inputStr));
        } catch (JsonProcessingException e) {
            JSONObject invalid = new JSONObject();
            invalid.put("message","Invalid JSON schema.");
            return ResponseEntity.badRequest().body(invalid);
        }

        Iterator<Map.Entry<String, JsonNode>> fields = inputPlan.fields();
        while (fields.hasNext()){
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            if (key != "linkedPlanServices"){
                // input value
                String inputValue = value.toString();
                // original value
                String planValue = plan.at("/" + key).toString();
                if (! inputValue.equals(planValue)){
                    JSONObject response = new JSONObject();
                    response.put("response", "Request refused!");
                    return ResponseEntity.badRequest().body(response);
                }
            }else {
                //check the schema
                ArrayNode planValue = (ArrayNode) plan.at("/linkedPlanServices");

                ArrayNode inputValue = (ArrayNode) value;

                for (int i = 0; i < inputValue.size(); i++){
                    boolean flag = true;
                    for (int j = 0; j < planValue.size(); j++){
                        if(inputValue.get(i).at("/objectId").toString().equals(planValue.get(j).at("/objectId").toString())) {
                            planValue.set(j, inputValue.get(i));
                            flag = false;
                        }
                    }
                    if (flag){
                        planValue.add(inputValue.get(i));
                    }
                }
                plan.set("linkedPlanServices", planValue);
            }
        }

        pr.save(plan);
        rabbitTemplate.convertAndSend("plan-exchange", "patch", id);

        JSONObject response = new JSONObject();
        response.put("response", "Request completed");
        String planString = null;
        try {
            planString = objectMapper.writeValueAsString(plan);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String responseEtag = DigestUtils.md5DigestAsHex(planString.getBytes());
        return ResponseEntity.ok().eTag(responseEtag).body(response);
    }

    public ResponseEntity delete(String id, String deleteEtag) throws JsonProcessingException {
        ObjectNode plan = (ObjectNode) pr.findByKey(id);
        //if not exist
        if (plan == null){
            JSONObject response = new JSONObject();
            response.put("response","Invalid plan id");
            return ResponseEntity.status(404).body(response);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String nodeJson = objectMapper.writeValueAsString(plan);
        String etag = DigestUtils.md5DigestAsHex(nodeJson.getBytes());

        deleteEtag = deleteEtag.replace("\"","");

        if (!deleteEtag.equals(etag)) { // Etag does not match
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        }
        rabbitTemplate.convertAndSend("plan-exchange", "delete", id);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pr.delete(id);
        return ResponseEntity.noContent().build();
    }
    public int calPoints(String[] operations) {
        Stack<Integer> stack = new Stack<Integer>();

        for (String o : operations){
            if (o.equals("C")){
                stack.pop();
            }else if (o.equals("D")){
                int score = stack.pop();
                stack.push(2 * score);
            }else if (o.equals("+")){
                int score1 = stack.pop();
                int score2 = stack.pop();
                stack.push(score1 + score2);
            }else {
                stack.push(Integer.parseInt(o));
            }
        }

        int sum = 0;

        while (!stack.isEmpty()){
            sum += stack.pop();
        }
        return sum;
    }

    public ResponseEntity<?> put(String jsonStr, String id, String etag){
        ResponseEntity<?> checkOutput = patchOrPutCheck(jsonStr, id, etag);
        if (checkOutput!=null) return checkOutput;

        JsonNode oriPlan = pr.findByKey(id);
        JsonNode newPlan = null;
        try {
            newPlan = objectMapper.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            JSONObject message = new JSONObject();
            message.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
        String newPlanId = newPlan.at("/objectId").asText();
        // Plan id does not match - 400 Bad Request
        if (!newPlanId.equals(id)) {
            JSONObject message = new JSONObject();
            message.put("message", "Plan id does not match!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
        // Plan exist && Etag match && Valid JSON Schema && Plan id match
        pr.save(newPlan);
        rabbitTemplate.convertAndSend("plan-exchange", "patch", id);
        JSONObject message = new JSONObject();
        message.put("message","Updated successfully!");
        return ResponseEntity.created(null).eTag(getEtag(newPlan)).body(message);
    }

    public ResponseEntity<?> patchOrPutCheck(String jsonStr, String id, String etag){
        JsonNode oriPlan = pr.findByKey(id);
        if (pr.findByKey(id) == null) {
            JSONObject message = new JSONObject();
            message.put("message", "Plan don't exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        // Etag does not match - 412 Precondition Failed
        String originalEtag = getEtag(oriPlan);
        if (!originalEtag.equals(etag.replace("\"",""))) return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        // JSON Schema is invalid - 400 Bad request
        SchemaValidator validator = new SchemaValidator();

        if (!validator.planValidate(jsonStr)) {
            JSONObject message = new JSONObject();
            message.put("message", "JsonSchema is inValid");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }
        return null;
    }

    private String getEtag(JsonNode node) {
        try {
            String nodeJson = objectMapper.writeValueAsString(node);
            return DigestUtils.md5DigestAsHex(nodeJson.getBytes());
        } catch (JsonProcessingException e) {
            System.out.println(new RuntimeException(e));
            return "";
        }
    }


}
