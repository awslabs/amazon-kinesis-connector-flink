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

package software.amazon.kinesis.connectors.flink.internals;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.runtime.metrics.groups.GenericMetricGroup;
import org.apache.flink.runtime.metrics.groups.OperatorMetricGroup;
import org.apache.flink.runtime.metrics.groups.UnregisteredMetricGroups;

import com.amazonaws.services.kinesis.model.HashKeyRange;
import com.amazonaws.services.kinesis.model.Shard;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;
import software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisher;
import software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisherFactory;
import software.amazon.kinesis.connectors.flink.metrics.KinesisConsumerMetricConstants;
import software.amazon.kinesis.connectors.flink.metrics.ShardConsumerMetricsReporter;
import software.amazon.kinesis.connectors.flink.model.KinesisStreamShardState;
import software.amazon.kinesis.connectors.flink.model.SequenceNumber;
import software.amazon.kinesis.connectors.flink.model.StartingPosition;
import software.amazon.kinesis.connectors.flink.model.StreamShardHandle;
import software.amazon.kinesis.connectors.flink.proxy.KinesisProxyInterface;
import software.amazon.kinesis.connectors.flink.proxy.KinesisProxyV2Interface;
import software.amazon.kinesis.connectors.flink.serialization.KinesisDeserializationSchemaWrapper;
import software.amazon.kinesis.connectors.flink.testutils.KinesisShardIdGenerator;
import software.amazon.kinesis.connectors.flink.testutils.TestSourceContext;
import software.amazon.kinesis.connectors.flink.testutils.TestableKinesisDataFetcher;
import software.amazon.kinesis.connectors.flink.util.AWSUtil;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static software.amazon.kinesis.connectors.flink.model.SentinelSequenceNumber.SENTINEL_SHARD_ENDING_SEQUENCE_NUM;

/**
 * Tests for the {@link ShardConsumer}.
 */
public class ShardConsumerTestUtils {

	public static ShardConsumerMetricsReporter assertNumberOfMessagesReceivedFromKinesis(
			final int expectedNumberOfMessages,
			final RecordPublisherFactory recordPublisherFactory,
			final SequenceNumber startingSequenceNumber,
			final Properties consumerProperties) throws InterruptedException {
		return assertNumberOfMessagesReceivedFromKinesis(
				expectedNumberOfMessages,
				recordPublisherFactory,
				startingSequenceNumber,
				consumerProperties,
				SENTINEL_SHARD_ENDING_SEQUENCE_NUM.get());
	}

	public static ShardConsumerMetricsReporter assertNumberOfMessagesReceivedFromKinesis(
			final int expectedNumberOfMessages,
			final RecordPublisherFactory recordPublisherFactory,
			final SequenceNumber startingSequenceNumber,
			final Properties consumerProperties,
			final GenericMetricGroup metricGroup) throws InterruptedException {
		return assertNumberOfMessagesReceivedFromKinesis(
				expectedNumberOfMessages,
				recordPublisherFactory,
				startingSequenceNumber,
				consumerProperties,
				SENTINEL_SHARD_ENDING_SEQUENCE_NUM.get(),
				metricGroup);
	}

