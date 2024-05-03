package com.hivemq.extensions.denywildcard;

import com.hivemq.extension.sdk.api.auth.parameter.SubscriptionAuthorizerInput;
import com.hivemq.extension.sdk.api.auth.parameter.SubscriptionAuthorizerOutput;
import com.hivemq.extension.sdk.api.client.parameter.ClientInformation;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;
import com.hivemq.extension.sdk.api.packets.subscribe.Subscription;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Test for specific topic filter behaviour - wildcard tests in DenyWildcardAuthorizerTest
public class DenyWildcardAuthorizerFiltersTest
{
    private @NotNull SubscriptionAuthorizerInput input;
    private @NotNull SubscriptionAuthorizerOutput output;

    @BeforeEach
    void setUp() {
        input = mock(SubscriptionAuthorizerInput.class);
        output = mock(SubscriptionAuthorizerOutput.class);
        final ClientInformation clientInformation = mock(ClientInformation.class);
        final Subscription subscription = mock(Subscription.class);
        when(input.getClientInformation()).thenReturn(clientInformation);
        when(clientInformation.getClientId()).thenReturn("client");
        when(input.getSubscription()).thenReturn(subscription);
    }

    @Test
    void test_subscriptionWhitelist_Overrides_WildcardRules() {
        // Arrange
        when(input.getSubscription().getTopicFilter()).thenReturn("#");
        final TopicFilters filter = new TopicFilters();
        filter.topicsWhitelist = new String[] { "#" };

        // Act
        new DenyWildcardAuthorizer(filter).authorizeSubscribe(input, output);

        // Assert
        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_subscriptionWhitelist_Overrides_Blacklist() {
        // Arrange
        when(input.getSubscription().getTopicFilter()).thenReturn("+/test");
        final TopicFilters filter = new TopicFilters();
        filter.topicsWhitelist = new String[] { "+/test" };
        filter.topicBeginsBlacklist = new String[] { "+" };

        // Act
        new DenyWildcardAuthorizer(filter).authorizeSubscribe(input, output);

        // Assert
        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_regexWhitelist_overrides_BlacklistRules()
    {
        // Arrange
        when(input.getSubscription().getTopicFilter()).thenReturn("/+/anystring/test/#");
        final TopicFilters filter = new TopicFilters();
        filter.topicRegexWhitelist = new String[] { "/\\+/[^/]+/test/#" };
        filter.topicBeginsBlacklist = new String[] { "/+", "+" };

        // Act
        new DenyWildcardAuthorizer(filter).authorizeSubscribe(input, output);

        // Assert
        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_topicBeginsBlacklist()
    {
        // Arrange
        when(input.getSubscription().getTopicFilter()).thenReturn("/test/+/123");
        final TopicFilters filter = new TopicFilters();
        filter.topicBeginsBlacklist = new String[] { "/test/+", "+" };

        // Act
        new DenyWildcardAuthorizer(filter).authorizeSubscribe(input, output);

        // Assert
        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }
}
