package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;
import tourGuide.gpsUtil.Attraction;
import tourGuide.gpsUtil.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;


public class TestPerformance {

//	GpsUtilService gpsUtilService;
//	RewardCentralService rewardCentralService;
//
//	@Autowired
//	public TestPerformance(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService) {
//		this.gpsUtilService = gpsUtilService;
//		this.rewardCentralService = rewardCentralService;
//	}


	/*
	 * A note on performance improvements:
	 *
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *
	 *
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent.
	 *
	 *     These are performance metrics that we are trying to hit:
	 *
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	//@Ignore
	@Test
	public void highVolumeTrackLocation() throws InterruptedException {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardCentralService rewardCentralService = new RewardCentralService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, rewardCentralService);

		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		// 10000 users around 14 minutes
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(rewardsService,gpsUtilService,rewardCentralService);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ExecutorService executorService = Executors.newFixedThreadPool(64);
		for(User user : allUsers) {
			executorService.execute(new Runnable() {
				public void run() {
					tourGuideService.trackUserLocation(user);
				}
			});
		}
		executorService.shutdown();
		executorService.awaitTermination(16, TimeUnit.MINUTES);
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	//@Ignore
	@Test
	public void highVolumeGetRewards() throws InterruptedException {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardCentralService rewardCentralService = new RewardCentralService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, rewardCentralService);
		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		//Getting concurrency error
		InternalTestHelper.setInternalUserNumber(100);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(rewardsService,gpsUtilService,rewardCentralService);

	    Attraction attraction = gpsUtilService.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
		ExecutorService executorService = Executors.newFixedThreadPool(64);
		for(User user : allUsers) {
			executorService.execute(new Runnable() {
				public void run() {
					rewardsService.calculateRewards(user);
				}
			});
		}
//	    allUsers.forEach(u -> rewardsService.calculateRewards(u));
		executorService.shutdown();
		executorService.awaitTermination(21, TimeUnit.MINUTES);
		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
