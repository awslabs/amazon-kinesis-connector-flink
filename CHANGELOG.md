# Changelog

## Release 1.0.1 (October 19th, 2020)

### Bug Fixes
* Fix HTTP client dependency issue when using EFO consumer with STS Assume Role credential provider
    ([pull request](https://github.com/awslabs/amazon-kinesis-connector-flink/pull/1)).
    
* Dependency version updates:
  - `org.apache.httpcomponents:httpclient` => `4.5.9` (shaded)
  - `org.apache.httpcomponents:httpcore` => `4.4.11` (shaded)

    