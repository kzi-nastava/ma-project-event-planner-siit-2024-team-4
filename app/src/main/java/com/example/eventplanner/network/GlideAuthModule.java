package com.example.eventplanner.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

import okhttp3.OkHttpClient;

@GlideModule
public final class GlideAuthModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        SharedPreferences preferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("jwt_token", null);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        if (token != null) {
            String finalToken = token;
            clientBuilder.addInterceptor(chain -> {
                okhttp3.Request originalRequest = chain.request();
                okhttp3.Request.Builder requestBuilder = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + finalToken);
                
                Log.d("GlideAuthModule", "Adding Authorization header to image request: " + originalRequest.url());
                
                return chain.proceed(requestBuilder.build());
            });
        } else {
            Log.w("GlideAuthModule", "JWT token not found in SharedPreferences. Images might not load if authentication is required.");
        }

        OkHttpClient okHttpClient = clientBuilder.build();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
    }
}

