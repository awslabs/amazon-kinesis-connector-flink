# Changelog

## Release 2.4.1 (October 31st 2022)
* Dependency Updates:
  * Jackson from `2.12.7` to `2.13.4`
  * Jackson Databind from `2.12.7` to `2.13.4.2`

## Release 1.6.1 (October 31st 2022)
* Dependency Updates:
  * Jackson Databind from `2.13.4` to `2.13.4.2`

## Release 2.4.0 (September 15th 2022)
* Dependency Updates:
  * AWS SDK v2 from 2.16.86 to 2.17.247 ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/55))
  * AWS SDK v1 from 1.12.7 to 1.12.276 ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/55))
  * Kinesis Consumer Library from 1.14.0 to 1.14.8 ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/55))
  * Guava from 29.0-jre to 30.0-jre ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/55))
  * Jackson from `2.12.1` to `2.12.7` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/59))
* Relax validation to support new AWS regions without SDK update ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/62))

## Release 1.5.0 (September 15th 2022)
* Dependency Updates:
  * AWS SDK v2 from 2.16.86 to 2.17.247 ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/57))
  * AWS SDK v1 from 1.12.7 to 1.12.276 ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/57))
  * Kinesis Consumer Library from 1.14.0 to 1.14.8 ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/57))
  * Guava from 29.0-jre to 30.0-jre ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/57))
  * Jackson from `2.12.1` to `2.12.7` ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/60))

## Release 2.3.1 (January 4th 2022)
* Update log4j test dependency to 2.17.1 ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/52))

## Release 2.3.0 (November 1st 2021)
* EAGER EFO registration strategy no longer de-registers consumers ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/48))
* Unregistering metric reporting for closed shards ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/43))

## Release 1.4.0 (November 1st 2021)
* EAGER EFO registration strategy no longer de-registers consumers ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/49))

## Release 1.3.0 (August 17th 2021)
* Unregistering metric reporting for closed shards ([pull-request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/45))
* Backporting changes from v2.2.0 to v1.x
  * Refactor `requestRecords` to prevent AWS SDK/Netty threads blocking for EFO ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/40))
  * Optionally populate ClientConfiguration from properties for EFO ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/41))
  * Increasing read timeout to 6 minutes and enabling TCP keep alive for EFO ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/42))

## Release 2.2.0 (August 3rd 2021)
* Refactor `requestRecords` to prevent AWS SDK/Netty threads blocking for EFO ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/40)
* Optionally populate ClientConfiguration from properties for EFO ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/41)
* Increasing read timeout to 6 minutes and enabling TCP keep alive for EFO ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/42)

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

    