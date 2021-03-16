package istio.fake.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.CollectionUtils;

/**
 * @author tanghuan@focusmedia.cn
 * @date 2019/7/14
 */
public class HttpRequestHeaderHolderImpl extends HttpRequestHeaderHolder {

    private List<String> tracingHeaderList;

    public HttpRequestHeaderHolderImpl(List<String> tracingHeaderList) {
        this.tracingHeaderList = tracingHeaderList;
    }

    private ThreadLocal<HttpServletRequest> httpServletRequestHolder =
            new InheritableThreadLocal<>();

    @Override
    public void requestInitialized(ServletRequestEvent requestEvent) {
        HttpServletRequest request = (HttpServletRequest) requestEvent.getServletRequest();
        httpServletRequestHolder.set(request);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent requestEvent) {
        httpServletRequestHolder.remove();
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequestHolder.get();
    }

    @Override
    public Map<String, Object> getHeaderMap() {
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            return null;
        }

        if (CollectionUtils.isEmpty(tracingHeaderList)) {
            return null;
        }

        Map<String, Object> headerMap = new HashMap<>(16);
        for (String name : tracingHeaderList) {
            String value = request.getHeader(name);
            if (value != null) {
                headerMap.put(name, request.getHeader(name));
            }
        }
        return headerMap;

    }
}