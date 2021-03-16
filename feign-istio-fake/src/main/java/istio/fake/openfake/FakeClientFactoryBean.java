package istio.fake.openfake;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import istio.fake.base.Client;
import istio.fake.base.Contract;
import istio.fake.base.Request;
import istio.fake.base.RequestInterceptor;
import istio.fake.base.codec.Decoder;
import istio.fake.base.codec.Encoder;
import istio.fake.base.codec.ErrorDecoder;
import istio.fake.base.log.FakeLogger;
import istio.fake.base.log.FakeLoggerFactory;
import istio.fake.support.HttpRequestHeaderHolder;


/**
 * @author Spencer Gibb
 * @author Venil Noronha
 * @author Eko Kurniawan Khannedy
 * @author Gregor Zurowski
 */
public class FakeClientFactoryBean
        implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {

    /***********************************
     * WARNING! Nothing in this class should be @Autowired. It causes NPEs because of some
     * lifecycle race condition.
     ***********************************/

    private Class<?> type;

    private String name;

    private String url;

    private boolean decode404;

    private ApplicationContext applicationContext;


    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.name, "Name must be set");
    }

    @Override
    public Object getObject() throws Exception {
        return getTarget();
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * @param <T> the target type of the ake client
     * @return a {@link Fake} client created with the specified data and the context
     * information
     */
    @SuppressWarnings("unchecked")
    <T> T getTarget() {
        FakeContext context = this.applicationContext.getBean(FakeContext.class);
        Fake.Builder builder = fake(context);

        if (!this.name.startsWith("http")) {
            this.url = "http://" + this.name;
        } else {
            this.url = this.name;
        }
        return (T) loadBalance(builder, context,
                new Target.HardCodedTarget<>(this.type, this.name, this.url));
    }

    protected Fake.Builder fake(FakeContext context) {
        FakeLoggerFactory loggerFactory = get(context, FakeLoggerFactory.class);
        FakeLogger logger = loggerFactory.create(this.type);

        // @formatter:off
        Fake.Builder builder = get(context, Fake.Builder.class)
                // required values
                .logger(logger)
                .encoder(get(context, Encoder.class))
                .decoder(get(context, Decoder.class))
                .contract(get(context, Contract.class))
                .headerHolder(get(context, HttpRequestHeaderHolder.class));
        // @formatter:on
        configureFeign(context, builder);

        return builder;
    }

    protected void configureFeign(FakeContext context, Fake.Builder builder) {
        FakeClientProperties properties = this.applicationContext
                .getBean(FakeClientProperties.class);
        if (properties != null) {
            if (properties.isDefaultToProperties()) {
                configureUsingConfiguration(context, builder);
                configureUsingProperties(
                        properties.getConfig().get(properties.getDefaultConfig()),
                        builder);
                configureUsingProperties(properties.getConfig().get(this.name),
                        builder);
            } else {
                configureUsingProperties(
                        properties.getConfig().get(properties.getDefaultConfig()),
                        builder);
                configureUsingProperties(properties.getConfig().get(this.name),
                        builder);
                configureUsingConfiguration(context, builder);
            }
        } else {
            configureUsingConfiguration(context, builder);
        }
    }

    protected void configureUsingConfiguration(FakeContext context,
                                               Fake.Builder builder) {
        FakeLogger.Level level = getOptional(context, FakeLogger.Level.class);
        if (level != null) {
            builder.logLevel(level);
        }
        ErrorDecoder errorDecoder = getOptional(context, ErrorDecoder.class);
        if (errorDecoder != null) {
            builder.errorDecoder(errorDecoder);
        }
        Request.Options options = getOptional(context, Request.Options.class);
        if (options != null) {
            builder.options(options);
        }
        Map<String, RequestInterceptor> requestInterceptors = context
                .getInstances(this.name, RequestInterceptor.class);
        if (requestInterceptors != null) {
            builder.requestInterceptors(requestInterceptors.values());
        }

        if (this.decode404) {
            builder.decode404();
        }
    }

    protected void configureUsingProperties(
            FakeClientProperties.FeignClientConfiguration config,
            Fake.Builder builder) {
        if (config == null) {
            return;
        }

        if (config.getLoggerLevel() != null) {
            builder.logLevel(config.getLoggerLevel());
        }

        if (config.getConnectTimeout() != null && config.getReadTimeout() != null) {
            builder.options(new Request.Options(config.getConnectTimeout(),
                    config.getReadTimeout()));
        }

        if (config.getErrorDecoder() != null) {
            ErrorDecoder errorDecoder = getOrInstantiate(config.getErrorDecoder());
            builder.errorDecoder(errorDecoder);
        }

        if (config.getRequestInterceptors() != null
                && !config.getRequestInterceptors().isEmpty()) {
            // this will add request interceptor to builder, not replace existing
            for (Class<RequestInterceptor> bean : config.getRequestInterceptors()) {
                RequestInterceptor interceptor = getOrInstantiate(bean);
                builder.requestInterceptor(interceptor);
            }
        }

        if (config.getDecode404() != null) {
            if (config.getDecode404()) {
                builder.decode404();
            }
        }

        if (Objects.nonNull(config.getEncoder())) {
            builder.encoder(getOrInstantiate(config.getEncoder()));
        }

        if (Objects.nonNull(config.getDecoder())) {
            builder.decoder(getOrInstantiate(config.getDecoder()));
        }

        if (Objects.nonNull(config.getContract())) {
            builder.contract(getOrInstantiate(config.getContract()));
        }
    }

    private <T> T getOrInstantiate(Class<T> tClass) {
        try {
            return this.applicationContext.getBean(tClass);
        } catch (NoSuchBeanDefinitionException e) {
            return BeanUtils.instantiateClass(tClass);
        }
    }

    protected <T> T get(FakeContext context, Class<T> type) {
        T instance = context.getInstance(this.name, type);
        if (instance == null) {
            throw new IllegalStateException(
                    "No bean found of type " + type + " for " + this.name);
        }
        return instance;
    }

    protected <T> T getOptional(FakeContext context, Class<T> type) {
        return context.getInstance(this.name, type);
    }

    protected <T> T loadBalance(Fake.Builder builder, FakeContext context,
                                Target.HardCodedTarget<T> target) {
        Client client = getOptional(context, Client.class);
        if (client != null) {
            builder.client(client);
            Targeter targeter = get(context, Targeter.class);
            return targeter.target(this, builder, context, target);
        }

        throw new IllegalStateException(
                "No Feign Client for loadBalancing defined. Did you forget to include spring-cloud-starter-netflix-ribbon?");
    }


    public Class<?> getType() {
        return this.type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDecode404() {
        return this.decode404;
    }

    public void setDecode404(boolean decode404) {
        this.decode404 = decode404;
    }

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FakeClientFactoryBean that = (FakeClientFactoryBean) o;
        return Objects.equals(this.applicationContext, that.applicationContext)
                && this.decode404 == that.decode404
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.applicationContext, this.decode404,
                this.name, this.type, this.url);
    }

    @Override
    public String toString() {
        return new StringBuilder("FeignClientFactoryBean{").append("type=")
                .append(this.type).append(", ").append("name='").append(this.name)
                .append("', ").append("url='").append(this.url).append("', ")
                .append("path='").append("', ").append("decode404=")
                .append(this.decode404).append(", ").append("applicationContext=")
                .append(this.applicationContext).append(", ")
                .append("}").toString();
    }

}
