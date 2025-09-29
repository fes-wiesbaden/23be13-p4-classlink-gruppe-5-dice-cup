package de.dicecup.classlink.features.registration.domain;

import java.time.Instant;
import java.util.UUID;

public record InviteResponseDto (
  UUID inviteId,
  Instant expiresAt,
  String link
) {}
