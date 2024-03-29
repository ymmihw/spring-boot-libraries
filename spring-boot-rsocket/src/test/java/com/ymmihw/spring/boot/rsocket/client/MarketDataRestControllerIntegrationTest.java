package com.ymmihw.spring.boot.rsocket.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.RequestSpec;
import org.springframework.messaging.rsocket.RSocketRequester.RetrieveSpec;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.ymmihw.spring.boot.rsocket.model.MarketData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WebFluxTest(value = MarketDataRestController.class)
public class MarketDataRestControllerIntegrationTest {

  @Autowired
  private WebTestClient testClient;

  @MockBean
  private RSocketRequester rSocketRequester;

  @Mock
  private RequestSpec requestSpec;

  @Mock
  private RetrieveSpec retrieveSpec;

  @Test
  public void whenInitiatesRequest_ThenGetsResponse() throws Exception {
    when(rSocketRequester.route("currentMarketData")).thenReturn(requestSpec);
    when(requestSpec.data(any())).thenReturn(retrieveSpec);
    MarketData marketData = new MarketData("X", 1);
    when(retrieveSpec.retrieveMono(MarketData.class)).thenReturn(Mono.just(marketData));

    testClient.get().uri("/current/{stock}", "X").exchange().expectStatus().isOk()
        .expectBody(MarketData.class).isEqualTo(marketData);
  }

  @Test
  public void whenInitiatesFireAndForget_ThenGetsNoResponse() throws Exception {
    when(rSocketRequester.route("collectMarketData")).thenReturn(requestSpec);
    when(requestSpec.data(any())).thenReturn(retrieveSpec);
    when(retrieveSpec.send()).thenReturn(Mono.empty());

    testClient.get().uri("/collect").exchange().expectStatus().isOk().expectBody(Void.class);
  }

  @Test
  public void whenInitiatesRequest_ThenGetsStream() throws Exception {
    when(rSocketRequester.route("feedMarketData")).thenReturn(requestSpec);
    when(requestSpec.data(any())).thenReturn(retrieveSpec);
    MarketData firstMarketData = new MarketData("X", 1);
    MarketData secondMarketData = new MarketData("X", 2);
    when(retrieveSpec.retrieveFlux(MarketData.class))
        .thenReturn(Flux.just(firstMarketData, secondMarketData));

    FluxExchangeResult<MarketData> result = testClient.get().uri("/feed/{stock}", "X")
        .accept(TEXT_EVENT_STREAM).exchange().expectStatus().isOk().returnResult(MarketData.class);
    Flux<MarketData> marketDataFlux = result.getResponseBody();
    StepVerifier.create(marketDataFlux).expectNext(firstMarketData).expectNext(secondMarketData)
        .thenCancel().verify();
  }
}
