/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.old.services.controller;

import fr.insee.sugoi.converter.mapper.OuganextSugoiMapper;
import fr.insee.sugoi.converter.ouganext.PasswordChangeRequestOuganext;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.SugoiUser;
import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.old.services.model.ConverterDomainRealm;
import fr.insee.sugoi.old.services.model.RealmStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Tag(
    name = "[Deprecated] - Manage password",
    description = "Old Enpoints to manage contacts password")
@SecurityRequirement(name = "basic")
public class ContactPasswordDomaineController {

  @Autowired private CredentialsService credentialsService;

  @Autowired private OuganextSugoiMapper ouganextSugoiMapper;

  @Autowired private ConverterDomainRealm converterDomainRealm;

  /**
   * Reinitialize a password with a random new password
   *
   * @param identifiant contact
   * @param domaine
   * @param modeEnvoisString "mail" or "courrier"
   * @param pcr mail address, mail format
   * @return NO_CONTENT if modification occured
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  @PostMapping(
      value = "/{domaine}/contact/{id}/password",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Reinitialize a password with a random new password", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "password successfully added to contact",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Contact not found",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            })
      })
  public ResponseEntity<?> reinitPassword(
      @Parameter(description = "Contact's id to update", required = true)
          @PathVariable(name = "id", required = true)
          String identifiant,
      @Parameter(
              description = "Name of the domaine where the operation will be made",
              required = true)
          @PathVariable(name = "domaine", required = true)
          String domaine,
      @Parameter(description = "Way to send login can be mail or courrier", required = false)
          @RequestParam(name = "modeEnvoi", required = false)
          List<String> modeEnvoisString,
      @Parameter(
              description = "Other infos to reset password: mail address, mail format",
              required = true)
          @RequestBody
          PasswordChangeRequestOuganext pcr,
      Authentication authentication) {
    RealmStorage realmUserStorage = converterDomainRealm.getRealmForDomain(domaine);

    Map<String, String> templateProperties = getTemplatePropertiesFromPCR(pcr);

    credentialsService.reinitPassword(
        realmUserStorage.getRealm(),
        realmUserStorage.getUserStorage(),
        identifiant,
        templateProperties,
        modeEnvoisString != null && modeEnvoisString.size() > 0 ? modeEnvoisString.get(0) : "MAIL",
        true,
        new ProviderRequest(
            new SugoiUser(
                authentication.getName(),
                authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(String::toUpperCase)
                    .collect(Collectors.toList())),
            false,
            null,
            false));
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  /**
   * Initialize a contact password with a provided password
   *
   * @param identifiant of the contact which password to initialize
   * @param domaine
   * @param pcr a PCR containting the new password
   * @return NO_CONTENT if password is initialized, CONFLICT if a password already exist or if new
   *     password does not respect password policy
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  @PostMapping(
      value = "/{domaine}/contact/{id}/password/first",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Initialize a contact password with a provided password", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "password successfully added to contact",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Contact not found",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            })
      })
  public ResponseEntity<?> initPassword(
      @Parameter(description = "Contact's id to update", required = true)
          @PathVariable(name = "id", required = true)
          String identifiant,
      @Parameter(
              description = "Name of the domaine where the operation will be made",
              required = true)
          @PathVariable(name = "domaine", required = true)
          String domaine,
      @Parameter(description = "Other infos to reset password: new password", required = true)
          @RequestBody
          PasswordChangeRequestOuganext pcr,
      Authentication authentication) {
    RealmStorage realmUserStorage = converterDomainRealm.getRealmForDomain(domaine);

    credentialsService.initPassword(
        realmUserStorage.getRealm(),
        realmUserStorage.getUserStorage(),
        identifiant,
        pcr.getNouveauMotDePasse(),
        true,
        new ProviderRequest(
            new SugoiUser(
                authentication.getName(),
                authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(String::toUpperCase)
                    .collect(Collectors.toList())),
            false,
            null,
            false));
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  /**
   * Change password
   *
   * @param identifiant contact which password will be changed
   * @param domaine
   * @param modeEnvoisString can be "mail" or "courrier"
   * @param pcr mail to send the new password, format informations, address if modeEnvois is
   *     address, new password and old password
   * @return NO_CONTENT if modification occured, UNAUTHORIZED if old password does not correspond or
   *     CONFLICT if does not respect password policy
   */
  @PutMapping(
      value = "/{domaine}/contact/{id}/password",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  @Operation(summary = "Change password of an user", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "password successfully change for the contact",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Contact not found",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            })
      })
  public ResponseEntity<?> changePassword(
      @Parameter(description = "Contact's id to update", required = true)
          @PathVariable(name = "id", required = true)
          String identifiant,
      @Parameter(
              description = "Name of the domaine where the operation will be made",
              required = true)
          @PathVariable(name = "domaine", required = true)
          String domaine,
      @Parameter(description = "Way to send login can be mail or courrier", required = false)
          @RequestParam(name = "modeEnvoi", required = false)
          List<String> modeEnvoisString,
      @Parameter(
              description =
                  "Other infos to reset password: mail to send the new password, format informations, ;address if modeEnvois is address, new password and old password",
              required = true)
          @RequestBody
          PasswordChangeRequestOuganext pcr,
      Authentication authentication) {
    RealmStorage realmUserStorage = converterDomainRealm.getRealmForDomain(domaine);

    credentialsService.changePassword(
        realmUserStorage.getRealm(),
        realmUserStorage.getUserStorage(),
        identifiant,
        pcr.getAncienMotDePasse(),
        pcr.getNouveauMotDePasse(),
        modeEnvoisString != null && modeEnvoisString.size() > 0 ? modeEnvoisString.get(0) : "MAIL",
        getTemplatePropertiesFromPCR(pcr),
        new ProviderRequest(
            new SugoiUser(
                authentication.getName(),
                authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(String::toUpperCase)
                    .collect(Collectors.toList())),
            false,
            null,
            false));
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private Map<String, String> getTemplatePropertiesFromPCR(PasswordChangeRequestOuganext pcr) {
    Map<String, String> templateProperties =
        pcr.getInfoFormattageEnvoi() != null
            ? ouganextSugoiMapper.serializeOuganextToMap(pcr.getInfoFormattageEnvoi())
            : new HashMap<>();

    if (pcr.getAdresseMessagerie() != null) {
      templateProperties.put("mail", pcr.getAdresseMessagerie());
    }
    return templateProperties;
  }
}
