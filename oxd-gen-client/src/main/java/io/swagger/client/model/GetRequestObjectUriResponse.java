/*
 * oxd-server
 * oxd-server
 *
 * OpenAPI spec version: 4.2
 * Contact: yuriyz@gluu.org
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
/**
 * GetRequestObjectUriResponse
 */


public class GetRequestObjectUriResponse {
  @SerializedName("request_uri")
  private String requestUri = null;

  public GetRequestObjectUriResponse requestUri(String requestUri) {
    this.requestUri = requestUri;
    return this;
  }

   /**
   * Get requestUri
   * @return requestUri
  **/
  @Schema(example = "https://<oxd-host>/get-request-object/d871gpie16np0f5kfv936sc33k", required = true, description = "")
  public String getRequestUri() {
    return requestUri;
  }

  public void setRequestUri(String requestUri) {
    this.requestUri = requestUri;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetRequestObjectUriResponse getRequestObjectUriResponse = (GetRequestObjectUriResponse) o;
    return Objects.equals(this.requestUri, getRequestObjectUriResponse.requestUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestUri);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetRequestObjectUriResponse {\n");
    
    sb.append("    requestUri: ").append(toIndentedString(requestUri)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
