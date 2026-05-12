package pl.goeuropa.counter.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.goeuropa.counter.configs.ApiProperties;
import pl.goeuropa.counter.repository.PeopleCountRepository;
import pl.goeuropa.counter.service.ScheduleTasksService;

import java.util.Map;

@Slf4j
@Component
public class JsessionScheduler {

    private final ApiProperties properties;
    private final RestClient restClient;
    private final ScheduleTasksService service;

    private final PeopleCountRepository peopleCountRepository = PeopleCountRepository.getInstance();

    public JsessionScheduler(ApiProperties properties, @Qualifier("video") RestClient restClient, ScheduleTasksService service) {
        this.properties = properties;
        this.restClient = restClient;
        this.service = service;
    }

    @PostConstruct
    @Scheduled(cron = "@daily")
    public void getJSessionId() {
        try {
            var key = properties.getVideoKey();
            var account = properties.getVideoAccount();
            var response = restClient.get()
                    .uri(properties.getLoginPath() + "?account={account}&password={key}", account, key)
                    .retrieve()
                    .body(Map.class);
            service.setJSessionId(response);
            log.info("Succeed generate JSESSIONID : [ {} ]", peopleCountRepository.getJSessionId());
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }
}
