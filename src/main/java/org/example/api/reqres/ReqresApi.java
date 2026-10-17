package org.example.api.reqres;

import org.example.api.reqres.model.CreateUserRequest;
import org.example.api.reqres.model.CreateUserResponse;
import org.example.api.reqres.model.LoginRequest;
import org.example.api.reqres.model.LoginResponse;
import org.example.api.reqres.model.SingleUserResponse;
import org.example.api.reqres.model.UpdateUserResponse;
import org.example.api.reqres.model.UserListResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit contract for https://reqres.in/api
 */
public interface ReqresApi {

    @GET("users")
    Call<UserListResponse> listUsers(@Query("page") int page);

    @GET("users/{id}")
    Call<SingleUserResponse> getUser(@Path("id") int id);

    @POST("users")
    Call<CreateUserResponse> createUser(@Body CreateUserRequest request);

    @PUT("users/{id}")
    Call<UpdateUserResponse> updateUser(@Path("id") int id, @Body CreateUserRequest request);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") int id);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
