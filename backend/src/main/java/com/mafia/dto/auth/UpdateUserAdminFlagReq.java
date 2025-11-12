package com.mafia.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user's admin flag")
public class UpdateUserAdminFlagReq {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private UUID userId;

  @Schema(description = "Whether the user should be admin")
  private boolean isAdmin;
}
