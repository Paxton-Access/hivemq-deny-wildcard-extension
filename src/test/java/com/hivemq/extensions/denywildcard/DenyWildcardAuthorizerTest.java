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

/**
 * @author Florian Limpoeck
 */
class DenyWildcardAuthorizerTest
{
    private @NotNull SubscriptionAuthorizerInput input;
    private @NotNull SubscriptionAuthorizerOutput output;
    private final @NotNull DenyWildcardAuthorizer authorizer = new DenyWildcardAuthorizer();

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
    void test_denied_hashtag() {
        when(input.getSubscription().getTopicFilter()).thenReturn("#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_shared_hash() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$share/group/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_hash() {
        when(input.getSubscription().getTopicFilter()).thenReturn("/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_shared_slash_hash() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$share/group//#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_plus_hash() {
        when(input.getSubscription().getTopicFilter()).thenReturn("+/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_shared_plus_hash() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$share/group/+/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_plus_plus() {
        when(input.getSubscription().getTopicFilter()).thenReturn("+/+");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_shared_plus_plus() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$share/group/+/+");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_success() {
        when(input.getSubscription().getTopicFilter()).thenReturn("topic");
        authorizer.authorizeSubscribe(input, output);

        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_success_non_root_hashtag() {
        when(input.getSubscription().getTopicFilter()).thenReturn("topic/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_success_shared_non_root_wildcard() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$share/group/topic/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_success_non_root_plus_wildcard() {
        when(input.getSubscription().getTopicFilter()).thenReturn("+/topic/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_success_shared_non_root_plus_wildcard() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$share/group/+/topic/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_success_non_root_plus() {
        when(input.getSubscription().getTopicFilter()).thenReturn("topic/+");
        authorizer.authorizeSubscribe(input, output);

        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_success_non_trailing_plus() {
        when(input.getSubscription().getTopicFilter()).thenReturn("+/topic");
        authorizer.authorizeSubscribe(input, output);

        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_denied_expired_hash() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$expired/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_expired_plus_plus() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$expired/+/+");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_expired_success() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$expired/topic/+");
        authorizer.authorizeSubscribe(input, output);

        verify(output).authorizeSuccessfully();
    }

    @Test
    void test_denied_dropped_hash() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$dropped/#");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_dropped_plus_plus() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$dropped/+/+");
        authorizer.authorizeSubscribe(input, output);

        verify(output).failAuthorization(SubackReasonCode.NOT_AUTHORIZED, DenyWildcardAuthorizer.REASON_STRING);
    }

    @Test
    void test_denied_dropped_success() {
        when(input.getSubscription().getTopicFilter()).thenReturn("$dropped/topic/+");
        authorizer.authorizeSubscribe(input, output);

        verify(output).authorizeSuccessfully();
    }
}
