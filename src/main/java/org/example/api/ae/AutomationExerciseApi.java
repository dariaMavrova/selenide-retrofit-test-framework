package org.example.api.ae;

import org.example.api.ae.model.BrandsResponse;
import org.example.api.ae.model.MessageResponse;
import org.example.api.ae.model.ProductsResponse;
import org.example.api.ae.model.UserDetailResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

import java.util.Map;

/**
 * Retrofit contract for https://automationexercise.com/api_list
 * The API takes form fields (not JSON) and always responds with HTTP 200 + {@code responseCode} in the body.
 */
public interface AutomationExerciseApi {

    @GET("productsList")
    Call<ProductsResponse> getProducts();

    @POST("productsList")
    Call<MessageResponse> postProducts();

    @GET("brandsList")
    Call<BrandsResponse> getBrands();

    @PUT("brandsList")
    Call<MessageResponse> putBrands();

    @FormUrlEncoded
    @POST("searchProduct")
    Call<ProductsResponse> searchProduct(@Field("search_product") String query);

    @POST("searchProduct")
    Call<MessageResponse> searchProductWithoutQuery();

    /** A null field is left out of the request body. */
    @FormUrlEncoded
    @POST("verifyLogin")
    Call<MessageResponse> verifyLogin(@Field("email") String email, @Field("password") String password);

    @DELETE("verifyLogin")
    Call<MessageResponse> deleteVerifyLogin();

    @FormUrlEncoded
    @POST("createAccount")
    Call<MessageResponse> createAccount(@FieldMap Map<String, String> account);

    @FormUrlEncoded
    @PUT("updateAccount")
    Call<MessageResponse> updateAccount(@FieldMap Map<String, String> account);

    /** Retrofit's @DELETE can't carry a body, but this endpoint expects form fields. */
    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "deleteAccount", hasBody = true)
    Call<MessageResponse> deleteAccount(@Field("email") String email, @Field("password") String password);

    @GET("getUserDetailByEmail")
    Call<UserDetailResponse> getUserDetailByEmail(@Query("email") String email);
}
