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
    private final String jsonldBasePath = AppConfig.getString("jsonld.basePath","");
    private final String jsonldLocalPath = AppConfig.getString("jsonld.localPath","");
    private final String jsonldType = AppConfig.getString("jsonld.type","");

    public static SchemaCacheLoader getInstance(){
        return gt;
    }
    private LoadingCache<String,String> cache;

    private SchemaCacheLoader() {
        long ttlCacheRefresh = AppConfig.getLong("schema.ttl", 43200L);
        cache = CacheBuilder.newBuilder().refreshAfterWrite(ttlCacheRefresh,TimeUnit.SECONDS).
                build(new CacheLoader<>() {
                          @Override
                          public String load(String fileName) throws Exception {
                              String schemaJson = jsonldBasePath + File.separator + jsonldType + File.separator + fileName;
                              File localDir = new File(jsonldLocalPath);
                              if (!localDir.exists()) localDir.mkdirs();
                              String localSchemaPath = localDir.getAbsolutePath() + File.separator + jsonldType + File.separator + fileName;
                              FileUtils.copyURLToFile(new URL(schemaJson), new File(localSchemaPath));
                              return localSchemaPath;
                          }
                      }
                );
    }

    public String getSchemaPath(String fileName) throws ExecutionException{
        return cache.get(fileName);
    }
}
