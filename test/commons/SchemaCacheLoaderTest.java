package commons;

import org.junit.Assert;
import org.junit.Test;

public class SchemaCacheLoaderTest {
    @Test
    public void getSchemaCachePath() throws Exception {
        SchemaCacheLoader cacheLoader = SchemaCacheLoader.getInstance();
        Assert.assertNotNull(cacheLoader.getSchemaPath("dialcode"));
    }
}
