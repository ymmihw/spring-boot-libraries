package com.ymmihw.spring.boot.rsocket.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;

@Configuration
public class ClientConfiguration {

  @Bean
  RSocketRequester rSocketRequester(RSocketStrategies rSocketStrategies) {
    return RSocketRequester.builder()
        .rsocketConnector(factory -> factory.dataMimeType(MimeTypeUtils.ALL_VALUE)
            .payloadDecoder(PayloadDecoder.ZERO_COPY))
        .rsocketStrategies(rSocketStrategies).transport(TcpClientTransport.create(7000));
  }
}
