/*
 * This file has been modified from the original.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.amazon.kinesis.connectors.flink.table;

import org.apache.flink.annotation.Internal;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.SourceFunctionProvider;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.DataType;
import org.apache.flink.util.Preconditions;

import software.amazon.kinesis.connectors.flink.FlinkKinesisConsumer;
import software.amazon.kinesis.connectors.flink.serialization.KinesisDeserializationSchema;
import software.amazon.kinesis.connectors.flink.serialization.KinesisDeserializationSchemaWrapper;

import java.util.Objects;
import java.util.Properties;

/**
 * Kinesis-backed {@link ScanTableSource}.
 */
@Internal
public class KinesisDynamicSource implements ScanTableSource {

	// --------------------------------------------------------------------------------------------
	// Scan format attributes
	// --------------------------------------------------------------------------------------------

	/**
	 * Data type to configure the format.
	 */
	private final DataType physicalDataType;

	/**
	 * Scan format for decoding records from Kinesis.
	 */
	private final DecodingFormat<DeserializationSchema<RowData>> decodingFormat;

	// --------------------------------------------------------------------------------------------
	// Kinesis-specific attributes
	// --------------------------------------------------------------------------------------------

	/**
	 * The Kinesis stream to consume.
	 */
	private final String stream;

	/**
	 * Properties for the Kinesis consumer.
	 */
	private final Properties consumerProperties;

	public KinesisDynamicSource(
			DataType physicalDataType,
			String stream,
			Properties consumerProperties,
			DecodingFormat<DeserializationSchema<RowData>> decodingFormat) {

		this.physicalDataType = Preconditions.checkNotNull(
				physicalDataType,
				"Physical data type must not be null.");
		this.stream = Preconditions.checkNotNull(
				stream,
				"Stream must not be null.");
		this.consumerProperties = Preconditions.checkNotNull(
				consumerProperties,
				"Properties for the Flink Kinesis consumer must not be null.");
		this.decodingFormat = Preconditions.checkNotNull(
				decodingFormat,
				"Decoding format must not be null.");
	}

	@Override
	public ChangelogMode getChangelogMode() {
		return decodingFormat.getChangelogMode();
	}

	@Override
	public ScanRuntimeProvider getScanRuntimeProvider(ScanContext runtimeProviderContext) {
		KinesisDeserializationSchema<RowData> deserializationSchema;
		DeserializationSchema<RowData> x = decodingFormat.createRuntimeDecoder(runtimeProviderContext, physicalDataType);

		deserializationSchema = new KinesisDeserializationSchemaWrapper<>(x);

		FlinkKinesisConsumer<RowData> kinesisConsumer = new FlinkKinesisConsumer<>(
				stream,
				deserializationSchema,
				consumerProperties);

		return SourceFunctionProvider.of(kinesisConsumer, false);
	}

	@Override
	public DynamicTableSource copy() {
		return new KinesisDynamicSource(
				physicalDataType,
				stream,
				consumerProperties,
				decodingFormat);
	}

	@Override
	public String asSummaryString() {
		return "Kinesis";
	}

	// --------------------------------------------------------------------------------------------
	// Value semantics for equals and hashCode
	// --------------------------------------------------------------------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		KinesisDynamicSource that = (KinesisDynamicSource) o;
		return Objects.equals(physicalDataType, that.physicalDataType) &&
				Objects.equals(stream, that.stream) &&
				Objects.equals(consumerProperties, that.consumerProperties) &&
				Objects.equals(decodingFormat, that.decodingFormat);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				physicalDataType,
				stream,
				consumerProperties,
				decodingFormat);
	}
}
