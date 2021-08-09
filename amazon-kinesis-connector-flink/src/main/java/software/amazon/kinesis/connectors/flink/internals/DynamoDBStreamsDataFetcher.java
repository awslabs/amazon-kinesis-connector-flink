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

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.runtime.metrics.groups.GenericMetricGroup;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import software.amazon.kinesis.connectors.flink.KinesisShardAssigner;
import software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisher;
import software.amazon.kinesis.connectors.flink.internals.publisher.RecordPublisherFactory;
import software.amazon.kinesis.connectors.flink.internals.publisher.polling.PollingRecordPublisherFactory;
import software.amazon.kinesis.connectors.flink.model.DynamoDBStreamsShardHandle;
import software.amazon.kinesis.connectors.flink.model.SequenceNumber;
import software.amazon.kinesis.connectors.flink.model.StartingPosition;
import software.amazon.kinesis.connectors.flink.model.StreamShardHandle;
import software.amazon.kinesis.connectors.flink.proxy.DynamoDBStreamsProxy;
import software.amazon.kinesis.connectors.flink.serialization.KinesisDeserializationSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dynamodb streams data fetcher.
 * @param <T> type of fetched data.
 */
public class DynamoDBStreamsDataFetcher<T> extends KinesisDataFetcher<T> {
	private final RecordPublisherFactory recordPublisherFactory;

	/**
	 * Constructor.
	 *
	 * @param streams list of streams to fetch data
	 * @param sourceContext source context
	 * @param runtimeContext runtime context
	 * @param configProps config properties
	 * @param deserializationSchema deserialization schema
	 * @param shardAssigner shard assigner
	 */
	public DynamoDBStreamsDataFetcher(List<String> streams,
			SourceFunction.SourceContext<T> sourceContext,
			RuntimeContext runtimeContext,
			Properties configProps,
			KinesisDeserializationSchema<T> deserializationSchema,
			KinesisShardAssigner shardAssigner) {

		this(streams, sourceContext, runtimeContext, configProps, deserializationSchema, shardAssigner, DynamoDBStreamsProxy::create);
	}

	@VisibleForTesting
	DynamoDBStreamsDataFetcher(List<String> streams,
			SourceFunction.SourceContext<T> sourceContext,
			RuntimeContext runtimeContext,
			Properties configProps,
			KinesisDeserializationSchema<T> deserializationSchema,
			KinesisShardAssigner shardAssigner,
			FlinkKinesisProxyFactory flinkKinesisProxyFactory) {
		super(streams,
				sourceContext,
				sourceContext.getCheckpointLock(),
				runtimeContext,
				configProps,
				deserializationSchema,
				shardAssigner,
				null,
				null,
				new AtomicReference<>(),
				new ArrayList<>(),
				createInitialSubscribedStreamsToLastDiscoveredShardsState(streams),
				flinkKinesisProxyFactory,
				null);

		this.recordPublisherFactory = new PollingRecordPublisherFactory(flinkKinesisProxyFactory);
	}

	@Override
	protected boolean shouldAdvanceLastDiscoveredShardId(String shardId, String lastSeenShardIdOfStream) {
		if (DynamoDBStreamsShardHandle.compareShardIds(shardId, lastSeenShardIdOfStream) <= 0) {
			// shardID update is valid only if the given shard id is greater
			// than the previous last seen shard id of the stream.
			return false;
		}

		return true;
	}

	@Override
	protected RecordPublisher createRecordPublisher(
				SequenceNumber sequenceNumber,
				Properties configProps,
				GenericMetricGroup metricGroup,
				StreamShardHandle subscribedShard) throws InterruptedException {
		StartingPosition startingPosition = StartingPosition.continueFromSequenceNumber(sequenceNumber);
		return recordPublisherFactory.create(startingPosition, getConsumerConfiguration(), metricGroup, subscribedShard);
	}

}
