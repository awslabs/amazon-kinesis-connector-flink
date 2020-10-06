# Amazon Kinesis Connector for Apache Flink

This is a fork of the official Apache Flink Kinesis Connector:
- https://github.com/apache/flink/tree/master/flink-connectors/flink-connector-kinesis

This connector library adds Enhanced Fanout (EFO) support for Flink 1.8, allowing you to utilise EFO on Kinesis Data Analytics (KDA).
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
    <version>1.0.0</version>
</dependency>
```  

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

## Support

We will support this connector until end of Q1 2021, 
or while KDA does not support an official EFO connector, whichever is later. 
Beyond this, we will not maintain patching or security for this repo.
The Apache Flink Kinesis connector should be used in preference of this library once KDA supports an EFO enabled version.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

