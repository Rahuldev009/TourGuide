package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class RewardCentralService {
    private Logger logger = LoggerFactory.getLogger(RewardCentralService.class);

    /**
     * Reward points for visiting particular location
     * @param attractionId is the unique id of the attraction
     * @param userId is the unique id of the user
     * @return rewards point for visiting particular attraction point from current location
     */
    public int getAttractionRewardPoints(UUID attractionId, UUID userId){
        WebClient.Builder webClientBuilder = WebClient.builder();
        String JsonResponseFrom =webClientBuilder.build()
                .get()
                .uri("http://localhost:8082/getAttractionRewardPoints?attractionId="+attractionId+"&userId="+userId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return Integer.parseInt(JsonResponseFrom);
    }

}
