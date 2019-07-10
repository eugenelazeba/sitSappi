package bdd;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {"bdd.steps"},
        features = {"classpath:scenarios/"},
        format = {"pretty", "html:target/html/"},
        strict = true
)
@Slf4j
public class TestRunner {

    public TestRunner() {
        log.info("Starting test");
    }
}


