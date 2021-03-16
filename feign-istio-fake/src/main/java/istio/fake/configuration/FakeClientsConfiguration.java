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
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.fasterxml.jackson.databind.Module;

import istio.fake.base.Client;
import istio.fake.base.Contract;
import istio.fake.base.codec.Decoder;
import istio.fake.base.codec.Encoder;
import istio.fake.base.log.DefaultFakeLoggerFactory;
import istio.fake.base.log.FakeLogger;
import istio.fake.base.log.FakeLoggerFactory;
import istio.fake.openfake.AnnotatedParameterProcessor;
import istio.fake.openfake.Fake;
import istio.fake.openfake.FakeFormatterRegistrar;
import istio.fake.openfake.OptionalDecoder;
import istio.fake.support.HttpRequestHeaderHolder;
import istio.fake.support.HttpRequestHeaderHolderImpl;
import istio.fake.support.PageJacksonModule;
import istio.fake.support.PageableSpringEncoder;
import istio.fake.support.ResponseEntityDecoder;
import istio.fake.support.SpringDecoder;
import istio.fake.support.SpringEncoder;
import istio.fake.support.SpringMvcContract;

/**
 * @author Dave Syer
 * @author Venil Noronha
 */
@Configuration
public class FakeClientsConfiguration {

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Autowired(required = false)
    private List<AnnotatedParameterProcessor> parameterProcessors = new ArrayList<>();

    @Autowired(required = false)
    private List<FakeFormatterRegistrar> fakeFormatterRegistrars = new ArrayList<>();

    @Autowired(required = false)
    private FakeLogger logger;

    @Bean
    @ConditionalOnMissingBean
    public Decoder fakeDecoder() {
        return new OptionalDecoder(
                new ResponseEntityDecoder(new SpringDecoder(this.messageConverters)));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass("org.springframework.data.domain.Pageable")
    public Encoder fakeEncoder() {
        return new SpringEncoder(this.messageConverters);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.domain.Pageable")
    @ConditionalOnMissingBean
    public Encoder fakeEncoderPageable() {
        return new PageableSpringEncoder(new SpringEncoder(this.messageConverters));
    }

    @Bean
    @ConditionalOnMissingBean
    public Contract fakeContract(ConversionService fakeConversionService) {
        return new SpringMvcContract(this.parameterProcessors, fakeConversionService);
    }

    @Bean
    public FormattingConversionService fakeConversionService() {
        FormattingConversionService conversionService = new DefaultFormattingConversionService();
        for (FakeFormatterRegistrar fakeFormatterRegistrar : this.fakeFormatterRegistrars) {
            fakeFormatterRegistrar.registerFormatters(conversionService);
        }
        return conversionService;
    }

    @Bean
    @ConditionalOnMissingBean(FakeLoggerFactory.class)
    public FakeLoggerFactory fakeLoggerFactory() {
        return new DefaultFakeLoggerFactory(this.logger);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.domain.Page")
    public Module pageJacksonModule() {
        return new PageJacksonModule();
    }


    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public Fake.Builder feignBuilder() {
        return Fake.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    public Client fakeClient() {
        return new Client.Default(null, null);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpRequestHeaderHolder httpRequestHeaderHolder() {
        return new HttpRequestHeaderHolderImpl(tracingHeaderList());
    }

    @Bean
    @ConditionalOnMissingBean
    public List<String> tracingHeaderList() {
        return Arrays.asList("x-request-id", "x-b3-traceid", "x-b3-spanid", "x-b3-sampled", "x-b3-flags", "Authorization",
                "x-ot-span-context", "x-datadog-trace-id", "x-datadog-parent-id", "x-datadog-sampled", "end-user", "user-agent");
    }

}
