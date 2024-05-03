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

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class TopicFilterFactoryTest {
    @Test
    public void buildConfigFromFile_returnsEmptyTopicFilters_whenEmptyJson() throws IOException
    {
        final String json = "{}";
        final TopicFilters topicFilters = TopicFilterFactory.BuildConfigFromString(json);
        assertNotNull(topicFilters);
        assertEquals(0, topicFilters.topicsWhitelist.length);
        assertEquals(0, topicFilters.topicBeginsBlacklist.length);
        assertEquals(0, topicFilters.topicRegexWhitelist.length);
    }

    @Test
    public void buildConfigFromFile_returnsTopicFilters_whenValidJson() throws IOException
    {
        final String json = "{\"topicsWhitelist\":[\"#/\",\"/#/\",\"+/\",\"/+/\"],\"topicBeginsBlacklist\":[\"/+/+/i-am-a-test/#\",\"/+/i-am-a-second-test\"],\"topicRegexWhitelist\":[\"/\\\\+/[^/]+/test/testing/#\"]}";
        final TopicFilters topicFilters = TopicFilterFactory.BuildConfigFromString(json);
        assertNotNull(topicFilters);
        assertEquals(4, topicFilters.topicsWhitelist.length);
        assertEquals(2, topicFilters.topicBeginsBlacklist.length);
        assertEquals(1, topicFilters.topicRegexWhitelist.length);
    }

    @Test
    public void buildConfigFromFile_returnsEmptyWhitelist_whenWhitelistMissing() throws IOException
    {
        final String json = "{\"topicBeginsBlacklist\":[\"/+/+/i-am-a-test/#\",\"/+/i-am-a-second-test\"],\"topicRegexWhitelist\":[\"/\\\\+/[^/]+/test/testing/#\"]}";
        final TopicFilters topicFilters = TopicFilterFactory.BuildConfigFromString(json);
        assertNotNull(topicFilters);
        assertEquals(0, topicFilters.topicsWhitelist.length);
        assertEquals(2, topicFilters.topicBeginsBlacklist.length);
        assertEquals(1, topicFilters.topicRegexWhitelist.length);
    }

    @Test
    public void buildConfigFromFile_returnsEmptyBlacklist_whenBlacklistMissing() throws IOException
    {
        final String json = "{\"topicsWhitelist\":[\"#/\",\"/#/\",\"+/\",\"/+/\"],\"topicRegexWhitelist\":[\"/\\\\+/[^/]+/test/testing/#\"]}";
        final TopicFilters topicFilters = TopicFilterFactory.BuildConfigFromString(json);
        assertNotNull(topicFilters);
        assertEquals(4, topicFilters.topicsWhitelist.length);
        assertEquals(0, topicFilters.topicBeginsBlacklist.length);
        assertEquals(1, topicFilters.topicRegexWhitelist.length);
    }

    @Test
    public void buildConfigFromFile_returnsEmptyRegex_whenRegexWhitelistMissing() throws IOException
    {
        final String json = "{\"topicsWhitelist\":[\"#/\",\"/#/\",\"+/\",\"/+/\"],\"topicBeginsBlacklist\":[\"/+/+/i-am-a-test/#\",\"/+/i-am-a-second-test\"]}";
        final TopicFilters topicFilters = TopicFilterFactory.BuildConfigFromString(json);
        assertNotNull(topicFilters);
        assertEquals(4, topicFilters.topicsWhitelist.length);
        assertEquals(2, topicFilters.topicBeginsBlacklist.length);
        assertEquals(0, topicFilters.topicRegexWhitelist.length);
    }
}
