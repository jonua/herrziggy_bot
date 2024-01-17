package me.jonua.herrziggy_bot.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static okhttp3.logging.HttpLoggingInterceptor.Level.*;

@Slf4j
@Configuration
public class GoogleCalendarApiConfiguration {
    @Value("${calendar.calendar-id}")
    private String calendarId;
    @Value("${google-cloud.calendar.api.key}")
    private String googleCloudCalendarApiKey;
    @Value("${default-zone-id}")
    private ZoneId zoneId;

    @Autowired
    private ObjectMapper jackson;

    @Bean
    public RetrofitGoogleCalendarApi googleCalendarApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/calendar/v3/calendars/" + calendarId + "/")
                .client(buildOkHttpClient())
                .addConverterFactory(buildConverterFactory())
                .build();

        return retrofit.create(RetrofitGoogleCalendarApi.class);
    }

    private Converter.Factory buildConverterFactory() {
        return JacksonConverterFactory.create(jackson);
    }

    private OkHttpClient buildOkHttpClient() {
        Duration timeoutDuration = Duration.of(30, ChronoUnit.SECONDS);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(log::debug);

        return new OkHttpClient.Builder()
                .callTimeout(timeoutDuration)
                .connectTimeout(timeoutDuration)
                .readTimeout(timeoutDuration)
                .writeTimeout(timeoutDuration)
                .addInterceptor(new GoogleAouKeyProvider(googleCloudCalendarApiKey, zoneId))
                .addInterceptor(chain -> {
                    boolean hasMultipart = isMultipartRequest(chain);
                    setupOkHttpLogLevel(loggingInterceptor, hasMultipart);
                    return chain.proceed(chain.request());
                })
                .addInterceptor(loggingInterceptor)
                .build();
    }

    private static boolean isMultipartRequest(Interceptor.Chain chain) {
        return chain.request().body() instanceof MultipartBody;
    }

    private static void setupOkHttpLogLevel(HttpLoggingInterceptor httpLoggingInterceptor, boolean multipartUploadRequest) {
        if (log.isWarnEnabled()) { // or lowest
            httpLoggingInterceptor.setLevel(BASIC);
        }

        if (log.isDebugEnabled()) { // or lowest
            httpLoggingInterceptor.setLevel(BODY);
        }

        if (multipartUploadRequest) {
            httpLoggingInterceptor.setLevel(HEADERS);
        }

        log.trace("okHttp3 log level has set up to {} level", httpLoggingInterceptor.getLevel().name());
    }

    @RequiredArgsConstructor
    public static class GoogleAouKeyProvider implements Interceptor {
        private final String googleApiKey;
        private final ZoneId timeZone;

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            HttpUrl url = chain.request().url().newBuilder()
                    .addQueryParameter("key", googleApiKey)
                    .addQueryParameter("timeZone", timeZone.toString())
                    .addQueryParameter("orderBy", "startTime")
                    .addQueryParameter("singleEvents", "true")
                    .build();

            Request request = chain.request().newBuilder()
                    .url(url)
                    .build();

            return chain.proceed(request);
        }
    }
}