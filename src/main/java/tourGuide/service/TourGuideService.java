package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.gpsUtil.Attraction;
import tourGuide.gpsUtil.Location;
import tourGuide.gpsUtil.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    public final Tracker tracker;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();
    boolean testMode = true;
    private GpsUtilService gpsUtilService;
    private RewardCentralService rewardCentralService;

    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    @Autowired
    public TourGuideService(RewardsService rewardsService, GpsUtilService gpsUtilService,
                            RewardCentralService rewardCentralService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardsService = rewardsService;
        this.rewardCentralService = rewardCentralService;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                trackUserLocation(user);
        return visitedLocation;
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        List<Attraction> nearbyAttractions = new ArrayList<>();
        for (Attraction attraction : gpsUtilService.getAttractions()) {
            if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
                nearbyAttractions.add(attraction);
            }
        }

        return nearbyAttractions;
    }

    public List<NearbyAttractionDto> getUserNearbyAttractions(User user){
        System.out.println("username ------"+ user.getUserName());
        Location userLocation = user.getLastVisitedLocation().location;
        List<NearbyAttractionDto> nearbyAttractionDtos = new ArrayList<>();
        PriorityQueue<NearbyAttractionDto> priorityQueue = new PriorityQueue<>(5, new Comparator<NearbyAttractionDto>() {
            @Override
            public int compare(NearbyAttractionDto o1, NearbyAttractionDto o2) {
                if (o1.getDistance() > o2.getDistance())
                    return 1;
                else if (o1.getDistance() < o2.getDistance())
                    return -1;
                return 0;
            }
        });
        List<Attraction> attractionList = gpsUtilService.getAttractions();

        for (int i = 0; i < attractionList.size(); i++) {
            NearbyAttractionDto nearbyAttractionDto = new NearbyAttractionDto();
            Attraction attraction = attractionList.get(i);
            nearbyAttractionDto.setAttractionName(attraction.attractionName);
            nearbyAttractionDto.setUserLocation(userLocation);
            Location location = new Location(attraction.latitude, attraction.longitude);
            nearbyAttractionDto.setAttractionLocation(location);
            double distance = rewardsService.getDistance(nearbyAttractionDto.getUserLocation(), nearbyAttractionDto.getAttractionLocation());
            nearbyAttractionDto.setDistance(distance);
            int rewardsPoints = rewardCentralService.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
            nearbyAttractionDto.setRewardPoints(rewardsPoints);
            priorityQueue.add(nearbyAttractionDto);
        }
        for (int i = 0; i < 5; i++) {
            nearbyAttractionDtos.add(priorityQueue.poll());
        }
        return nearbyAttractionDtos;
    }

    public HashMap<String, Location> getAllUsersCurrentLocation(){
        List<User> userList = getAllUsers();
        HashMap<String, Location> locationList = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            String userId = userList.get(i).getUserId().toString();
            Location location = userList.get(i).getLastVisitedLocation().location;
            locationList.put(userId, location);
        }
        return locationList;
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
