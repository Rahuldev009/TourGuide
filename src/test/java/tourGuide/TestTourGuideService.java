package tourGuide;

import org.junit.Test;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.gpsUtil.Attraction;
import tourGuide.gpsUtil.Location;
import tourGuide.gpsUtil.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestTourGuideService {
    GpsUtilService gpsUtilService = new GpsUtilService();
    RewardCentralService rewardCentralService = new RewardCentralService();
    RewardsService rewardsService = new RewardsService(gpsUtilService, rewardCentralService);
    TourGuideService tourGuideService = new TourGuideService(rewardsService, gpsUtilService, rewardCentralService);


    @Test
    public void getUserLocation() {
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        tourGuideService.tracker.stopTracking();
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }

    @Test
    public void addUser() {
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);
        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());
        tourGuideService.tracker.stopTracking();
        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);
        List<User> allUsers = tourGuideService.getAllUsers();
        tourGuideService.tracker.stopTracking();
        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() {
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        tourGuideService.tracker.stopTracking();
        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    public void getNearbyAttractions() {
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new Location(33.817595D, -117.922008D), new Date());
        List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
        tourGuideService.tracker.stopTracking();
        assertEquals(4, attractions.size());
    }

    @Test
    public void getTripDeals() {
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        List<Provider> providers = tourGuideService.getTripDeals(user);
        tourGuideService.tracker.stopTracking();
        assertEquals(5, providers.size());
    }

    @Test
    public void getUserNearbyAttractions() {
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(33.817595D, -118.922008D), new Date()));
        List<NearbyAttractionDto> nearbyAttractionDtos = tourGuideService.getUserNearbyAttractions(user);
        tourGuideService.tracker.stopTracking();
        assertEquals(5, nearbyAttractionDtos.size());
        assertTrue(nearbyAttractionDtos.get(0).getDistance() < nearbyAttractionDtos.get(1).getDistance());
    }

    @Test
    public void getAllUsersCurrentLocation() {
        InternalTestHelper.setInternalUserNumber(0);
        List<User> allUsers = tourGuideService.getAllUsers();
        HashMap<String, Location> locationList = tourGuideService.getAllUsersCurrentLocation();
        tourGuideService.tracker.stopTracking();
        assertEquals(allUsers.size(), locationList.size());
    }

}
