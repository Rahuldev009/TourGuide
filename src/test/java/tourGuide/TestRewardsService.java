package tourGuide;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRewardsService {

//    GpsUtilService gpsUtilService;
//    RewardCentralService rewardCentralService;
//
//
//    @Autowired
//    public TestRewardsService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService) {
//        this.gpsUtilService = gpsUtilService;
//        this.rewardCentralService = rewardCentralService;
//    }

    private Logger logger = LoggerFactory.getLogger(TestRewardsService.class);
    @Test
    public void userGetRewards() {

        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardCentralService rewardCentralService = new RewardCentralService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, rewardCentralService);

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(rewardsService, gpsUtilService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        tourGuideService.trackUserLocation(user);
        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();
        assertTrue(userRewards.size() == 1);
    }

    @Test
    public void isWithinAttractionProximity() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardCentralService rewardCentralService = new RewardCentralService();
         RewardsService rewardsService = new RewardsService(gpsUtilService, rewardCentralService);
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    //@Ignore // Needs fixed - can throw ConcurrentModificationException
    @Test
    public void nearAllAttractions() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardCentralService rewardCentralService = new RewardCentralService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, rewardCentralService);
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);

        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(rewardsService, gpsUtilService);

        rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
        List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
        tourGuideService.tracker.stopTracking();

        assertEquals(gpsUtilService.getAttractions().size(), userRewards.size());
    }

}
