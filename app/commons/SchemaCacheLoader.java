package commons;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.FileUtils;

public class SchemaCacheLoader {
    private static SchemaCacheLoader gt = new SchemaCacheLoader();
    private final String schemaBasePath = AppConfig.getString("schema.basePath","");
    private final String schemaLocalPath = AppConfig.getString("schema.localPath","");

    public static SchemaCacheLoader getInstance(){
        return gt;
    }
    private LoadingCache<String,String> cache;

    private SchemaCacheLoader() {
        long ttlCacheRefresh = AppConfig.getLong("schema.ttl", 43200L);
        cache = CacheBuilder.newBuilder().refreshAfterWrite(ttlCacheRefresh,TimeUnit.SECONDS).
                build(new CacheLoader<String, String>(){
                          @Override
                          public String load(String type) throws Exception {
                              String schemaJson = schemaBasePath+File.separator+type+File.separator+"contextSchema.json";
                              File localDir = new File(schemaLocalPath);
                              if (!localDir.exists()) localDir.mkdirs();
                              String localSchemaPath = localDir.getAbsolutePath() + File.separator + type + File.separator + "contextSchema.json";
                              FileUtils.copyURLToFile(new URL(schemaJson), new File(localSchemaPath));
                              return localSchemaPath;
                          }
                      }
                );
    }

    public String getSchemaPath(String type) throws ExecutionException{
        return cache.get(type);
    }
}
