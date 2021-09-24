# Amazon Kinesis Connector for Apache Flink

This is a fork of [the official Apache Flink Kinesis Connector](https://github.com/apache/flink/tree/master/flink-connectors/flink-connector-kinesis).

The fork backports the following features to older versions of Flink:

  - Enhanced Fanout (EFO) support for Flink 1.8/1.11. For the original contributions see:
    - [FLIP-128: Enhanced Fan Out for AWS Kinesis Consumers](https://cwiki.apache.org/confluence/display/FLINK/FLIP-128%3A+Enhanced+Fan+Out+for+AWS+Kinesis+Consumers)
    - [FLINK-17688: Support consuming Kinesis' enhanced fanout for flink-connector-kinesis](https://issues.apache.org/jira/browse/FLINK-17688)
  - Support for KDS data sources and sinks in Table API and SQL for Flink 1.11. For the original contributions see:
    - [FLINK-18858: Kinesis Flink SQL Connector](https://issues.apache.org/jira/browse/FLINK-18858)

Both features are already available in the official Apache Flink connector for Flink 1.12.

## Quickstart 

You no longer need to build the Kinesis Connector from source. 
Add the following dependency to your project to start using the connector.

### For Flink 1.8
```xml
<dependency>
    <groupId>software.amazon.kinesis</groupId>
    <artifactId>amazon-kinesis-connector-flink</artifactId>
    <version>1.3.0</version>
</dependency>
```  

### For Flink 1.11
```xml
<dependency>
    <groupId>software.amazon.kinesis</groupId>
    <artifactId>amazon-kinesis-connector-flink</artifactId>
    <version>2.2.0</version>
</dependency>
```  

Refer to the official Apache Flink documentation for more information on configuring the connector:
- [Amazon Kinesis Data Streams Connector](https://ci.apache.org/projects/flink/flink-docs-master/dev/connectors/kinesis.html)
- [Amazon Kinesis Data Streams SQL Connector](https://ci.apache.org/projects/flink/flink-docs-master/dev/table/connectors/kinesis.html)

Note that Flink 1.11 does not support [the "Available Metadata" functionality from upstream Table/SQL connector documentation](https://ci.apache.org/projects/flink/flink-docs-master/dev/table/connectors/kinesis.html#available-metadata). 
If you want to expose KDS metadata fields in your table definitions, consider upgrading to Flink 1.12 or higher and using the KDS connector from the upstream repository.

## SQL Connector

The `amazon-kinesis-sql-connector-flink` module can be used to build a fully shaded connector that can be used with Flink SQL client.
 
## Migration

If you are migrating from the Apache Flink Kinesis Connector, you should perform the following steps:
  
  1. Replace the dependency in your application `pom.xml`
  1. Migrate the prefix of packages for referenced classes
      - From: `org.apache.flink.streaming.connectors.kinesis`
      - To: `software.amazon.kinesis.connectors.flink`
    
For example
 
  - `com.amazonaws.services.kinesisanalytics.flink.connectors.FlinkKinesisConsumer` becomes
  - `software.amazon.kinesis.connectors.flink.FlinkKinesisConsumer`.

## Flink Versions

This connector is compatible with Flink 1.11.
For a version of this connector that is compatible with Flink 1.8 [use the `1.x` release line](https://github.com/awslabs/amazon-kinesis-connector-flink/tree/release-1.0).
Other versions of Flink may work, but are not officially supported. 

## Support

We will support this connector until KDA adds support for Apache Flink 1.12. 
Beyond this, we will not maintain patching or security for this repo.
The Apache Flink Kinesis connector should be used instead of this library where possible.

## Using EFO

Two additional properties are required to enable EFO on your `FlinkKinesisConsumer`:
- `RECORD_PUBLISHER_TYPE`: Set this parameter to EFO for your application to use an EFO consumer to access the Kinesis Data Stream data.
- `EFO_CONSUMER_NAME`: Set this parameter to a string value that is unique among the consumers of this stream. Re-using a consumer name in the same Kinesis Data Stream will cause the previous consumer using that name to be terminated.

To configure a FlinkKinesisConsumer to use EFO, add the following parameters to the consumer:
```java
consumerConfig.putIfAbsent(ConsumerConfigConstants.RECORD_PUBLISHER_TYPE, "EFO");
consumerConfig.putIfAbsent(ConsumerConfigConstants.EFO_CONSUMER_NAME, "efo-consumer");
```

Note the additional IAM permissions required to use EFO:

```json
{
  "Sid": "AllStreams",
  "Effect": "Allow",
  "Action": [
    "kinesis:ListShards",
    "kinesis:ListStreamConsumers"
  ],
  "Resource": "arn:aws:kinesis:<region>:<account>:stream/*"
},
{
  "Sid": "Stream",
  "Effect": "Allow",
  "Action": [
    "kinesis:DescribeStreamSummary",
    "kinesis:RegisterStreamConsumer"
  ],
  "Resource": "arn:aws:kinesis:<region>:<account>:stream/<stream-name>"
},
{
  "Sid": "Consumer",
  "Effect": "Allow",
  "Action": [
    "kinesis:DescribeStreamConsumer",
    "kinesis:SubscribeToShard",
    "kinesis:DeregisterStreamConsumer"
  ],
  "Resource": [
    "arn:aws:kinesis:<region>:<account>:stream/<stream-name>/consumer/<consumer-name>",
    "arn:aws:kinesis:<region>:<account>:stream/<stream-name>/consumer/<consumer-name>:*"
  ]
}
```

For more information refer to [the official EFO documentation for the KDS connector](https://ci.apache.org/projects/flink/flink-docs-master/dev/connectors/kinesis.html#using-enhanced-fan-out).

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

