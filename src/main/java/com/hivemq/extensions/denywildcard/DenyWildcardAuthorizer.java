/*
 * Copyright 2019-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.extensions.denywildcard;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.auth.SubscriptionAuthorizer;
import com.hivemq.extension.sdk.api.auth.parameter.SubscriptionAuthorizerInput;
import com.hivemq.extension.sdk.api.auth.parameter.SubscriptionAuthorizerOutput;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DenyWildcard-Extension is an extension which denies a wildcard subscription
 * on top level for any Client. That means that you are not allowed to
 * subscribe for "#" only. Any sub level wildcard subscription like "/house/#"
 * is not affected and still possible.
 * Your Client disconnects after subscription on top level wildcard.
 *
 * @author Florian Limpoeck
 * @author Lukas Brandl
 */
class DenyWildcardAuthorizer implements SubscriptionAuthorizer {
    static final @NotNull DenyWildcardAuthorizer INSTANCE = new DenyWildcardAuthorizer(TopicFilterFactory.BuildConfigFromFile());
    static final @NotNull String REASON_STRING = "Root wildcard subscriptions are not supported.";
    private final @NotNull TopicFilters TOPIC_FILTERS;

    private static final @NotNull Pattern SHARED_SUBSCRIPTION_PATTERN = Pattern.compile("\\$share(/.*?/(.*))");
    private static final @NotNull Logger LOG = LoggerFactory.getLogger(DenyWildcardAuthorizer.class);
    private static final @NotNull String WILDCARD_CHARS = "#/+";
    private static final @NotNull String[] MQTT_PREFIXES = { "$share/", "$expired/", "$dropped/" };

    public DenyWildcardAuthorizer(final TopicFilters filters) {
        TOPIC_FILTERS = filters;
    }

    public DenyWildcardAuthorizer() {
        TOPIC_FILTERS = new TopicFilters();
    }

    @Override
    public void authorizeSubscribe(
            final @NotNull SubscriptionAuthorizerInput subscriptionAuthorizerInput,
            final @NotNull SubscriptionAuthorizerOutput subscriptionAuthorizerOutput)
    {
        String topicFilter = subscriptionAuthorizerInput.getSubscription().getTopicFilter();
        topicFilter = removeMqttPrefixes(topicFilter);

        if (isInvalidTopic(topicFilter)) {
            LOG.debug("Client {} tried to subscribe to a denied first level wildcard topic filter '{}'",
                    subscriptionAuthorizerInput.getClientInformation().getClientId(),
                    topicFilter);
            subscriptionAuthorizerOutput.failAuthorization(SubackReasonCode.NOT_AUTHORIZED, REASON_STRING);
        } else {
            subscriptionAuthorizerOutput.authorizeSuccessfully();
        }
    }

    private String removeMqttPrefixes(final String topicFilter)
    {
        for (final String prefix : MQTT_PREFIXES) {
            // Shares are handled differently to other MQTT prefixes
            if (topicFilter.startsWith("$share/")) {
                final Matcher matcher = SHARED_SUBSCRIPTION_PATTERN.matcher(topicFilter);
                if (matcher.matches()) {
                    return matcher.group(2);
                }
            }
            // Remove prefix
            else if (topicFilter.startsWith(prefix)) {
                return topicFilter.substring(prefix.length());
            }
        }
        return topicFilter;
    }

    private boolean isInvalidTopic(final String topic)
    {
        // Validate against whitelist
        if(Arrays.asList(TOPIC_FILTERS.topicsWhitelist).contains(topic))
        {
            return false;
        }

        // Validate against regex whitelist
        try
        {
            for (final String regexWhitelistTopic : TOPIC_FILTERS.topicRegexWhitelist)
            {
                if (topic.matches(regexWhitelistTopic))
                {
                    return false;
                }
            }
        }
        catch (final Exception e)
        {
            LOG.error("An error occurred while checking regex whitelist topics: {}", e.getMessage());
        }

        // Validate against begins with blacklist
        if (StringUtils.startsWithAny(topic, TOPIC_FILTERS.topicBeginsBlacklist))
        {
            LOG.debug("Topic {} starts with a denied topic", topic);
            return true;
        }

        // Validate doesn't only contain wildcard characters
        if (StringUtils.containsOnly(topic, WILDCARD_CHARS))
        {
            LOG.debug("Topic {} starts with a denied topic", topic);
            return true;
        }

        return false;
    }
}
