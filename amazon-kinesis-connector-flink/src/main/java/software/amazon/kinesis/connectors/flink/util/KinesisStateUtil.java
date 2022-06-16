package software.amazon.kinesis.connectors.flink.util;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.runtime.TupleSerializer;
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer;
import software.amazon.kinesis.connectors.flink.model.SequenceNumber;
import software.amazon.kinesis.connectors.flink.model.StreamShardMetadata;

/**
 * Utilities for Flink Kinesis connector state management.
 */
public class KinesisStateUtil {
    /**
     * Creates state serializer for kinesis shard sequence number.
     * Using of the explicit state serializer with KryoSerializer is needed because otherwise
     * users cannot use 'disableGenericTypes' properties with KinesisConsumer, see FLINK-24943 for details
     *
     * @return state serializer
     */
    public static TupleSerializer<Tuple2<StreamShardMetadata, SequenceNumber>> createShardsStateSerializer(ExecutionConfig executionConfig) {
        // explicit serializer will keep the compatibility with GenericTypeInformation and allow to disableGenericTypes for users
        TypeSerializer<?>[] fieldSerializers = new TypeSerializer<?>[] {
                TypeInformation.of(StreamShardMetadata.class).createSerializer(executionConfig),
                new KryoSerializer<>(SequenceNumber.class, executionConfig)
        };
        @SuppressWarnings("unchecked")
        Class<Tuple2<StreamShardMetadata, SequenceNumber>> tupleClass = (Class<Tuple2<StreamShardMetadata, SequenceNumber>>) (Class<?>) Tuple2.class;
        return new TupleSerializer<>(tupleClass, fieldSerializers);
    }
}
