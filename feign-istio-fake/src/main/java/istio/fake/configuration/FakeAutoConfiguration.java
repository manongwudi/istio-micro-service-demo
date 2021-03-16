/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package istio.fake.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import istio.fake.support.FakeHttpClientProperties;
import istio.fake.openfake.DefaultTargeter;
import istio.fake.openfake.Fake;
import istio.fake.openfake.FakeClientProperties;
import istio.fake.openfake.FakeClientSpecification;
import istio.fake.openfake.FakeContext;
import istio.fake.openfake.Targeter;

/**
 * @author Spencer Gibb
 * @author Julien Roy
 */
@Configuration
@ConditionalOnClass(Fake.class)
@EnableConfigurationProperties({ FakeClientProperties.class,
		FakeHttpClientProperties.class })
public class FakeAutoConfiguration {

	@Autowired(required = false)
	private List<FakeClientSpecification> configurations = new ArrayList<>();

	@Bean
	public FakeContext fakeContext() {
		FakeContext context = new FakeContext();
		context.setConfigurations(this.configurations);
		return context;
	}

	@Configuration
	protected static class HystrixFeignTargeterConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public Targeter fakeTargeter() {
			return new DefaultTargeter();
		}

	}

	// the following configuration is for alternate feign clients if
	// ribbon is not on the class path.
	// see corresponding configurations in FeignRibbonClientAutoConfiguration
	// for load balanced ribbon clients.
//	@Configuration
//	@ConditionalOnClass(ApacheHttpClient.class)
//	@ConditionalOnMissingClass("com.netflix.loadbalancer.ILoadBalancer")
//	@ConditionalOnMissingBean(CloseableHttpClient.class)
//	@ConditionalOnProperty(value = "feign.httpclient.enabled", matchIfMissing = true)
//	protected static class HttpClientFeignConfiguration {
//
//		private final Timer connectionManagerTimer = new Timer(
//				"FeignApacheHttpClientConfiguration.connectionManagerTimer", true);
//
//		@Autowired(required = false)
//		private RegistryBuilder registryBuilder;
//
//		private CloseableHttpClient httpClient;
//
//		@Bean
//		@ConditionalOnMissingBean(HttpClientConnectionManager.class)
//		public HttpClientConnectionManager connectionManager(
//				ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
//				FeignHttpClientProperties httpClientProperties) {
//			final HttpClientConnectionManager connectionManager = connectionManagerFactory
//					.newConnectionManager(httpClientProperties.isDisableSslValidation(),
//							httpClientProperties.getMaxConnections(),
//							httpClientProperties.getMaxConnectionsPerRoute(),
//							httpClientProperties.getTimeToLive(),
//							httpClientProperties.getTimeToLiveUnit(),
//							this.registryBuilder);
//			this.connectionManagerTimer.schedule(new TimerTask() {
//				@Override
//				public void run() {
//					connectionManager.closeExpiredConnections();
//				}
//			}, 30000, httpClientProperties.getConnectionTimerRepeat());
//			return connectionManager;
//		}
//
//		@Bean
//		public CloseableHttpClient httpClient(ApacheHttpClientFactory httpClientFactory,
//				HttpClientConnectionManager httpClientConnectionManager,
//				FeignHttpClientProperties httpClientProperties) {
//			RequestConfig defaultRequestConfig = RequestConfig.custom()
//					.setConnectTimeout(httpClientProperties.getConnectionTimeout())
//					.setRedirectsEnabled(httpClientProperties.isFollowRedirects())
//					.build();
//			this.httpClient = httpClientFactory.createBuilder()
//					.setConnectionManager(httpClientConnectionManager)
//					.setDefaultRequestConfig(defaultRequestConfig).build();
//			return this.httpClient;
//		}
//
//		@Bean
//		@ConditionalOnMissingBean(Client.class)
//		public Client feignClient(HttpClient httpClient) {
//			return new ApacheHttpClient(httpClient);
//		}
//
//		@PreDestroy
//		public void destroy() throws Exception {
//			this.connectionManagerTimer.cancel();
//			if (this.httpClient != null) {
//				this.httpClient.close();
//			}
//		}
//
//	}
//
//	@Configuration
//	@ConditionalOnClass(OkHttpClient.class)
//	@ConditionalOnMissingClass("com.netflix.loadbalancer.ILoadBalancer")
//	@ConditionalOnMissingBean(okhttp3.OkHttpClient.class)
//	@ConditionalOnProperty("feign.okhttp.enabled")
//	protected static class OkHttpFeignConfiguration {
//
//		private okhttp3.OkHttpClient okHttpClient;
//
//		@Bean
//		@ConditionalOnMissingBean(ConnectionPool.class)
//		public ConnectionPool httpClientConnectionPool(
//				FeignHttpClientProperties httpClientProperties,
//				OkHttpClientConnectionPoolFactory connectionPoolFactory) {
//			Integer maxTotalConnections = httpClientProperties.getMaxConnections();
//			Long timeToLive = httpClientProperties.getTimeToLive();
//			TimeUnit ttlUnit = httpClientProperties.getTimeToLiveUnit();
//			return connectionPoolFactory.create(maxTotalConnections, timeToLive, ttlUnit);
//		}
//
//		@Bean
//		public okhttp3.OkHttpClient client(OkHttpClientFactory httpClientFactory,
//				ConnectionPool connectionPool,
//				FeignHttpClientProperties httpClientProperties) {
//			Boolean followRedirects = httpClientProperties.isFollowRedirects();
//			Integer connectTimeout = httpClientProperties.getConnectionTimeout();
//			Boolean disableSslValidation = httpClientProperties.isDisableSslValidation();
//			this.okHttpClient = httpClientFactory.createBuilder(disableSslValidation)
//					.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
//					.followRedirects(followRedirects).connectionPool(connectionPool)
//					.build();
//			return this.okHttpClient;
//		}
//
//		@PreDestroy
//		public void destroy() {
//			if (this.okHttpClient != null) {
//				this.okHttpClient.dispatcher().executorService().shutdown();
//				this.okHttpClient.connectionPool().evictAll();
//			}
//		}
//
//		@Bean
//		@ConditionalOnMissingBean(Client.class)
//		public Client feignClient(okhttp3.OkHttpClient client) {
//			return new OkHttpClient(client);
//		}
//
//	}

}
