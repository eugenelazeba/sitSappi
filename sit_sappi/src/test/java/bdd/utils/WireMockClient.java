package bdd.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.junit.Assert;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class WireMockClient {
    private String MOCK_ADMIN_BASE_URL = "/__admin";
    private String ADD_NEW_MAPPING_URL = MOCK_ADMIN_BASE_URL + "/mappings/new";
    private String RESET_MAPPING_URL = MOCK_ADMIN_BASE_URL + "/mappings/reset";
    private String FIND_REQUESTS_URL = MOCK_ADMIN_BASE_URL + "/requests/find";
    private String FIND_REQUESTS = MOCK_ADMIN_BASE_URL + "/requests";
    private String RESET_REQUESTS_URL = MOCK_ADMIN_BASE_URL + "/requests/reset";

    private String baseUrl;
    private String proxyBaseUrl;

    private ObjectMapper objectMapper;

    public WireMockClient(String baseUrl, String proxyBaseUrl) {
        this.baseUrl = baseUrl;
        this.proxyBaseUrl = proxyBaseUrl;
        this.objectMapper = new ObjectMapper();
    }

    public void enableProxy(String urlPattern, HttpMethod method) throws UnirestException {

        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return objectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return objectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        ObjectNode newMappingRequest = objectMapper.createObjectNode();

        newMappingRequest.set("request",
                objectMapper.createObjectNode()
                        .put("method", method.name())
                        .put("urlPattern", urlPattern));
        newMappingRequest.set("response",
                objectMapper.createObjectNode()
                        .put("proxyBaseUrl", proxyBaseUrl));

        String addNewUrl = baseUrl + ADD_NEW_MAPPING_URL;

        log.info("Enabling proxy on '{}' for '{}'", proxyBaseUrl, urlPattern);


        HttpResponse response = Unirest.post(addNewUrl).body(newMappingRequest).asJson();
        log.info("Response status: {} {}", response.getStatus(), response.getStatusText());

        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatus());
    }

    public List<JsonNode> findRequestsByUrlPattern(String urlPattern, HttpMethod method) throws UnirestException {
        ObjectNode findRequest = objectMapper.createObjectNode()
                .put("method", method.name())
                .put("urlPattern", urlPattern);

        String findRequestsUrl = baseUrl + FIND_REQUESTS_URL;

        log.info("Fetching requests for path '{}'", urlPattern);
        HttpResponse<com.mashape.unirest.http.JsonNode> response = Unirest.post(findRequestsUrl).body(findRequest.toString()).asJson();

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());

        JSONArray requests = response.getBody().getObject().getJSONArray("requests");
        return StreamSupport.stream(requests.spliterator(), false)
                .map(r -> toJson(r.toString()))
                .collect(Collectors.toList());
    }

    public List<JsonNode> findRequestsByUrlPath(String urlPath, HttpMethod method) throws UnirestException {
        ObjectNode findRequest = objectMapper.createObjectNode()
                .put("method", method.name())
                .put("urlPath", urlPath);

        String findRequestsUrl = baseUrl + FIND_REQUESTS_URL;

        log.info("Fetching requests for path '{}'", urlPath);
        HttpResponse<com.mashape.unirest.http.JsonNode> response = Unirest.post(findRequestsUrl).body(findRequest.toString()).asJson();

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());

        JSONArray requests = response.getBody().getObject().getJSONArray("requests");
        return StreamSupport.stream(requests.spliterator(), false)
                .map(r -> toJson(r.toString()))
                .collect(Collectors.toList());
    }

    public List<JsonNode> findAllRequests() throws UnirestException {
        String findRequestsUrl = baseUrl + FIND_REQUESTS;

        log.info("Fetching requests for path '{}'", findRequestsUrl);
        HttpResponse<com.mashape.unirest.http.JsonNode> response = Unirest.get(findRequestsUrl).asJson();

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());

        JSONArray requests = response.getBody().getObject().getJSONArray("requests");
        return StreamSupport.stream(requests.spliterator(), false)
                .map(r -> toJson(r.toString()))
                .collect(Collectors.toList());
    }


    public void resetLoggedRequests() throws UnirestException {
        String resetRequestsUrl = baseUrl + RESET_REQUESTS_URL;

        HttpResponse response = Unirest.post(resetRequestsUrl).asString();

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    public void resetMappedRequests() throws UnirestException {
        String resetRequestsUrl = baseUrl + RESET_MAPPING_URL;

        HttpResponse response = Unirest.post(resetRequestsUrl).asString();

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
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
