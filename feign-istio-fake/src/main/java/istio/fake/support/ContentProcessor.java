//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package istio.fake.support;

import java.nio.charset.Charset;
import java.util.Map;

import istio.fake.FakeException;
import istio.fake.base.RequestTemplate;

public interface ContentProcessor {
    String CONTENT_TYPE_HEADER = "Content-Type";
    String CRLF = "\r\n";

    void process(RequestTemplate var1, Charset var2, Map<String, Object> var3) throws FakeException;

    ContentType getSupportedContentType();
}
