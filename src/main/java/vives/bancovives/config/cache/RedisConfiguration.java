package vives.bancovives.config.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Clase de configuración para la caché Redis en la aplicación.
 * Esta clase establece la conexión con el servidor Redis, el RedisTemplate para las operaciones de datos,
 * y el RedisCacheManager para administrar las instancias de caché.
 *
 * @author Diego Novillo Luceño
 * @since 1.0.0
 */
@Configuration
@EnableRedisRepositories
public class RedisConfiguration {

    /**
     * Nombre de host del servidor Redis.
     */
    @Value("${redis.host}")
    String host;

    /**
     * Número de puerto del servidor Redis.
     */
    @Value("${redis.port}")
    int port;

    /**
     * Tiempo de espera para las entradas de caché en segundos.
     */
    @Value("${entry.timeout}")
    int entryTimeout;

    /**
     * Crea una {} para conectarse al servidor Redis.
     *
     * @return Instancia de JedisConnectionFactory
     */
    @Bean
    public JedisConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        return new JedisConnectionFactory(configuration);
    }

    /**
     * Crea un {@link RedisTemplate} para realizar operaciones Redis.
     *
     * @return Instancia de {@link RedisTemplate}
     */
    @Bean
    public RedisTemplate<String, Object> template() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new JdkSerializationRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Crea un RedisCacheManager para administrar las instancias de caché.
     *
     * @return Instancia de RedisCacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(entryTimeout))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory())
                .cacheDefaults(cacheConfig)
                .build();
    }
}