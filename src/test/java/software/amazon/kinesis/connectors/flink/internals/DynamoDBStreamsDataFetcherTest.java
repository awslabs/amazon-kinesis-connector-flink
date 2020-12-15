package software.amazon.kinesis.connectors.flink.internals;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import org.junit.Test;
import software.amazon.kinesis.connectors.flink.model.StreamShardHandle;
import software.amazon.kinesis.connectors.flink.proxy.KinesisProxyInterface;
import software.amazon.kinesis.connectors.flink.serialization.KinesisDeserializationSchemaWrapper;
import software.amazon.kinesis.connectors.flink.testutils.TestSourceContext;
import software.amazon.kinesis.connectors.flink.testutils.TestUtils;

import static com.amazonaws.services.kinesis.model.ShardIteratorType.LATEST;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static software.amazon.kinesis.connectors.flink.internals.KinesisDataFetcher.DEFAULT_SHARD_ASSIGNER;
import static software.amazon.kinesis.connectors.flink.model.SentinelSequenceNumber.SENTINEL_LATEST_SEQUENCE_NUM;

/**
 * Tests for {@link DynamoDBStreamsDataFetcher}.
 */
public class DynamoDBStreamsDataFetcherTest {

	@Test
	public void testCreateShardConsumerRespectsShardIteratorTypeLatest() throws Exception {
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

		fetcher.createShardConsumer(0, dummyStreamShardHandle, SENTINEL_LATEST_SEQUENCE_NUM.get(), runtimeContext.getMetricGroup());

		verify(kinesis).getShardIterator(dummyStreamShardHandle, LATEST.toString(), null);
	}

}
