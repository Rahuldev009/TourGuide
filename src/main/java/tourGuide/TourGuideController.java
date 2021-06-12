package tourGuide;

import com.jsoniter.output.JsonStream;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.gpsUtil.Location;
import tourGuide.gpsUtil.VisitedLocation;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

import java.util.HashMap;
import java.util.List;

@RestController
public class TourGuideController {
    private TourGuideService tourGuideService;

    @Autowired
    public TourGuideController(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
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
    public String getNearbyAttractions(@RequestParam String userName) {
        User user = getUser(userName);
        List<NearbyAttractionDto> nearbyAttractionDtos = tourGuideService.getUserNearbyAttractions(user);
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
        HashMap<String, Location> locationList = tourGuideService.getAllUsersCurrentLocation();
        return JsonStream.serialize(locationList);
    }

    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }

    @RequestMapping("/getUserPreference")
    public String getUserPreference(@RequestParam String userName) {
        User user = getUser(userName);
        return JsonStream.serialize(user.getUserPreferences());
    }

    @RequestMapping("/setUserPreference")
    public void setUserPreference(@RequestParam String userName, @RequestParam int attractionProximity, @RequestParam Money lowerPricePoint,
                                  @RequestParam Money highPricePoint, @RequestParam int tripDuration,
                                  @RequestParam int ticketQuantity, @RequestParam int numberOfAdults,
                                  @RequestParam int numberOfChildren) {
        User user = getUser(userName);
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setAttractionProximity(attractionProximity);
        userPreferences.setLowerPricePoint(lowerPricePoint);
        userPreferences.setHighPricePoint(highPricePoint);
        userPreferences.setTripDuration(tripDuration);
        userPreferences.setTicketQuantity(ticketQuantity);
        userPreferences.setNumberOfAdults(numberOfAdults);
        userPreferences.setNumberOfChildren(numberOfChildren);
        user.setUserPreferences(userPreferences);
    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }


}