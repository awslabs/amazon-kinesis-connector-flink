/*
 * This file has been modified from the original.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.amazon.kinesis.connectors.flink.util;

import org.junit.Test;
import software.amazon.kinesis.connectors.flink.internals.publisher.fanout.StreamConsumerRegistrar;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.EFORegistrationType.EAGER;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.EFO_CONSUMER_NAME;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.EFO_REGISTRATION_TYPE;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.RECORD_PUBLISHER_TYPE;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.RecordPublisherType.EFO;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.efoConsumerArn;

/**
 * Tests for {@link StreamConsumerRegistrar}.
 */
public class StreamConsumerRegistrarUtilTest {

	@Test
	public void testRegisterStreamConsumers() throws Exception {
		Properties configProps = new Properties();
		configProps.setProperty(EFO_CONSUMER_NAME, "consumer-name");

		StreamConsumerRegistrar registrar = mock(StreamConsumerRegistrar.class);
		when(registrar.registerStreamConsumer("stream-1", "consumer-name"))
			.thenReturn("stream-1-consumer-arn");
		when(registrar.registerStreamConsumer("stream-2", "consumer-name"))
			.thenReturn("stream-2-consumer-arn");

		StreamConsumerRegistrarUtil.registerStreamConsumers(registrar, configProps, Arrays.asList("stream-1", "stream-2"));

		assertEquals("stream-1-consumer-arn", configProps.getProperty(efoConsumerArn("stream-1")));
		assertEquals("stream-2-consumer-arn", configProps.getProperty(efoConsumerArn("stream-2")));
	}

	@Test
	public void testDeregisterStreamConsumersMissingStreamArn() throws Exception {
		Properties configProps = new Properties();
		configProps.setProperty(RECORD_PUBLISHER_TYPE, EFO.name());
		configProps.setProperty(EFO_CONSUMER_NAME, "consumer-name");

		List<String> streams = Arrays.asList("stream-1", "stream-2");
		StreamConsumerRegistrar registrar = mock(StreamConsumerRegistrar.class);

		StreamConsumerRegistrarUtil.deregisterStreamConsumers(registrar, configProps, streams);

		verify(registrar).deregisterStreamConsumer("stream-1");
		verify(registrar).deregisterStreamConsumer("stream-2");
	}

	@Test
	public void testDeregisterStreamConsumersOnlyDeregistersEFOLazilyInitializedConsumers() {
		Properties configProps = getDefaultConfiguration();
		configProps.setProperty(RECORD_PUBLISHER_TYPE, EFO.name());
		configProps.put(EFO_REGISTRATION_TYPE, EAGER.name());
		List<String> streams = Arrays.asList("stream-1");
		StreamConsumerRegistrar registrar = mock(StreamConsumerRegistrar.class);

		StreamConsumerRegistrarUtil.deregisterStreamConsumers(registrar, configProps, streams);

		verifyZeroInteractions(registrar);
	}

	private Properties getDefaultConfiguration() {
		Properties configProps = new Properties();
		configProps.setProperty(EFO_CONSUMER_NAME, "consumer-name");
		return configProps;
	}
}
