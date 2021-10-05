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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * GetAuthorizationUrlParams
 */


public class GetAuthorizationUrlParams {
  @SerializedName("oxd_id")
  private String oxdId = null;

  @SerializedName("scope")
  private List<String> scope = null;

  @SerializedName("acr_values")
  private List<String> acrValues = null;

  @SerializedName("prompt")
  private String prompt = null;

  @SerializedName("state")
  private String state = null;

  @SerializedName("nonce")
  private String nonce = null;

  @SerializedName("redirect_uri")
  private String redirectUri = null;

  @SerializedName("response_types")
  private List<String> responseTypes = null;

  @SerializedName("custom_parameters")
  private Map<String, String> customParameters = null;

  @SerializedName("params")
  private Map<String, String> params = null;

  public GetAuthorizationUrlParams oxdId(String oxdId) {
    this.oxdId = oxdId;
    return this;
  }

   /**
   * Get oxdId
   * @return oxdId
  **/
  @Schema(example = "bcad760f-91ba-46e1-a020-05e4281d91b6", required = true, description = "")
  public String getOxdId() {
    return oxdId;
  }

  public void setOxdId(String oxdId) {
    this.oxdId = oxdId;
  }

  public GetAuthorizationUrlParams scope(List<String> scope) {
    this.scope = scope;
    return this;
  }

  public GetAuthorizationUrlParams addScopeItem(String scopeItem) {
    if (this.scope == null) {
      this.scope = new ArrayList<String>();
    }
    this.scope.add(scopeItem);
    return this;
  }

   /**
   * Get scope
   * @return scope
  **/
  @Schema(example = "[\"openid\"]", description = "")
  public List<String> getScope() {
    return scope;
  }

  public void setScope(List<String> scope) {
    this.scope = scope;
  }

  public GetAuthorizationUrlParams acrValues(List<String> acrValues) {
    this.acrValues = acrValues;
    return this;
  }

  public GetAuthorizationUrlParams addAcrValuesItem(String acrValuesItem) {
    if (this.acrValues == null) {
      this.acrValues = new ArrayList<String>();
    }
    this.acrValues.add(acrValuesItem);
    return this;
  }

   /**
   * Get acrValues
   * @return acrValues
  **/
  @Schema(example = "[\"basic\"]", description = "")
  public List<String> getAcrValues() {
    return acrValues;
  }

  public void setAcrValues(List<String> acrValues) {
    this.acrValues = acrValues;
  }

  public GetAuthorizationUrlParams prompt(String prompt) {
    this.prompt = prompt;
    return this;
  }

   /**
   * Get prompt
   * @return prompt
  **/
  @Schema(description = "")
  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public GetAuthorizationUrlParams state(String state) {
    this.state = state;
    return this;
  }

   /**
   * Get state
   * @return state
  **/
  @Schema(description = "")
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public GetAuthorizationUrlParams nonce(String nonce) {
    this.nonce = nonce;
    return this;
  }

   /**
   * Get nonce
   * @return nonce
  **/
  @Schema(description = "")
  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public GetAuthorizationUrlParams redirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
    return this;
  }

   /**
   * Get redirectUri
   * @return redirectUri
  **/
  @Schema(example = "https://client.example.org/cb", description = "")
  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public GetAuthorizationUrlParams responseTypes(List<String> responseTypes) {
    this.responseTypes = responseTypes;
    return this;
  }

  public GetAuthorizationUrlParams addResponseTypesItem(String responseTypesItem) {
    if (this.responseTypes == null) {
      this.responseTypes = new ArrayList<String>();
    }
    this.responseTypes.add(responseTypesItem);
    return this;
  }

   /**
   * Provide a list of the OAuth 2.0 response_type values that the Client is declaring that it will restrict itself to using. If omitted, the default is that the Client will use only the code response type.
   * @return responseTypes
  **/
  @Schema(example = "[\"code\"]", description = "Provide a list of the OAuth 2.0 response_type values that the Client is declaring that it will restrict itself to using. If omitted, the default is that the Client will use only the code response type.")
  public List<String> getResponseTypes() {
    return responseTypes;
  }

  public void setResponseTypes(List<String> responseTypes) {
    this.responseTypes = responseTypes;
  }

  public GetAuthorizationUrlParams customParameters(Map<String, String> customParameters) {
    this.customParameters = customParameters;
    return this;
  }

  public GetAuthorizationUrlParams putCustomParametersItem(String key, String customParametersItem) {
    if (this.customParameters == null) {
      this.customParameters = new HashMap<String, String>();
    }
    this.customParameters.put(key, customParametersItem);
    return this;
  }

   /**
   * Get customParameters
   * @return customParameters
  **/
  @Schema(description = "")
  public Map<String, String> getCustomParameters() {
    return customParameters;
  }

  public void setCustomParameters(Map<String, String> customParameters) {
    this.customParameters = customParameters;
  }

  public GetAuthorizationUrlParams params(Map<String, String> params) {
    this.params = params;
    return this;
  }

  public GetAuthorizationUrlParams putParamsItem(String key, String paramsItem) {
    if (this.params == null) {
      this.params = new HashMap<String, String>();
    }
    this.params.put(key, paramsItem);
    return this;
  }

   /**
   * Get params
   * @return params
  **/
  @Schema(description = "")
  public Map<String, String> getParams() {
    return params;
  }

  public void setParams(Map<String, String> params) {
    this.params = params;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetAuthorizationUrlParams getAuthorizationUrlParams = (GetAuthorizationUrlParams) o;
    return Objects.equals(this.oxdId, getAuthorizationUrlParams.oxdId) &&
        Objects.equals(this.scope, getAuthorizationUrlParams.scope) &&
        Objects.equals(this.acrValues, getAuthorizationUrlParams.acrValues) &&
        Objects.equals(this.prompt, getAuthorizationUrlParams.prompt) &&
        Objects.equals(this.state, getAuthorizationUrlParams.state) &&
        Objects.equals(this.nonce, getAuthorizationUrlParams.nonce) &&
        Objects.equals(this.redirectUri, getAuthorizationUrlParams.redirectUri) &&
        Objects.equals(this.responseTypes, getAuthorizationUrlParams.responseTypes) &&
        Objects.equals(this.customParameters, getAuthorizationUrlParams.customParameters) &&
        Objects.equals(this.params, getAuthorizationUrlParams.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(oxdId, scope, acrValues, prompt, state, nonce, redirectUri, responseTypes, customParameters, params);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetAuthorizationUrlParams {\n");
    
    sb.append("    oxdId: ").append(toIndentedString(oxdId)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    acrValues: ").append(toIndentedString(acrValues)).append("\n");
    sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    nonce: ").append(toIndentedString(nonce)).append("\n");
    sb.append("    redirectUri: ").append(toIndentedString(redirectUri)).append("\n");
    sb.append("    responseTypes: ").append(toIndentedString(responseTypes)).append("\n");
    sb.append("    customParameters: ").append(toIndentedString(customParameters)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
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
