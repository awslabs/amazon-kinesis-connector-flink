# Amazon Kinesis Connector for Apache Flink

This is a fork of the official Apache Flink Kinesis Connector:
- https://github.com/apache/flink/tree/master/flink-connectors/flink-connector-kinesis

This connector library adds Enhanced Fanout (EFO) support for Flink 1.8/1.11, allowing you to utilise EFO on Kinesis Data Analytics (KDA).
EFO is already available in the official Apache Flink connector for Flink 1.12.   
- https://issues.apache.org/jira/browse/FLINK-17688
- https://cwiki.apache.org/confluence/display/FLINK/FLIP-128%3A+Enhanced+Fan+Out+for+AWS+Kinesis+Consumers

## Quickstart 

You no longer need to build the Kinesis Connector from source. 
Add the following dependency to your project to start using the connector.

```xml
<dependency>
    <groupId>software.amazon.kinesis</groupId>
    <artifactId>amazon-kinesis-connector-flink</artifactId>
    <version>1.6.1</version>
</dependency>
```  

Refer to the official Apache Flink documentation for more information on configuring the connector:
- https://ci.apache.org/projects/flink/flink-docs-stable/dev/connectors/kinesis.html 

## Migration

If you are migrating from the Apache Flink Kinesis Connector, you should perform the following steps:
  
  1. Replace the dependency in your application `pom.xml`
  1. Migrate the prefix of packages for referenced classes
      1. From: `org.apache.flink.streaming.connectors.kinesis`
      1. To: `software.amazon.kinesis.connectors.flink`
    
For example:
  
  - `FlinkKinesisConsumer`
      - From: `com.amazonaws.services.kinesisanalytics.flink.connectors.FlinkKinesisConsumer`
      - To: `software.amazon.kinesis.connectors.flink.FlinkKinesisConsumer`

## Flink Versions

This connector is supported for Flink 1.8 and Flink 1.11. 
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

Refer to the offical Apache Flink development documentation for more information:
- https://ci.apache.org/projects/flink/flink-docs-master/dev/connectors/kinesis.html 

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

