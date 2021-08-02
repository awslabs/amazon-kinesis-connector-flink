/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.amazon.kinesis.connectors.flink.proxy;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.ClientConfigurationFactory;
import org.junit.Test;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.internal.NettyConfiguration;
import software.amazon.kinesis.connectors.flink.config.AWSConfigConstants;
import software.amazon.kinesis.connectors.flink.testutils.TestUtils;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static software.amazon.kinesis.connectors.flink.util.AWSUtil.AWS_CLIENT_CONFIG_PREFIX;

/**
 * Test for methods in the {@link KinesisProxyV2Factory} class.
 */
public class KinesisProxyV2FactoryTest {

	@Test
	public void testClientConfigurationPopulatedFromDefaults() throws Exception {
		Properties properties = properties();

		KinesisProxyV2Interface proxy = KinesisProxyV2Factory.createKinesisProxyV2(properties);
		NettyConfiguration nettyConfiguration = getNettyConfiguration(proxy);

		assertEquals(defaultClientConfiguration().getConnectionTimeout(), nettyConfiguration.connectTimeoutMillis());
	}

	@Test
	public void testClientConfigurationPopulatedFromProperties() throws Exception {
		Properties properties = properties();
		properties.setProperty(AWS_CLIENT_CONFIG_PREFIX + "connectionTimeout", "12345");

		KinesisProxyV2Interface proxy = KinesisProxyV2Factory.createKinesisProxyV2(properties);
		NettyConfiguration nettyConfiguration = getNettyConfiguration(proxy);

		assertEquals(12345, nettyConfiguration.connectTimeoutMillis());
	}

	@Test
	public void testClientConfigurationPopulatedTcpKeepAliveDefaults() throws Exception {
		Properties properties = properties();

		KinesisProxyV2Interface proxy = KinesisProxyV2Factory.createKinesisProxyV2(properties);
		NettyConfiguration nettyConfiguration = getNettyConfiguration(proxy);

		assertTrue(nettyConfiguration.tcpKeepAlive());
	}

	@Test
	public void testClientConfigurationPopulatedTcpKeepAliveFromProperties() throws Exception {
		Properties properties = properties();
		properties.setProperty(AWS_CLIENT_CONFIG_PREFIX + "useTcpKeepAlive", "false");

		KinesisProxyV2Interface proxy = KinesisProxyV2Factory.createKinesisProxyV2(properties);
		NettyConfiguration nettyConfiguration = getNettyConfiguration(proxy);

		assertFalse(nettyConfiguration.tcpKeepAlive());
	}

	private NettyConfiguration getNettyConfiguration(final KinesisProxyV2Interface kinesis) throws Exception {
		NettyNioAsyncHttpClient httpClient = getField("httpClient", kinesis);
		return getField("configuration", httpClient);
	}

	private <T> T getField(String fieldName, Object obj) throws Exception {
		Field field = obj.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return (T) field.get(obj);
	}

	private ClientConfiguration defaultClientConfiguration() {
		return new ClientConfigurationFactory().getConfig();
	}

	private Properties properties() {
		Properties properties = TestUtils.efoProperties();
		properties.setProperty(AWSConfigConstants.AWS_REGION, "eu-west-2");
		return properties;
	}

}
