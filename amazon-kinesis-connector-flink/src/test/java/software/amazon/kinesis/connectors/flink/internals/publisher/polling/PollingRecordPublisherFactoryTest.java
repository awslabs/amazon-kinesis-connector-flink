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

package software.amazon.kinesis.connectors.flink.internals.publisher.polling;

import org.junit.Test;
import software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisher;
import software.amazon.kinesis.connectors.flink.model.StartingPosition;
import software.amazon.kinesis.connectors.flink.model.StreamShardHandle;
import software.amazon.kinesis.connectors.flink.proxy.KinesisProxy;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.SHARD_USE_ADAPTIVE_READS;
import static software.amazon.kinesis.connectors.flink.internals.ShardConsumerTestUtils.createFakeShardConsumerMetricGroup;
import static software.amazon.kinesis.connectors.flink.model.SentinelSequenceNumber.SENTINEL_LATEST_SEQUENCE_NUM;

/**
 * Tests for {@link PollingRecordPublisherFactory}.
 */
public class PollingRecordPublisherFactoryTest {

	private final PollingRecordPublisherFactory factory = new PollingRecordPublisherFactory(props -> mock(KinesisProxy.class));

	@Test
	public void testBuildPollingRecordPublisher() throws Exception {
		RecordPublisher recordPublisher = factory.create(
			StartingPosition.restartFromSequenceNumber(SENTINEL_LATEST_SEQUENCE_NUM.get()),
			new Properties(),
			createFakeShardConsumerMetricGroup(),
			mock(StreamShardHandle.class));

		assertTrue(recordPublisher instanceof PollingRecordPublisher);
		assertFalse(recordPublisher instanceof AdaptivePollingRecordPublisher);
	}

	@Test
	public void testBuildAdaptivePollingRecordPublisher() throws Exception {
		Properties properties = new Properties();
		properties.setProperty(SHARD_USE_ADAPTIVE_READS, "true");

		RecordPublisher recordPublisher = factory.create(
			StartingPosition.restartFromSequenceNumber(SENTINEL_LATEST_SEQUENCE_NUM.get()),
			properties,
			createFakeShardConsumerMetricGroup(),
			mock(StreamShardHandle.class));

		assertTrue(recordPublisher instanceof PollingRecordPublisher);
		assertTrue(recordPublisher instanceof AdaptivePollingRecordPublisher);
	}
}
