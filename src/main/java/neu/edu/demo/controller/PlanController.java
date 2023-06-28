package neu.edu.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import neu.edu.demo.repository.PlanRepository;
import neu.edu.demo.validator.SchemaValidator;
import neu.edu.demo.service.PlanService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class PlanController {

    @Autowired
    PlanRepository pr;
    @Autowired
    PlanService ps;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/home")
    public String home() {
        return "this is homepage.";
    }

    @GetMapping("/plan/find/{key}")
    public ResponseEntity get(@PathVariable String key) throws JsonProcessingException {
        JsonNode plan = pr.findByKey(key);
        //Plan plan = pr.findByKey(key);
        if (plan == null){
            JSONObject message = new JSONObject();
            message.put("message","Not Found.");
            return ResponseEntity.status(404).body(message);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String planString = objectMapper.writeValueAsString(plan);
        String etag = DigestUtils.md5DigestAsHex(planString.getBytes());
        return ResponseEntity.ok().eTag(etag).body(plan);
    }

    @PostMapping("/plan/save")
    public ResponseEntity create(@RequestBody String inputStr, HttpServletResponse response) throws JsonProcessingException {
        //validate
        SchemaValidator validator = new SchemaValidator();
        if (validator.planValidate(inputStr)){
            //cast String plan to the instance of Class Plan
            ObjectMapper objectMapper = new ObjectMapper();
            //Plan plan= MAPPER.readValue(inputStr, Plan.class);
            JsonNode plan = objectMapper.readTree(inputStr);
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("objectId",plan.at("/objectId").asText());
            pr.save(plan);
            //get etag
            String planString = objectMapper.writeValueAsString(plan);
            String etag = DigestUtils.md5DigestAsHex(planString.getBytes());

            rabbitTemplate.convertAndSend("plan-exchange", "plan.create", inputStr);
            return ResponseEntity.created(null).eTag(etag).body(jsonObj);
        }

        JSONObject invalid = new JSONObject();
        invalid.put("message","Invalid JSON schema.");
        return ResponseEntity.badRequest().body(invalid);
    }

    @DeleteMapping("/plan/delete/{key}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public ResponseEntity delete(@PathVariable String key, @RequestHeader("If-Match") String etag) throws JsonProcessingException {

        return ps.delete(key, etag);
    }

    @PatchMapping("/plan/patch/{id}")
    public ResponseEntity patch(@RequestBody String inputStr, @PathVariable String id, @RequestHeader("If-Match") String etag) throws JsonProcessingException {
        return ps.patch(inputStr, id, etag);
    }

    @PutMapping("/plan/put/{id}")
    public ResponseEntity<?> put(@RequestBody String jsonStr, @PathVariable String id, @RequestHeader("If-Match") String etag){
        return ps.put(jsonStr, id, etag);
    }
}
