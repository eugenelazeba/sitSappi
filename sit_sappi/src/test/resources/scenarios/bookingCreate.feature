Feature: Atcomres -> SAPPI

  Scenario Outline:
    Given  execute call to wiremock with <RETRIES> and <POLL_INTERVAL_MS> to receive sappi booking with <RES_ID> and save response in <BOOKING_PATH>
    Then compare <ATCOMRES_NODE> response with <SAPPI_NODE> request

    Examples:
      | RETRIES | POLL_INTERVAL_MS | RES_ID   | BOOKING_PATH          | ATCOMRES_NODE | SAPPI_NODE   |
      | 6       | 15000            | 92021582 | sappi/requestData.xml | Data_Hub      | ns3:Data_Hub |