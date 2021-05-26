package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class RewardCentralService {
    private Logger logger = LoggerFactory.getLogger(RewardCentralService.class);

    public int getAttractionRewardPoints(UUID attractionId, UUID userId){
        WebClient.Builder webClientBuilder = WebClient.builder();
        //TODO: 1. Write loggers to print response times 2. Use url parameters option instead of appending to the URL
        String JsonResponseFrom =webClientBuilder.build()
                .get()
                .uri("http://localhost:8082/getAttractionRewardPoints?attractionId="+attractionId+"&userId="+userId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return Integer.parseInt(JsonResponseFrom);

    }
}
