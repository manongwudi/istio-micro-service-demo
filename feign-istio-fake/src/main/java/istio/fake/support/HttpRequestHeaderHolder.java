package istio.fake.support;

import java.util.Map;

import javax.servlet.ServletRequestListener;

public abstract class HttpRequestHeaderHolder implements ServletRequestListener {

    public abstract Map<String, Object> getHeaderMap();

}
