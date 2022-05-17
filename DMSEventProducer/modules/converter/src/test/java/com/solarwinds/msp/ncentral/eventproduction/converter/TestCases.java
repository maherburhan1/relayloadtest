package com.solarwinds.msp.ncentral.eventproduction.converter;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TestCases<InputType, ExpectedResultType> {

    private List<Tuple3<InputType, ExpectedResultType, String>> testCases;

    private TestCases(List<Tuple3<InputType, ExpectedResultType, String>> testCases) {
        this.testCases = testCases;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public List<Tuple3<InputType, ExpectedResultType, String>> getTestCases() {
        return testCases;
    }

    public Stream<Arguments> toArguments() {
        return getTestCases().stream()
                .map(tc -> Arguments.of(tc.v1(), tc.v2(), tc.v3()));
    }

    public static class Builder<InputType, ExpectedResultType> {

        private final List<Tuple3<InputType, ExpectedResultType, String>> testCases = new ArrayList<>();

        private Builder() {
        }

        public Builder<InputType, ExpectedResultType> addTestCase(InputType input, ExpectedResultType expectedResult,
                String description) {
            testCases.add(Tuple.tuple(input, expectedResult, description));
            return this;
        }

        public TestCases<InputType, ExpectedResultType> build() {
            return new TestCases<>(Collections.unmodifiableList(testCases));
        }
    }
}