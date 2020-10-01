/*
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

import org.junit.Test;
import software.amazon.awssdk.services.kinesis.model.StartingPosition;
import software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants;
import software.amazon.kinesis.connectors.flink.internals.publisher.fanout.FanOutRecordPublisherFactory;
import software.amazon.kinesis.connectors.flink.metrics.ShardConsumerMetricsReporter;
import software.amazon.kinesis.connectors.flink.model.SentinelSequenceNumber;
import software.amazon.kinesis.connectors.flink.model.SequenceNumber;
import software.amazon.kinesis.connectors.flink.proxy.KinesisProxyV2Interface;
import software.amazon.kinesis.connectors.flink.testutils.FakeKinesisFanOutBehavioursFactory;
import software.amazon.kinesis.connectors.flink.testutils.TestUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static software.amazon.awssdk.services.kinesis.model.ShardIteratorType.AFTER_SEQUENCE_NUMBER;
import static software.amazon.awssdk.services.kinesis.model.ShardIteratorType.AT_SEQUENCE_NUMBER;
import static software.amazon.awssdk.services.kinesis.model.ShardIteratorType.AT_TIMESTAMP;

/**
 * Tests for the {@link ShardConsumer} using Fan Out consumption mocked Kinesis behaviours.
 */
public class ShardConsumerFanOutTest {

	@Test
	public void testEmptyShard() throws Exception {
		FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.emptyShard();

		assertNumberOfMessagesReceivedFromKinesis(0, kinesis, ShardConsumerTestUtils.fakeSequenceNumber());

		assertEquals(1, kinesis.getNumberOfSubscribeToShardInvocations());
	}

	@Test
	public void testStartFromLatestIsTranslatedToTimestamp() throws Exception {
		Instant now = Instant.now();
		FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.boundedShard().build();
		SequenceNumber sequenceNumber = SentinelSequenceNumber.SENTINEL_LATEST_SEQUENCE_NUM.get();

		// Fake behaviour defaults to 10 messages
		assertNumberOfMessagesReceivedFromKinesis(10, kinesis, sequenceNumber, TestUtils.efoProperties());

		StartingPosition actual = kinesis.getStartingPositionForSubscription(0);
		assertEquals(AT_TIMESTAMP, actual.type());
		assertTrue(now.equals(actual.timestamp()) || now.isBefore(actual.timestamp()));
	}

	@Test
	public void testStartFromLatestReceivesNoRecordsContinuesToUseTimestamp() throws Exception {
		FakeKinesisFanOutBehavioursFactory.AbstractSingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.emptyBatchFollowedBySingleRecord();

		SequenceNumber sequenceNumber = SentinelSequenceNumber.SENTINEL_LATEST_SEQUENCE_NUM.get();

		// Fake behaviour defaults to 10 messages
		assertNumberOfMessagesReceivedFromKinesis(1, kinesis, sequenceNumber, TestUtils.efoProperties());

		// This fake Kinesis will give 2 subscriptions
		assertEquals(2, kinesis.getNumberOfSubscribeToShardInvocations());

		assertEquals(AT_TIMESTAMP, kinesis.getStartingPositionForSubscription(0).type());
		assertEquals(AT_TIMESTAMP, kinesis.getStartingPositionForSubscription(1).type());
	}

	@Test
	public void testBoundedShardConsumesFromTimestamp() throws Exception {
		String format = "yyyy-MM-dd'T'HH:mm";
		String timestamp = "2020-07-02T09:14";
		Instant expectedTimestamp = new SimpleDateFormat(format).parse(timestamp).toInstant();

		FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.boundedShard().build();

		Properties consumerConfig = TestUtils.efoProperties();
		consumerConfig.setProperty(ConsumerConfigConstants.STREAM_INITIAL_TIMESTAMP, timestamp);
		consumerConfig.setProperty(ConsumerConfigConstants.STREAM_TIMESTAMP_DATE_FORMAT, format);
		SequenceNumber sequenceNumber = SentinelSequenceNumber.SENTINEL_AT_TIMESTAMP_SEQUENCE_NUM.get();

		// Fake behaviour defaults to 10 messages
		assertNumberOfMessagesReceivedFromKinesis(10, kinesis, sequenceNumber, consumerConfig);

		StartingPosition actual = kinesis.getStartingPositionForSubscription(0);
		assertEquals(AT_TIMESTAMP, actual.type());
		assertEquals(expectedTimestamp, actual.timestamp());
	}

	@Test
	public void testMillisBehindReported() throws Exception {
		FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory
			.boundedShard()
			.withMillisBehindLatest(123L)
			.build();

		// Fake behaviour defaults to 10 messages
		ShardConsumerMetricsReporter metrics = assertNumberOfMessagesReceivedFromKinesis(10, kinesis, ShardConsumerTestUtils.fakeSequenceNumber());

		assertEquals(123L, metrics.getMillisBehindLatest());
	}

