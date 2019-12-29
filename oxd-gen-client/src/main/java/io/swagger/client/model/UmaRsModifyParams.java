/*
 * oxd-server
 * oxd-server
 *
 * OpenAPI spec version: 4.0
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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * UmaRsModifyParams
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-12-28T11:07:10.713Z")
public class UmaRsModifyParams {
  @SerializedName("oxd_id")
  private String oxdId = null;

  @SerializedName("path")
  private String path = null;

  @SerializedName("http_method")
  private String httpMethod = null;

  @SerializedName("scopes")
  private List<String> scopes = null;

  @SerializedName("scope_expression")
  private String scopeExpression = null;

  public UmaRsModifyParams oxdId(String oxdId) {
    this.oxdId = oxdId;
    return this;
  }

   /**
   * Get oxdId
   * @return oxdId
  **/
  @ApiModelProperty(example = "bcad760f-91ba-46e1-a020-05e4281d91b6", required = true, value = "")
  public String getOxdId() {
    return oxdId;
  }

  public void setOxdId(String oxdId) {
    this.oxdId = oxdId;
  }

  public UmaRsModifyParams path(String path) {
    this.path = path;
    return this;
  }

   /**
   * Get path
   * @return path
  **/
  @ApiModelProperty(example = "/ws/document", required = true, value = "")
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public UmaRsModifyParams httpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
    return this;
  }

   /**
   * Get httpMethod
   * @return httpMethod
  **/
  @ApiModelProperty(example = "POST", required = true, value = "")
  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public UmaRsModifyParams scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public UmaRsModifyParams addScopesItem(String scopesItem) {
    if (this.scopes == null) {
      this.scopes = new ArrayList<String>();
    }
    this.scopes.add(scopesItem);
    return this;
  }

   /**
   * Get scopes
   * @return scopes
  **/
  @ApiModelProperty(value = "")
  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public UmaRsModifyParams scopeExpression(String scopeExpression) {
    this.scopeExpression = scopeExpression;
    return this;
  }

   /**
   * Get scopeExpression
   * @return scopeExpression
  **/
  @ApiModelProperty(value = "")
  public String getScopeExpression() {
    return scopeExpression;
  }

  public void setScopeExpression(String scopeExpression) {
    this.scopeExpression = scopeExpression;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UmaRsModifyParams umaRsModifyParams = (UmaRsModifyParams) o;
    return Objects.equals(this.oxdId, umaRsModifyParams.oxdId) &&
        Objects.equals(this.path, umaRsModifyParams.path) &&
        Objects.equals(this.httpMethod, umaRsModifyParams.httpMethod) &&
        Objects.equals(this.scopes, umaRsModifyParams.scopes) &&
        Objects.equals(this.scopeExpression, umaRsModifyParams.scopeExpression);
  }

  @Override
  public int hashCode() {
    return Objects.hash(oxdId, path, httpMethod, scopes, scopeExpression);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UmaRsModifyParams {\n");
    
    sb.append("    oxdId: ").append(toIndentedString(oxdId)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    httpMethod: ").append(toIndentedString(httpMethod)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    scopeExpression: ").append(toIndentedString(scopeExpression)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

