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

import com.google.gson.Gson;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;

public class TopicFilterFactory
{
    private static final @NotNull Logger LOG = LoggerFactory.getLogger(TopicFilterFactory.class);
    private static final @NotNull String DEFAULT_FILEPATH = "/opt/hivemq/conf/filter.json";

    public static TopicFilters BuildConfigFromFile()
    {
        return BuildConfigFromFile(DEFAULT_FILEPATH);
    }

    public static TopicFilters BuildConfigFromFile(final String filepath)
    {
        try
        {
            final BufferedReader reader = new BufferedReader(new FileReader(filepath));
            final String json = BuildStringFromBufferedReader(reader);
            LOG.info("Found topic filter configuration file at: {}", filepath);
            return BuildConfigFromFile(json);
        }
        catch(final Exception exception)
        {
            LOG.warn("Could not load filter configuration file: {}", exception.getMessage());
            return new TopicFilters();
        }
    }

    public static TopicFilters BuildConfigFromString(final String json)
    {
        try
        {
            final TopicFilters filter = new Gson().fromJson(json, TopicFilters.class);
            LOG.info("Topic filter configuration: {}", json);
            return filter;
        }
        catch(final Exception exception)
        {
            LOG.warn("Could not create topic filters from String: {}", exception.getMessage());
            return new TopicFilters();
        }
    }

    private static String BuildStringFromBufferedReader(final BufferedReader reader)
    {
        try
        {
            final StringBuilder stringBuilder = new StringBuilder();
            String line;
            final String ls = System.lineSeparator();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            // delete the last new line separator
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();
            return stringBuilder.toString();
        }
        catch(final Exception exception)
        {
            LOG.warn("Could not create topic filters from BufferedReader: {}", exception.getMessage());
            return "";
        }
    }
}
