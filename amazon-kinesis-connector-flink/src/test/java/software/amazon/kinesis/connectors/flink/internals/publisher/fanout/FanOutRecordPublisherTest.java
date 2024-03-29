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

package software.amazon.kinesis.connectors.flink.internals.publisher.fanout;

import com.amazonaws.http.timers.client.SdkInterruptedException;
import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord;
import io.netty.handler.timeout.ReadTimeoutException;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.model.LimitExceededException;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.ResourceNotFoundException;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardEvent;
import software.amazon.kinesis.connectors.flink.internals.publisher.RecordBatch;
import software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisher;
import software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisher.RecordPublisherRunResult;
import software.amazon.kinesis.connectors.flink.model.SequenceNumber;
import software.amazon.kinesis.connectors.flink.model.StartingPosition;
import software.amazon.kinesis.connectors.flink.proxy.FullJitterBackoff;
import software.amazon.kinesis.connectors.flink.proxy.KinesisProxyV2Interface;
import software.amazon.kinesis.connectors.flink.testutils.FakeKinesisFanOutBehavioursFactory;
import software.amazon.kinesis.connectors.flink.testutils.FakeKinesisFanOutBehavioursFactory.SingleShardFanOutKinesisV2;
import software.amazon.kinesis.connectors.flink.testutils.FakeKinesisFanOutBehavioursFactory.SubscriptionErrorKinesisV2;
import software.amazon.kinesis.connectors.flink.testutils.TestUtils.TestConsumer;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.awssdk.services.kinesis.model.ShardIteratorType.AFTER_SEQUENCE_NUMBER;
import static software.amazon.awssdk.services.kinesis.model.ShardIteratorType.AT_SEQUENCE_NUMBER;
import static software.amazon.awssdk.services.kinesis.model.ShardIteratorType.AT_TIMESTAMP;
import static software.amazon.awssdk.services.kinesis.model.ShardIteratorType.LATEST;
import static software.amazon.awssdk.services.kinesis.model.ShardIteratorType.TRIM_HORIZON;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.EFO_CONSUMER_NAME;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.RECORD_PUBLISHER_TYPE;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.RecordPublisherType.EFO;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.SUBSCRIBE_TO_SHARD_BACKOFF_BASE;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.SUBSCRIBE_TO_SHARD_BACKOFF_EXPONENTIAL_CONSTANT;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.SUBSCRIBE_TO_SHARD_BACKOFF_MAX;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.SUBSCRIBE_TO_SHARD_RETRIES;
import static software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisher.RecordPublisherRunResult.CANCELLED;
import static software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisher.RecordPublisherRunResult.COMPLETE;
import static software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisher.RecordPublisherRunResult.INCOMPLETE;
import static software.amazon.kinesis.connectors.flink.model.SentinelSequenceNumber.SENTINEL_EARLIEST_SEQUENCE_NUM;
import static software.amazon.kinesis.connectors.flink.model.SentinelSequenceNumber.SENTINEL_LATEST_SEQUENCE_NUM;
import static software.amazon.kinesis.connectors.flink.testutils.FakeKinesisFanOutBehavioursFactory.emptyShard;
import static software.amazon.kinesis.connectors.flink.testutils.FakeKinesisFanOutBehavioursFactory.singletonShard;
import static software.amazon.kinesis.connectors.flink.testutils.TestUtils.createDummyStreamShardHandle;

/**
 * Tests for {@link FanOutRecordPublisher}.
 */