	public static ShardConsumerMetricsReporter assertNumberOfMessagesReceivedFromKinesis(
				final int expectedNumberOfMessages,
				final RecordPublisherFactory recordPublisherFactory,
				final SequenceNumber startingSequenceNumber,
				final Properties consumerProperties,
				final SequenceNumber expectedLastProcessedSequenceNum,
				final GenericMetricGroup metricGroup) throws InterruptedException {
		ShardConsumerMetricsReporter shardMetricsReporter = new ShardConsumerMetricsReporter(metricGroup);
		StreamShardHandle fakeToBeConsumedShard = getMockStreamShard("fakeStream", 0);

		LinkedList<KinesisStreamShardState> subscribedShardsStateUnderTest = new LinkedList<>();
		subscribedShardsStateUnderTest.add(
			new KinesisStreamShardState(KinesisDataFetcher.convertToStreamShardMetadata(fakeToBeConsumedShard),
				fakeToBeConsumedShard, startingSequenceNumber));

		TestSourceContext<String> sourceContext = new TestSourceContext<>();

		KinesisDeserializationSchemaWrapper<String> deserializationSchema = new KinesisDeserializationSchemaWrapper<>(
			new SimpleStringSchema());
		TestableKinesisDataFetcher<String> fetcher =
			new TestableKinesisDataFetcher<>(
				Collections.singletonList("fakeStream"),
				sourceContext,
				consumerProperties,
				deserializationSchema,
				10,
				2,
				new AtomicReference<>(),
				subscribedShardsStateUnderTest,
				KinesisDataFetcher.createInitialSubscribedStreamsToLastDiscoveredShardsState(Collections.singletonList("fakeStream")),
				Mockito.mock(KinesisProxyInterface.class),
				Mockito.mock(KinesisProxyV2Interface.class));

		final StreamShardHandle shardHandle = subscribedShardsStateUnderTest.get(0).getStreamShardHandle();
		final SequenceNumber lastProcessedSequenceNum = subscribedShardsStateUnderTest.get(0).getLastProcessedSequenceNum();
		final StartingPosition startingPosition = AWSUtil.getStartingPosition(lastProcessedSequenceNum, consumerProperties);

		final RecordPublisher recordPublisher = recordPublisherFactory
			.create(startingPosition, fetcher.getConsumerConfiguration(), metricGroup, shardHandle);

		int shardIndex = fetcher.registerNewSubscribedShardState(subscribedShardsStateUnderTest.get(0));
		new ShardConsumer<>(
			fetcher,
			recordPublisher,
			shardIndex,
			shardHandle,
			lastProcessedSequenceNum,
			shardMetricsReporter,
			deserializationSchema)
			.run();

		assertEquals(expectedNumberOfMessages, sourceContext.getCollectedOutputs().size());
		assertEquals(expectedLastProcessedSequenceNum, subscribedShardsStateUnderTest.get(0).getLastProcessedSequenceNum());

		return shardMetricsReporter;
	}

	public static ShardConsumerMetricsReporter assertNumberOfMessagesReceivedFromKinesis(
			final int expectedNumberOfMessages,
			final RecordPublisherFactory recordPublisherFactory,
			final SequenceNumber startingSequenceNumber,
			final Properties consumerProperties,
			final SequenceNumber expectedLastProcessedSequenceNum) throws InterruptedException {
		return assertNumberOfMessagesReceivedFromKinesis(
				expectedNumberOfMessages,
				recordPublisherFactory,
				startingSequenceNumber,
				consumerProperties,
				expectedLastProcessedSequenceNum,
				createFakeShardConsumerMetricGroup());
	}

	public static StreamShardHandle getMockStreamShard(String streamName, int shardId) {
		return new StreamShardHandle(
			streamName,
			new Shard()
				.withShardId(KinesisShardIdGenerator.generateFromShardOrder(shardId))
				.withHashKeyRange(
					new HashKeyRange()
						.withStartingHashKey("0")
						.withEndingHashKey(new BigInteger(StringUtils.repeat("FF", 16), 16).toString())));
	}

	public static SequenceNumber fakeSequenceNumber() {
		return new SequenceNumber("fakeStartingState");
	}

	public static GenericMetricGroup createFakeShardConsumerMetricGroup(OperatorMetricGroup metricGroup) {
		return (GenericMetricGroup) metricGroup
				.addGroup(KinesisConsumerMetricConstants.STREAM_METRICS_GROUP, "fakeStream")
				.addGroup(KinesisConsumerMetricConstants.SHARD_METRICS_GROUP, "shardId-000000000000");
	}

	public static GenericMetricGroup createFakeShardConsumerMetricGroup() {
		return createFakeShardConsumerMetricGroup(UnregisteredMetricGroups
				.createUnregisteredOperatorMetricGroup());
	}
}
