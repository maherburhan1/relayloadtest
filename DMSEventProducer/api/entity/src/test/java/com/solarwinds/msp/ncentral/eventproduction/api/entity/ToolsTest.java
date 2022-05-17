package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolsTest {

    @Mock
    private Event eventMock;

    @Test
    void setNullableField() {
        Map<String, Object> entityValues = new HashMap<>();
        entityValues.put("setBizappsCustomerId", null);

        MspContextOuterClass.MspContext expectedMspContext = createMspContext();
        MspContextOuterClass.MspContext.Builder mspEventBusEntityBuilder =
                MspContextOuterClass.MspContext.newBuilder().mergeFrom(expectedMspContext);
        Tools.setNullableField(mspEventBusEntityBuilder, entityValues);
        MspContextOuterClass.MspContext mspEventBusEntity = mspEventBusEntityBuilder.build();
        Assertions.assertEquals(expectedMspContext, mspEventBusEntity, "Set null to protobuf entity");
    }

    private static MspContextOuterClass.MspContext createMspContext() {
        return MspContextOuterClass.MspContext.newBuilder()
                .setBizAppsCustomerId("BizappsCustomerId-1")
                .setSystemGuid("NcentralServerGuid-1")
                .build();
    }

    @Test
    void getMspContext() {
        when(eventMock.getBizappsCustomerId()).thenReturn(Optional.of("BizappsCustomerId-1"));
        when(eventMock.getNcentralServerGuid()).thenReturn("NcentralServerGuid-1");

        Assertions.assertEquals(createMspContext(), Tools.getMspContext(eventMock), "Set null to protobuf entity");
    }

    @Test
    void toTimestamp_returns_epoch_timestamp_of_zonedDateTime() {
        TimeZone originalTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.ofHoursMinutes(5, 15)));
            ZonedDateTime now = ZonedDateTime.now();
            assertThat(Tools.toTimestamp(now)).isEqualTo(convertToEpochTimestamp(now));
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }

    private static Timestamp convertToEpochTimestamp(ZonedDateTime zonedDateTime) {
        return Timestamp.newBuilder()
                .setSeconds(zonedDateTime.toEpochSecond())
                .setNanos(zonedDateTime.getNano())
                .build();
    }

    @ParameterizedTest(name = "Run {index}: String value={0}")
    @MethodSource("getBoolean_Parameters")
    void getBoolean(Map<String, String> input, String attributeName, Boolean expectedResult) {
        Assertions.assertEquals(expectedResult, Tools.getBoolean(input, attributeName));
    }

    private static Stream<Arguments> getBoolean_Parameters() {
        return Stream.of(Arguments.of(Collections.singletonMap("testAttribute", null), "testAttribute", false),
                Arguments.of(Collections.singletonMap("testAttribute", ""), "testAttribute", false),
                Arguments.of(Collections.singletonMap("testAttribute", "1"), "testAttribute", true),
                Arguments.of(Collections.singletonMap("testAttribute", "1 "), "testAttribute", true),
                Arguments.of(Collections.singletonMap("testAttribute", "true"), "testAttribute", true),
                Arguments.of(Collections.singletonMap("testAttribute", "TRUE"), "testAttribute", true));
    }

    @ParameterizedTest(name = "Run {index}: String value={0}")
    @MethodSource("getLong_Parameters")
    void getLong(Map<String, String> input, String attributeName, Long expectedResult) {
        Assertions.assertEquals(expectedResult, Tools.getLong(input, attributeName));
    }

    private static Stream<Arguments> getLong_Parameters() {
        return Stream.of(Arguments.of(Collections.singletonMap("testAttribute", null), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", ""), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", "1"), "testAttribute", 1L),
                Arguments.of(Collections.singletonMap("testAttribute", "true"), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", "100000000"), "testAttribute", 100000000L),
                Arguments.of(Collections.singletonMap("testAttribute", "100000.0001"), "testAttribute", null));
    }

    @ParameterizedTest(name = "Run {index}: String value={0}")
    @MethodSource("getFloat_Parameters")
    void getFloat(Map<String, String> input, String attributeName, Float expectedResult) {
        Assertions.assertEquals(expectedResult, Tools.getFloat(input, attributeName));
    }

    private static Stream<Arguments> getFloat_Parameters() {
        return Stream.of(Arguments.of(Collections.singletonMap("testAttribute", null), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", ""), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", "1"), "testAttribute", 1F),
                Arguments.of(Collections.singletonMap("testAttribute", "true"), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", "100000000"), "testAttribute", 100000000F),
                Arguments.of(Collections.singletonMap("testAttribute", "100000.0001"), "testAttribute", 100000.0001F));
    }

    @ParameterizedTest(name = "Run {index}: String value={0}")
    @MethodSource("getInteger_Parameters")
    void getInteger(Map<String, String> input, String attributeName, Integer expectedResult) {
        Assertions.assertEquals(expectedResult, Tools.getInteger(input, attributeName));
    }

    private static Stream<Arguments> getInteger_Parameters() {
        return Stream.of(Arguments.of(Collections.singletonMap("testAttribute", null), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", ""), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", "1"), "testAttribute", 1),
                Arguments.of(Collections.singletonMap("testAttribute", "true"), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", "100000000"), "testAttribute", 100000000),
                Arguments.of(Collections.singletonMap("testAttribute", "100000.0001"), "testAttribute", null));
    }

    @ParameterizedTest(name = "Run {index}: String value={0}")
    @MethodSource("getDouble_Parameters")
    void getDouble(Map<String, String> input, String attributeName, Double expectedResult) {
        Assertions.assertEquals(expectedResult, Tools.getDouble(input, attributeName));
    }

    private static Stream<Arguments> getDouble_Parameters() {
        return Stream.of(Arguments.of(Collections.singletonMap("testAttribute", null), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", ""), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", "1"), "testAttribute", 1D),
                Arguments.of(Collections.singletonMap("testAttribute", "true"), "testAttribute", null),
                Arguments.of(Collections.singletonMap("testAttribute", "100000000"), "testAttribute", 100000000D),
                Arguments.of(Collections.singletonMap("testAttribute", "100000.0001"), "testAttribute", 100000.0001D));
    }
}