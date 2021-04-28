package com.ymmihw.spring.boot.rsocket.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.MimeTypeUtils;
import com.ymmihw.spring.boot.rsocket.model.MarketData;
import com.ymmihw.spring.boot.rsocket.model.MarketDataRequest;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;

@SpringBootTest
@TestPropertySource(properties = {"spring.rsocket.server.port=7000"})
public class MarketDataRSocketControllerLiveTest {

  @Autowired
  private RSocketRequester rSocketRequester;

  @SpyBean
  private MarketDataRSocketController rSocketController;

  @Test
  public void whenGetsFireAndForget_ThenReturnsNoResponse() throws InterruptedException {
    final MarketData marketData = new MarketData("X", 1);
    rSocketRequester.route("collectMarketData").data(marketData).send()
        .block(Duration.ofSeconds(10));

    sleepForProcessing();
    verify(rSocketController).collectMarketData(any());
  }

  @Test
  public void whenGetsRequest_ThenReturnsResponse() throws InterruptedException {
    final MarketDataRequest marketDataRequest = new MarketDataRequest("X");
    rSocketRequester.route("currentMarketData").data(marketDataRequest)
        .retrieveMono(MarketData.class).subscribe();
    sleepForProcessing();
    verify(rSocketController).currentMarketData(any());
  }

  @Test
  public void whenGetsRequest_ThenReturnsStream() throws InterruptedException {
    final MarketDataRequest marketDataRequest = new MarketDataRequest("X");
    rSocketRequester.route("feedMarketData").data(marketDataRequest).retrieveFlux(MarketData.class)
        .subscribe();
    sleepForProcessing();
    verify(rSocketController).feedMarketData(any());
  }

  private void sleepForProcessing() throws InterruptedException {
    Thread.sleep(1000);
  }

  @TestConfiguration
  public static class ClientConfiguration {

    @Bean
    @Lazy
    RSocketRequester rSocketRequester(RSocketStrategies rSocketStrategies) {
      return RSocketRequester.builder()
          .rsocketConnector(factory -> factory.dataMimeType(MimeTypeUtils.ALL_VALUE)
              .payloadDecoder(PayloadDecoder.ZERO_COPY))
          .rsocketStrategies(rSocketStrategies).transport(TcpClientTransport.create(7000));
    }
  }
}
