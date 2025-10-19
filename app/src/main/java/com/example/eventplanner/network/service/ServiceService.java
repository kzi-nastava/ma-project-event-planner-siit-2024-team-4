package com.example.eventplanner.network.service;

import com.example.eventplanner.dto.CreateServiceDTO;
import com.example.eventplanner.dto.ServiceDTO;
import com.example.eventplanner.dto.UpdateServiceDTO;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceService {

    @GET("services")
    Call<List<ServiceDTO>> getAllServices(@Header("Authorization") String token);

    @GET("services/my-services")
    Call<List<ServiceDTO>> getMyServices(@Header("Authorization") String token, @Query("providerId") Long providerId);

    @GET("services/my-services")
    Call<List<ServiceDTO>> getServicesByProviderId(@Header("Authorization") String token, @Query("providerId") Long providerId);

    @GET("services/{id}")
    Call<ServiceDTO> getServiceById(@Header("Authorization") String token, @Path("id") Long id);

    @Multipart
    @POST("services")
    Call<ServiceDTO> createService(
            @Header("Authorization") String token,
            @Part("dto") RequestBody dto,
            @Part List<MultipartBody.Part> files
    );

    @Multipart
    @PUT("services/{id}")
    Call<ServiceDTO> updateService(
            @Header("Authorization") String token,
            @Path("id") Long id,
            @Part("dto") RequestBody dto,
            @Part List<MultipartBody.Part> files
    );

    @DELETE("services/{id}")
    Call<Void> deleteService(@Header("Authorization") String token, @Path("id") Long id);

    @GET("services/search")
    Call<List<ServiceDTO>> searchServices(@Header("Authorization") String token, @Query("name") String name);

    @GET("services/filter")
    Call<List<ServiceDTO>> filterServices(
            @Header("Authorization") String token,
            @Query("categoryId") Long categoryId,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("isAvailable") Boolean isAvailable,
            @Query("eventTypeId") Long eventTypeId
    );

    @PUT("services/{id}/price-discount")
    Call<ServiceDTO> updatePriceAndDiscount(
            @Header("Authorization") String token,
            @Path("id") Long id,
            @Body PriceDiscountUpdateDTO dto
    );

    class PriceDiscountUpdateDTO {
        private double price;
        private double discount;

        public PriceDiscountUpdateDTO(double price, double discount) {
            this.price = price;
            this.discount = discount;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getDiscount() {
            return discount;
        }

        public void setDiscount(double discount) {
            this.discount = discount;
        }
    }
}
