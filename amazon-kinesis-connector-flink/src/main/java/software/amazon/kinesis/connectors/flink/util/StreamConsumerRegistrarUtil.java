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

import org.apache.flink.annotation.Internal;
import org.apache.flink.annotation.VisibleForTesting;

import software.amazon.kinesis.connectors.flink.FlinkKinesisException;
import software.amazon.kinesis.connectors.flink.internals.publisher.fanout.FanOutRecordPublisherConfiguration;
import software.amazon.kinesis.connectors.flink.internals.publisher.fanout.StreamConsumerRegistrar;
import software.amazon.kinesis.connectors.flink.proxy.FullJitterBackoff;
import software.amazon.kinesis.connectors.flink.proxy.KinesisProxyV2Factory;
import software.amazon.kinesis.connectors.flink.proxy.KinesisProxyV2Interface;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.EFO_CONSUMER_NAME;
import static software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants.efoConsumerArn;
import static software.amazon.kinesis.connectors.flink.util.AwsV2Util.isEagerEfoRegistrationType;
import static software.amazon.kinesis.connectors.flink.util.AwsV2Util.isLazyEfoRegistrationType;
import static software.amazon.kinesis.connectors.flink.util.AwsV2Util.isUsingEfoRecordPublisher;

/**
 * A utility class that creates instances of {@link StreamConsumerRegistrar} and handles batch operations.
 */
@Internal
public class StreamConsumerRegistrarUtil {

	/**
	 * Registers stream consumers for the given streams if EFO is enabled with EAGER registration strategy.
	 *
	 * @param configProps the properties to parse configuration from
	 * @param streams the stream to register consumers against
	 */
	public static void eagerlyRegisterStreamConsumers(final Properties configProps, final List<String> streams) {
		if (!isUsingEfoRecordPublisher(configProps) || !isEagerEfoRegistrationType(configProps)) {
			return;
		}

		registerStreamConsumers(configProps, streams);
	}

	/**
	 * Registers stream consumers for the given streams if EFO is enabled with LAZY registration strategy.
	 *
	 * @param configProps the properties to parse configuration from
	 * @param streams the stream to register consumers against
	 */
	public static void lazilyRegisterStreamConsumers(final Properties configProps, final List<String> streams) {
		if (!isUsingEfoRecordPublisher(configProps) || !isLazyEfoRegistrationType(configProps)) {
			return;
		}

		registerStreamConsumers(configProps, streams);
	}

	/**
	 * Deregisters stream consumers for the given streams if EFO is enabled with LAZY registration strategy.
	 *
	 * @param configProps the properties to parse configuration from
	 * @param streams the stream to register consumers against
	 */
	public static void deregisterStreamConsumers(final Properties configProps, final List<String> streams) {
		if (isConsumerDeregistrationRequired(configProps)) {
			StreamConsumerRegistrar registrar = createStreamConsumerRegistrar(configProps, streams);
			try {
				deregisterStreamConsumers(registrar, configProps, streams);
			} finally {
				registrar.close();
			}
		}
	}

	private static boolean isConsumerDeregistrationRequired(final Properties configProps) {
		return isUsingEfoRecordPublisher(configProps) && isLazyEfoRegistrationType(configProps);
	}

	private static void registerStreamConsumers(final Properties configProps, final List<String> streams) {
		StreamConsumerRegistrar registrar = createStreamConsumerRegistrar(configProps, streams);

		try {
			registerStreamConsumers(registrar, configProps, streams);
		} finally {
			registrar.close();
		}
	}

	@VisibleForTesting
	static void registerStreamConsumers(
			final StreamConsumerRegistrar registrar,
			final Properties configProps,
			final List<String> streams) {
		String streamConsumerName = configProps.getProperty(EFO_CONSUMER_NAME);

		for (String stream : streams) {
			try {
				String streamConsumerArn = registrar.registerStreamConsumer(stream, streamConsumerName);
				configProps.setProperty(efoConsumerArn(stream), streamConsumerArn);
			} catch (ExecutionException ex) {
				throw new FlinkKinesisStreamConsumerRegistrarException("Error registering stream: " + stream, ex);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new FlinkKinesisStreamConsumerRegistrarException("Error registering stream: " + stream, ex);
			}
		}
	}

	@VisibleForTesting
	static void deregisterStreamConsumers(
			final StreamConsumerRegistrar registrar,
			final Properties configProps,
			final List<String> streams) {
		if (isConsumerDeregistrationRequired(configProps)) {
			for (String stream : streams) {
				try {
					registrar.deregisterStreamConsumer(stream);
				} catch (ExecutionException ex) {
					throw new FlinkKinesisStreamConsumerRegistrarException("Error deregistering stream: " + stream, ex);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					throw new FlinkKinesisStreamConsumerRegistrarException("Error registering stream: " + stream, ex);
				}
			}
		}
	}

	private static StreamConsumerRegistrar createStreamConsumerRegistrar(final Properties configProps, final List<String> streams) {
		FullJitterBackoff backoff = new FullJitterBackoff();
		FanOutRecordPublisherConfiguration configuration = new FanOutRecordPublisherConfiguration(configProps, streams);
		KinesisProxyV2Interface kinesis = KinesisProxyV2Factory.createKinesisProxyV2(configProps);

		return new StreamConsumerRegistrar(kinesis, configuration, backoff);
	}

	/**
	 * A semantic {@link RuntimeException} thrown to indicate errors de-/registering stream consumers.
	 */
	@Internal
	public static class FlinkKinesisStreamConsumerRegistrarException extends FlinkKinesisException {

		public FlinkKinesisStreamConsumerRegistrarException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}

}