public class FanOutRecordPublisherTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static final long EXPECTED_SUBSCRIBE_TO_SHARD_MAX = 1;
	private static final long EXPECTED_SUBSCRIBE_TO_SHARD_BASE = 2;
	private static final double EXPECTED_SUBSCRIBE_TO_SHARD_POW = 0.5;
	private static final int EXPECTED_SUBSCRIBE_TO_SHARD_RETRIES = 3;

	private static final String DUMMY_SEQUENCE = "1";

	private static final SequenceNumber SEQUENCE_NUMBER = new SequenceNumber(DUMMY_SEQUENCE);

	private static final SequenceNumber AGGREGATED_SEQUENCE_NUMBER = new SequenceNumber(DUMMY_SEQUENCE, 1L);

	@Test
	public void testToSdkV2StartingPositionAfterSequenceNumber() throws Exception {
		SingleShardFanOutKinesisV2 kinesis = emptyShard();

		RecordPublisher publisher = createRecordPublisher(kinesis, StartingPosition.continueFromSequenceNumber(SEQUENCE_NUMBER));
		publisher.run(new TestConsumer());

		assertEquals(DUMMY_SEQUENCE, kinesis.getStartingPositionForSubscription(0).sequenceNumber());
		assertEquals(AFTER_SEQUENCE_NUMBER, kinesis.getStartingPositionForSubscription(0).type());
	}

	@Test
	public void testToSdkV2StartingPositionAtSequenceNumber() throws Exception {
		SingleShardFanOutKinesisV2 kinesis = emptyShard();

		RecordPublisher publisher = createRecordPublisher(kinesis, StartingPosition.restartFromSequenceNumber(AGGREGATED_SEQUENCE_NUMBER));
		publisher.run(new TestConsumer());

		assertEquals(DUMMY_SEQUENCE, kinesis.getStartingPositionForSubscription(0).sequenceNumber());
		assertEquals(AT_SEQUENCE_NUMBER, kinesis.getStartingPositionForSubscription(0).type());
	}

	@Test
	public void testToSdkV2StartingPositionLatest() throws Exception {
		SingleShardFanOutKinesisV2 kinesis = emptyShard();

		RecordPublisher publisher = createRecordPublisher(kinesis, latest());
		publisher.run(new TestConsumer());

		assertNull(kinesis.getStartingPositionForSubscription(0).sequenceNumber());
		assertEquals(LATEST, kinesis.getStartingPositionForSubscription(0).type());
	}

	@Test
	public void testToSdkV2StartingPositionTrimHorizon() throws Exception {
		SingleShardFanOutKinesisV2 kinesis = emptyShard();

		RecordPublisher publisher = createRecordPublisher(kinesis, StartingPosition.continueFromSequenceNumber(SENTINEL_EARLIEST_SEQUENCE_NUM.get()));
		publisher.run(new TestConsumer());

		assertNull(kinesis.getStartingPositionForSubscription(0).sequenceNumber());
		assertEquals(TRIM_HORIZON, kinesis.getStartingPositionForSubscription(0).type());
	}

	@Test
	public void testToSdkV2StartingPositionAtTimeStamp() throws Exception {
		SingleShardFanOutKinesisV2 kinesis = emptyShard();
		Date now = new Date();

		RecordPublisher publisher = createRecordPublisher(kinesis, StartingPosition.fromTimestamp(now));
		publisher.run(new TestConsumer());

		assertEquals(now.toInstant(), kinesis.getStartingPositionForSubscription(0).timestamp());
		assertEquals(AT_TIMESTAMP, kinesis.getStartingPositionForSubscription(0).type());
	}

	@Test
	public void testToSdkV1Records() throws Exception {
		Date now = new Date();
		byte[] data = new byte[] { 0, 1, 2, 3 };

		Record record = Record
			.builder()
			.approximateArrivalTimestamp(now.toInstant())
			.partitionKey("pk")
			.sequenceNumber("sn")
			.data(SdkBytes.fromByteArray(data))
			.build();

		KinesisProxyV2Interface kinesis = singletonShard(createSubscribeToShardEvent(record));
		RecordPublisher publisher = createRecordPublisher(kinesis, latest());

		TestConsumer consumer = new TestConsumer();
		publisher.run(consumer);

		UserRecord actual = consumer.getRecordBatches().get(0).getDeaggregatedRecords().get(0);
		assertFalse(actual.isAggregated());
		assertEquals(now, actual.getApproximateArrivalTimestamp());
		assertEquals("sn", actual.getSequenceNumber());
		assertEquals("pk", actual.getPartitionKey());
		assertThat(toByteArray(actual.getData()), Matchers.equalTo(data));
	}

	@Test
	public void testExceptionThrownInConsumerPropagatesToRecordPublisher() throws Exception {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("An error thrown from the consumer");

		SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.boundedShard().build();
		RecordPublisher recordPublisher = createRecordPublisher(kinesis);

		recordPublisher.run(batch -> {
			throw new RuntimeException("An error thrown from the consumer");
		});
	}

	@Test
	public void testResourceNotFoundWhenObtainingSubscriptionTerminatesApplication() throws Exception {
		thrown.expect(ResourceNotFoundException.class);

		KinesisProxyV2Interface kinesis = FakeKinesisFanOutBehavioursFactory.resourceNotFoundWhenObtainingSubscription();
		RecordPublisher recordPublisher = createRecordPublisher(kinesis);

		recordPublisher.run(new TestConsumer());
	}

	@Test
	public void testShardConsumerCompletesIfResourceNotFoundExceptionThrownFromSubscription() throws Exception {
		ResourceNotFoundException exception = ResourceNotFoundException.builder().build();
		SubscriptionErrorKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.errorDuringSubscription(exception);
		RecordPublisher recordPublisher = createRecordPublisher(kinesis);
		TestConsumer consumer = new TestConsumer();

		assertEquals(COMPLETE, recordPublisher.run(consumer));

		// Will exit on the first subscription
		assertEquals(1, kinesis.getNumberOfSubscribeToShardInvocations());
	}

	@Test
	public void testShardConsumerRetriesIfLimitExceededExceptionThrownFromSubscription() throws Exception {
		LimitExceededException exception = LimitExceededException.builder().build();
		SubscriptionErrorKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.errorDuringSubscription(exception);
		RecordPublisher recordPublisher = createRecordPublisher(kinesis);
		TestConsumer consumer = new TestConsumer();

		RecordPublisherRunResult result = recordPublisher.run(consumer);

		// An exception is thrown after the 5th record in each subscription, therefore we expect to receive 5 records
		assertEquals(5, consumer.getRecordBatches().size());
		assertEquals(1, kinesis.getNumberOfSubscribeToShardInvocations());

		// INCOMPLETE is returned to indicate the shard is not complete
		assertEquals(INCOMPLETE, result);
	}

	@Test
	public void testSubscribeToShardBacksOffForRetryableError() throws Exception {
		LimitExceededException retryableError = LimitExceededException.builder().build();
		SubscriptionErrorKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.errorDuringSubscription(retryableError);
		FanOutRecordPublisherConfiguration configuration = createConfiguration();

		FullJitterBackoff backoff = mock(FullJitterBackoff.class);
		when(backoff.calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), anyInt())).thenReturn(100L);

		new FanOutRecordPublisher(latest(), "arn", createDummyStreamShardHandle(), kinesis, configuration, backoff)
			.run(new TestConsumer());

		verify(backoff).calculateFullJitterBackoff(
			EXPECTED_SUBSCRIBE_TO_SHARD_BASE,
			EXPECTED_SUBSCRIBE_TO_SHARD_MAX,
			EXPECTED_SUBSCRIBE_TO_SHARD_POW,
			1
		);

		verify(backoff).sleep(100L);
	}

	@Test
	public void testSubscribeToShardFailsWhenMaxRetriesExceeded() throws Exception {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Maximum retries exceeded for SubscribeToShard. Failed 3 times.");

		Properties efoProperties = createEfoProperties();
		efoProperties.setProperty(SUBSCRIBE_TO_SHARD_RETRIES, String.valueOf(EXPECTED_SUBSCRIBE_TO_SHARD_RETRIES));
		FanOutRecordPublisherConfiguration configuration = new FanOutRecordPublisherConfiguration(efoProperties, emptyList());

		LimitExceededException retryableError = LimitExceededException.builder().build();
		SubscriptionErrorKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.errorDuringSubscription(retryableError);
		FullJitterBackoff backoff = mock(FullJitterBackoff.class);

		FanOutRecordPublisher recordPublisher = new FanOutRecordPublisher(latest(), "arn", createDummyStreamShardHandle(), kinesis, configuration, backoff);

		int count = 0;
		while (recordPublisher.run(new TestConsumer()) == INCOMPLETE) {
			if (++count > EXPECTED_SUBSCRIBE_TO_SHARD_RETRIES) {
				break;
			}
		}
	}

	@Test
	public void testSubscribeToShardIgnoresReadTimeoutInRetryPolicy() throws Exception {
		Properties efoProperties = createEfoProperties();
		efoProperties.setProperty(SUBSCRIBE_TO_SHARD_RETRIES, String.valueOf(EXPECTED_SUBSCRIBE_TO_SHARD_RETRIES));
		FanOutRecordPublisherConfiguration configuration = new FanOutRecordPublisherConfiguration(efoProperties, emptyList());

		ReadTimeoutException retryableError = ReadTimeoutException.INSTANCE;
		FakeKinesisFanOutBehavioursFactory.SubscriptionErrorKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.errorDuringSubscription(retryableError);
		FullJitterBackoff backoff = mock(FullJitterBackoff.class);

		FanOutRecordPublisher recordPublisher = new FanOutRecordPublisher(latest(), "arn", createDummyStreamShardHandle(), kinesis, configuration, backoff);

		int count = 0;
		while (recordPublisher.run(new TestConsumer()) == RecordPublisherRunResult.INCOMPLETE) {
			if (++count > EXPECTED_SUBSCRIBE_TO_SHARD_RETRIES) {
				break;
			}
		}

		// No exception is thrown, but we still backoff.
		verify(backoff, times(EXPECTED_SUBSCRIBE_TO_SHARD_RETRIES + 1)).calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), anyInt());
	}

	@Test
	public void testSubscribeToShardBacksOffAttemptIncreases() throws Exception {
		LimitExceededException retryableError = LimitExceededException.builder().build();
		SubscriptionErrorKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.errorDuringSubscription(retryableError);
		FanOutRecordPublisherConfiguration configuration = createConfiguration();

		FullJitterBackoff backoff = mock(FullJitterBackoff.class);

		FanOutRecordPublisher recordPublisher = new FanOutRecordPublisher(latest(), "arn", createDummyStreamShardHandle(), kinesis, configuration, backoff);

		recordPublisher.run(new TestConsumer());
		recordPublisher.run(new TestConsumer());
		recordPublisher.run(new TestConsumer());

		verify(backoff).calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), eq(1));
		verify(backoff).calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), eq(2));
		verify(backoff).calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), eq(3));

		verify(backoff, never()).calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), eq(0));
		verify(backoff, never()).calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), eq(4));
	}

	@Test
	public void testBackOffAttemptResetsWithSuccessfulSubscription() throws Exception {
		SubscriptionErrorKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory.alternatingSuccessErrorDuringSubscription();
		FanOutRecordPublisherConfiguration configuration = createConfiguration();

		FullJitterBackoff backoff = mock(FullJitterBackoff.class);

		FanOutRecordPublisher recordPublisher = new FanOutRecordPublisher(latest(), "arn", createDummyStreamShardHandle(), kinesis, configuration, backoff);

		recordPublisher.run(new TestConsumer());
		recordPublisher.run(new TestConsumer());
		recordPublisher.run(new TestConsumer());

		// Expecting:
		// - first attempt to fail, and backoff attempt #1
		// - second attempt to succeed, and reset attempt index
		// - third attempt to fail, and backoff attempt #1

		verify(backoff, times(2)).calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), eq(1));

		verify(backoff, never()).calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), eq(0));
		verify(backoff, never()).calculateFullJitterBackoff(anyLong(), anyLong(), anyDouble(), eq(2));
	}

	@Test
	public void testRecordDurability() throws Exception {
		SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory
			.boundedShard()
			.withBatchCount(10)
			.withBatchesPerSubscription(3)
			.withRecordsPerBatch(12)
			.build();

		RecordPublisher recordPublisher = createRecordPublisher(kinesis);
		TestConsumer consumer = new TestConsumer();

		int count = 0;
		while (recordPublisher.run(consumer) == INCOMPLETE) {
			if (++count > 4) {
				break;
			}
		}

		List<UserRecord> userRecords = flattenToUserRecords(consumer.getRecordBatches());

		// Should have received 10 * 12 = 120 records
		assertEquals(120, userRecords.size());

		int expectedSequenceNumber = 1;
		for (UserRecord record : userRecords) {
			assertEquals(String.valueOf(expectedSequenceNumber++), record.getSequenceNumber());
		}
	}

	@Test
	public void testAggregatedRecordDurability() throws Exception {
		SingleShardFanOutKinesisV2 kinesis = FakeKinesisFanOutBehavioursFactory
			.boundedShard()
			.withBatchCount(10)
			.withAggregationFactor(5)
			.withRecordsPerBatch(12)
			.build();

		RecordPublisher recordPublisher = createRecordPublisher(kinesis);
		TestConsumer consumer = new TestConsumer();

		int count = 0;
		while (recordPublisher.run(consumer) == INCOMPLETE) {
			if (++count > 5) {
				break;
			}
		}

		List<UserRecord> userRecords = flattenToUserRecords(consumer.getRecordBatches());

		// Should have received 10 * 12 * 5 = 600 records
		assertEquals(600, userRecords.size());

		int sequence = 1;
		long subsequence = 0;
		for (UserRecord userRecord : userRecords) {
			assertEquals(String.valueOf(sequence), userRecord.getSequenceNumber());
			assertEquals(subsequence++, userRecord.getSubSequenceNumber());

			if (subsequence == 5) {
				sequence++;
				subsequence = 0;
			}
		}
	}

	@Test
	public void testInterruptedPublisherReturnsCancelled() throws Exception {
		KinesisProxyV2Interface kinesis = FakeKinesisFanOutBehavioursFactory.errorDuringSubscription(new SdkInterruptedException(null));

		RecordPublisher publisher = createRecordPublisher(kinesis, StartingPosition.continueFromSequenceNumber(SEQUENCE_NUMBER));
		RecordPublisherRunResult actual = publisher.run(new TestConsumer());

		assertEquals(CANCELLED, actual);
	}

	private List<UserRecord> flattenToUserRecords(final List<RecordBatch> recordBatch) {
		return recordBatch
			.stream()
			.flatMap(b -> b.getDeaggregatedRecords().stream())
			.collect(Collectors.toList());
	}

	private byte[] toByteArray(final ByteBuffer byteBuffer) {
		byte[] dataBytes = new byte[byteBuffer.remaining()];
		byteBuffer.get(dataBytes);
		return dataBytes;
	}

	private RecordPublisher createRecordPublisher(final KinesisProxyV2Interface kinesis) {
		return createRecordPublisher(kinesis, latest());
	}

	private RecordPublisher createRecordPublisher(final KinesisProxyV2Interface kinesis, final StartingPosition startingPosition) {
		return new FanOutRecordPublisher(startingPosition, "arn", createDummyStreamShardHandle(), kinesis, createConfiguration(), new FullJitterBackoff());
	}

	private FanOutRecordPublisherConfiguration createConfiguration() {
		return new FanOutRecordPublisherConfiguration(createEfoProperties(), emptyList());
	}

	private Properties createEfoProperties() {
		Properties config = new Properties();
		config.setProperty(RECORD_PUBLISHER_TYPE, EFO.name());
		config.setProperty(EFO_CONSUMER_NAME, "dummy-efo-consumer");
		config.setProperty(SUBSCRIBE_TO_SHARD_BACKOFF_BASE, String.valueOf(EXPECTED_SUBSCRIBE_TO_SHARD_BASE));
		config.setProperty(SUBSCRIBE_TO_SHARD_BACKOFF_MAX, String.valueOf(EXPECTED_SUBSCRIBE_TO_SHARD_MAX));
		config.setProperty(SUBSCRIBE_TO_SHARD_BACKOFF_EXPONENTIAL_CONSTANT, String.valueOf(EXPECTED_SUBSCRIBE_TO_SHARD_POW));
		return config;
	}

	private SubscribeToShardEvent createSubscribeToShardEvent(final Record...records) {
		return SubscribeToShardEvent
			.builder()
			.records(records)
			.build();
	}

	private StartingPosition latest() {
		return StartingPosition.continueFromSequenceNumber(SENTINEL_LATEST_SEQUENCE_NUM.get());
	}

}
