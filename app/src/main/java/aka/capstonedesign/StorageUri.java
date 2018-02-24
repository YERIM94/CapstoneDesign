package aka.capstonedesign;

import java.util.*;

/**
 * Created by 예림 on 2017-11-24.
 */

public class StorageUri {
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public java.util.Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uri", uri);

        return result;
    }
}
