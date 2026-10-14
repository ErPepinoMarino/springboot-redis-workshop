package com.redisworkshop.sessions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SessionTtlTest {

    @Autowired
    SessionService sessions;

    @Test
    void elTtlReflejaLaSesion() {
        String token = sessions.create("user", "USER");

        assertThat(sessions.ttlMillis(token)).isGreaterThan(0);

        sessions.delete(token);

        assertThat(sessions.ttlMillis(token)).isEqualTo(-2);
    }
}