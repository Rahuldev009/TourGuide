package tourGuide;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jsoniter.output.JsonStream;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.util.Utilities;
import tripPricer.Provider;

import java.util.*;

@RestController
public class TourGuideController {
    TourGuideService tourGuideService;
    RewardsService rewardsService;
    WebClient.Builder webClientBuilder;
    Utilities utilities;

    @Autowired
    public void TourGuideController(TourGuideService tourGuideService, RewardsService rewardsService, WebClient.Builder webClientBuilder, Utilities utilities) {
        this.tourGuideService = tourGuideService;
        this.rewardsService = rewardsService;
        this.webClientBuilder = webClientBuilder;
        this.utilities = utilities;
    }

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.location);
    }

    //  TODO: Change this method to no longer return a List of Attractions.
    //  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
    //  Return a new JSON object that contains:
    // Name of Tourist attraction,
    // Tourist attractions lat/long,
    // The user's location lat/long,
    // The distance in miles between the user's location and each of the attractions.
    // The reward points for visiting each Attraction.
    //    Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) throws JsonProcessingException {
        User user = tourGuideService.getUser(userName);
        Location userLocation = user.getLastVisitedLocation().location;
        List<NearbyAttractionDto> nearbyAttractionDtos = new ArrayList<>();
        PriorityQueue<NearbyAttractionDto> priorityQueue = new PriorityQueue<>(5, new Comparator<NearbyAttractionDto>() {
            @Override
            public int compare(NearbyAttractionDto o1, NearbyAttractionDto o2) {
                if(o1.getDistance()>o2.getDistance())
                    return 1;
                else if (o1.getDistance()<o2.getDistance())
                    return -1;
                return 0;
            }
        });

//       String s1 =webClientBuilder.build()
//                .get()
//                .uri("http://localhost:8081/getAttractions")
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        Gson gson = new Gson();
//        List<Attraction> attractionList = gson.fromJson(s1, new TypeToken<List<Attraction>>(){}.getType());
//        System.out.println("Attraction object --------"+ attractionList.get(2).attractionName);

        List<Attraction> attractionList = utilities.getAttractionList();

        for (int i = 0; i < attractionList.size(); i++) {
            NearbyAttractionDto nearbyAttractionDto = new NearbyAttractionDto();
            nearbyAttractionDto.setAttractionName(attractionList.get(i).attractionName);
            nearbyAttractionDto.setUserLocation(userLocation);
            Location location = new Location(attractionList.get(i).latitude,attractionList.get(i).longitude);
            nearbyAttractionDto.setAttractionLocation(location);
            double distance = rewardsService.getDistance(nearbyAttractionDto.getUserLocation(),nearbyAttractionDto.getAttractionLocation());
            nearbyAttractionDto.setDistance(distance);
            int rewardsPoints = utilities.getAttractionRewardPoints(attractionList.get(i).attractionId,user.getUserId());
            nearbyAttractionDto.setRewardPoints(rewardsPoints);
            priorityQueue.add(nearbyAttractionDto);
        }

        for (int i = 0; i <5 ; i++) {
            nearbyAttractionDtos.add(priorityQueue.poll()) ;
        }
        return JsonStream.serialize(nearbyAttractionDtos);
    }


    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName) {
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        // TODO: Get a list of every user's most recent location as JSON
        //- Note: does not use gpsUtil to query for their current location,
        //        but rather gathers the user's current location from their stored location history.
        //
        // Return object should be the just a JSON mapping of userId to Locations similar to:
        //     {
        //        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
        //        ...
        //     }
        List<User> userList = tourGuideService.getAllUsers();
        HashMap<String,Location> locationList = new HashMap();
        for (int i = 0; i < userList.size(); i++) {
            String userId = userList.get(i).getUserId().toString();
            Location location = userList.get(i).getLastVisitedLocation().location;
            locationList.put(userId, location);
        }
        return JsonStream.serialize(locationList);
    }

    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }


}