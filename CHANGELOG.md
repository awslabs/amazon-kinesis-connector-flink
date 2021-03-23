# Changelog

## Release 2.0.1 (March 23rd, 2021)
* Fix `PollingRecordPublisher` to respect `SHARD_GETRECORDS_INTERVAL_MILLIS`.
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/17)) 
  ([Apache Flink JIRA](https://issues.apache.org/jira/browse/FLINK-21661))
  
* Update EFO consumer error handling to treat Interrupted exceptions as non-recoverable.
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/16)) 
  ([Apache Flink JIRA](https://issues.apache.org/jira/browse/FLINK-21933))

## Release 2.0.0 (December 22nd, 2020)
* Add KDS connector support in the Table API and SQL layer.
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/6))

## Release 1.1.1 (March 23rd, 2021)
* Fix `PollingRecordPublisher` to respect `SHARD_GETRECORDS_INTERVAL_MILLIS`.
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/17)) 
  ([Apache Flink JIRA](https://issues.apache.org/jira/browse/FLINK-21661))
  
* Update EFO consumer error handling to treat Interrupted exceptions as non-recoverable.
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/16)) 
  ([Apache Flink JIRA](https://issues.apache.org/jira/browse/FLINK-21933))

## Release 1.1.0 (December 22nd, 2020)
* Migrate EFO consumers to use `DescribeStreamSummary` rather than `DescribeStream`. `DescribeStreamSummary` has a 
higher TPS quota, resulting in reduced startup time for high parallelism sources. You may need add IAM permission for 
`kinesis:DescribeStreamSummary` while upgrading to this version 
 ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/10))
 
* Fix issue preventing DynamoDB stream consumers to start from `LATEST` 
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/11), 
  [issue](https://github.com/awslabs/amazon-kinesis-connector-flink/issues/9))

## Release 1.0.4 (November 11th, 2020)
* Fix issue when Polling consumer using timestamp with empty shard
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/6))

## Release 1.0.3 (November 6th, 2020)
* Optimise `ShardConsumer` to clone `DersializationSchema` once per object rather than once per record
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/5))

## Release 1.0.2 (October 30th, 2020)
* Ignore `ReadTimeoutException` from `SubcribeToShard` retry policy to prevent app restarting due to high backpressure
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/3))
  
* Optimise error handling. Introduced a separate error delivery mechanism to improve performance under high backpressure
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/4)) 

## Release 1.0.1 (October 19th, 2020)

### Bug Fixes
* Fix HTTP client dependency issue when using EFO consumer with STS Assume Role credential provider
    ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/1)).
    
* Dependency version updates:
  - `org.apache.httpcomponents:httpclient` => `4.5.9` (shaded)
  - `org.apache.httpcomponents:httpcore` => `4.4.11` (shaded)

    