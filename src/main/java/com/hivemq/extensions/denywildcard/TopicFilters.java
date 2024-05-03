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

/**
 * TopicFilters is a class that holds the topic filter configuration for the DenyWildcard extension.
 */
public class TopicFilters
{
    /**
     * Whitelisted topics that ignore other restrictions in the extension
     */
    public String[] topicsWhitelist = new String[0];

    /**
     * Blacklisted topic beginnings that are not allowed to be subscribed to (e.g. "+/+/" would block "+/+/test")
     */
    public String[] topicBeginsBlacklist = new String[0];

    /**
     * Whitelisted topics containing regex expressions that ignore other restrictions in the extension
     */
    public String[] topicRegexWhitelist = new String[0];
}
