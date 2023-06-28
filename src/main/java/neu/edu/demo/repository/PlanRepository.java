package neu.edu.demo.repository;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PlanRepository {

    @Autowired
    RedisTemplate<Object,Object> redisTemplate;

    public JsonNode findByKey(String key){

        JsonNode plan = (JsonNode) redisTemplate.opsForValue().get(key);
        return plan;
    }

    public void save(JsonNode plan){
        redisTemplate.opsForValue().set(plan.at("/objectId").asText(),plan);
    }

    public void delete(String key){
        redisTemplate.delete(key);
    }
}
