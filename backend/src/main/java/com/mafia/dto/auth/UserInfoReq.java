package com.mafia.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoReq {
  @Schema(description = "User info request", requiredMode = Schema.RequiredMode.REQUIRED)
  private String query;
}
