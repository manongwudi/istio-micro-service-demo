package istio.fake.openfake;

import org.springframework.cloud.context.named.NamedContextFactory;

import istio.fake.configuration.FakeClientsConfiguration;

public class FakeContext extends NamedContextFactory<FakeClientSpecification> {

    public FakeContext() {
        super(FakeClientsConfiguration.class, "fake", "fake.client.name");
    }

}