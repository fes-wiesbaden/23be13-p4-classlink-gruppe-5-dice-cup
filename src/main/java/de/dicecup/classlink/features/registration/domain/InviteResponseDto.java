package de.dicecup.classlink.features.registration.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InviteResponseDto (
  UUID inviteId,
  OffsetDateTime expiresAt,
  String link
) {}
