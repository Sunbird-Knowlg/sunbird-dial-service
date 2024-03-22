package org.sunbird.commons;

import org.junit.Assert;
import org.junit.Test;

public class SchemaCacheLoaderTest {
    @Test
    public void getSchemaCachePath() throws Exception {
        SchemaCacheLoader cacheLoader = SchemaCacheLoader.getInstance();
        Assert.assertNotNull(cacheLoader.getSchemaPath("contextValidation.json"));
        Assert.assertNotNull(cacheLoader.getSchemaPath("context.json"));
    }
}
