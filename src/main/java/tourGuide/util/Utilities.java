package tourGuide.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gpsUtil.location.Attraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;


public class Utilities {

   private  WebClient.Builder webClientBuilder;

   @Autowired
    public void Utilities(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public List<Attraction> getAttractionList(){
        String jsonResponseFromGetAttraction = webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/getAttractions")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Gson gson = new Gson();
        List<Attraction> attractionList = gson.fromJson(jsonResponseFromGetAttraction,
                new TypeToken<List<Attraction>>(){}.getType());
        return attractionList;
    }

    public int getAttractionRewardPoints(UUID attractionId, UUID userId){

        String JsonResponseFrom =webClientBuilder.build()
                .get()
                .uri("http://localhost:8082/getAttractionRewardPoints?attractionId="+attractionId+"&userId="+userId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return Integer.parseInt(JsonResponseFrom);

    }

}
