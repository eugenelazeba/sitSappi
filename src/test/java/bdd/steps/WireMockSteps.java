package bdd.steps;

import bdd.utils.WireMockClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static bdd.steps.Sappi.PATH_RESOURCES;

@Slf4j
public class WireMockSteps {


    static final Config config = ConfigFactory.load();

    private WireMockClient wireMockClient;

    private String sappiUrlPath;
    private String baseUrl;
    private String proxyBaseUrl;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        baseUrl = config.getString("wireMock.baseUrl");
        proxyBaseUrl = config.getString("sappi.proxyBaseUrl");
        sappiUrlPath = config.getString("sappi.urlPath");

        wireMockClient = new WireMockClient(baseUrl, proxyBaseUrl);
        wireMockClient.enableProxy(sappiUrlPath, HttpMethod.POST);
    }

    @After
    public void tearDown() throws Exception {
        //  wireMockClient.resetLoggedRequests();
    }

    @Given("execute call to wiremock with (\\w+) and (\\w+) to receive sappi booking with (\\S+) and save response in (\\S+)")
    public void getBookingToSappi(int retries, long pollIntervalMs, String resId, String pathFolder) throws
            UnirestException, InterruptedException {
        Optional<JsonNode> bookingToSappi = null;
        for (int i = 0; i < retries; i++) {
            List<JsonNode> requests = wireMockClient.findRequestsByUrlPath(config.getString("sappi.urlPath"), HttpMethod.POST);
            bookingToSappi = requests.stream()
                    .map(r -> toJson(r.path("body").toString()))
                    .filter(b -> b.asText().contains(resId))
                    .findFirst();
            if (bookingToSappi.isPresent()) {
                try (FileWriter file = new FileWriter(PATH_RESOURCES + pathFolder)) {
                    file.write(bookingToSappi.get().asText());
                    log.info("booking from Wiremock :" + bookingToSappi.get().asText());
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Thread.sleep(pollIntervalMs);
        }
        Assert.assertTrue("booking was not found in wiremock :", bookingToSappi.isPresent());
    }


    private JsonNode toJson(String jsonAsString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(jsonAsString);
        } catch (IOException ioe) {
            return null;
        }
    }
}