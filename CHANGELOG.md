# Changelog

## Release 2.1.0 (June 29th 2021)
* Dependency Updates:
  * AWS SDK v1 from `1.11.844` to `1.12.7` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/37))
  * AWS SDK v2 from `2.13.52` to `2.16.86` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/37))
  * AWS DynamoDB Streams Kinesis Adapater from `1.5.0` to `1.5.3` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/37)) 
  * Guava from `18` to `29.0-jre` ([pull request 1](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/35), [pull request 2](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/39))
  * Jackson from `2.10.1` to `2.12.1` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/37))

## Release 1.2.0 (June 29th 2021)
* Dependency Updates:
  * AWS SDK v1 from `1.11.844` to `1.12.7` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/38))
  * AWS SDK v2 from `2.13.52` to `2.16.86` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/38))
  * AWS DynamoDB Streams Kinesis Adapater from `1.5.0` to `1.5.3` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/38)) 
  * Guava from `18` to `29.0-jre` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/36))
  * Jackson from `2.10.1` to `2.12.1` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/38))

## Release 1.1.2 (May 27th, 2021)
* Backport Fix issue where KinesisDataFetcher.shutdownFetcher() hangs.
  ([issue](https://github.com/awslabs/amazon-kinesis-connector-flink/issues/23), 
  [pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/32))
  
* Backport - Log error when shutting down Kinesis Data Fetcher.
  ([issue](https://github.com/awslabs/amazon-kinesis-connector-flink/issues/22), 
  [pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/32))
  
* Backport - Treating TimeoutException as Recoverable Exception.
  ([issue](https://github.com/awslabs/amazon-kinesis-connector-flink/issues/21), 
  [pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/32))
  
* Backport - Add time-out for acquiring subscription and passing events from network to source thread to prevent deadlock.
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/31)) 

## Release 2.0.3 (April 20th, 2021)
* Fix issue where KinesisDataFetcher.shutdownFetcher() hangs.
  ([issue](https://github.com/awslabs/amazon-kinesis-connector-flink/issues/23), 
  [pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/24))
  
* Log error when shutting down Kinesis Data Fetcher.
  ([issue](https://github.com/awslabs/amazon-kinesis-connector-flink/issues/22), 
  [pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/25))
  
* Treating TimeoutException as Recoverable Exception.
  ([issue](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/28), 
  [pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/issues/21))
  
* Adding a SQL connector module for SQL client.
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/26))

## Release 2.0.2 (March 29th, 2021)
* Add time-out for acquiring subscription and passing events from network to source thread to prevent deadlock.
  ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/18)) 

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

    