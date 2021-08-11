/*
 *  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License").
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package software.amazon.kinesis.connectors.flink.internals;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.runtime.metrics.groups.OperatorMetricGroup;

import org.junit.Test;
import software.amazon.kinesis.connectors.flink.model.StreamShardHandle;
import software.amazon.kinesis.connectors.flink.proxy.KinesisProxyInterface;
import software.amazon.kinesis.connectors.flink.serialization.KinesisDeserializationSchemaWrapper;
import software.amazon.kinesis.connectors.flink.testutils.TestSourceContext;
import software.amazon.kinesis.connectors.flink.testutils.TestUtils;

import java.util.Properties;

import static com.amazonaws.services.kinesis.model.ShardIteratorType.LATEST;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static software.amazon.kinesis.connectors.flink.internals.KinesisDataFetcher.DEFAULT_SHARD_ASSIGNER;
import static software.amazon.kinesis.connectors.flink.internals.ShardConsumerTestUtils.createFakeShardConsumerMetricGroup;
import static software.amazon.kinesis.connectors.flink.model.SentinelSequenceNumber.SENTINEL_LATEST_SEQUENCE_NUM;

/**
 * Tests for {@link DynamoDBStreamsDataFetcher}.
 */
public class DynamoDBStreamsDataFetcherTest {

	@Test
	public void testCreateRecordPublisherRespectsShardIteratorTypeLatest() throws Exception {
		RuntimeContext runtimeContext = TestUtils.getMockedRuntimeContext(1, 0);
		KinesisProxyInterface kinesis = mock(KinesisProxyInterface.class);

		DynamoDBStreamsDataFetcher<String> fetcher = new DynamoDBStreamsDataFetcher<>(
				singletonList("fakeStream"),
				new TestSourceContext<>(),
				runtimeContext,
				TestUtils.getStandardProperties(),
				new KinesisDeserializationSchemaWrapper<>(new SimpleStringSchema()),
				DEFAULT_SHARD_ASSIGNER,
				config -> kinesis);

		StreamShardHandle dummyStreamShardHandle = TestUtils.createDummyStreamShardHandle("dummy-stream", "0");

		fetcher.createRecordPublisher(
				SENTINEL_LATEST_SEQUENCE_NUM.get(),
				new Properties(),
				createFakeShardConsumerMetricGroup((OperatorMetricGroup) runtimeContext.getMetricGroup()),
				dummyStreamShardHandle);

		verify(kinesis).getShardIterator(dummyStreamShardHandle, LATEST.toString(), null);
	}

}
