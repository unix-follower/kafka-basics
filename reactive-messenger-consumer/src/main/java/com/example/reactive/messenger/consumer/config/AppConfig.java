package com.example.reactive.messenger.consumer.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.Conditions;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.netty.LogbookServerHandler;

@Configuration
class AppConfig {
  @Bean
  ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
        .configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, true)
        .setVisibility(VisibilityChecker.Std.allPublicInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
  }

  @Bean
  Logbook logbook() {
    return Logbook.builder()
        .condition(
            Conditions.exclude(
                Conditions.requestTo("/actuator/**")
            )
        )
        .sink(
            new DefaultSink(
                new DefaultHttpLogFormatter(),
                new DefaultHttpLogWriter()
            )
        )
        .build();
  }

  @Bean
  NettyServerCustomizer nettyServerCustomizer(Logbook logbook) {
    return server -> server.doOnConnection(connection ->
        connection.addHandlerLast(new LogbookServerHandler(logbook))
    );
  }
}
