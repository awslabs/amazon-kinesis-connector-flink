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
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.table.data.RowData;
import org.apache.flink.util.Preconditions;

import software.amazon.kinesis.connectors.flink.serialization.KinesisDeserializationSchema;

import java.io.IOException;

/**
 * A {@link KinesisDeserializationSchema} adaptor for {@link RowData} records that delegates
 * physical data deserialization to an inner {@link DeserializationSchema} and appends requested
 * metadata to the end of the deserialized {@link RowData} record.
 */
@Internal
public final class RowDataKinesisDeserializationSchema
	implements KinesisDeserializationSchema<RowData> {

	private static final long serialVersionUID = 5551095193778230749L;

	/** A {@link DeserializationSchema} to deserialize the physical part of the row. */
	private final DeserializationSchema<RowData> physicalDeserializer;

	/** The type of the produced {@link RowData} records (physical data with appended metadata]. */
	private final TypeInformation<RowData> producedTypeInfo;

	public RowDataKinesisDeserializationSchema(
		DeserializationSchema<RowData> physicalDeserializer,
		TypeInformation<RowData> producedTypeInfo) {
		this.physicalDeserializer = Preconditions.checkNotNull(physicalDeserializer);
		this.producedTypeInfo = Preconditions.checkNotNull(producedTypeInfo);
	}

	@Override
	public void open(DeserializationSchema.InitializationContext context) throws Exception {
		physicalDeserializer.open(context);
	}

	@Override
	public RowData deserialize(
		byte[] recordValue,
		String partitionKey,
		String seqNum,
		long approxArrivalTimestamp,
		String stream,
		String shardId) throws IOException {

		return physicalDeserializer.deserialize(recordValue);
	}

	@Override
	public TypeInformation<RowData> getProducedType() {
		return producedTypeInfo;
	}
}
