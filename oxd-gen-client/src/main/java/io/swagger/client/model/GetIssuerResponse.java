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
import io.swagger.client.model.WebFingerLink;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * GetIssuerResponse
 */


public class GetIssuerResponse {
  @SerializedName("subject")
  private String subject = null;

  @SerializedName("links")
  private List<WebFingerLink> links = null;

  public GetIssuerResponse subject(String subject) {
    this.subject = subject;
    return this;
  }

   /**
   * Get subject
   * @return subject
  **/
  @Schema(example = "admin@jenkins-ldap.gluu.org", description = "")
  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public GetIssuerResponse links(List<WebFingerLink> links) {
    this.links = links;
    return this;
  }

  public GetIssuerResponse addLinksItem(WebFingerLink linksItem) {
    if (this.links == null) {
      this.links = new ArrayList<WebFingerLink>();
    }
    this.links.add(linksItem);
    return this;
  }

   /**
   * Get links
   * @return links
  **/
  @Schema(description = "")
  public List<WebFingerLink> getLinks() {
    return links;
  }

  public void setLinks(List<WebFingerLink> links) {
    this.links = links;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetIssuerResponse getIssuerResponse = (GetIssuerResponse) o;
    return Objects.equals(this.subject, getIssuerResponse.subject) &&
        Objects.equals(this.links, getIssuerResponse.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, links);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetIssuerResponse {\n");
    
    sb.append("    subject: ").append(toIndentedString(subject)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
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
