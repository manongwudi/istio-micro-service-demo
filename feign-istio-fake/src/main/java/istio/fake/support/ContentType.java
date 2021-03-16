//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package istio.fake.support;

public enum ContentType {
    UNDEFINED("undefined"),
    URLENCODED("application/x-www-form-urlencoded"),
    MULTIPART("multipart/form-data");

    private final String header;

    private ContentType(String header) {
        this.header = header;
    }

    public static ContentType of(String str) {
        if (str == null) {
            return UNDEFINED;
        } else {
            String trimmed = str.trim();
            ContentType[] var2 = values();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                ContentType type = var2[var4];
                if (trimmed.startsWith(type.getHeader())) {
                    return type;
                }
            }

            return UNDEFINED;
        }
    }

    public String getHeader() {
        return this.header;
    }
}