	@Test
	public void testBoundedShardConsumesCorrectNumberOfMessages() throws Exception {
		FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory
			.boundedShard()
			.withBatchCount(10)
			.withRecordsPerBatch(5)
			.build();

		// 10 batches of 5 records = 50
		assertNumberOfMessagesReceivedFromKinesis(50, kinesis, ShardConsumerTestUtils.fakeSequenceNumber());

		assertEquals(1, kinesis.getNumberOfSubscribeToShardInvocations());
	}

	@Test
	public void testBoundedShardResubscribesToShard() throws Exception {
		FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory
			.boundedShard()
			.withBatchCount(100)
			.withRecordsPerBatch(10)
			.withBatchesPerSubscription(5)
			.build();

		// 100 batches of 10 records = 1000
		assertNumberOfMessagesReceivedFromKinesis(1000, kinesis, ShardConsumerTestUtils.fakeSequenceNumber());

		// 100 batches / 5 batches per subscription = 20 subscriptions
		assertEquals(20, kinesis.getNumberOfSubscribeToShardInvocations());

		// Starting from non-aggregated sequence number means we should start AFTER the sequence number
		assertEquals(AFTER_SEQUENCE_NUMBER, kinesis.getStartingPositionForSubscription(0).type());
	}

	@Test
	public void testBoundedShardWithAggregatedRecords() throws Exception {
		FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory
			.boundedShard()
			.withBatchCount(100)
			.withRecordsPerBatch(10)
			.withAggregationFactor(100)
			.build();

		// 100 batches of 10 records * 100 aggregation factor = 100000
		assertNumberOfMessagesReceivedFromKinesis(100000, kinesis, ShardConsumerTestUtils.fakeSequenceNumber());
	}

	@Test
	public void testBoundedShardResumingConsumptionFromAggregatedSubsequenceNumber() throws Exception {
		FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory
			.boundedShard()
			.withBatchCount(10)
			.withRecordsPerBatch(1)
			.withAggregationFactor(10)
			.build();

		SequenceNumber subsequenceNumber = new SequenceNumber("1", 5);

		// 10 batches of 1 record * 10 aggregation factor - 6 previously consumed subsequence records (0,1,2,3,4,5) = 94
		assertNumberOfMessagesReceivedFromKinesis(94, kinesis, subsequenceNumber);

		// Starting from aggregated sequence number means we should start AT the sequence number
		assertEquals(AT_SEQUENCE_NUMBER, kinesis.getStartingPositionForSubscription(0).type());
	}

	@Test
	public void testSubscribeToShardUsesCorrectStartingSequenceNumbers() throws Exception {
		FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory
			.boundedShard()
			.withBatchCount(10)
			.withRecordsPerBatch(1)
			.withBatchesPerSubscription(2)
			.build();

		// 10 batches of 1 records = 10
		assertNumberOfMessagesReceivedFromKinesis(10, kinesis, new SequenceNumber("0"));

		// 10 batches / 2 batches per subscription = 5 subscriptions
		assertEquals(5, kinesis.getNumberOfSubscribeToShardInvocations());

		// Starting positions should correlate to the last consumed sequence number
		assertStartingPositionAfterSequenceNumber(kinesis.getStartingPositionForSubscription(0), "0");
		assertStartingPositionAfterSequenceNumber(kinesis.getStartingPositionForSubscription(1), "2");
		assertStartingPositionAfterSequenceNumber(kinesis.getStartingPositionForSubscription(2), "4");
		assertStartingPositionAfterSequenceNumber(kinesis.getStartingPositionForSubscription(3), "6");
		assertStartingPositionAfterSequenceNumber(kinesis.getStartingPositionForSubscription(4), "8");
	}

	private void assertStartingPositionAfterSequenceNumber(
			final StartingPosition startingPosition,
			final String sequenceNumber) {
		assertEquals(AFTER_SEQUENCE_NUMBER, startingPosition.type());
		assertEquals(sequenceNumber, startingPosition.sequenceNumber());
	}

	private ShardConsumerMetricsReporter assertNumberOfMessagesReceivedFromKinesis(
				final int expectedNumberOfMessages,
				final KinesisProxyV2Interface kinesis,
				final SequenceNumber startingSequenceNumber) throws Exception {
		return assertNumberOfMessagesReceivedFromKinesis(
			expectedNumberOfMessages,
			kinesis,
			startingSequenceNumber,
			TestUtils.efoProperties());
	}

	private ShardConsumerMetricsReporter assertNumberOfMessagesReceivedFromKinesis(
			final int expectedNumberOfMessages,
			final KinesisProxyV2Interface kinesis,
			final SequenceNumber startingSequenceNumber,
			final Properties consumerConfig) throws Exception {
		return ShardConsumerTestUtils.assertNumberOfMessagesReceivedFromKinesis(
			expectedNumberOfMessages,
			new FanOutRecordPublisherFactory(kinesis),
			startingSequenceNumber,
			consumerConfig);
	}

}
