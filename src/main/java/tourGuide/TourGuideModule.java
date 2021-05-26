package tourGuide;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;


@Configuration
public class TourGuideModule {



    //TODO: Remove this from here


//    @Bean
//    public GpsUtilService gpsUtilService() {
//        return new GpsUtilService();
//    }
//    @Bean
//    public RewardCentralService rewardCentralService() {
//        return new RewardCentralService();
//    }
//    @Bean
//	public RewardsService rewardsService() {
//		return new RewardsService(gpsUtilService,rewardCentralService);
//	}
//
//    @Bean
//    public TourGuideService tourGuideService(){
//        return new TourGuideService(rewardsService(),gpsUtilService);
//    }

//	@Bean
//	public WebClient.Builder getWebClientBuilder(){
//		return WebClient.builder();
//	}

}
