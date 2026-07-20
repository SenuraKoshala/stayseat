package com.stayseat.hotelbooking.dto;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {
}
